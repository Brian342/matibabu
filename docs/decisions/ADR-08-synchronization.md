# ADR: Offline-First Cross-Facility Synchronization

* **Status:** Proposed
* **Date:** 2026-09-15

## Context

Matibabu is deployed offline-first at individual facilities: each facility runs its own instance against a local database (ADR-010) and must remain fully usable with no internet access. The country additionally needs one canonical, online record per patient, aggregating clinical activity across every facility that has ever treated them, aligned with the Kenya Health Information System (KHIS/DHIS2) and Kenya's Data Protection Act 2019 treatment of health data as sensitive personal data.

Two properties of the deployment make this harder than an ordinary client/server sync problem:

* A facility may go extended periods with no connectivity at all, during which it must keep registering patients, running encounters, and creating referrals normally.
* A patient may be seen at more than one facility while both were offline with respect to each other. Each facility then creates its own local `Patient` record, with its own UUID, for the same physical person. Only one canonical identity for that person may exist online.

`docs/architecture/architectureOverview.md` and `docs/architecture/patientregistry.md` already reserve a `com.matibabu.backend.synchronization` package and name its responsibilities — retries, conflicts, idempotency, ordering, partial connectivity — but no implementation exists yet. This ADR fills that gap.

A line-based diff between local and remote copies (e.g. `git diff`-style comparison of exported records) was considered and rejected. Matibabu's data is structured, typed, and invariant-bearing (a `Referral` can only leave `PENDING` once, an `Encounter` has an explicit status machine); diffing serialized snapshots discards that structure and gives no principled way to reconcile two independently created `Patient` identities for the same person — it can only compare content that already agrees on identity, which is precisely the part that doesn't agree here.

The existing architecture is, however, already well shaped for a different approach: every clinical mutation goes through exactly one named use case (`RegisterPatientService`, `StartEncounterService`, `CreateReferralService`, `CompleteReferralService`, ...), and every aggregate already carries a globally unique, time-ordered identity (ADR-004) plus creation/update timestamps.

## Decision

### One codebase, two deployment roles

Matibabu continues to be a single codebase. A configuration property, `app.node.role`, selects between two roles:

```text
facility  (default)  — runs at a hospital, local database, fully offline-capable
hub                  — the national online system, PostgreSQL (ADR-010)
```

Both roles run the identical domain and application layers. A `hub` node exposes the same clinical APIs a `facility` node does (so a directly-connected site, e.g. a head office, can act as a facility itself) plus two additions: a synchronization ingestion API and a patient-identity-matching workflow. Nothing about `Patient`, `Encounter`, `MedicalRecord`, or `Referral` changes to support this — the difference is entirely in wiring and in the new `synchronization` module, consistent with ADR-002 and ADR-003 keeping the domain independent of where it runs.

This matters for correctness, not just convenience: because the hub applies incoming data through the *same* application use cases a facility uses locally, every domain invariant (`ReferralNotPendingException`, encounter status validation, duplicate-phone handling, ...) is enforced identically everywhere. The hub never reconstructs state via direct SQL.

### Outbound sync: an outbox, not a diff

Every application service that commits a domain mutation also appends one `SyncOutboxEntry` in the same local transaction as the domain write:

```text
SyncOutboxEntry
├── id               (UUID v7, per ADR-004)
├── facilityId
├── aggregateType     PATIENT | ENCOUNTER | MEDICAL_RECORD | REFERRAL
├── aggregateId
├── operation         e.g. "PatientRegistered", "ReferralCompleted"
├── payload           the data needed to replay this operation
├── sequenceNumber     monotonically increasing per facility
├── createdAt
└── syncedAt           null until acknowledged by the hub
```

The outbox entry is written by the same use case that performs the domain mutation, not derived afterward by inspecting state. This is what makes the record of "what changed" exact and ordered, without comparing snapshots.

### Sync transport: resumable, idempotent push

