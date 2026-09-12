package com.matibabu.backend.domain.medicine;

import com.github.f4b6a3.uuid.UuidCreator;

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
            boolean active
    ) {
        Medicine medicine = new Medicine(name, genericName, atcCode, form, strength, kemlCode);
        medicine.id = id;
        medicine.active = active;
        return medicine;
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
}
