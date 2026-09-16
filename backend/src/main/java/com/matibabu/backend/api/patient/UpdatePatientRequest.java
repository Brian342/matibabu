package com.matibabu.backend.api.patient;

import com.matibabu.backend.domain.patient.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/*
 * nationalId and birthCertificateNumber are optional, and updatable
 * here since a document is often captured after initial registration
 * rather than at it. See docs/decisions/ADR-08-synchronization.md.
 */
public record UpdatePatientRequest(

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotNull
        @PastOrPresent
        LocalDate dateOfBirth,

        @NotNull
        Gender gender,

        @NotBlank
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone number must be valid (7-15 digits with optional '+' prefix)")
        String phoneNumber,

        @NotBlank
        String address,

        @Size(max = 50)
        String nationalId,

        @Size(max = 50)
        String birthCertificateNumber
) {
}
