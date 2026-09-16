package com.matibabu.backend.infrastructure.persistence.facility;

import com.matibabu.backend.domain.facility.Facility;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FacilityMapper {

    FacilityEntity toEntity(Facility facility);

    default Facility toDomain(FacilityEntity entity) {
        if (entity == null) {
            return null;
        }

        return Facility.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getType(),
                entity.getMflCode(),
                entity.isActive()
        );
    }
}
