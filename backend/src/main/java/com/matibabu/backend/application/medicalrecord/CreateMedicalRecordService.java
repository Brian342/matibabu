package com.matibabu.backend.application.medicalrecord;

import com.matibabu.backend.exception.EncounterNotFoundException;
import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateMedicalRecordService implements CreateMedicalRecordUseCase {

    private final MedicalRecordRepository medicalRecordRepository;
    private final EncounterRepository encounterRepository;

    public CreateMedicalRecordService(
            MedicalRecordRepository medicalRecordRepository,
            EncounterRepository encounterRepository
    ) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.encounterRepository = encounterRepository;
    }

    @Override
    public MedicalRecord create(UUID encounterId) {

        /*
         * Find the encounter first.
         *
         * A medical record cannot exist for an encounter
         * that does not exist.
         */
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(
                        () -> new EncounterNotFoundException(encounterId)
                );

        /*
         * The encounter already knows which patient it belongs to.
         * We use that patient ID when creating the medical record.
         */
        UUID patientId = encounter.getPatientId();

        /*
         * Create the medical record and associate it with
         * both the patient and the encounter.
         */
        MedicalRecord medicalRecord =
                new MedicalRecord(patientId, encounterId);

        // Persist the new medical record.
        return medicalRecordRepository.save(medicalRecord);
    }
}