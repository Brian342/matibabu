package com.matibabu.backend.infrastructure.persistence.referral;

import com.matibabu.backend.domain.referral.Referral;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReferralMapper {

    ReferralEntity toEntity(Referral referral);

    default Referral toDomain(ReferralEntity entity) {
        if (entity == null) {
            return null;
        }

        return Referral.reconstitute(
                entity.getId(),
                entity.getEncounterId(),
                entity.getPatientId(),
                entity.getReferringClinicianId(),
                entity.getDiagnosisId(),
                entity.getReason(),
                entity.getUrgency(),
                entity.getReceivingFacilityId(),
                entity.getDepartment(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getResolvedBy(),
                entity.getResolvedAt()
        );
    }
}
