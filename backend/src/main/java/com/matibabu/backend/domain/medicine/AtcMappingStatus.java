package com.matibabu.backend.domain.medicine;

/*
 * Provenance of Medicine.atcCode.
 *
 * CONFIRMED     - a reviewer (pharmacist/admin) has verified this code.
 * AUTO_MATCHED  - assigned by the KEML import matcher on name
 *                 similarity; not yet reviewed by a person.
 * NEEDS_REVIEW  - the importer could not find a confident match
 *                 (common for fixed-dose combinations); atcCode is
 *                 null until a reviewer resolves it.
 * UNMAPPED      - a reviewer has determined no ATC code applies, or
 *                 this medicine hasn't gone through KEML import at all.
 */
public enum AtcMappingStatus {
    CONFIRMED,
    AUTO_MATCHED,
    NEEDS_REVIEW,
    UNMAPPED
}
