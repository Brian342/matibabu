package com.matibabu.backend.application.referral;

import com.matibabu.backend.domain.referral.Referral;
import com.matibabu.backend.domain.referral.ReferralRepository;
import com.matibabu.backend.exception.ReferralNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CancelReferralService implements CancelReferralUseCase {

    private final ReferralRepository referralRepository;

    public CancelReferralService(ReferralRepository referralRepository) {
        this.referralRepository = referralRepository;
    }

    @Override
    @Transactional
    public Referral cancel(UUID referralId, UUID resolvedBy) {
        Referral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new ReferralNotFoundException(referralId));

        referral.cancel(resolvedBy, Instant.now());
        return referralRepository.save(referral);
    }
}
