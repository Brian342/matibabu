package com.matibabu.backend.application.referral;

import com.matibabu.backend.domain.referral.Referral;

import java.util.UUID;

public interface CancelReferralUseCase {
    Referral cancel(UUID referralId, UUID resolvedBy);
}
