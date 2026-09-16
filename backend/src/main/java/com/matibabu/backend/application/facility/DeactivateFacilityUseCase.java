package com.matibabu.backend.application.facility;

import com.matibabu.backend.domain.facility.Facility;

import java.util.UUID;

public interface DeactivateFacilityUseCase {
    Facility deactivate(UUID id);
}
