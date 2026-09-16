package com.matibabu.backend.domain.facility;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.Objects;
import java.util.UUID;


public class Facility {

    private final UUID id;
    private String name;
    private FacilityType type;

    private String mflCode;

    private boolean active;

    private Facility(UUID id, String name, FacilityType type, String mflCode) {
        this.id = Objects.requireNonNull(id, "Facility ID cannot be null");

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A facility must have a name");
        }
        this.name = name;

        this.type = Objects.requireNonNull(type, "A facility must have a type");
        this.mflCode = mflCode;
        this.active = true;
    }

    public static Facility create(String name, FacilityType type, String mflCode) {
        return new Facility(UuidCreator.getTimeOrderedEpoch(), name, type, mflCode);
    }

    public static Facility reconstitute(
            UUID id, String name, FacilityType type, String mflCode, boolean active
    ) {
        Facility facility = new Facility(id, name, type, mflCode);
        facility.active = active;
        return facility;
    }

    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A facility must have a name");
        }
        this.name = name;
    }

    public void updateMflCode(String mflCode) {
        this.mflCode = mflCode;
    }

    public void deactivate() {
        this.active = false;
    }

    public void reactivate() {
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public FacilityType getType() {
        return type;
    }

    public String getMflCode() {
        return mflCode;
    }

    public boolean isActive() {
        return active;
    }
}
