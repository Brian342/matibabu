package com.matibabu.backend.application.encounter;

import com.matibabu.backend.exception.EncounterNotFoundException;
import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import com.matibabu.backend.domain.encounter.EncounterStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GetEncounterServiceTests {

    /*
     * Verifies that the service returns an Encounter when
     * the requested encounter exists in the repository.
     *
     * Expected flow:
     *
     * Repository contains encounter
     *          ↓
     * GetEncounterService.getById()
     *          ↓
     * Encounter is returned
     */
    @Test
    void shouldReturnEncounterWhenFound() {

        UUID patientId = UUID.randomUUID();

        UUID attendingClinicianId =
                UUID.randomUUID();

        UUID facilityId =
                UUID.randomUUID();

        Instant startedAt =
                Instant.parse("2026-08-21T08:00:00Z");

        // Create an Encounter that will be returned by the fake repository.
        Encounter encounter =
                Encounter.start(patientId, attendingClinicianId, facilityId, startedAt);

        /*
         * Use a simple fake repository
         * The fake repository will return the encounter created above
         * when the service asks for it.
         */
        EncounterRepository repository =
                new FakeEncounterRepository(encounter);

        // Create the application service using the repository.
        GetEncounterService service =
                new GetEncounterService(repository);

        // Ask the service to retrieve the encounter.
        Encounter result =
                service.getById(encounter.getId());

        // The service should return an Encounter.
        assertNotNull(result);

        // The returned encounter should have the same ID.
        assertEquals(
                encounter.getId(),
                result.getId()
        );

        // The returned encounter should belong to the correct patient.
        assertEquals(
                patientId,
                result.getPatientId()
        );

        // A newly started encounter should still be ACTIVE.
        assertEquals(
                EncounterStatus.ACTIVE,
                result.getStatus()
        );
    }


    /*
     * Verifies that the service throws EncounterNotFoundException
     * when the requested encounter does not exist.
     *
     * Expected flow:
     *
     * Repository cannot find encounter
     *          ↓
     * Optional.empty()
     *          ↓
     * GetEncounterService.getById()
     *          ↓
     * EncounterNotFoundException
     */
    @Test
    void shouldThrowWhenEncounterNotFound() {

        // Generate an ID that does not exist in the repository.
        UUID encounterId = UUID.randomUUID();

        /*
         * Passing null to the fake repository means that it has
         * no encounter to return.
         */
        EncounterRepository repository =
                new FakeEncounterRepository(null);

        // Create the service using the empty repository.
        GetEncounterService service =
                new GetEncounterService(repository);

        /*
         * The service should translate the empty Optional returned
         * by the repository into an EncounterNotFoundException.
         */
        assertThrows(
                EncounterNotFoundException.class,
                () -> service.getById(encounterId)
        );
    }


    /*
     * A small fake implementation of EncounterRepository used
     * only for these unit tests.
     *
     * We use a fake instead of Mockito because the project is
     * currently testing services without a mocking framework.
     */
    private static class FakeEncounterRepository
            implements EncounterRepository {

        private final Encounter encounter;

        /*
         * The fake repository receives an Encounter that it should
         * return when findById() is called.
         *
         * If null is supplied, the repository behaves as though
         * the encounter does not exist.
         */
        private FakeEncounterRepository(Encounter encounter) {
            this.encounter = encounter;
        }

        /*
         * Save is required because EncounterRepository defines it.
         *
         * This test does not use save(), so we simply return the
         * supplied encounter.
         */
        @Override
        public Encounter save(Encounter encounter) {
            return encounter;
        }

        /*
         * Simulates looking up an encounter by ID.
         *
         * If the fake repository contains an encounter and its ID
         * matches the requested ID, return it.
         *
         * Otherwise, behave like a real repository when no record
         * is found by returning Optional.empty().
         */
        @Override
        public Optional<Encounter> findById(UUID id) {

            if (encounter != null &&
                    encounter.getId().equals(id)) {

                return Optional.of(encounter);
            }

            return Optional.empty();
        }
    }
}