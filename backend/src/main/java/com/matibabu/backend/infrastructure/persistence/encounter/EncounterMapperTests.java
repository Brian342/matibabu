
        package com.matibabu.backend.infrastructure.persistence.encounter;

import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EncounterMapperTest {

    private final EncounterMapper mapper =
            Mappers.getMapper(EncounterMapper.class);

    /*
     * Verifies that the MapStruct mapper correctly converts
     * a domain Encounter into an EncounterEntity.
     */
    @Test
    void shouldMapDomainToEntity() {

        UUID patientId = UUID.randomUUID();
        UUID attendingClinicianId = UUID.randomUUID();

        Instant startedAt =
                Instant.parse("2026-08-21T08:00:00Z");

        // Create a domain encounter.
        Encounter encounter =
                Encounter.start(
                        patientId,
                        attendingClinicianId,
                        startedAt
                );

        // Convert domain object to persistence entity.
        EncounterEntity entity =
                mapper.toEntity(encounter);

        // Verify all important fields were mapped.
        assertEquals(
                encounter.getId(),
                entity.getId()
        );

        assertEquals(
                encounter.getPatientId(),
                entity.getPatientId()
        );

        assertEquals(
                encounter.getAttendingClinicianId(),
                entity.getAttendingClinicianId()
        );

        assertEquals(
                encounter.getStartedAt(),
                entity.getStartedAt()
        );

        assertEquals(
                encounter.getStatus(),
                entity.getStatus()
        );

        assertEquals(
                encounter.getEndedAt(),
                entity.getEndedAt()
        );
    }

    /*
     * Verifies that an EncounterEntity can be reconstructed
     * back into the domain Encounter.
     */
    @Test
    void shouldMapEntityToDomain() {

        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID attendingClinicianId = UUID.randomUUID();

        Instant startedAt =
                Instant.parse("2026-08-21T08:00:00Z");

        Instant endedAt =
                Instant.parse("2026-08-21T10:00:00Z");

        // Create a persistence entity.
        EncounterEntity entity =
                new EncounterEntity();

        entity.setId(encounterId);
        entity.setPatientId(patientId);
        entity.setAttendingClinicianId(attendingClinicianId);
        entity.setStartedAt(startedAt);
        entity.setStatus(EncounterStatus.DISCHARGED);
        entity.setEndedAt(endedAt);

        // Convert persistence entity back to the domain.
        Encounter encounter =
                mapper.toDomain(entity);

        // Verify that all values survived the conversion.
        assertEquals(
                encounterId,
                encounter.getId()
        );

        assertEquals(
                patientId,
                encounter.getPatientId()
        );

        assertEquals(
                attendingClinicianId,
                encounter.getAttendingClinicianId()
        );

        assertEquals(
                startedAt,
                encounter.getStartedAt()
        );

        assertEquals(
                EncounterStatus.DISCHARGED,
                encounter.getStatus()
        );

        assertEquals(
                endedAt,
                encounter.getEndedAt()
        );
    }
}

