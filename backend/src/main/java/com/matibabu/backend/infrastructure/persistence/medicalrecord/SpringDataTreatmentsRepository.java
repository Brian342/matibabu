package com.matibabu.backend.infrastructure.persistence.medicalrecord;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataTreatmentsRepository extends JpaRepository<TreatmentEntity, UUID> {

    List<TreatmentEntity> findByMedicalRecordId(UUID medicalRecordId);

    void deleteByMedicalRecordId(UUID medicalRecordId);
}
