package com.matibabu.backend.exception;

import java.util.UUID;

public class InvalidDiagnosisReferenceException extends RuntimeException {
    public InvalidDiagnosisReferenceException(UUID diagnosisId, UUID encounterId) {
        super("Diagnosis " + diagnosisId + " does not belong to encounter " + encounterId);
    }
}
