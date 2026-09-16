package com.matibabu.backend.domain.patient;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PatientTest {

    @Test
    void createsPatientWithGeneratedUuidAndTimestamps() {
        LocalDate dob = LocalDate.of(1995, 6, 15);
        Patient patient = new Patient("John", "Kamau", dob, Gender.MALE, "+254712345678", "Nairobi", null, null);

        assertNotNull(patient.getId());
        assertEquals(7, patient.getId().version());
        assertEquals("John", patient.getFirstName());
        assertEquals("Kamau", patient.getLastName());
        assertEquals(dob, patient.getDateOfBirth());
        assertEquals(Gender.MALE, patient.getGender());
        assertEquals("+254712345678", patient.getPhoneNumber());
        assertEquals("Nairobi", patient.getAddress());
        assertNull(patient.getNationalId());
        assertNull(patient.getBirthCertificateNumber());
        assertNotNull(patient.getCreatedAt());
        assertNotNull(patient.getUpdatedAt());
        assertEquals(patient.getCreatedAt(), patient.getUpdatedAt());
    }

    @Test
    void createsPatientWithIdentityDocuments() {
        LocalDate dob = LocalDate.of(1995, 6, 15);
        Patient patient = new Patient(
                "John", "Kamau", dob, Gender.MALE, "+254712345678", "Nairobi",
                "12345678", null
        );

        assertEquals("12345678", patient.getNationalId());
        assertNull(patient.getBirthCertificateNumber());
    }

    @Test
    void updatesDemographicInformationAndRefreshesUpdatedAt() throws InterruptedException {
        LocalDate dob = LocalDate.of(1995, 6, 15);
        Patient patient = new Patient("John", "Kamau", dob, Gender.MALE, "+254712345678", "Nairobi", null, null);
        UUID id = patient.getId();
        Instant initialCreatedAt = patient.getCreatedAt();
        Instant initialUpdatedAt = patient.getUpdatedAt();

        Thread.sleep(10);

        patient.update("John", "Kamau", dob, Gender.MALE, "+254700000000", "Mombasa", null, null);

        assertEquals(id, patient.getId());
        assertEquals("John", patient.getFirstName());
        assertEquals("Kamau", patient.getLastName());
        assertEquals("+254700000000", patient.getPhoneNumber());
        assertEquals("Mombasa", patient.getAddress());
        assertEquals(initialCreatedAt, patient.getCreatedAt());
        assertTrue(patient.getUpdatedAt().isAfter(initialUpdatedAt));
    }

    /*
     * Identity documents are often captured after initial
     * registration rather than at it, so update() must be able to
     * set them on a patient that previously had none.
     */
    @Test
    void updateCanSetPreviouslyMissingIdentityDocuments() {
        LocalDate dob = LocalDate.of(1995, 6, 15);
        Patient patient = new Patient("John", "Kamau", dob, Gender.MALE, "+254712345678", "Nairobi", null, null);

        patient.update("John", "Kamau", dob, Gender.MALE, "+254712345678", "Nairobi", "12345678", "BC-001");

        assertEquals("12345678", patient.getNationalId());
        assertEquals("BC-001", patient.getBirthCertificateNumber());
    }

    @Test
    void reconstitutesPatient() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");
        LocalDate dob = LocalDate.of(1990, 1, 1);

        Patient patient = Patient.reconstitute(
                id,
                "Jane",
                "Doe",
                dob,
                Gender.FEMALE,
                "+254711111111",
                "Kisumu",
                "87654321",
                "BC-002",
                createdAt,
                updatedAt
        );

        assertEquals(id, patient.getId());
        assertEquals("Jane", patient.getFirstName());
        assertEquals("Doe", patient.getLastName());
        assertEquals(dob, patient.getDateOfBirth());
        assertEquals(Gender.FEMALE, patient.getGender());
        assertEquals("+254711111111", patient.getPhoneNumber());
        assertEquals("Kisumu", patient.getAddress());
        assertEquals("87654321", patient.getNationalId());
        assertEquals("BC-002", patient.getBirthCertificateNumber());
        assertEquals(createdAt, patient.getCreatedAt());
        assertEquals(updatedAt, patient.getUpdatedAt());
    }

    /*
     * Existing patients predate these fields; reconstitute must
     * tolerate nulls rather than rejecting the row.
     */
    @Test
    void reconstitutesPatientWithoutIdentityDocuments() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        LocalDate dob = LocalDate.of(1990, 1, 1);

        Patient patient = Patient.reconstitute(
                id, "Jane", "Doe", dob, Gender.FEMALE, "+254711111111", "Kisumu",
                null, null, createdAt, createdAt
        );

        assertNull(patient.getNationalId());
        assertNull(patient.getBirthCertificateNumber());
    }
}
