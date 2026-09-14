# ADR: Medicine Reference Catalog (KEML / ATC Mapping)

* **Status:** Accepted, partially implemented
* **Date:** 2026-09-14

## Context

Matibabu needs a local reference catalog of prescribable medicines so a
clinician can select a medicine when recording a `Treatment`, rather
than typing a drug name as free text.

The catalog is seeded from the Kenya Essential Medicines List (KEML),
Kenya's national formulary. KEML is updated infrequently (roughly
every few years) and does not itself publish WHO Anatomical
Therapeutic Chemical (ATC) classification codes. ATC codes are
useful for the catalog to support: standardized drug interaction/
classification lookups, interoperability with systems (including
DHIS2, planned) that expect ATC-coded medicine data, and grouping
medicines by therapeutic class independent of local naming.

The WHO Model List of Essential Medicines, which KEML is substantially
derived from, does publish ATC codes for its entries (via the
electronic EML at list.essentialmeds.org). This makes automated,
name-based matching between KEML and the WHO ATC index a viable way
to populate most of the catalog, provided the result is clearly
separated from the smaller set of entries — chiefly fixed-dose
combinations — that automated matching cannot confidently resolve.

## Decision

### Data model

A single `medicines` table (introduced in `V12`) holds the reference
catalog, with `atc_code` and `keml_code` as flat nullable columns on
one row rather than a normalized many-to-many mapping table. Most
KEML entries are single-ingredient and map to exactly one ATC code,
so the added complexity of a join table was not justified at this
stage; this can be revisited if fixed-dose combinations requiring
multiple ATC codes per medicine become common enough to need it.

An `AtcMappingStatus` enum records the provenance of `atc_code`:

```text
CONFIRMED     - a reviewer has verified this code
AUTO_MATCHED  - matched by name against the WHO ATC index, unreviewed
NEEDS_REVIEW  - the importer had no confident match
UNMAPPED      - no ATC code applies, or none has been set
```

`keml_version` records which KEML edition a row was seeded/last
confirmed against (e.g. "KEML 2023"), and `reviewed_by`/`reviewed_at`
record who resolved a mapping out of `NEEDS_REVIEW`, and when.

### Offline matching tool

KEML-to-ATC matching is done by a standalone Python script
(`tools/keml-atc-matcher/`), run manually against a real KEML export
and a WHO ATC index export whenever KEML is updated. It is
deliberately **not** part of the running application:

* It runs a handful of times total (once per KEML edition, every few
  years), so it doesn't need to be a maintained in-app feature.
* It produces Flyway migration files as output, which are reviewed
  and committed like any other schema change, rather than mutating
  the database directly at runtime.
* Confident name matches are written as `AUTO_MATCHED`; anything
  below the similarity threshold is written as `NEEDS_REVIEW` with no
  `atc_code` set, rather than forcing a low-confidence guess into the
  catalog.

### Reviewer resolution

Confirming or rejecting an `AUTO_MATCHED`/`NEEDS_REVIEW` mapping is
exposed through the API, gated to `ADMIN`/`SUPER_ADMIN`
(`/api/admin/medicines/needs-review`,
`PATCH /api/admin/medicines/{id}/atc-mapping`), not to general
clinicians. Resolving a mapping is a pharmacology judgment call on
shared reference data, not a per-patient prescribing action, and the
existing `medicines` table was already documented (in `V12`) as not
expected to be edited through the normal clinical workflow. The
reviewer identity is taken from the authenticated session, consistent
with how `EncounterController.start()` derives the attending
clinician, rather than trusted from client input.

The `Medicine` domain object exposes this as two explicit
transitions — `confirmAtcCode(atcCode, reviewedBy, reviewedAt)` and
`markUnmapped(reviewedBy, reviewedAt)` — so a bulk seed import cannot
mark its own guesses as confirmed; only an explicit reviewer action
can.

### Referential integrity

