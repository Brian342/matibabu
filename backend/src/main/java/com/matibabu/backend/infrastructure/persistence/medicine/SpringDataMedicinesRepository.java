package com.matibabu.backend.infrastructure.persistence.medicine;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataMedicinesRepository extends JpaRepository<MedicineEntity, UUID> {

    List<MedicineEntity> findByActiveTrue();

    List<MedicineEntity> findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndGenericNameContainingIgnoreCaseOrActiveTrueAndAtcCodeContainingIgnoreCase(
            String name, String genericName, String atcCode
    );
}
