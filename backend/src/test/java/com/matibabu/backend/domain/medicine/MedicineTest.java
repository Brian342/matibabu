package com.matibabu.backend.domain.medicine;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MedicineTest {

    @Test
    void newMedicineWithAtcCodeIsConfirmedByDefault() {
        Medicine medicine = new Medicine("Amoxil", "Amoxicillin", "J01CA04", "capsule", "500mg", "KEML-001");

        assertEquals("J01CA04", medicine.getAtcCode());
        assertEquals(AtcMappingStatus.CONFIRMED, medicine.getAtcMappingStatus());
    }

    @Test
    void newMedicineWithoutAtcCodeIsUnmappedByDefault() {
        Medicine medicine = new Medicine("Some Drug", "Some Generic", null, "tablet", "10mg", "KEML-099");

        assertNull(medicine.getAtcCode());
        assertEquals(AtcMappingStatus.UNMAPPED, medicine.getAtcMappingStatus());
    }

    @Test
    void confirmAtcCodeSetsCodeStatusAndReviewer() {
        Medicine medicine = new Medicine("Some Drug", "Some Generic", null, "tablet", "10mg", "KEML-099");
        UUID reviewer = UUID.randomUUID();
        Instant now = Instant.now();

        medicine.confirmAtcCode("N02BE01", reviewer, now);

        assertEquals("N02BE01", medicine.getAtcCode());
        assertEquals(AtcMappingStatus.CONFIRMED, medicine.getAtcMappingStatus());
        assertEquals(reviewer, medicine.getReviewedBy());
        assertEquals(now, medicine.getReviewedAt());
    }

    @Test
    void confirmAtcCodeRejectsNullCode() {
        Medicine medicine = new Medicine("Some Drug", "Some Generic", null, "tablet", "10mg", "KEML-099");

        assertThrows(NullPointerException.class, () ->
                medicine.confirmAtcCode(null, UUID.randomUUID(), Instant.now()));
    }

    @Test
    void markUnmappedClearsCodeAndSetsReviewer() {
        Medicine medicine = new Medicine("Amoxil", "Amoxicillin", "J01CA04", "capsule", "500mg", "KEML-001");
        UUID reviewer = UUID.randomUUID();
        Instant now = Instant.now();

        medicine.markUnmapped(reviewer, now);

        assertNull(medicine.getAtcCode());
        assertEquals(AtcMappingStatus.UNMAPPED, medicine.getAtcMappingStatus());
        assertEquals(reviewer, medicine.getReviewedBy());
        assertEquals(now, medicine.getReviewedAt());
    }

    @Test
    void reconstitutePreservesReviewMetadata() {
        UUID id = UUID.randomUUID();
        UUID reviewer = UUID.randomUUID();
        Instant reviewedAt = Instant.now();

        Medicine medicine = Medicine.reconstitute(
                id, "Amoxil", "Amoxicillin", "J01CA04", "capsule", "500mg", "KEML-001",
                true, AtcMappingStatus.CONFIRMED, "KEML 2023", reviewer, reviewedAt
        );

        assertEquals(id, medicine.getId());
        assertEquals(AtcMappingStatus.CONFIRMED, medicine.getAtcMappingStatus());
        assertEquals("KEML 2023", medicine.getKemlVersion());
        assertEquals(reviewer, medicine.getReviewedBy());
        assertEquals(reviewedAt, medicine.getReviewedAt());
    }
}