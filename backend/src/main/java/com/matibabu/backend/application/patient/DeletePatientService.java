package com.matibabu.backend.application.patient;

import com.matibabu.backend.domain.patient.PatientRepository;
import com.matibabu.backend.exception.PatientNotFoundException;

import java.util.UUID;

public class DeletePatientService implements DeletePatientUseCase {

    private final PatientRepository patientRepository;

    public DeletePatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public void delete(UUID id) {
        if (!patientRepository.existsById(id)) {
            throw new PatientNotFoundException(id);
        }
        patientRepository.deleteById(id);
    }
}
