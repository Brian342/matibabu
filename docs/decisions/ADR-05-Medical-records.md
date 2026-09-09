# ADR: Medical Record Management

* **Status:** Accepted
* **Date:** 2026-08-28

## Context

Matibabu requires a medical record system for storing and managing clinical information collected during patient care.

A medical record needs to provide a structured way to capture information such as:

* Vital signs
* Clinical observations
* Diagnoses
* Treatments

The medical record is part of the patient's clinical history and is associated with the encounter during which the information was recorded.

The implementation also needs to fit the existing architecture of the backend, which separates responsibilities between the API, application, domain, and persistence layers.

## Decision

We introduced a dedicated medical-record domain model and supporting application and API functionality.

A `MedicalRecord` is associated with:

* A patient
* An encounter
* A creation timestamp

The medical record acts as the central domain object for clinical information collected during the encounter.

It maintains collections of:

* `Vital`
* `ClinicalObservation`
* `Diagnosis`
* `Treatment`

### Medical Record Creation

Medical records are created in the context of an existing encounter.

When creating a medical record, the application obtains the patient associated with the encounter and creates the record using both identifiers.

This ensures that the medical record maintains a clear relationship with the clinical encounter while remaining associated with the correct patient.

### Clinical Information

The medical record provides domain operations for adding clinical information:

```text
MedicalRecord
    ├── Vital
    ├── ClinicalObservation
    ├── Diagnosis
    └── Treatment
```

Each clinical entry has its own identifier and relevant information.

This allows different types of clinical information to evolve independently while remaining part of the medical record.

### Application Layer

Dedicated application services were introduced for the main medical-record operations:

* Create a medical record
* Retrieve a medical record
* Add a vital
* Add a clinical observation
* Add a diagnosis
* Add a treatment

These services coordinate the domain objects and repository without placing business logic inside the API layer.

### API Layer

REST endpoints were introduced under:

```text
/api/medical-records
```

The API supports creating and retrieving medical records as well as adding clinical information to an existing record.

The controller delegates operations to the application layer and converts domain objects into API response objects.

### Persistence

The medical-record persistence model stores the information required to reconstruct the domain object.

The repository abstraction keeps persistence concerns separated from the domain model.

Database changes continue to be managed through Flyway migrations.

### Identifiers and timestamps

Medical records and clinical entries use UUIDs for identifiers.

The project uses time-ordered UUID generation to provide unique identifiers while maintaining useful ordering characteristics for persisted records.

Medical records maintain a `createdAt` timestamp to record when the record was created.

## Rationale

The medical record is modeled as a domain concept rather than treating individual clinical entries as unrelated pieces of data.

This provides a clear structure for clinical information:

```text
Patient
   │
   └── Encounter
          │
          └── MedicalRecord
                 ├── Vitals
                 ├── Observations
                 ├── Diagnoses
                 └── Treatments
```

Associating the record with the encounter provides clinical context while retaining the patient relationship needed for the patient's medical history.

Separating the operations into application services keeps the responsibilities of the architecture clear:

```text
API
 │
 ▼
Application
 │
 ▼
Domain
 │
 ▼
Repository
 │
 ▼
Persistence
```

This also provides a foundation for adding additional clinical information in the future without placing increasing amounts of business logic into the controller.

## Consequences

### Positive

* Clinical information is organized around a single medical-record concept.
* Medical records retain their patient and encounter context.
* Different clinical information types can be added independently.
* Business logic remains outside the controller.
* The domain model remains independent of Spring and persistence details.
* UUID-based identifiers support the project's offline-first requirements.
* The design provides a foundation for extending medical records with additional clinical information.

### Trade-offs

* The medical-record functionality introduces additional application services and domain classes.
* Creating a medical record requires an existing encounter.
* The persistence model must evolve as additional medical-record information is introduced.
* The API may require further refinement as frontend requirements become clearer.

## Implementation

The functionality introduced includes:

* `MedicalRecord` domain model
* Medical-record repository integration
* Medical-record creation
* Medical-record retrieval
* Vital recording
* Clinical observation recording
* Diagnosis recording
* Treatment recording
* REST API endpoints
* Medical-record API response model
* Persistence integration
* Flyway migration updates

The functionality was integrated into `main` and the Maven test suite was executed successfully after integration.

## Status

**Accepted and implemented.**
