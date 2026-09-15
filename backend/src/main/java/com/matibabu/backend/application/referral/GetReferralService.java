package com.matibabu.backend.application.referral;

import com.matibabu.backend.domain.referral.Referral;
import com.matibabu.backend.domain.referral.ReferralRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetReferralService implements GetReferralUseCase {

    private final ReferralRepository referralRepository;

    public GetReferralService(ReferralRepository referralRepository) {
        this.referralRepository = referralRepository;
    }

    @Override
    public Referral getById(UUID id) {
        return referralRepository.findById(id)
                .orElseThrow(() -> new ReferralNotFoundException(id));
    }

    @Override
    public List<Referral> listByEncounter(UUID encounterId) {
        return referralRepository.findByEncounterId(encounterId);
    }

    @Override
    public List<Referral> listByPatient(UUID patientId) {
        return referralRepository.findByPatientId(patientId);
    }
}
