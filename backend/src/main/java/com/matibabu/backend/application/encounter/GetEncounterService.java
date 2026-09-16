package com.matibabu.backend.application.encounter;

import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import com.matibabu.backend.exception.EncounterNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetEncounterService implements GetEncounterUseCase {
    private final EncounterRepository encounterRepository;

    public GetEncounterService(EncounterRepository encounterRepository) {
        this.encounterRepository = encounterRepository;
    }

    @Override
    public Encounter getById(UUID id) {
        return encounterRepository.findById(id)
                .orElseThrow(() -> new EncounterNotFoundException(id));
    }
}