`reviewed_by` is not enforced as a foreign key at the database level.
SQLite (used for local/offline persistence) cannot add a foreign-key
constraint to an existing table via `ALTER TABLE ... ADD CONSTRAINT`
— the same limitation already documented in `V15` for
`attending_clinician_id` on `encounters`. Referential integrity for
`reviewed_by` is therefore enforced by the application layer instead.

### KEML updates over time

Because KEML changes infrequently and existing `medicine_id` values
are referenced by treatments, a future KEML edition is imported as an
incremental diff (new/changed rows via further migrations,
`Medicine.deactivate()` for withdrawn entries) rather than a full
table rebuild.

## Rationale

Automating the bulk of KEML-to-ATC matching, while keeping ambiguous
cases explicitly unresolved rather than guessed, avoids two failure
modes: hand-coding hundreds of ATC codes (slow, error-prone, and
opaque about where the codes came from), and silently trusting a
fuzzy match for a clinical prescribing system (risks a wrong ATC code
looking equally authoritative as a correct one).

Keeping the matching tool outside the running application matches its
actual usage pattern — a rare, offline, reviewable data-preparation
step — rather than adding a permanent in-app import feature for
something that happens every few years.

Restricting resolution to admins, and deriving the reviewer from the
session rather than the request body, follows the same reasoning
already applied elsewhere in the system (`EncounterController`) and
keeps a clinical-judgment action out of the front-line prescribing
workflow.

## Consequences

### Positive

* Most of the KEML catalog can be populated without hand-typing ATC
  codes, while low-confidence matches are never silently accepted.
* Every `atc_code` value has traceable provenance
  (`atc_mapping_status`, `keml_version`, `reviewed_by`, `reviewed_at`).
* The matching tool is decoupled from the application's build and
  runtime, keeping it simple and dependency-free.
* Resolution of ambiguous mappings is auditable and restricted to an
  appropriate role.

### Trade-offs

* The flat single-ATC-code-per-medicine model does not yet support a
  medicine legitimately needing more than one ATC code (e.g. some
  fixed-dose combinations); this would require introducing a mapping
  table if/when it becomes a real need.
* `reviewed_by` has no database-level referential integrity on
  SQLite; a bad UUID written outside the normal application path
  would not be caught by the database.
* The matcher tool depends on the reviewer supplying accurate KEML
  and WHO ATC index exports; it does not itself validate the
  clinical correctness of a match, only name similarity.
* Seeding real KEML data has not yet happened — the schema, tooling,
  and review workflow are in place, but the catalog itself is still
  empty pending an actual KEML export.

## Implementation

The functionality introduced so far includes:

* `medicines` table (`V12`) with `atc_code`/`keml_code` columns
* `atc_mapping_status`/`keml_version` columns (`V16`)
* `reviewed_by`/`reviewed_at` audit columns (`V17`)
* `AtcMappingStatus` domain enum
* `Medicine.confirmAtcCode(...)` / `Medicine.markUnmapped(...)`
  domain transitions
* `MedicineRepository.findByAtcMappingStatusIn(...)` and `save(...)`
* `ListUnresolvedAtcMappingsUseCase`/`Service` (review queue)
* `ResolveAtcMappingUseCase`/`Service` (confirm/reject)
* `MedicineAtcReviewController` —
  `GET /api/admin/medicines/needs-review`,
  `PATCH /api/admin/medicines/{id}/atc-mapping`
* `tools/keml-atc-matcher/` — offline KEML-to-ATC matching script and
  review-correction script, with accompanying README
* Domain-level unit tests (`MedicineTest`)

Not yet implemented:

* Seeding the catalog from a real KEML export (the tooling exists;
  the actual import has not been run against real KEML data)
* Frontend UI for browsing/searching the catalog or for the admin
  review queue
* A mapping table for medicines needing more than one ATC code

## Status

**Accepted. Schema, domain logic, and review workflow implemented and
tested (`mvn test` passing locally). Catalog not yet seeded with real
KEML data.**