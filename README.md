# Matibabu

**Matibabu** is an offline-first Electronic Medical Records (EMR) platform designed for clinical environments where reliable connectivity cannot be assumed.

The system is being built around a local-first clinical workflow: clinical data can be captured and persisted locally, while synchronization with remote infrastructure is treated as a separate concern.

The backend is implemented with **Java and Spring Boot**, with a domain-oriented architecture separating clinical rules from application orchestration, persistence, and external integrations.

> **Project status:** Active development

---

## Overview

Clinical environments do not always have reliable network connectivity. An EMR that depends on continuous access to a remote server can therefore become a constraint on clinical workflows.

Matibabu takes an **offline-first** approach.

The local application is able to work against a local database while the backend maintains a clear boundary between:

* clinical domain logic
* application use cases
* persistence
* authentication and security
* synchronization
* external health-information systems

The long-term architecture is intended to support synchronization from local data to remote infrastructure and, eventually, interoperability with **DHIS2**.

---

## Current Capabilities

The backend currently provides clinical workflows around:

### Patients

* Patient registration
* Patient retrieval
* Patient listing
* Patient updates
* Patient search
* Validation of patient data
* Duplicate phone-number handling
* Stable UUID-based patient identity

### Encounters

* Starting clinical encounters
* Recording the attending clinician
* Encounter lifecycle management
* Discharging encounters
* Cancelling encounters
* Encounter persistence
* Encounter status validation

### Medical Records

The medical-record domain supports clinical information associated with patient care, including clinical observations and diagnoses.

The medical-record model is designed so that clinical information remains part of the domain rather than being coupled directly to persistence concerns.

### Referrals

Referrals are modeled as an independent domain concept with their own lifecycle.

A referral can record:

* originating encounter
* patient
* referring clinician
* optional diagnosis
* reason for referral
* urgency
* receiving facility
* optional department
* referral status
* resolution information

The current referral lifecycle is:

```text
PENDING
   ├── COMPLETED
   └── CANCELLED
```

The receiving facility is currently represented as free text. A first-class facility model can be introduced later without coupling the referral domain to an under-specified facility concept.

---

## Architecture

Matibabu follows a domain-oriented architecture influenced by **Domain-Driven Design and Clean Architecture**.

The primary goal is to keep clinical rules independent from frameworks and infrastructure.

A simplified dependency direction is:

```text
                    API
                     │
                     ▼
              Application Layer
                     │
                     ▼
               Domain Layer
                 ▲       │
                 │       ▼
        Infrastructure / Persistence
                 │
                 ▼
               SQLite
```

The important boundary is that the domain does not depend on the persistence implementation.

For example:

```text
Application Service
       │
       ▼
Domain Repository Interface
       │
       ▲
       │
Persistence Adapter
       │
       ▼
Spring Data / JDBC
       │
       ▼
SQLite
```

This allows the application and domain logic to remain independent of whether data is stored locally, remotely, or eventually synchronized with another system.

---

## Project Structure

The backend is organized around domain capabilities rather than a single global technical-layer structure.

A simplified representation is:

```text
src/
└── main/
    ├── java/com/matibabu/backend/
    │
    ├── domain/
    │   ├── patient/
    │   ├── encounter/
    │   ├── medicalrecord/
    │   └── referral/
    │
    ├── application/
    │   ├── patient/
    │   ├── encounter/
    │   ├── medicalrecord/
    │   └── referral/
    │
    ├── infrastructure/
    │   └── persistence/
    │       ├── patient/
    │       ├── encounter/
    │       ├── medicalrecord/
    │       └── referral/
    │
    └── api/
        └── ...
```

The exact package structure may evolve as the system grows, but the architectural boundary remains important:

> **Domain rules should not depend on infrastructure details.**

---

## Domain Layer

The domain layer contains the clinical concepts and rules that define Matibabu's behaviour.

Examples include:

* `Patient`
* `Encounter`
* `Referral`
* `ReferralStatus`
* `ReferralUrgency`
* domain-specific exceptions
* repository interfaces used as persistence ports

Domain objects are responsible for protecting their own invariants.

For example, an encounter controls its valid lifecycle transitions rather than allowing controllers or database adapters to arbitrarily modify its state.

Similarly, a referral cannot be completed or cancelled once it has already left the `PENDING` state.

---

## Application Layer

The application layer coordinates use cases.

Application services are responsible for:

* receiving application requests
* invoking domain behaviour
* coordinating repositories
* controlling transaction boundaries where required
* returning application-level results

The application layer does not contain persistence implementation details.

This keeps use cases testable without requiring the full Spring application context.

---

## Persistence

The current local persistence implementation uses **SQLite**.

```text
Application
     │
     ▼
Domain Repository Port
     │
     ▼
Persistence Adapter
     │
     ▼
SQLite
```

Database schema changes are managed through **Flyway migrations**.

Hibernate/JPA is configured to validate the existing schema rather than silently creating or modifying the database structure.

This makes schema evolution explicit and version-controlled.

---

## Database

The local development database is SQLite:

```text
jdbc:sqlite:./matibabu-local.db
```

The backend uses:

* SQLite JDBC
* Hibernate
* Hibernate Community Dialects
* Flyway
* schema validation

Migrations are located under:

```text
src/main/resources/db/migration/
```

Database changes should be introduced through migrations rather than relying on automatic schema generation.

---

## Security

Authentication and application security are handled using **Spring Security**.

Security concerns remain outside the clinical domain model.

The backend also considers CSRF/XSRF protection as part of the web security boundary rather than making security mechanisms part of the clinical domain.

