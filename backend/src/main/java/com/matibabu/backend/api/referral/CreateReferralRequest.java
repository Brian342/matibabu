package com.matibabu.backend.api.referral;

import com.matibabu.backend.domain.referral.ReferralUrgency;

import java.util.UUID;

/*
 * diagnosisId is nullable: a referral can legitimately precede a
 * confirmed diagnosis (see Referral's own documentation).
 * referringClinicianId is deliberately absent — taken from the
 * authenticated session in the controller, same reasoning as
 * EncounterController.start().
 */
public record CreateReferralRequest(
        UUID diagnosisId,
        String reason,
        ReferralUrgency urgency,
        String receivingFacility,
        String department
) {
}
