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

### Patient identity: the one real cross-facility conflict

Local `Patient` UUIDs are permanent. A facility's encounters, medical records, and referrals keep referencing their local patient UUID forever, whether or not that facility has ever synced. Synchronization never renumbers a local patient.

The hub introduces a new concept to reconcile identity across facilities:

```text
MasterPatientRecord            — the one canonical online identity for a person
PatientIdentityLink            — (facilityId, localPatientId) → MasterPatientRecord
PatientMatchCandidate          — an ambiguous match awaiting human review
```

When a `PatientRegistered` (or demographic-update) entry arrives, the hub's matching engine compares it against existing `MasterPatientRecord`s using a normalized deterministic key — first name, last name, date of birth, and gender, strengthened by phone number where present:

* **Strong match** (core identifying fields agree) → the local patient is linked to the existing `MasterPatientRecord`; no new canonical identity is created.
* **No match** → a new `MasterPatientRecord` is created, linked to this one local patient.
* **Ambiguous match** (e.g. name and date of birth agree but phone number doesn't, or name similarity falls within a fuzzy-match threshold without being exact) → a `PatientMatchCandidate` is queued for a records officer to confirm or reject. The local patient still receives a provisional `MasterPatientRecord` of its own immediately, so care is never blocked on review — review only ever *links* records together after the fact, it never deletes or withholds clinical data.

This deliberately does not require a national identifier to exist first. When one becomes available (a Huduma Namba–style number, or a future SHA-issued identifier), it becomes the strongest possible key in the same matching engine without changing its shape — it is a strengthening of the match, not a prerequisite for it.

### Standards and compliance boundary

* Sync payload shapes are kept structurally close to the HL7 FHIR resources they conceptually correspond to (`Encounter`, `Condition`, `ServiceRequest`, `Patient`), even though the wire format is not literally FHIR yet. This keeps a future KHIS/DHIS2 or national health-exchange integration an addition at the boundary, consistent with ADR-07's existing position that DHIS2 concerns stay outside the core domain.
* All sync traffic is TLS-only. The synchronization module must not log full payloads above debug level, since they carry patient-identifying clinical data — sensitive personal data under Kenya's Data Protection Act 2019.
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
* The design leaves room for a national patient identifier to be introduced later purely as a stronger matching key.
* Sync payload shapes are chosen to make a future FHIR/DHIS2 boundary integration additive rather than a rework.

### Trade-offs

* There is an inherent eventual-consistency window: a facility that stays offline for days is simply not visible to the rest of the country until it syncs. This is accepted as the nature of the requirement, not a defect to engineer away.
* Ambiguous patient matches require ongoing human review capacity at the hub; this is an operational cost, not just a technical one.
* Every mutating use case gains a responsibility (writing an outbox entry) it didn't have before, which touches every existing application service.
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
│   └── SyncClient.java              (facility role: batches and pushes unsynced entries)
├── ingestion
│   ├── SyncIngestionController.java (hub role: receives batches, acknowledges per entry)
│   └── SyncIngestionService.java    (dispatches each entry back to the matching use case)
└── identity
    ├── MasterPatientRecord.java
    ├── PatientIdentityLink.java
    ├── PatientMatchCandidate.java
    └── PatientMatchingService.java
```

Facility-side schema addition: a `sync_outbox` table (new Flyway migration) storing entries as described above.

Hub-only schema additions (`master_patient_records`, `patient_identity_links`, `patient_match_candidates`) are only meaningful when `app.node.role=hub`; they can be introduced via a separate Flyway migration location activated by that profile, consistent with the existing `application-local.properties` / `application-prod.properties` split.

## Future Evolution

This ADR deliberately leaves several concerns for later decisions or implementation work:

1. **National patient identifier**

   Once a national identifier (e.g. Huduma Namba or an SHA-issued number) is available, `Patient` should gain an optional field for it, and the matching engine should treat an exact match on that field as decisive on its own, ahead of the name/DOB/phone heuristic.

2. **DHIS2 and FHIR-native integration**

   This ADR keeps sync payloads FHIR-shaped but not FHIR-literal. A future ADR should decide whether the hub exposes a genuine FHIR API, building on the master-patient and encounter stream this ADR establishes, per ADR-07's existing note that DHIS2-specific concerns belong at the integration boundary.

3. **Receiving-facility referral workflow**

   Once a referral's receiving facility becomes a first-class reference (ADR-07's deferred facility model) rather than free text, referrals stop being single-writer, and this ADR's ordering assumption needs to be revisited for that aggregate specifically.

4. **Field-level demographic conflicts**

   If two facilities register conflicting demographic edits for the same person before either syncs (e.g. two different phone numbers), the initial version of this design surfaces that as an ordinary ambiguous-match review rather than an automatic field-level merge. A dedicated merge UI can be introduced later if operational experience shows plain review isn't sufficient.

## Status

**Proposed.** No part of the `synchronization` module exists yet. This ADR is intended to be reviewed and agreed by the team, per the Patient Registry contract's requirement that domain-boundary changes be discussed before implementation, before any of the above is built.