When a facility detects connectivity, a `SyncClient` pushes unsynced outbox entries to the hub's ingestion endpoint in batches, ordered by `sequenceNumber`, over TLS, authenticated as the facility itself (a device/service credential, distinct from clinician login sessions used for clinical endpoints).

The hub acknowledges per entry id. A facility marks `syncedAt` only once acknowledged, so re-sending a batch after a dropped connection is safe — the hub treats a repeated entry id as already-applied and is a no-op. Facilities are independent streams: the hub applies each facility's entries in that facility's own sequence order, but never needs to order entries *across* facilities, because the facility-scoped aggregates (`Encounter`, `MedicalRecord`, `Referral`) are only ever written by the facility that owns them.

### Applying synced entries at the hub

The hub's `SyncIngestionService` maps each outbox entry back onto the corresponding use case interface — a `ReferralCompleted` entry invokes `CompleteReferralUseCase.complete(...)`, a `PatientRegistered` entry invokes patient registration (subject to the identity matching below). Because these aggregates are single-writer (owned by one facility), applying them centrally is a replay, not a merge: there is no concurrent-edit conflict to resolve for an encounter or a referral, only ordering, which `sequenceNumber` already guarantees. If a sequence is invalid regardless (e.g. completing an already-cancelled referral), the same domain exception fires as it would locally — that signals a bug or a reordering defect to be investigated, not something for the hub to silently coerce.

**Creation entries must preserve the facility-generated id, not mint a new one.** Every aggregate constructor (`Patient`, `Encounter`, `MedicalRecord`, `Referral`, and child objects like `Diagnosis`/`Treatment`/`Vital`/`ClinicalObservation`) generates its own UUID v7 at creation time (ADR-004); only each aggregate's `reconstitute(...)` factory accepts an externally-supplied id, and today that path exists solely to hydrate an aggregate back from its own local database. A creation-type outbox entry (`PatientRegistered`, `MedicalRecordCreated`, `DiagnosisAdded`, ...) therefore carries the id(s) generated at the facility, and the hub's ingestion path for *creation* operations hydrates via `reconstitute(...)` under those exact ids and persists directly, rather than calling the normal create-use-case (which would mint a fresh id at the hub). Getting this wrong would silently break every cross-reference that crosses the sync boundary — a `MedicalRecord` synced under a new hub-generated id would no longer match the `medicalRecordId` a later-synced `Referral` from the same facility refers to. Non-creation operations (`AddDiagnosis`, `CompleteReferral`, status transitions, ...) are unaffected, since they already operate against an id that was established by an earlier, already-applied creation entry.

**Medical records specifically are a root aggregate built up incrementally.** A `MedicalRecord` is created once per encounter, then grows via separate calls — `AddDiagnosis`, `AddTreatment`, `AddClinicalObservation`, `AddVital` — each of which is its own use case and therefore its own outbox entry, referencing the parent `MedicalRecord`'s id as `aggregateId`. The hub applies these in the facility's `sequenceNumber` order, so a `DiagnosisAdded` entry is never processed before the `MedicalRecordCreated` entry it depends on — the same per-facility ordering guarantee already relied on elsewhere in this ADR, just exercised across more steps for this aggregate than for, say, a `Referral`.

### Patient identity: the one real cross-facility conflict

Local `Patient` UUIDs are permanent. A facility's encounters, medical records, and referrals keep referencing their local patient UUID forever, whether or not that facility has ever synced. Synchronization never renumbers a local patient.

The hub introduces a new concept to reconcile identity across facilities:

```text
MasterPatientRecord            — the one canonical online identity for a person
PatientIdentityLink            — (facilityId, localPatientId) → MasterPatientRecord
PatientMatchCandidate          — an ambiguous match awaiting human review
```

`Patient` gains two optional identifying-document fields, captured at registration when available:

```text
nationalId               — Kenyan national ID number (adults, typically 18+)
birthCertificateNumber   — birth certificate number (minors, before a national ID exists)
```

