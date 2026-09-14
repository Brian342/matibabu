package com.matibabu.backend.application.medicine;

import com.matibabu.backend.domain.medicine.Medicine;

import java.util.UUID;

public interface ResolveAtcMappingUseCase {

    /*
     * A reviewer confirms the ATC code for a medicine that was
     * AUTO_MATCHED or NEEDS_REVIEW.
     */
    Medicine confirm(UUID medicineId, String atcCode, UUID reviewedBy);

    /*
     * A reviewer determines no ATC code applies to this medicine
     * (e.g. an unassigned fixed-dose combination), rather than
     * leaving it in NEEDS_REVIEW indefinitely.
     */
    Medicine markUnmapped(UUID medicineId, UUID reviewedBy);
}