package com.matibabu.backend.application.facility;

import com.matibabu.backend.domain.facility.Facility;
import com.matibabu.backend.domain.facility.FacilityType;

public interface CreateFacilityUseCase {
    Facility create(String name, FacilityType type, String mflCode);
}
