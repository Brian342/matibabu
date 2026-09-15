package com.matibabu.backend.application.referral;

import com.matibabu.backend.domain.referral.Referral;

import java.util.List;
import java.util.UUID;

public interface GetReferralUseCase {

    Referral getById(UUID id);

    List<Referral> listByEncounter(UUID encounterId);

    List<Referral> listByPatient(UUID patientId);
}
