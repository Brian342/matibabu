package com.matibabu.backend.application.medicalrecord;

import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import com.matibabu.backend.domain.medicalrecord.Treatment;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AddTreatment {

    private final MedicalRecordRepository medicalRecordRepository;

    public AddTreatment(MedicalRecordRepository medicalRecordRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
    }

    public MedicalRecord execute(
            UUID medicalRecordId,
            UUID medicineId,
            UUID prescribedByClinicianId,
            String dose,
            String doseUnit,
            String route,
            String frequency,
            Integer durationDays,
            String notes
    ) {
        MedicalRecord medicalRecord =
                medicalRecordRepository.findById(medicalRecordId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Medical record not found: " + medicalRecordId
                        ));

        Treatment treatment = new Treatment(
                medicalRecordId,
                medicineId,
                prescribedByClinicianId,
                dose,
                doseUnit,
                route,
                frequency,
                durationDays,
                notes
        );

        medicalRecord.addTreatment(treatment);

        return medicalRecordRepository.save(medicalRecord);
    }
}