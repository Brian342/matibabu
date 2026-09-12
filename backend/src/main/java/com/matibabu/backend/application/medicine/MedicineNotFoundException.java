package com.matibabu.backend.application.medicine;

import java.util.UUID;

public class MedicineNotFoundException extends RuntimeException {

    public MedicineNotFoundException(UUID id) {
        super("Medicine not found: " + id);
    }
}
