package com.matibabu.backend.domain.medicine;

import com.github.f4b6a3.uuid.UuidCreator;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Medicine {
    private UUID id;
    private String name;
    private String genericName;

    // WHO ATC classification code, e.g. "J01CA04" for amoxicillin.
    // Nullable: not every locally-used medicine has been mapped yet.
    private String atcCode;

    // Dosage form, e.g. "tablet", "syrup", "injection".
    private String form;

    // e.g. "500mg"
    private String strength;

    // Reference code/line item from the Kenya Essential Medicines List.
    // Nullable: some medicines in use locally may not be on the KEML.
    private String kemlCode;

    private boolean active;

    // Provenance of the atcCode value. See AtcMappingStatus for what
    // each state means. Bulk-seeded rows arrive as AUTO_MATCHED or
    // NEEDS_REVIEW; only an explicit confirm/reject by an authorized
    // reviewer moves a row to CONFIRMED or UNMAPPED.
    private AtcMappingStatus atcMappingStatus;

    // Which KEML edition this row was seeded/last confirmed against,
    // e.g. "KEML 2023". Nullable for medicines added outside a KEML
    // import.
    private String kemlVersion;

    // Who resolved atcMappingStatus out of NEEDS_REVIEW, and when.
    // Both null until a reviewer acts on this row.
    private UUID reviewedBy;
    private Instant reviewedAt;

    public Medicine(
            String name,
            String genericName,
            String atcCode,
            String form,
            String strength,
            String kemlCode
    ) {
        this.id = UuidCreator.getTimeOrderedEpoch();
        this.name = Objects.requireNonNull(name, "Medicine name cannot be null");
        this.genericName = genericName;
        this.atcCode = atcCode;
        this.form = form;
        this.strength = strength;
        this.kemlCode = kemlCode;
        this.active = true;
        this.atcMappingStatus = atcCode != null
                ? AtcMappingStatus.CONFIRMED
                : AtcMappingStatus.UNMAPPED;
    }

    /*
     * Reconstruct an existing medicine from persisted data.
     */
    public static Medicine reconstitute(
            UUID id,
            String name,
            String genericName,
            String atcCode,
            String form,
            String strength,
            String kemlCode,
            boolean active,
            AtcMappingStatus atcMappingStatus,
            String kemlVersion,
            UUID reviewedBy,
            Instant reviewedAt
    ) {
        Medicine medicine = new Medicine(name, genericName, atcCode, form, strength, kemlCode);
        medicine.id = id;
        medicine.active = active;
        medicine.atcMappingStatus = atcMappingStatus;
        medicine.kemlVersion = kemlVersion;
        medicine.reviewedBy = reviewedBy;
        medicine.reviewedAt = reviewedAt;
        return medicine;
    }

    /*
     * A reviewer (pharmacist/admin) confirms the correct ATC code for
     * a medicine that was previously AUTO_MATCHED or NEEDS_REVIEW.
     * This is the only path that sets status to CONFIRMED — a bulk
     * seed import cannot self-certify its own guesses.
     */
    public void confirmAtcCode(String atcCode, UUID reviewedBy, Instant reviewedAt) {
        this.atcCode = Objects.requireNonNull(atcCode, "atcCode cannot be null when confirming a mapping");
        this.atcMappingStatus = AtcMappingStatus.CONFIRMED;
        this.reviewedBy = Objects.requireNonNull(reviewedBy, "reviewedBy cannot be null");
        this.reviewedAt = Objects.requireNonNull(reviewedAt, "reviewedAt cannot be null");
    }

    /*
     * A reviewer determines no ATC code genuinely applies (e.g. some
     * fixed-dose combinations have no assigned ATC5 code) rather than
     * leaving the row sitting in NEEDS_REVIEW indefinitely.
     */
    public void markUnmapped(UUID reviewedBy, Instant reviewedAt) {
        this.atcCode = null;
        this.atcMappingStatus = AtcMappingStatus.UNMAPPED;
        this.reviewedBy = Objects.requireNonNull(reviewedBy, "reviewedBy cannot be null");
        this.reviewedAt = Objects.requireNonNull(reviewedAt, "reviewedAt cannot be null");
    }

    /*
     * Retiring a medicine (e.g. withdrawn from the KEML) should not
     * delete it, since existing treatments may still reference it.
     */
    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGenericName() {
        return genericName;
    }

    public String getAtcCode() {
        return atcCode;
    }

    public String getForm() {
        return form;
    }

    public String getStrength() {
        return strength;
    }

    public String getKemlCode() {
        return kemlCode;
    }

    public boolean isActive() {
        return active;
    }

    public AtcMappingStatus getAtcMappingStatus() {
        return atcMappingStatus;
    }

    public String getKemlVersion() {
        return kemlVersion;
    }

    public UUID getReviewedBy() {
        return reviewedBy;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }
}