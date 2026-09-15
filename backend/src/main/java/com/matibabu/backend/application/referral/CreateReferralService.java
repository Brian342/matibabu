package com.matibabu.backend.application.referral;

import com.matibabu.backend.application.encounter.EncounterNotFoundException;
import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import com.matibabu.backend.domain.referral.Referral;
import com.matibabu.backend.domain.referral.ReferralRepository;
import com.matibabu.backend.domain.referral.ReferralUrgency;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class CreateReferralService implements CreateReferralUseCase {

    private final ReferralRepository referralRepository;
    private final EncounterRepository encounterRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public CreateReferralService(
            ReferralRepository referralRepository,
            EncounterRepository encounterRepository,
            MedicalRecordRepository medicalRecordRepository
    ) {
        this.referralRepository = referralRepository;
        this.encounterRepository = encounterRepository;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    @Override
    public Referral create(
            UUID encounterId,
            UUID referringClinicianId,
            UUID diagnosisId,
            String reason,
            ReferralUrgency urgency,
            String receivingFacility,
            String department
    ) {
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new EncounterNotFoundException(encounterId));

        /*
         * If a diagnosis was given, it must actually belong to this
         * encounter's medical record — otherwise nothing stops a
         * referral from linking to an unrelated patient's diagnosis
         * by mistake. This check belongs here, not in the Referral
         * domain object, since Referral has no reference to
         * MedicalRecord to verify against.
         */
        if (diagnosisId != null) {
            MedicalRecord medicalRecord = medicalRecordRepository.findByEncounterId(encounterId)
                    .orElseThrow(() -> new InvalidDiagnosisReferenceException(diagnosisId, encounterId));

            boolean diagnosisBelongsToEncounter = medicalRecord.getDiagnoses().stream()
                    .anyMatch(diagnosis -> diagnosis.getId().equals(diagnosisId));

            if (!diagnosisBelongsToEncounter) {
                throw new InvalidDiagnosisReferenceException(diagnosisId, encounterId);
            }
        }

        Referral referral = Referral.create(
                encounterId,
                encounter.getPatientId(),
                referringClinicianId,
                diagnosisId,
                reason,
                urgency,
                receivingFacility,
                department,
                Instant.now()
        );

        return referralRepository.save(referral);
    }
}
