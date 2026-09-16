package com.matibabu.backend.infrastructure.persistence.patient;

import com.matibabu.backend.domain.patient.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mapping(target = "id", source = "id")
    PatientEntity toEntity(Patient patient);

    default Patient toDomain(PatientEntity entity) {
        if (entity == null) {
            return null;
        }
        return Patient.reconstitute(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getDateOfBirth(),
                entity.getGender(),
                entity.getPhoneNumber(),
                entity.getAddress(),
                entity.getNationalId(),
                entity.getBirthCertificateNumber(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}