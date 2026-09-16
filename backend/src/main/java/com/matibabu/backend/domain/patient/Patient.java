package com.matibabu.backend.domain.patient;

import com.github.f4b6a3.uuid.UuidCreator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class Patient {

    private UUID uuid;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String phoneNumber;
    private String address;

    // Identity documents. Both are nullable: a patient may be a
    // newborn with neither yet issued, or registered before staff
    // capture the document. Neither is required to register a
    // patient. See docs/decisions/ADR-08-synchronization.md.
    private String nationalId;
    private String birthCertificateNumber;

    private Instant createdAt;
    private Instant updatedAt;

    public Patient(
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            Gender gender,
            String phoneNumber,
            String address,
            String nationalId,
            String birthCertificateNumber
    ) {
        this.uuid = UuidCreator.getTimeOrderedEpoch();
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.nationalId = nationalId;
        this.birthCertificateNumber = birthCertificateNumber;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /*
     * Reconstruct an existing patient from persisted data.
     */
    public static Patient reconstitute(
            UUID uuid,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            Gender gender,
            String phoneNumber,
            String address,
            String nationalId,
            String birthCertificateNumber,
            Instant createdAt,
            Instant updatedAt
    ) {
        Patient patient = new Patient(
                firstName,
                lastName,
                dateOfBirth,
                gender,
                phoneNumber,
                address,
                nationalId,
                birthCertificateNumber
        );

        patient.uuid = uuid;
        patient.createdAt = createdAt;
        patient.updatedAt = updatedAt != null ? updatedAt : createdAt;

        return patient;
    }

    public void update(
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            Gender gender,
            String phoneNumber,
            String address,
            String nationalId,
            String birthCertificateNumber
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.nationalId = nationalId;
        this.birthCertificateNumber = birthCertificateNumber;
        this.updatedAt = Instant.now();
    }

    // Getters

    public UUID getId() {
        return uuid;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getNationalId() {
        return nationalId;
    }

    public String getBirthCertificateNumber() {
        return birthCertificateNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}