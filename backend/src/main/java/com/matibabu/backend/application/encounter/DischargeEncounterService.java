package com.matibabu.backend.application.encounter;

import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import com.matibabu.backend.exception.EncounterNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class DischargeEncounterService implements DischargeEncounterUseCase {

    private final EncounterRepository encounterRepository;

    public DischargeEncounterService(
            EncounterRepository encounterRepository
    ) {
        this.encounterRepository = encounterRepository;
    }

    @Override
    public void discharge(UUID encounterId, Instant now) {

        Encounter encounter =
                encounterRepository.findById(encounterId)
                        .orElseThrow(
                                () -> new EncounterNotFoundException(encounterId)
                        );

        // The domain controls whether the encounter can be discharged.
        
        encounter.discharge(now);

        // Persist updated encounter.
        encounterRepository.save(encounter);
    }
}