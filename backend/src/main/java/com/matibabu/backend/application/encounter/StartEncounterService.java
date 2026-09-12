package com.matibabu.backend.application.encounter;

import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class StartEncounterService implements StartEncounterUseCase {

    // Repository handles persistence.
    // The Encounter domain object handles the business rules.
    private final EncounterRepository encounterRepository;

    public StartEncounterService(EncounterRepository encounterRepository) {
        this.encounterRepository = encounterRepository;
    }

    @Override
    public Encounter start(
            UUID patientId,
            UUID attendingClinicianId,
            Instant now
    ) {
        Encounter encounter =
                Encounter.start(patientId, attendingClinicianId, now);

        return encounterRepository.save(encounter);
    }
}