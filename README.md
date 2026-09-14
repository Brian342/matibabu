# Matibabu Offline-First EMR Lite

Matibabu is an offline-first Electronic Medical Record (EMR) system for managing patient and clinical data in environments where connectivity may be unreliable.

The system is being developed as a full-stack application with a frontend client and a Java/Spring Boot backend.

## Architecture

```text
                    ┌─────────────────────┐
                    │      Frontend       │
                    │                     │
                    │  Patient workflows  │
                    │  Encounter workflows│
                    │  Clinical workflows │
                    └──────────┬──────────┘
                               │
                           REST API
                               │
                               ▼
                    ┌─────────────────────┐
                    │       Backend       │
                    │                     │
                    │       API           │
                    │    Application      │
                    │      Domain         │
                    │   Infrastructure    │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │   Local Database    │
                    │       SQLite        │
                    └──────────┬──────────┘
                               │
                         Synchronization
                               │
                               ▼
                    ┌─────────────────────┐
                    │   Remote Database   │
                    └──────────┬──────────┘
                               │
                        DHIS2 Integration
                               │
                               ▼
                    ┌─────────────────────┐
                    │       DHIS2         │
                    └─────────────────────┘
```

## Backend Architecture

The backend follows Clean Architecture and Domain-Driven Design principles.

```text
backend/
├── api/
├── application/
├── domain/
└── infrastructure/
    └── persistence/
```

### Responsibilities

**API**

Handles HTTP requests, responses, validation, and API-level exception handling.

**Application**

Implements use cases and coordinates domain objects with repository abstractions.

**Domain**

Contains entities, value concepts, business rules, state transitions, and repository interfaces.

**Infrastructure**

Contains persistence implementations, JPA entities, MapStruct mappers, Spring Data repositories, and database configuration.

The domain does not depend on Spring or persistence implementation details.

---

## Technology Stack

### Backend

* Java
* Spring Boot
* Spring Web
* Spring Security
* Spring Data JPA
* Hibernate
* MapStruct
* Flyway
* SQLite
* PostgreSQL
* Maven
* JUnit

### Development & Testing

* IntelliJ IDEA
* Apidog
* GitHub Actions

### Integration

* DHIS2

---

# Implemented Functionality

## Patient Management

Implemented:

* Patient registration
* Patient retrieval
* Patient listing
* Patient update
* Patient deletion
* Patient search by phone number
* Duplicate phone-number validation

### API

```text
POST   /api/patients
GET    /api/patients/{id}
GET    /api/patients
PUT    /api/patients/{id}
DELETE /api/patients/{id}
GET    /api/patients/search
GET    /api/patients/phone/{phoneNumber}
```

---

## Encounter Management

An encounter represents a clinical interaction between a patient and the healthcare facility.

Current lifecycle:

```text
ACTIVE
  ├── DISCHARGED
  └── CANCELLED
```

Implemented:

* Encounter creation
* Encounter retrieval
* Discharge
* Cancellation
* Lifecycle validation
* Prevention of operations on inactive encounters
* Validation of encounter timestamps

### API

```text
POST /api/encounters/{patientId}
GET  /api/encounters/{id}

POST /api/encounters/{id}/discharge
POST /api/encounters/{id}/cancel
```

Encounter identifiers use UUIDv7/time-ordered UUIDs.

---

## Medical Records

A medical record belongs to an encounter.

```text
Patient
   │
   └── Encounter
          │
          └── Medical Record
```

Implemented:

* Medical record creation
* Medical record retrieval by encounter
* Encounter validation
* Patient association derived from the encounter
* One medical record per encounter

### API

```text
POST /api/encounters/{encounterId}/medical-record
GET  /api/encounters/{encounterId}/medical-record
```

The database enforces the one-record-per-encounter constraint.

---

## Medicine Reference Catalog

A local catalog of prescribable medicines, seeded from the Kenya
Essential Medicines List (KEML) and coded against the WHO ATC
classification, so a clinician can select a medicine when recording a
`Treatment` instead of typing a drug name as free text.

KEML does not itself publish ATC codes, so each medicine's ATC
mapping carries an explicit provenance status rather than being
treated as always-trustworthy:

```text
CONFIRMED     - a reviewer has verified this code
AUTO_MATCHED  - matched automatically against the WHO ATC index
NEEDS_REVIEW  - no confident automated match was found
UNMAPPED      - no ATC code applies
```

Implemented:

* Medicine search/browse (name, generic name, ATC code)
* Medicine retrieval by id
* ATC mapping provenance tracking (`atc_mapping_status`, `keml_version`)
* Admin review queue for unresolved mappings
* Admin confirm/reject workflow for an ATC mapping, with reviewer and
  timestamp recorded
* Soft deactivation of medicines withdrawn from KEML

Not yet implemented:

* Seeding the catalog from a real KEML export (schema and tooling are
  in place; the catalog is currently empty pending an actual import)
* Frontend UI for the catalog or the admin review queue

### API

```text
GET   /api/medicines
GET   /api/medicines/{id}

GET   /api/admin/medicines/needs-review
PATCH /api/admin/medicines/{id}/atc-mapping
```

The admin endpoints are restricted to `ADMIN`/`SUPER_ADMIN`, since
resolving an ATC mapping is a reference-data judgment call, not a
per-patient prescribing action.

