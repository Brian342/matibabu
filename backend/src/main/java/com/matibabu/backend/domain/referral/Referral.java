package com.matibabu.backend.domain.referral;

import com.github.f4b6a3.uuid.UuidCreator;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Referral {

    private final UUID id;
    private final UUID encounterId;
    private final UUID patientId;
    private final UUID referringClinicianId;

    // The diagnosis that prompted this referral, if any. Optional:

    private final UUID diagnosisId;

    private final String reason;
    private final ReferralUrgency urgency;

    // Free text for now: Matibabu does not yet have a first-class facility concept TODO
    private final String receivingFacility;

    private final String department;

    private ReferralStatus status;
    private final Instant createdAt;
    private UUID resolvedBy;
    private Instant resolvedAt;

    private Referral(
            UUID id,
            UUID encounterId,
            UUID patientId,
            UUID referringClinicianId,
            UUID diagnosisId,
            String reason,
            ReferralUrgency urgency,
            String receivingFacility,
            String department,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "Referral ID cannot be null");
        this.encounterId = Objects.requireNonNull(encounterId, "Encounter ID cannot be null");
        this.patientId = Objects.requireNonNull(patientId, "Patient ID cannot be null");
        this.referringClinicianId = Objects.requireNonNull(referringClinicianId, "Referring clinician ID cannot be null");
        this.diagnosisId = diagnosisId;

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A referral must have a reason");
        }
        this.reason = reason;

        this.urgency = Objects.requireNonNull(urgency, "A referral must have an urgency");

        if (receivingFacility == null || receivingFacility.isBlank()) {
            throw new IllegalArgumentException("A referral must specify a receiving facility");
        }
        this.receivingFacility = receivingFacility;

        this.department = department;
        this.createdAt = Objects.requireNonNull(createdAt, "Created time cannot be null");
        this.status = ReferralStatus.PENDING;
    }

    public static Referral create(
            UUID encounterId,
            UUID patientId,
            UUID referringClinicianId,
            UUID diagnosisId,
            String reason,
            ReferralUrgency urgency,
            String receivingFacility,
            String department,
            Instant now
    ) {
        return new Referral(
                UuidCreator.getTimeOrderedEpoch(),
                encounterId,
                patientId,
                referringClinicianId,
                diagnosisId,
                reason,
                urgency,
                receivingFacility,
                department,
                now
        );
    }


    public void complete(UUID resolvedBy, Instant resolvedAt) {
        ensurePending();
        this.status = ReferralStatus.COMPLETED;
        this.resolvedBy = Objects.requireNonNull(resolvedBy, "resolvedBy cannot be null");
        this.resolvedAt = Objects.requireNonNull(resolvedAt, "resolvedAt cannot be null");
    }

    public void cancel(UUID resolvedBy, Instant resolvedAt) {
        ensurePending();
        this.status = ReferralStatus.CANCELLED;
        this.resolvedBy = Objects.requireNonNull(resolvedBy, "resolvedBy cannot be null");
        this.resolvedAt = Objects.requireNonNull(resolvedAt, "resolvedAt cannot be null");
    }

    private void ensurePending() {
        if (status != ReferralStatus.PENDING) {
            throw new ReferralNotPendingException(
                    "Referral is already " + status + " and cannot be changed"
            );
        }
    }

    public static Referral reconstitute(
            UUID id,
            UUID encounterId,
            UUID patientId,
            UUID referringClinicianId,
            UUID diagnosisId,
            String reason,
            ReferralUrgency urgency,
            String receivingFacility,
            String department,
            ReferralStatus status,
            Instant createdAt,
            UUID resolvedBy,
            Instant resolvedAt
    ) {
        Referral referral = new Referral(
                id, encounterId, patientId, referringClinicianId, diagnosisId,
                reason, urgency, receivingFacility, department, createdAt
        );
        referral.status = status;
        referral.resolvedBy = resolvedBy;
        referral.resolvedAt = resolvedAt;
        return referral;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UUID getReferringClinicianId() {
        return referringClinicianId;
    }

    public UUID getDiagnosisId() {
        return diagnosisId;
    }

    public String getReason() {
        return reason;
    }

    public ReferralUrgency getUrgency() {
        return urgency;
    }

    public String getReceivingFacility() {
        return receivingFacility;
    }

    public String getDepartment() {
        return department;
    }

    public ReferralStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UUID getResolvedBy() {
        return resolvedBy;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }
}
