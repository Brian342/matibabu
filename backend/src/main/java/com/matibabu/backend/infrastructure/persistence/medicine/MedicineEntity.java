package com.matibabu.backend.infrastructure.persistence.medicine;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
}
