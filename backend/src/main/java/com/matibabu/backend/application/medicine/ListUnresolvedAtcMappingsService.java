package com.matibabu.backend.application.medicine;

import com.matibabu.backend.domain.medicine.AtcMappingStatus;
import com.matibabu.backend.domain.medicine.Medicine;
import com.matibabu.backend.domain.medicine.MedicineRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListUnresolvedAtcMappingsService implements ListUnresolvedAtcMappingsUseCase {

    private final MedicineRepository medicineRepository;

    public ListUnresolvedAtcMappingsService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @Override
    public List<Medicine> list() {
        return medicineRepository.findByAtcMappingStatusIn(
                List.of(AtcMappingStatus.AUTO_MATCHED, AtcMappingStatus.NEEDS_REVIEW)
        );
    }
}
