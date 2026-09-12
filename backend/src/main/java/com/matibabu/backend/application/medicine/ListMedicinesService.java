package com.matibabu.backend.application.medicine;

import com.matibabu.backend.domain.medicine.Medicine;
import com.matibabu.backend.domain.medicine.MedicineRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListMedicinesService implements ListMedicinesUseCase {

    private final MedicineRepository medicineRepository;

    public ListMedicinesService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @Override
    public List<Medicine> search(String query) {
        if (query == null || query.isBlank()) {
            return medicineRepository.findAllActive();
        }

        return medicineRepository.search(query);
    }
}
