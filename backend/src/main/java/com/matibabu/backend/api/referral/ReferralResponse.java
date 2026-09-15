package com.matibabu.backend.api.referral;

import com.matibabu.backend.domain.referral.Referral;
import com.matibabu.backend.domain.referral.ReferralStatus;
import com.matibabu.backend.domain.referral.ReferralUrgency;

import java.time.Instant;
import java.util.UUID;

public record ReferralResponse(
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

    public static ReferralResponse from(Referral referral) {
        return new ReferralResponse(
                referral.getId(),
                referral.getEncounterId(),
                referral.getPatientId(),
                referral.getReferringClinicianId(),
                referral.getDiagnosisId(),
                referral.getReason(),
                referral.getUrgency(),
                referral.getReceivingFacility(),
                referral.getDepartment(),
                referral.getStatus(),
                referral.getCreatedAt(),
                referral.getResolvedBy(),
                referral.getResolvedAt()
        );
    }
}
