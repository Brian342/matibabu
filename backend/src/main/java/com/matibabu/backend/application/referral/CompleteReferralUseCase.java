package com.matibabu.backend.application.referral;

import com.matibabu.backend.domain.referral.Referral;

import java.util.UUID;

public interface CompleteReferralUseCase {
    Referral complete(UUID referralId, UUID resolvedBy);
}
