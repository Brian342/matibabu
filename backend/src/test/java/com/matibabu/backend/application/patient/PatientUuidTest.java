package com.matibabu.backend.application.patient;

import com.matibabu.backend.domain.patient.Gender;
import com.matibabu.backend.domain.patient.Patient;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PatientUuidTest {

    @Test
    void generatesUuidV7() {
        Patient patient = new Patient(
                "Test",
                "Patient",
                LocalDate.of(2000, 1, 1),
                Gender.MALE,
                "0700000000",
                "Nairobi",
                null,
                null
        );

        assertEquals(7, patient.getId().version());
    }
}