package com.matibabu.backend.infrastructure.persistence.encounter;

import com.matibabu.backend.domain.encounter.EncounterStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "encounters")
public class EncounterEntity {
    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID patientId;

    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID attendingClinicianId;

    private Instant startedAt;

    @Enumerated(EnumType.STRING)
    private EncounterStatus status;

    private Instant endedAt;

    protected EncounterEntity(){
        //required by JPA
    }


    //setters

    public void setId(UUID id) {
        this.id = id;
    }
    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public void setAttendingClinicianId(UUID attendingClinicianId) {
        this.attendingClinicianId = attendingClinicianId;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public void setStatus(EncounterStatus status) {
        this.status = status;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }


    //getters
    public UUID getId() {
        return id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UUID getAttendingClinicianId() {
        return attendingClinicianId;
    }

    public EncounterStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }
}
