package com.matibabu.backend.application.referral;

import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import com.matibabu.backend.domain.facility.FacilityRepository;
import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import com.matibabu.backend.domain.referral.Referral;
import com.matibabu.backend.domain.referral.ReferralRepository;
import com.matibabu.backend.domain.referral.ReferralUrgency;
import com.matibabu.backend.exception.EncounterNotFoundException;
import com.matibabu.backend.exception.FacilityNotFoundException;
import com.matibabu.backend.exception.InvalidDiagnosisReferenceException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class CreateReferralService implements CreateReferralUseCase {

    private final ReferralRepository referralRepository;
    private final EncounterRepository encounterRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final FacilityRepository facilityRepository;

    public CreateReferralService(
            ReferralRepository referralRepository,
            EncounterRepository encounterRepository,
            MedicalRecordRepository medicalRecordRepository,
            FacilityRepository facilityRepository
    ) {
        this.referralRepository = referralRepository;
        this.encounterRepository = encounterRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.facilityRepository = facilityRepository;
    }

    @Override
    public Referral create(
            UUID encounterId,
            UUID referringClinicianId,
            UUID diagnosisId,
            String reason,
            ReferralUrgency urgency,
            UUID receivingFacilityId,
            String department
    ) {
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new EncounterNotFoundException(encounterId));

        facilityRepository.findById(receivingFacilityId)
                .orElseThrow(() -> new FacilityNotFoundException(receivingFacilityId));

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
                receivingFacilityId,
                department,
                Instant.now()
        );

        return referralRepository.save(referral);
    }
}
