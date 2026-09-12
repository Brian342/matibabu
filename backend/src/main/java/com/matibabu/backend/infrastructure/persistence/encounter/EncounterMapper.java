package com.matibabu.backend.infrastructure.persistence.encounter;

import com.matibabu.backend.domain.encounter.Encounter;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EncounterMapper {

    EncounterEntity toEntity(Encounter encounter);

    default Encounter toDomain(EncounterEntity entity) {
        if (entity == null) {
            return null;
        }

        return Encounter.reconstitute(
                entity.getId(),
                entity.getPatientId(),
                entity.getAttendingClinicianId(),
                entity.getStartedAt(),
                entity.getStatus(),
                entity.getEndedAt()
        );
    }
}