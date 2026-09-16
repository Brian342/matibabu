package com.matibabu.backend.exception;

import java.util.UUID;

public class FacilityNotFoundException extends RuntimeException {
    public FacilityNotFoundException(UUID id) {
        super("Facility not found: " + id);
    }
}
