package com.matibabu.backend.application.encounter;

import com.matibabu.backend.exception.EncounterNotFoundException;
import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import com.matibabu.backend.domain.encounter.EncounterStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DischargeEncounterServiceTest {

    /*
     * Simple in-memory implementation of EncounterRepository.
     *
     * This lets us test the application service without
     * connecting to a DB.
     */
    private static class InMemoryEncounterRepository
            implements EncounterRepository {

        private final Map<UUID, Encounter> encounters = new HashMap<>();

        @Override
        public Encounter save(Encounter encounter) {
            encounters.put(encounter.getId(), encounter);
            return encounter;
        }

        @Override
        public Optional<Encounter> findById(UUID id) {
            return Optional.ofNullable(encounters.get(id));
        }
    }

    @Test
    void shouldDischargeExistingEncounter() {

        UUID patientId = UUID.randomUUID();

        UUID attendingClinicianId =
                UUID.randomUUID();

        Instant startedAt =
                Instant.parse("2026-08-20T10:00:00Z");

        Instant dischargedAt =
                Instant.parse("2026-08-20T12:00:00Z");

        // Create the repository used by the application service.
        InMemoryEncounterRepository repository =
                new InMemoryEncounterRepository();

        // Create an active encounter.
        Encounter encounter =
                Encounter.start(patientId, attendingClinicianId, startedAt);

        // Persist the encounter in our in-memory repository.
        repository.save(encounter);

        // Create the application service.
        DischargeEncounterService service =
                new DischargeEncounterService(repository);

        // Execute the use case.
        service.discharge(
                encounter.getId(),
                dischargedAt
        );

        // Verify that the domain operation happened.
        assertEquals(
                EncounterStatus.DISCHARGED,
                encounter.getStatus()
        );

        // Verify that the discharge time was recorded.
        assertEquals(
                dischargedAt,
                encounter.getEndedAt()
        );

        // Verify that the encounter is still available
        // through the repository after being saved.
        Encounter savedEncounter =
                repository.findById(encounter.getId())
                        .orElseThrow();

        assertEquals(
                EncounterStatus.DISCHARGED,
                savedEncounter.getStatus()
        );
    }

    @Test
    void shouldThrowWhenEncounterDoesNotExist() {

        InMemoryEncounterRepository repository =
                new InMemoryEncounterRepository();

        DischargeEncounterService service =
                new DischargeEncounterService(repository);

        UUID encounterId = UUID.randomUUID();

        Instant dischargedAt =
                Instant.parse("2026-08-20T12:00:00Z");

        assertThrows(
                EncounterNotFoundException.class,
                () -> service.discharge(
                        encounterId,
                        dischargedAt
                )
        );
    }
}