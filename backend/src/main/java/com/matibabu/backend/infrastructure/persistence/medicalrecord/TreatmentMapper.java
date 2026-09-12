package com.matibabu.backend.infrastructure.persistence.medicalrecord;

import com.matibabu.backend.domain.medicalrecord.Treatment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TreatmentMapper {

    default TreatmentEntity toEntity(Treatment treatment) {
        if (treatment == null) {
            return null;
        }

        TreatmentEntity entity = new TreatmentEntity();
        entity.setId(treatment.getId());
        entity.setMedicalRecordId(treatment.getMedicalRecordId());
        entity.setMedicineId(treatment.getMedicineId().orElse(null));
        entity.setPrescribedByClinicianId(treatment.getPrescribedByClinicianId());
        entity.setDose(treatment.getDose());
        entity.setDoseUnit(treatment.getDoseUnit());
        entity.setRoute(treatment.getRoute());
        entity.setFrequency(treatment.getFrequency());
        entity.setDurationDays(treatment.getDurationDays());
        entity.setNotes(treatment.getNotes());
        entity.setPrescribedAt(treatment.getPrescribedAt());
        return entity;
    }

    default Treatment toDomain(TreatmentEntity entity) {
        if (entity == null) {
            return null;
        }

        return Treatment.reconstitute(
                entity.getId(),
                entity.getMedicalRecordId(),
                entity.getMedicineId(),
                entity.getPrescribedByClinicianId(),
                entity.getDose(),
                entity.getDoseUnit(),
                entity.getRoute(),
                entity.getFrequency(),
                entity.getDurationDays(),
                entity.getNotes(),
                entity.getPrescribedAt()
        );
    }
}