This keeps the domain focused on clinical behaviour while allowing the security implementation to evolve independently.

---

## Testing

Testing is treated as part of the architecture rather than only as endpoint verification.

The project contains tests at multiple levels:

### Domain tests

Verify clinical rules and invariants without starting Spring.

### Application/service tests

Verify use-case orchestration and repository interactions using lightweight in-memory implementations where appropriate.

### Persistence tests

Verify mappings and repository adapters against the actual persistence configuration.

### Integration tests

Verify API behaviour through the Spring application context, including:

* HTTP responses
* validation
* persistence
* error handling
* domain/application integration

The project intentionally avoids making Mockito a dependency of the application test strategy. Where possible, tests use real implementations, in-memory repository implementations, or the actual persistence layer.

---

## Database Migrations

Flyway migrations are versioned and committed with the source code.

A migration should represent a deliberate schema change.

For example:

```text
V18__create_referrals_table.sql
```

introduced the referral persistence model.

When adding a new migration, ensure that the filename follows Flyway's naming convention:

```text
V<version>__<description>.sql
```

The double underscore between the version and description is significant.

---

## Running Locally

### Requirements

You will need:

* Java 25
* Maven Wrapper
* Git

The project uses the Maven Wrapper, so a system-wide Maven installation is not required.

Check Java:

```bash
java -version
```

Clone the repository:

```bash
git clone git@github.com:mfalme1k0/matibabu.git
cd matibabu/backend
```

Make the Maven wrapper executable if necessary:

```bash
chmod +x mvnw
```

Run the test suite:

```bash
./mvnw clean test
```

Run the application:

```bash
./mvnw spring-boot:run
```

The local SQLite database is created/used from the configured application datasource.

---

## Configuration

Environment-specific configuration should not be committed with credentials or other secrets.

Local configuration can be supplied through Spring Boot's normal configuration mechanisms.

The local development setup uses SQLite so that the backend can operate without requiring a separately managed database server.

---

## Architectural Decisions

Important architectural decisions are documented separately from the implementation.

They are maintained under:

```text
docs/decisions/
```

The ADRs capture decisions that have architectural consequences rather than documenting every implementation detail.

Current decisions cover areas including:

* medicines and reference data
* clinical domain boundaries
* persistence
* referrals
* other architectural constraints introduced during development

The intent is to keep the ADR collection focused rather than creating an ADR for every code-level decision.

---

## Offline-First Direction

Offline capability is not treated as an afterthought.

The intended data flow is:

```text
             ┌─────────────────┐
             │ Clinical Client │
             └────────┬────────┘
                      │
                      ▼
             ┌─────────────────┐
             │ Local Database  │
             │     SQLite      │
             └────────┬────────┘
                      │
                 Synchronization
                      │
                      ▼
             ┌─────────────────┐
             │ Remote Backend  │
             └────────┬────────┘
                      │
                 Integration
                      │
                      ▼
             ┌─────────────────┐
             │     DHIS2       │
             └─────────────────┘
```

The synchronization layer is intentionally kept separate from the clinical domain.

This means clinical operations should not need to know whether the system is currently connected to the network.

---

## DHIS2 Integration

DHIS2 integration is part of the longer-term interoperability direction of Matibabu.

It is **not currently treated as a dependency of the core clinical domain**.

The intended approach is to map Matibabu's internal clinical model to external interoperability requirements at an integration boundary.

This avoids introducing DHIS2-specific identifiers and concepts directly into core domain objects unless they become genuine domain requirements.

---

## Design Principles

The project is guided by several principles:

### Domain independence

Clinical rules should not depend on Spring, JPA, SQLite, or external systems.

### Explicit persistence

Database schema changes should be versioned and reviewable.

### Offline-first operation

Clinical workflows should not assume continuous network availability.

### Small aggregates

Aggregates should protect meaningful invariants without becoming containers for unrelated functionality.

### Explicit boundaries

External systems such as DHIS2 should integrate through boundaries rather than leaking into the domain model.

### Testable use cases

Application services should be testable without requiring every test to boot the complete application.

### Evolution over premature abstraction

The architecture should provide clear extension points without introducing concepts before the domain requires them.

---

## Development Workflow

A typical feature should move through the following stages:

```text
Requirement
    │
    ▼
Domain model / invariant
    │
    ▼
Application use case
    │
    ▼
Repository port
    │
    ▼
Infrastructure adapter
    │
    ▼
Database migration
    │
    ▼
API
    │
    ▼
Tests
```

Architecturally significant decisions should be captured in an ADR.

Implementation details that do not have long-term architectural consequences should remain in the code and its tests rather than generating unnecessary documentation.

---

## Current Development Priorities

The current implementation is focused on establishing the clinical backend and its persistence boundaries.

The broader roadmap includes:

* completing the clinical workflows
* strengthening offline-first behaviour
* implementing local-to-remote synchronization
* defining synchronization conflict handling
* integrating with DHIS2
* expanding interoperability around clinical data
* continuing to harden security and automated testing

These are intentionally separate concerns from the core clinical domain.

---

## Contributing

Development should preserve the architectural boundaries already established in the project.

Before introducing a new feature:

1. Identify the domain concept involved.
2. Define its invariants.
3. Keep framework-specific concerns outside the domain.
4. Introduce repository ports where persistence is required.
5. Implement infrastructure adapters separately.
6. Add or update the database migration when the schema changes.
7. Test domain rules independently.
8. Add integration coverage where the feature crosses application boundaries.
9. Add an ADR only when the decision has architectural significance.

---

## License

See [LICENSE](LICENSE) for the project's license.
