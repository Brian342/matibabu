package com.matibabu.backend.application.encounter;

import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import com.matibabu.backend.exception.EncounterNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
@Service
public class CancelEncounterService implements CancelEncounterUseCase {

    private final EncounterRepository encounterRepository;

    public CancelEncounterService(EncounterRepository encounterRepository) {
        this.encounterRepository = encounterRepository;
    }

    @Override
    // domain encounter class handles the business logic of cancelling an encounter
    public void cancel(UUID encounterId, Instant now) {
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new EncounterNotFoundException(encounterId));

        encounter.cancel(now);

        encounterRepository.save(encounter);
    }
}