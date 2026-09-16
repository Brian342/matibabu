package com.matibabu.backend.api.facility;

import com.matibabu.backend.domain.facility.FacilityType;

public record CreateFacilityRequest(
        String name,
        FacilityType type,
        String mflCode
) {
}
