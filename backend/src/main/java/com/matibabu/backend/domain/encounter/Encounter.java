package com.matibabu.backend.domain.encounter;

import com.github.f4b6a3.uuid.UuidCreator;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Encounter {

    private final UUID id;
    private final UUID patientId;
    private final Instant startedAt;

    // The clinician who attended/performed this encounter.
    // Nullable only for encounters recorded before this field existed
    // (see reconstitute); every newly started encounter requires one.
    private UUID attendingClinicianId;

    // The facility that recorded this encounter.
    // Nullable only for encounters recorded before this field existed
    // (see reconstitute); every newly started encounter requires one.
    private UUID facilityId;

    private EncounterStatus status;
    private Instant endedAt;

    private Encounter(
            UUID id,
            UUID patientId,
            Instant startedAt

    ) {
        this.id = Objects.requireNonNull(id, "Encounter ID cannot be null");
        this.patientId = Objects.requireNonNull(patientId, "Patient ID cannot be null");
        this.startedAt = Objects.requireNonNull(startedAt, "Start time cannot be null");


        this.status = EncounterStatus.ACTIVE;
        this.endedAt = null;
    }
    //start an encounter
    public static Encounter start(UUID patientId, UUID attendingClinicianId, UUID facilityId, Instant now) {
        UUID encounterId = UuidCreator.getTimeOrderedEpoch();
        Encounter encounter = new Encounter(
                encounterId,
                patientId,
                now
        );
        encounter.attendingClinicianId = Objects.requireNonNull(
                attendingClinicianId,
                "An encounter must record which clinician is attending"
        );
        encounter.facilityId = Objects.requireNonNull(
                facilityId,
                "An encounter must record which facility it belongs to"
        );
        return encounter;
    }
    //discharge an encounter
    public void discharge(Instant now) {
        ensureActive();

        Objects.requireNonNull(now, "End time cannot be null");

        if (now.isBefore(startedAt)) {
            throw new IllegalArgumentException(
                    "End time cannot be before start time"
            );
        }

        this.status = EncounterStatus.DISCHARGED;
        this.endedAt = now;
    }
    //cancel an encounter
    //TODO handle cancellation constraints
    public void cancel(Instant now) {
        ensureActive();

        Objects.requireNonNull(now, "End time cannot be null");

        if (now.isBefore(startedAt)) {
            throw new IllegalArgumentException(
                    "End time cannot be before start time"
            );
        }

        this.status = EncounterStatus.CANCELLED;
        this.endedAt = now;
    }
    //check if a discharge is active
    private void ensureActive() {
        if (status != EncounterStatus.ACTIVE) {
            throw new EncounterNotActiveException(
                    "Encounter is no longer active"
            );
        }
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

    public UUID getFacilityId() {
        return facilityId;
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
    //if an encounter exists, reconstitute using existing data
    public static Encounter reconstitute(
            UUID id,
            UUID patientId,
            UUID attendingClinicianId,
            UUID facilityId,
            Instant startedAt,
            EncounterStatus status,
            Instant endedAt
    ) {
        Encounter encounter = new Encounter(id, patientId, startedAt);
        encounter.attendingClinicianId = attendingClinicianId;
        encounter.facilityId = facilityId;
        encounter.status = status;
        encounter.endedAt = endedAt;
        return encounter;
    }


}