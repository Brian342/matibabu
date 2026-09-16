package com.matibabu.backend.application.patient;

import com.matibabu.backend.domain.patient.Gender;
import com.matibabu.backend.domain.patient.Patient;

import java.time.LocalDate;
import java.util.UUID;

public interface UpdatePatientUseCase {

    Patient update(
            UUID id,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            Gender gender,
            String phoneNumber,
            String address,
            String nationalId,
            String birthCertificateNumber
    );
}