Both are nullable: a patient may be a newborn with neither yet issued, or registered before staff capture the document. Neither is required to register a patient locally — a facility must never be blocked from registering someone because they lack ID paperwork.

When a `PatientRegistered` (or demographic-update) entry arrives, the hub's matching engine checks candidates against existing `MasterPatientRecord`s in two tiers:

* **Tier 1 — identity document.** If the incoming record carries a `nationalId` or `birthCertificateNumber` and an existing `MasterPatientRecord` carries the same value in the same field, that is treated as decisive: the local patient is linked automatically, without falling through to demographic comparison at all. Conversely, if both records carry a value for the *same* field and the values **differ**, that is treated as decisive evidence they are *not* the same person — even if every demographic field matches (e.g. twins, a name shared within a family) — and the incoming record is never auto-linked to that candidate on demographic grounds alone.
* **Tier 2 — demographic heuristic.** Used whenever neither record has a usable identity document to compare (one or both fields absent on one side), following the same strong/no-match/ambiguous matching described below on first name, last name, date of birth, and gender, strengthened by phone number where present:
  * **Strong match** (core identifying fields agree) → linked to the existing `MasterPatientRecord`.
  * **No match** → a new `MasterPatientRecord` is created, linked to this one local patient.
  * **Ambiguous match** (e.g. name and date of birth agree but phone number doesn't, or name similarity falls within a fuzzy-match threshold without being exact) → a `PatientMatchCandidate` is queued for a records officer to confirm or reject. The local patient still receives a provisional `MasterPatientRecord` of its own immediately, so care is never blocked on review — review only ever *links* records together after the fact, it never deletes or withholds clinical data.

A national identifier field was previously deferred to Future Evolution; it is brought into the core design now because Kenya's own civil registration already gives every person one of these two identifiers from birth, so requiring the matching engine to work without them (Tier 2) remains necessary, but treating them as the strongest available key does not need to wait for a future national digital-identity rollout — birth certificates and national IDs already exist today. A future SHA-issued or Huduma Namba–style identifier would slot into Tier 1 as an additional identity-document field without changing the tiering logic.

### Reducing duplicate registration when a facility is online

Everything above reconciles duplicate local patients *after the fact*, once both facilities' outbox entries reach the hub. That is the only option while a facility is offline, but it means a patient who has already been seen elsewhere still gets a brand-new local `Patient` (and a brand-new provisional `MasterPatientRecord`) every time they register at a facility that hasn't synced with theirs yet — which, for someone who moves between a handful of facilities regularly, could mean several parallel provisional identities sitting in the hub's review queue before matching ever catches up.

When a facility *is* online, registration can do better: before creating a new local `Patient`, the facility calls a read-only hub endpoint — `PatientLookupController` under the `synchronization` module — passing the same fields the matching engine itself would use (identity document if captured, otherwise name/DOB/gender/phone). The hub returns candidate matches using the identical Tier 1 / Tier 2 rules already defined above, but the response is deliberately minimal: enough to let staff confirm identity (name, DOB, gender, a masked identity-document tail, and how many other facilities already hold records for this person), never another facility's full patient record or clinical history — a facility has no legitimate need to see clinical detail it wasn't the one to record, and returning it would violate the same Data Protection Act boundary this ADR already applies to sync payloads generally.

This lookup is strictly advisory and best-effort:

* If the hub is unreachable, times out, or returns no match, registration proceeds exactly as it does today — a facility must never be blocked from registering someone because a network call failed. This is not a new constraint on the offline-first guarantee, it's a strict subset of it.
* If the hub returns a candidate, front-desk staff are shown it and asked to confirm or dismiss — the system never auto-substitutes another facility's identity for a new local registration. Local-first still holds: a new local `Patient` is created either way, with its own new local UUID, exactly as if the lookup had not happened.
* If staff confirm the candidate, the local `Patient` is registered carrying an optimistic hint — the confirmed `MasterPatientRecord` id — that rides along in its `PatientRegistered` outbox entry. When that entry eventually reaches the hub's matching engine, a confirmed hint short-circuits straight to a link, skipping the Tier 1/Tier 2 heuristic entirely, since a human already did the same confirmation the matching engine exists to approximate. An unconfirmed or absent hint falls back to ordinary Tier 1/Tier 2 matching as already described.

This reduces, but does not eliminate, duplicate provisional identities — it only helps when the registering facility happens to be online at that moment. The asynchronous matching engine remains the backstop for every case this lookup can't reach.

### Standards and compliance boundary

* Sync payload shapes are kept structurally close to the HL7 FHIR resources they conceptually correspond to (`Encounter`, `Condition`, `ServiceRequest`, `Patient`), even though the wire format is not literally FHIR yet. This keeps a future KHIS/DHIS2 or national health-exchange integration an addition at the boundary, consistent with ADR-07's existing position that DHIS2 concerns stay outside the core domain.
* All sync traffic is TLS-only. The synchronization module must not log full payloads above debug level, since they carry patient-identifying clinical data — sensitive personal data under Kenya's Data Protection Act 2019.
* `nationalId` and `birthCertificateNumber` are the two most sensitive fields in this design — a national ID number is itself sufficient to enable identity theft if it leaks — so they get treatment beyond the general PII handling above: never returned in API responses beyond what a match-review workflow strictly needs, masked (e.g. last 4 digits only) anywhere they appear in a UI or audit log entry, and excluded entirely from the general application-level audit logging used elsewhere, in favor of the dedicated match-decision audit entry below.
* Every sync ingestion and every patient-match decision, automatic or manual, is recorded as an audit entry: facility, actor (a matching-engine rule or a named records officer), decision, and timestamp. This is required both for clinical-safety review of mistaken links and for DPA accountability.

## Rationale

An outbox keyed to existing use cases costs almost nothing architecturally: Matibabu already models every mutation as a single named operation with a UUID-identified aggregate, so "what changed, in what order" is already implicit in how the system is built — the outbox just makes it explicit and durable. Reusing the same application services at the hub, rather than writing separate ingestion logic, means domain invariants only need to be correct once.

Treating patient identity as a linking problem rather than a merge problem avoids the one outcome that would be clinically unacceptable: two people's histories getting silently combined, or one person's history being partly discarded, because two facilities described them slightly differently while offline. Every real health information exchange with this shape (multiple independent registration points, one national identity) solves it with a master patient index and human-reviewed candidates, not automatic field-level merging — Matibabu follows the same pattern instead of inventing a bespoke one.

## Consequences

### Positive

* Facilities remain fully functional with zero connectivity; nothing about local clinical workflows changes.
* The hub enforces the exact same domain rules as every facility, because it runs the exact same application layer.
* Sync is naturally idempotent and resumable: a dropped connection mid-batch never double-applies or loses an entry.
* No local patient, encounter, or referral history is ever deleted or overwritten by the sync process — reconciliation is additive (linking), not destructive.
* Matching prefers an exact identity-document match (national ID or birth certificate number) over demographic heuristics wherever one is available, and treats conflicting document numbers as a hard signal against auto-linking even when demographics look similar.
* Sync payload shapes are chosen to make a future FHIR/DHIS2 boundary integration additive rather than a rework.
* When a facility is online, an advisory hub lookup at registration time can catch a returning patient before a duplicate local identity is even created, without weakening the offline-first guarantee — the lookup degrades to a no-op the moment the hub isn't reachable.

### Trade-offs

* There is an inherent eventual-consistency window: a facility that stays offline for days is simply not visible to the rest of the country until it syncs. This is accepted as the nature of the requirement, not a defect to engineer away.
* Ambiguous patient matches require ongoing human review capacity at the hub; this is an operational cost, not just a technical one.
* Every mutating use case gains a responsibility (writing an outbox entry) it didn't have before, which touches every existing application service.
* Creation-type ingestion at the hub cannot reuse the ordinary create-use-case call path unchanged, since that path always mints a fresh id; it must hydrate via each aggregate's `reconstitute(...)` factory instead, under the id the facility already generated. This is a real, if narrow, difference between "how a facility creates something" and "how the hub applies a creation it received," and needs its own tests rather than being assumed to fall out of reusing the use case layer.
* A provisional `MasterPatientRecord` created before a match is confirmed means the "one canonical record per patient" property is only eventually true, not always true — acceptable because it never blocks care, but worth naming explicitly.
* This ADR does not yet address the case where a referral's *receiving* facility needs to act on a referral it did not create — today's single-writer-per-aggregate assumption (Referral is only mutated by the referring facility, per ADR-07) will need revisiting once a receiving-facility workflow exists.

## Implementation

Proposed package layout under `com.matibabu.backend.synchronization`, per the boundary already named in `docs/architecture/patientregistry.md`:

```text
synchronization
├── outbox
│   ├── SyncOutboxEntry.java
│   └── SyncOutboxRepository.java
├── client
│   ├── SyncClient.java              (facility role: batches and pushes unsynced entries)
│   └── PatientLookupClient.java     (facility role: advisory hub lookup at registration time)
├── ingestion
│   ├── SyncIngestionController.java (hub role: receives batches, acknowledges per entry)
│   └── SyncIngestionService.java    (dispatches each entry back to the matching use case)
└── identity
    ├── MasterPatientRecord.java
    ├── PatientIdentityLink.java
    ├── PatientMatchCandidate.java
    ├── PatientMatchingService.java
    └── PatientLookupController.java (hub role: read-only, minimal-PII candidate search)
```

Facility-side schema additions: a `sync_outbox` table (new Flyway migration) storing entries as described above, and two new nullable columns on `patients` — `national_id` and `birth_certificate_number` — each with a partial unique index (unique where non-null) so a single facility's own local database cannot itself register the same document number under two different patients, which is also a first line of defense before the value ever reaches the hub's matching engine.

Hub-only schema additions (`master_patient_records`, `patient_identity_links`, `patient_match_candidates`) are only meaningful when `app.node.role=hub`; they can be introduced via a separate Flyway migration location activated by that profile, consistent with the existing `application-local.properties` / `application-prod.properties` split. The hub indexes `master_patient_records` on `national_id` and `birth_certificate_number` to make Tier 1 lookups a direct index hit rather than a scan.

## Future Evolution

This ADR deliberately leaves several concerns for later decisions or implementation work:

1. **Future national digital identifier**

   `nationalId` and `birthCertificateNumber` cover Kenya's existing civil registration documents. If a future SHA-issued or Huduma Namba–style digital identifier is introduced, it should be added as an additional Tier 1 identity-document field alongside them, not a replacement — some patients will hold one identifier before the other becomes available.

2. **DHIS2 and FHIR-native integration**

   This ADR keeps sync payloads FHIR-shaped but not FHIR-literal. A future ADR should decide whether the hub exposes a genuine FHIR API, building on the master-patient and encounter stream this ADR establishes, per ADR-07's existing note that DHIS2-specific concerns belong at the integration boundary.

3. **Receiving-facility referral workflow**

   Once a referral's receiving facility becomes a first-class reference (ADR-07's deferred facility model) rather than free text, referrals stop being single-writer, and this ADR's ordering assumption needs to be revisited for that aggregate specifically.

4. **Field-level demographic conflicts**

   If two facilities register conflicting demographic edits for the same person before either syncs (e.g. two different phone numbers), the initial version of this design surfaces that as an ordinary ambiguous-match review rather than an automatic field-level merge. A dedicated merge UI can be introduced later if operational experience shows plain review isn't sufficient.

## Status

**Proposed.** No part of the `synchronization` module exists yet. This ADR is intended to be reviewed and agreed by the team, per the Patient Registry contract's requirement that domain-boundary changes be discussed before implementation, before any of the above is built.
