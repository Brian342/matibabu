package com.matibabu.backend.application.medicine;

import com.matibabu.backend.domain.medicine.Medicine;

import java.util.UUID;

public interface ResolveAtcMappingUseCase {
    //revierwer confirms atc code
    Medicine confirm(UUID medicineId, String atcCode, UUID reviewedBy);

    //no atc code sxists for the medicine
    Medicine markUnmapped(UUID medicineId, UUID reviewedBy);
}