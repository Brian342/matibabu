package com.matibabu.backend.api.medicalrecord;

import java.util.UUID;

public record AddTreatmentRequest(
        UUID medicineId,
        String dose,
        String doseUnit,
        String route,
        String frequency,
        Integer durationDays,
        String notes
) {
}
