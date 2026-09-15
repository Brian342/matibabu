package com.matibabu.backend.domain.referral;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReferralRepository {

    Referral save(Referral referral);

    Optional<Referral> findById(UUID id);

    List<Referral> findByEncounterId(UUID encounterId);

    List<Referral> findByPatientId(UUID patientId);

    List<Referral> findByStatus(ReferralStatus status);
}
