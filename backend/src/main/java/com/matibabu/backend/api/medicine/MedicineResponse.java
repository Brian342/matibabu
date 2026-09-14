package com.matibabu.backend.api.medicine;

import com.matibabu.backend.domain.medicine.AtcMappingStatus;
import com.matibabu.backend.domain.medicine.Medicine;

import java.time.Instant;
import java.util.UUID;

public record MedicineResponse(
        UUID id,
        String name,
        String genericName,
        String atcCode,
        String form,
        String strength,
        String kemlCode,
        boolean active,
        AtcMappingStatus atcMappingStatus,
        String kemlVersion,
        UUID reviewedBy,
        Instant reviewedAt
) {

    public static MedicineResponse from(Medicine medicine) {
        return new MedicineResponse(
                medicine.getId(),
                medicine.getName(),
                medicine.getGenericName(),
                medicine.getAtcCode(),
                medicine.getForm(),
                medicine.getStrength(),
                medicine.getKemlCode(),
                medicine.isActive(),
                medicine.getAtcMappingStatus(),
                medicine.getKemlVersion(),
                medicine.getReviewedBy(),
                medicine.getReviewedAt()
        );
    }
}