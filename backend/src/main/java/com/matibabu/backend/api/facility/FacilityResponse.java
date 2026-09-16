package com.matibabu.backend.api.facility;

import com.matibabu.backend.domain.facility.Facility;
import com.matibabu.backend.domain.facility.FacilityType;

import java.util.UUID;

public record FacilityResponse(
        UUID id,
        String name,
        FacilityType type,
        String mflCode,
        boolean active
) {

    public static FacilityResponse from(Facility facility) {
        return new FacilityResponse(
                facility.getId(),
                facility.getName(),
                facility.getType(),
                facility.getMflCode(),
                facility.isActive()
        );
    }
}
