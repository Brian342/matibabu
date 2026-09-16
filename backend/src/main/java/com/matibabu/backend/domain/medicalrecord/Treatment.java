package com.matibabu.backend.domain.medicalrecord;

import com.github.f4b6a3.uuid.UuidCreator;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/*
 * A Treatment records something prescribed or performed during a
 * medical record: normally a medication, dosed and administered in a
 * specific way, by a specific clinician.
 *
 * It references a Medicine from the catalog (domain.medicine) rather
 * than storing a drug name as free text, so treatments can be:
 *  - checked against a patient's known allergies,
 *  - aggregated for medicine-utilization / reporting via
 *    the medicine's WHO ATC code.
 *
 * "notes" remains available for anything the structured fields don't
 * capture (e.g. a non-drug intervention, or extra instructions).
 */
public class Treatment {

    private UUID id;
    private UUID medicalRecordId;

    // The prescribed medicine. Nullable: a treatment may describe a
    // non-drug intervention (e.g. "wound dressing changed") that has
    // no catalog entry, in which case notes is required instead.
    private UUID medicineId;

    // The clinician who performed/prescribed this treatment.
    private UUID prescribedByClinicianId;

    private String dose;
    private String doseUnit;
    private String route;
    private String frequency;
    private Integer durationDays;
    private String notes;

    private Instant prescribedAt;

    public Treatment(
            UUID medicalRecordId,
            UUID medicineId,
            UUID prescribedByClinicianId,
            String dose,
            String doseUnit,
            String route,
            String frequency,
            Integer durationDays,
            String notes
    ) {
        if (medicineId == null && (notes == null || notes.isBlank())) {
            throw new IllegalArgumentException(
                    "A treatment must reference a medicine from the catalog " +
                            "or include notes describing what was done"
            );
        }

        this.id = UuidCreator.getTimeOrderedEpoch();
        this.medicalRecordId = Objects.requireNonNull(
                medicalRecordId, "Medical record ID cannot be null"
        );
        this.prescribedByClinicianId = Objects.requireNonNull(
                prescribedByClinicianId,
                "A treatment must record which clinician performed it"
        );
        this.medicineId = medicineId;
        this.dose = dose;
        this.doseUnit = doseUnit;
        this.route = route;
        this.frequency = frequency;
        this.durationDays = durationDays;
        this.notes = notes;
        this.prescribedAt = Instant.now();
    }

    public static Treatment reconstitute(
            UUID id,
            UUID medicalRecordId,
            UUID medicineId,
            UUID prescribedByClinicianId,
            String dose,
            String doseUnit,
            String route,
            String frequency,
            Integer durationDays,
            String notes,
            Instant prescribedAt
    ) {
        Treatment treatment = new Treatment(
                medicalRecordId,
                medicineId,
                prescribedByClinicianId != null ? prescribedByClinicianId : UuidCreator.getTimeOrderedEpoch(),
                dose,
                doseUnit,
                route,
                frequency,
                durationDays,
                notes
        );

        treatment.id = id;
        treatment.prescribedByClinicianId = prescribedByClinicianId;
        treatment.prescribedAt = prescribedAt;

        return treatment;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMedicalRecordId() {
        return medicalRecordId;
    }

    public Optional<UUID> getMedicineId() {
        return Optional.ofNullable(medicineId);
    }

    public UUID getPrescribedByClinicianId() {
        return prescribedByClinicianId;
    }

    public String getDose() {
        return dose;
    }

    public String getDoseUnit() {
        return doseUnit;
    }

    public String getRoute() {
        return route;
    }

    public String getFrequency() {
        return frequency;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getPrescribedAt() {
        return prescribedAt;
    }
}
