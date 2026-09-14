package com.matibabu.backend.infrastructure.persistence.medicine;

import com.matibabu.backend.domain.medicine.Medicine;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MedicineMapper {

    MedicineEntity toEntity(Medicine medicine);

    default Medicine toDomain(MedicineEntity entity) {
        if (entity == null) {
            return null;
        }

        return Medicine.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getGenericName(),
                entity.getAtcCode(),
                entity.getForm(),
                entity.getStrength(),
                entity.getKemlCode(),
                entity.isActive(),
                entity.getAtcMappingStatus(),
                entity.getKemlVersion(),
                entity.getReviewedBy(),
                entity.getReviewedAt()
        );
    }
}