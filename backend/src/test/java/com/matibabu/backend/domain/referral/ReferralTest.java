package com.matibabu.backend.domain.referral;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReferralTest {

    private Referral newReferral() {
        return Referral.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "Suspected fracture, needs imaging",
                ReferralUrgency.URGENT,
                "Kenyatta National Hospital",
                "Radiology",
                Instant.now()
        );
    }

    @Test
    void newReferralIsPending() {
        Referral referral = newReferral();

        assertEquals(ReferralStatus.PENDING, referral.getStatus());
        assertNull(referral.getResolvedBy());
        assertNull(referral.getResolvedAt());
    }

    @Test
    void referralIdIsTimeOrdered() {
        Referral referral = newReferral();

        assertEquals(7, referral.getId().version());
    }

    @Test
    void diagnosisIdIsOptional() {
        Referral referral = newReferral();

        assertNull(referral.getDiagnosisId());
    }

    @Test
    void referralRejectsBlankReason() {
        assertThrows(IllegalArgumentException.class, () ->
                Referral.create(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null,
                        "   ", ReferralUrgency.ROUTINE, "Some Hospital", null, Instant.now()
                ));
    }

    @Test
    void referralRejectsBlankReceivingFacility() {
        assertThrows(IllegalArgumentException.class, () ->
                Referral.create(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null,
                        "Reason", ReferralUrgency.ROUTINE, "", null, Instant.now()
                ));
    }

    @Test
    void referralRequiresUrgency() {
        assertThrows(NullPointerException.class, () ->
                Referral.create(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null,
                        "Reason", null, "Some Hospital", null, Instant.now()
                ));
    }

    @Test
    void completeSetsStatusResolverAndTimestamp() {
        Referral referral = newReferral();
        UUID resolver = UUID.randomUUID();
        Instant now = Instant.now();

        referral.complete(resolver, now);

        assertEquals(ReferralStatus.COMPLETED, referral.getStatus());
        assertEquals(resolver, referral.getResolvedBy());
        assertEquals(now, referral.getResolvedAt());
    }

    @Test
    void cancelSetsStatusResolverAndTimestamp() {
        Referral referral = newReferral();
        UUID resolver = UUID.randomUUID();
        Instant now = Instant.now();

        referral.cancel(resolver, now);

        assertEquals(ReferralStatus.CANCELLED, referral.getStatus());
        assertEquals(resolver, referral.getResolvedBy());
    }

    @Test
    void completingAlreadyResolvedReferralThrows() {
        Referral referral = newReferral();
        referral.complete(UUID.randomUUID(), Instant.now());

        assertThrows(ReferralNotPendingException.class, () ->
                referral.complete(UUID.randomUUID(), Instant.now()));
    }

    @Test
    void cancellingAlreadyResolvedReferralThrows() {
        Referral referral = newReferral();
        referral.cancel(UUID.randomUUID(), Instant.now());

        assertThrows(ReferralNotPendingException.class, () ->
                referral.cancel(UUID.randomUUID(), Instant.now()));
    }

    @Test
    void reconstitutePreservesAllFields() {
        UUID id = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID referringClinicianId = UUID.randomUUID();
        UUID diagnosisId = UUID.randomUUID();
        UUID resolvedBy = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant resolvedAt = Instant.now();

        Referral referral = Referral.reconstitute(
                id, encounterId, patientId, referringClinicianId, diagnosisId,
                "Reason", ReferralUrgency.EMERGENCY, "Hospital", "Cardiology",
                ReferralStatus.COMPLETED, createdAt, resolvedBy, resolvedAt
        );

        assertEquals(id, referral.getId());
        assertEquals(diagnosisId, referral.getDiagnosisId());
        assertEquals(ReferralStatus.COMPLETED, referral.getStatus());
        assertEquals(resolvedBy, referral.getResolvedBy());
        assertEquals(resolvedAt, referral.getResolvedAt());
    }
}
