package com.matibabu.backend.infrastructure.persistence.referral;

import com.matibabu.backend.domain.referral.ReferralStatus;
import com.matibabu.backend.domain.referral.ReferralUrgency;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "referrals")
public class ReferralEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID encounterId;

    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID patientId;

    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID referringClinicianId;

    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID diagnosisId;

    private String reason;

    @Enumerated(EnumType.STRING)
    private ReferralUrgency urgency;

    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID receivingFacilityId;
    private String department;

    @Enumerated(EnumType.STRING)
    private ReferralStatus status;

    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID resolvedBy;

    private Instant resolvedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(UUID encounterId) {
        this.encounterId = encounterId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getReferringClinicianId() {
        return referringClinicianId;
    }

    public void setReferringClinicianId(UUID referringClinicianId) {
        this.referringClinicianId = referringClinicianId;
    }

    public UUID getDiagnosisId() {
        return diagnosisId;
    }

    public void setDiagnosisId(UUID diagnosisId) {
        this.diagnosisId = diagnosisId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ReferralUrgency getUrgency() {
        return urgency;
    }

    public void setUrgency(ReferralUrgency urgency) {
        this.urgency = urgency;
    }

    public UUID getReceivingFacilityId() {
        return receivingFacilityId;
    }

    public void setReceivingFacilityId(UUID receivingFacilityId) {
        this.receivingFacilityId = receivingFacilityId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public ReferralStatus getStatus() {
        return status;
    }

    public void setStatus(ReferralStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(UUID resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
