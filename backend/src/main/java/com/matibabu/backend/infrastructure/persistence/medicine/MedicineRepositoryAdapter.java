package com.matibabu.backend.infrastructure.persistence.medicine;

import com.matibabu.backend.domain.medicine.AtcMappingStatus;
import com.matibabu.backend.domain.medicine.Medicine;
import com.matibabu.backend.domain.medicine.MedicineRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MedicineRepositoryAdapter implements MedicineRepository {

    private final SpringDataMedicinesRepository jpaRepository;
    private final MedicineMapper mapper;

    public MedicineRepositoryAdapter(
            SpringDataMedicinesRepository jpaRepository,
            MedicineMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Medicine> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Medicine> search(String query) {
        return jpaRepository
                .findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndGenericNameContainingIgnoreCaseOrActiveTrueAndAtcCodeContainingIgnoreCase(
                        query, query, query
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Medicine> findAllActive() {
        return jpaRepository.findByActiveTrue()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Medicine> findByAtcMappingStatusIn(List<AtcMappingStatus> statuses) {
        return jpaRepository.findByAtcMappingStatusIn(statuses)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void save(Medicine medicine) {
        jpaRepository.save(mapper.toEntity(medicine));
    }
}