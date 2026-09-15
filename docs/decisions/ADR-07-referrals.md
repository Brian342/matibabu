# ADR: Referral Management

* **Status:** Accepted, implemented
* **Date:** 2026-09-15

## Context

Matibabu needs to support referrals when a clinician determines that a patient requires care from another facility, department, or specialty.

A referral is created during the context of a clinical encounter, but it is not simply another field on the encounter or medical record. A referral has its own identity, lifecycle, queries, and resolution state.

For example, a referral may remain pending after the encounter that created it has ended. Users may also need to query referrals independently, such as finding referrals that are still pending.

The referral may optionally be associated with the diagnosis that prompted it. However, a confirmed diagnosis is not always available at the time a referral is made. For example, a patient may be referred for imaging specifically to establish a diagnosis.

Matibabu also does not yet have a first-class `Facility` domain concept. Introducing one solely to support referrals would add a broader concept before the system has an established facility model.

The system is offline-first, with local SQLite persistence followed by synchronization to a remote database and eventual DHIS2 integration. Referral data therefore needs a stable domain representation that does not depend on the availability of the remote system.

## Decision

### Referral as an independent aggregate

A `Referral` is modeled as its own domain aggregate with its own identity and repository.

It is not nested inside `MedicalRecord` or `Encounter`.

The referral maintains references to the clinical context from which it originated:

* `encounterId` — the encounter during which the referral was created
* `patientId` — the patient being referred
* `referringClinicianId` — the clinician who created the referral
* `diagnosisId` — the diagnosis associated with the referral, when one exists

The referral therefore preserves its clinical context without making its lifecycle dependent on the lifecycle of the encounter.

### Referral information

A referral contains:

* a reason for referral
* an urgency
* a receiving facility
* an optional department
* its current status
* its creation timestamp
* resolution information when applicable

The reason is required because a referral must communicate why further care is being requested.

The urgency is represented by an explicit domain enum:

```text
ROUTINE
URGENT
EMERGENCY
```

The receiving facility is currently represented as free text.

The `department` remains optional because the destination facility may be known without a specific department being identified.

### Optional diagnosis

`diagnosisId` is nullable.

A referral does not require a confirmed diagnosis because referrals may be made precisely to investigate or establish a diagnosis.

When a diagnosis exists, the referral may retain a reference to the diagnosis that prompted the referral.

### Referral lifecycle

A referral begins in the `PENDING` state.

Its lifecycle is:

```text
PENDING
   ├── COMPLETED
   └── CANCELLED
```

Only a pending referral may transition to another state.

Completing or cancelling a referral records:

* the actor who resolved it
* the time at which it was resolved

A completed or cancelled referral is immutable with respect to its lifecycle state. Attempting to change it again is rejected by the domain.

The domain model is responsible for enforcing these lifecycle invariants.

Authorization for who may complete or cancel a referral remains outside the domain model and belongs to the API/security layer.

### Receiving facility

`receivingFacility` is intentionally modeled as plain text for the current implementation.

Matibabu does not yet have a first-class `Facility` aggregate or reference-data model that can safely be reused here.

The field is therefore not treated as a foreign-key identity.

When a facility model is introduced, the referral model can be evolved so that the destination is represented by a stable facility identifier.

This avoids prematurely introducing a facility bounded context simply to support the current referral workflow.

### Persistence

The referral is persisted independently from the medical record.

The database maintains foreign-key relationships to the clinical entities that already exist:

* `encounters`
* `patients`
* `medical_record_diagnoses`
* `clinicians`

The encounter relationship uses cascade deletion because the referral is created in the context of an encounter and the current persistence model treats the encounter as the owning clinical context.

The referral has indexes on:

* `encounter_id`
* `patient_id`
* `status`

The status index supports queries such as retrieving pending referrals without requiring the application to scan the entire referral table.

### Offline-first compatibility

Referral creation and lifecycle changes are local domain operations and do not require connectivity to a remote system.

The referral therefore exists as normal clinical data in the local SQLite database and can subsequently participate in the application's synchronization process.

The referral model does not contain remote-system-specific fields or depend directly on DHIS2 concepts.

