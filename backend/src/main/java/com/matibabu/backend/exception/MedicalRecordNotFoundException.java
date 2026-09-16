package com.matibabu.backend.exception;

import java.util.UUID;

public class MedicalRecordNotFoundException extends RuntimeException {
    public MedicalRecordNotFoundException(UUID id) {
        super("Medical Record not found: " + id);
    }
}
