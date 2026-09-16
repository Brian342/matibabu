package com.matibabu.backend.application.medicine;

import com.matibabu.backend.domain.medicine.Medicine;
import com.matibabu.backend.domain.medicine.MedicineRepository;
import com.matibabu.backend.exception.MedicineNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetMedicineService implements GetMedicineUseCase {

    private final MedicineRepository medicineRepository;

    public GetMedicineService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @Override
    public Medicine getById(UUID id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException(id));
    }
}
