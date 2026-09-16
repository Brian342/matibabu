-- Combines two previously separate migrations (renumbered to V21
-- after a facilities table landed at V19/V20 on main), per
-- docs/decisions/ADR-08-synchronization.md.

-- The facility that recorded this encounter. Nullable because
-- existing encounters predate this concept; every newly started
-- encounter records one going forward. Now that a facilities table
-- exists (V19__create_facilities_table.sql), this is a real foreign
-- key rather than a bare UUID.

ALTER TABLE encounters ADD COLUMN facility_id CHAR(36) REFERENCES facilities(id);

CREATE INDEX ix_encounters_facility_id ON encounters (facility_id);

-- Identity documents used for cross-facility patient matching. Both
-- nullable: a newborn may have neither yet, and neither is required
-- to register a patient. Partial unique indexes (rather than a plain
-- unique constraint) so multiple patients may share a NULL value.
--
-- national_id already exists as a column (V3__add_patient_details.sql),
-- left over from an earlier, more elaborate patient schema that was
-- never wired up to the domain model and has sat unused and
-- unindexed since. This migration only adds the missing uniqueness
-- guarantee for it; it does not recreate the column.

ALTER TABLE patients ADD COLUMN birth_certificate_number VARCHAR(50);

CREATE UNIQUE INDEX ux_patients_national_id ON patients (national_id) WHERE national_id IS NOT NULL;
CREATE UNIQUE INDEX ux_patients_birth_certificate_number ON patients (birth_certificate_number) WHERE birth_certificate_number IS NOT NULL;
