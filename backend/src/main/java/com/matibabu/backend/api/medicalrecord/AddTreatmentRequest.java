package com.matibabu.backend.api.medicalrecord;

import java.util.UUID;

/*
 * medicineId is nullable: a treatment describing a non-drug
 * intervention can omit it as long as notes is provided instead
 * (enforced by the Treatment domain object).
 */
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
