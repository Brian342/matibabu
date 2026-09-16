-- The facility that recorded this encounter. Nullable because
-- existing encounters predate this concept; every newly started
-- encounter records one going forward. No foreign key: Matibabu does
-- not yet have a first-class Facility table (see the same reasoning
-- for referrals.receiving_facility in V18).

ALTER TABLE encounters ADD COLUMN facility_id CHAR(36);

CREATE INDEX ix_encounters_facility_id ON encounters (facility_id);
