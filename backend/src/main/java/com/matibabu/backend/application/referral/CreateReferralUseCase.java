package com.matibabu.backend.application.referral;

import com.matibabu.backend.domain.referral.Referral;
import com.matibabu.backend.domain.referral.ReferralUrgency;

import java.util.UUID;

public interface CreateReferralUseCase {

    Referral create(
            UUID encounterId,
            UUID referringClinicianId,
            UUID diagnosisId,
            String reason,
            ReferralUrgency urgency,
            String receivingFacility,
            String department
    );
}
