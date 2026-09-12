package com.matibabu.backend.infrastructure.persistence.medicalrecord;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "medical_record_treatments")
public class TreatmentEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID medicalRecordId;

    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID medicineId;

    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID prescribedByClinicianId;

    private String dose;
    private String doseUnit;
    private String route;
    private String frequency;
    private Integer durationDays;
    private String notes;
    private Instant prescribedAt;

    protected TreatmentEntity() {
        // Required by JPA
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setMedicalRecordId(UUID medicalRecordId) {
        this.medicalRecordId = medicalRecordId;
    }

    public void setMedicineId(UUID medicineId) {
        this.medicineId = medicineId;
    }

    public void setPrescribedByClinicianId(UUID prescribedByClinicianId) {
        this.prescribedByClinicianId = prescribedByClinicianId;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public void setDoseUnit(String doseUnit) {
        this.doseUnit = doseUnit;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setPrescribedAt(Instant prescribedAt) {
        this.prescribedAt = prescribedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMedicalRecordId() {
        return medicalRecordId;
    }

    public UUID getMedicineId() {
        return medicineId;
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
