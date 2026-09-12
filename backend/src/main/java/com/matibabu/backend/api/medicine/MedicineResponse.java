package com.matibabu.backend.api.medicine;

import com.matibabu.backend.domain.medicine.Medicine;

import java.util.UUID;

public record MedicineResponse(
        UUID id,
        String name,
        String genericName,
        String atcCode,
        String form,
        String strength,
        String kemlCode,
        boolean active
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
                medicine.isActive()
        );
    }
}
