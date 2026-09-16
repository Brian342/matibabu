package com.matibabu.backend.exception;

import java.util.UUID;

public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(UUID id) {
        super("Patient not found: " + id);
    }

    public PatientNotFoundException(String message) {
        super(message);
    }
}