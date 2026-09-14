package com.matibabu.backend.infrastructure.persistence.medicine;

import com.matibabu.backend.domain.medicine.AtcMappingStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "medicines")
public class MedicineEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    private String name;
    private String genericName;
    private String atcCode;
    private String form;
    private String strength;
    private String kemlCode;
    private boolean active;

    @Enumerated(EnumType.STRING)
    private AtcMappingStatus atcMappingStatus;

    private String kemlVersion;

    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID reviewedBy;

    private Instant reviewedAt;

    protected MedicineEntity() {
        // Required by JPA
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public void setAtcCode(String atcCode) {
        this.atcCode = atcCode;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public void setKemlCode(String kemlCode) {
        this.kemlCode = kemlCode;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public void setAtcMappingStatus(AtcMappingStatus atcMappingStatus) {
        this.atcMappingStatus = atcMappingStatus;
    }

    public AtcMappingStatus getAtcMappingStatus() {
        return atcMappingStatus;
    }

    public void setKemlVersion(String kemlVersion) {
        this.kemlVersion = kemlVersion;
    }

    public String getKemlVersion() {
        return kemlVersion;
    }

    public void setReviewedBy(UUID reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public UUID getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }
}