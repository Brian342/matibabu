package com.matibabu.backend.application.encounter;

import com.matibabu.backend.config.NodeIdentity;
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
    private final NodeIdentity nodeIdentity;

    public StartEncounterService(EncounterRepository encounterRepository, NodeIdentity nodeIdentity) {
        this.encounterRepository = encounterRepository;
        this.nodeIdentity = nodeIdentity;
    }

    @Override
    public Encounter start(
            UUID patientId,
            UUID attendingClinicianId,
            Instant now
    ) {
        Encounter encounter =
                Encounter.start(patientId, attendingClinicianId, nodeIdentity.facilityId(), now);

        return encounterRepository.save(encounter);
    }
}