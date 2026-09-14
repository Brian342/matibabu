package com.matibabu.backend.infrastructure.persistence.medicine;

import com.matibabu.backend.domain.medicine.AtcMappingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataMedicinesRepository extends JpaRepository<MedicineEntity, UUID> {

    List<MedicineEntity> findByActiveTrue();

    List<MedicineEntity> findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndGenericNameContainingIgnoreCaseOrActiveTrueAndAtcCodeContainingIgnoreCase(
            String name, String genericName, String atcCode
    );

    List<MedicineEntity> findByAtcMappingStatusIn(List<AtcMappingStatus> statuses);
}