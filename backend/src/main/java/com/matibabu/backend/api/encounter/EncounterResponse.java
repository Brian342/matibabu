package com.matibabu.backend.api.encounter;

import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterStatus;

import java.time.Instant;
import java.util.UUID;

public record EncounterResponse(
        UUID id,
        UUID patientId,
        UUID attendingClinicianId,
        Instant startedAt,
        EncounterStatus status,
        Instant endedAt
) {

    public static EncounterResponse from(Encounter encounter) {
        return new EncounterResponse(
                encounter.getId(),
                encounter.getPatientId(),
                encounter.getAttendingClinicianId(),
                encounter.getStartedAt(),
                encounter.getStatus(),
                encounter.getEndedAt()
        );
    }
}