Any future DHIS2 mapping belongs at the integration boundary rather than inside the referral domain model.

## Rationale

Modeling referrals as an independent aggregate reflects their actual lifecycle.

An encounter answers the question:

> What clinical interaction occurred?

A referral answers a different question:

> What further care was requested, where was it requested, and has that request been resolved?

Keeping the two concepts separate allows a referral to remain pending after the originating encounter has been discharged or cancelled, while still retaining the encounter and patient context from which it originated.

Making the diagnosis optional reflects actual clinical workflow. A referral may be required to obtain information needed before a diagnosis can be confirmed.

Keeping the receiving facility as free text is deliberately conservative. A proper facility model will eventually be useful for interoperability and synchronization, but introducing one before its broader requirements are understood would couple referrals to an under-specified concept.

The lifecycle is intentionally small. Matibabu currently needs to distinguish referrals that are still pending from referrals that have been completed or cancelled. Additional states can be introduced later if the workflow demonstrates a real need for them.

## Consequences

### Positive

* Referrals have an independent identity and lifecycle.
* Pending referrals can be queried independently of encounters and medical records.
* Referral lifecycle rules are enforced by the domain rather than by controllers or persistence code.
* A referral can be created without a confirmed diagnosis.
* The model retains the patient, encounter, and referring clinician context.
* The local/offline workflow does not depend on remote connectivity.
* The domain remains independent of Spring, JPA, SQLite, and DHIS2.
* The model leaves room for a future first-class facility concept without prematurely introducing one.

### Trade-offs

* `receivingFacility` is currently free text and therefore cannot provide strong referential integrity.
* Facility names may be inconsistent until a facility/reference-data model is introduced.
* The referral currently represents only a simple pending/completed/cancelled workflow.
* The referral keeps both `patientId` and `encounterId`, which introduces duplicated contextual information but makes the referral independently addressable and queryable.
* The current persistence relationship to the encounter means the database lifecycle of the referral must be reconsidered if referrals eventually need to survive deletion of their originating encounter.

## Implementation

The referral domain consists of:

* `Referral`
* `ReferralStatus`
* `ReferralUrgency`
* `ReferralRepository`
* `ReferralNotPendingException`

The aggregate exposes creation and lifecycle operations rather than allowing callers to directly manipulate its state.

Creation establishes:

```text
PENDING
```

The lifecycle operations are:

```text
complete(resolvedBy, resolvedAt)
cancel(resolvedBy, resolvedAt)
```

Both operations reject transitions from a non-pending state.

The persistence schema is introduced by the referral Flyway migration and contains:

```text
referrals
├── id
├── encounter_id
├── patient_id
├── referring_clinician_id
├── diagnosis_id
├── reason
├── urgency
├── receiving_facility
├── department
├── status
├── created_at
├── resolved_by
└── resolved_at
```

The infrastructure layer is responsible for mapping the persistence representation to and from the domain aggregate, following the existing Clean Architecture boundary used elsewhere in Matibabu.

## Future Evolution

This decision deliberately leaves several concerns for later architectural decisions or implementation work:

1. **Facility model**

   When Matibabu introduces a first-class facility concept, `receivingFacility` should be migrated from free text to a stable facility reference.

2. **Referral synchronization**

   Referral synchronization should be designed as part of the broader offline-first synchronization architecture rather than embedded in the referral aggregate.

3. **DHIS2 interoperability**

   DHIS2-specific identifiers and mappings should remain outside the core referral domain unless the integration requirements demonstrate that they represent genuine domain concepts.

4. **Receiving-side workflow**

   The current model records that a referral was completed and who resolved it. A future receiving-facility workflow may require additional concepts such as acceptance, rejection, appointment, or transfer-of-care states. These should only be introduced when the actual workflow requires them.

## Status

**Accepted and implemented.**

The referral aggregate, lifecycle rules, repository boundary, and persistence schema are implemented. The current model intentionally keeps the receiving facility as free text and limits the lifecycle to `PENDING`, `COMPLETED`, and `CANCELLED`.

Future facility modeling, synchronization, and DHIS2-specific referral integration should build on this decision rather than introducing remote-system concerns into the referral domain.
