package com.matibabu.backend.infrastructure.persistence.medicalrecord;

import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MedicalRecordMapper {


    MedicalRecordEntity toEntity(MedicalRecord medicalRecord);


     //Reconstruct the domain object from persisted data.

    default MedicalRecord toDomain(MedicalRecordEntity entity) {

        if (entity == null) {
            return null;
        }

        return MedicalRecord.reconstitute(
                entity.getId(),
                entity.getPatientId(),
                entity.getEncounterId(),
                entity.getCreatedAt()
        );
    }
}