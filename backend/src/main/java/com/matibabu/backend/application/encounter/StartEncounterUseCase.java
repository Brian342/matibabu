package com.matibabu.backend.application.encounter;

import com.matibabu.backend.domain.encounter.Encounter;

import java.time.Instant;
import java.util.UUID;

public interface StartEncounterUseCase {

    Encounter start(
            UUID patientId,
            UUID attendingClinicianId,
            Instant now
    );
}