KEML-to-ATC matching itself is done offline via
`tools/keml-atc-matcher/`, a standalone script run manually whenever
KEML is updated (roughly every few years) rather than as a running
application feature. See `docs/decisions/ADR-06-Medicines.md` for the
full design rationale.

---

## Clinical Data

Clinical information is represented as separate domain concepts:

```text
Medical Record
├── Vitals
├── Clinical Observations
├── Diagnoses
└── Treatments
```

### Vitals

Supported types:

```text
TEMPERATURE
BLOOD_PRESSURE
HEART_RATE
RESPIRATORY_RATE
OXYGEN_SATURATION
WEIGHT
HEIGHT
```

### Diagnoses

Supported types:

```text
SUSPECTED
CONFIRMED
DIFFERENTIAL
```

Clinical data persistence is implemented using relational child tables:

```text
medical_record_vitals
medical_record_observations
medical_record_diagnoses
medical_record_treatments
```

Each clinical entry references its parent medical record through a foreign key.

---

# Authentication

Authentication is implemented using Spring Security and session-based authentication.

The database includes clinician and Spring Session persistence.

Initial administrator provisioning is handled through a versioned Flyway migration.

---

# Persistence

Database schema evolution is managed with Flyway.

The current schema includes:

```text
patients
encounters
medicalrecords

medical_record_vitals
medical_record_observations
medical_record_diagnoses
medical_record_treatments

medicines

clinicians

SPRING_SESSION
SPRING_SESSION_ATTRIBUTES
```

The application currently uses SQLite for local/offline persistence, with PostgreSQL supported for remote/server-based persistence.

---

# Testing

Automated tests cover the implemented domain, application, and persistence behavior.

Testing includes:

* Patient operations
* Encounter lifecycle
* Medical record operations
* Domain validation
* Repository behavior
* Error scenarios
* Persistence constraints

API behavior is additionally verified through Apidog.

The current automated test suite is passing.

---

# Error Handling

Application and domain exceptions are handled centrally using `@RestControllerAdvice`.

The API currently handles cases including:

```text
404 NOT FOUND
409 CONFLICT
400 BAD REQUEST
```

Examples include:

* Patient not found
* Encounter not found
* Medical record not found
* Duplicate patient phone number
* Invalid encounter state
* Database integrity conflicts
* Invalid request parameters

---

# Development Status

| Component                       | Status     |
| ------------------------------- | ---------- |
| Project architecture            | ✅          |
| Patient management              | ✅          |
| Encounter management            | ✅          |
| Encounter lifecycle             | ✅          |
| Medical records                 | ✅          |
| Clinical data model             | ✅          |
| Clinical data persistence       | ✅          |
| Medicine reference catalog      | 🚧         |
| Local database                  | ✅          |
| Database migrations             | ✅          |
| Authentication                  | ✅          |
| Exception handling              | ✅          |
| Automated tests                 | ✅          |
| API testing                     | ✅          |
| Frontend                        | 🚧         |
| Remote database synchronization | ⏭️ Next    |
| DHIS2 integration               | ⏭️ Planned |

---

# Current Workflow

The implemented clinical workflow is:

```text
Patient
   ↓
Encounter
   ↓
Medical Record
   ↓
Clinical Data
   ├── Vital
   ├── Observation
   ├── Diagnosis
   └── Treatment
```

The next system-level workflow is:

```text
Local Database
      ↓
Synchronization
      ↓
Remote Database
      ↓
DHIS2
```

---

# Roadmap

## 1. Remote Database Synchronization

The next major implementation phase is synchronization between the local database and the remote database.

Key concerns include:

* Change detection
* Sync state
* Conflict handling
* Retry behavior
* Idempotency
* Connectivity recovery
* Data consistency

## 2. DHIS2 Integration

After establishing reliable remote synchronization, Matibabu will integrate with DHIS2 for the required health-information workflows.

The integration will define:

* Data mappings
* DHIS2 API interaction
* Synchronization boundaries
* Failure handling
* Retry behavior
* Idempotency
* Reporting requirements

---

# Architectural Decisions

Significant architectural decisions are documented through ADRs.

Current ADRs include:

* **ADR-001 — architecture decisions**
* **ADR-01 — Encounters**
* **ADR-02- did web authentication**
* **ADR-03- github issues**
* **ADR-04- update on status**
* **ADR-05- Medical records**
* **ADR-06- Medicines (KEML / ATC mapping)**
* **ADR-07- summary**

ADRs are used for decisions that affect the architecture, boundaries, or development process rather than documenting routine implementation details.

---

# Development Guidelines

When extending the system:

1. Keep business rules in the domain.
2. Keep use-case orchestration in the application layer.
3. Keep HTTP concerns in the API layer.
4. Keep persistence concerns in infrastructure.
5. Define repository abstractions independently of persistence technology.
6. Use database constraints for data integrity.
7. Add automated tests for new behavior.
8. Use ADRs for significant architectural decisions.
9. Verify API behavior independently of unit tests.

---

## Project Status

**Current milestone:** Core clinical workflow implemented.

**Next milestone:** Local-to-remote database synchronization.

**Following milestone:** DHIS2 integration.