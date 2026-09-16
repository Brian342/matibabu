package com.matibabu.backend.domain.medicine;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicineRepository {

    Optional<Medicine> findById(UUID id);

    List<Medicine> search(String query);

    List<Medicine> findAllActive();


    List<Medicine> findByAtcMappingStatusIn(List<AtcMappingStatus> statuses);

    void save(Medicine medicine);
}