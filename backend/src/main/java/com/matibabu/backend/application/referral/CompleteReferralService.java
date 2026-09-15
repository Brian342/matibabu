package com.matibabu.backend.application.referral;

import com.matibabu.backend.domain.referral.Referral;
import com.matibabu.backend.domain.referral.ReferralRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CompleteReferralService implements CompleteReferralUseCase {

    private final ReferralRepository referralRepository;

    public CompleteReferralService(ReferralRepository referralRepository) {
        this.referralRepository = referralRepository;
    }

    @Override
    @Transactional
    public Referral complete(UUID referralId, UUID resolvedBy) {
        Referral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new ReferralNotFoundException(referralId));

        referral.complete(resolvedBy, Instant.now());
        return referralRepository.save(referral);
    }
}
