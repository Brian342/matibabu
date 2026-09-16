package com.matibabu.backend.api.referral;

import com.matibabu.backend.domain.referral.ReferralUrgency;

import java.util.UUID;


public record CreateReferralRequest(
        UUID diagnosisId,
        String reason,
        ReferralUrgency urgency,
        String receivingFacility,
        String department
) {
}
