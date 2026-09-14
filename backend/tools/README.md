# KEML → ATC matcher

One-off tooling for generating a Flyway seed migration that maps KEML
drug entries to WHO ATC codes. Not part of the running application —
you run this locally each time KEML is updated (every few years) and
commit the generated migration files.

## Why this exists

KEML does not publish ATC codes. WHO's Model List of Essential
Medicines (which KEML largely derives from) does, via the electronic
EML at https://list.essentialmeds.org. This tool automates matching
KEML's generic/INN names against a WHO ATC index export, and clearly
separates confident matches from ones a human needs to check.

## Workflow

1. **Get your two source files:**
    - `keml_export.csv` — your KEML edition, columns: `generic_name,
     brand_name, form, strength, keml_code`
    - `atc_index.csv` — WHO ATC index export, columns: `atc_code,
     atc_name` (exportable from the eEML, or the WHO ATC/DDD index)

2. **Run the matcher:**
   ```
   python match_keml_to_atc.py \
       --keml keml_export.csv \
       --atc atc_index.csv \
       --keml-version "KEML 2023" \
       --out-migration ../../backend/src/main/resources/db/migration/V17__seed_keml_2023_medicines.sql \
       --out-review needs_review.csv
   ```

3. **Review `needs_review.csv`** — a pharmacist fills in
   `confirmed_atc_code` for each row (or leaves it blank if no ATC
   code genuinely applies, e.g. some fixed-dose combinations).

4. **Generate the confirmation migration:**
   ```
   python apply_review_corrections.py \
       --review needs_review.csv \
       --keml-version "KEML 2023" \
       --out-migration ../../backend/src/main/resources/db/migration/V18__confirm_keml_2023_atc_mappings.sql
   ```

5. Run `mvn flyway:migrate` (or just start the app — Flyway runs on
   boot) and commit both migration files.

## On future KEML updates

Don't re-run this against a truncated table — existing `medicine_id`
rows are referenced by treatments/prescriptions. Instead:
- Run the matcher against only the new/changed KEML rows.
- Write an `UPDATE ... WHERE keml_code = ...` migration for changed
  entries, and `UPDATE medicines SET active = false WHERE keml_code
  IN (...)` for anything withdrawn from KEML — see
  `Medicine.deactivate()`, which exists for exactly this.

## Match threshold

`MATCH_THRESHOLD = 0.92` in `match_keml_to_atc.py` controls how
strict the fuzzy match is before something is trusted as
`AUTO_MATCHED` vs routed to `NEEDS_REVIEW`. Exact name matches always
short-circuit to a confident match regardless of this threshold.