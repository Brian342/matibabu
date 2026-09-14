package com.matibabu.backend.application.medicine;

import com.matibabu.backend.domain.medicine.Medicine;
import com.matibabu.backend.domain.medicine.MedicineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ResolveAtcMappingService implements ResolveAtcMappingUseCase {

    private final MedicineRepository medicineRepository;

    public ResolveAtcMappingService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @Override
    @Transactional
    public Medicine confirm(UUID medicineId, String atcCode, UUID reviewedBy) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new MedicineNotFoundException(medicineId));

        medicine.confirmAtcCode(atcCode, reviewedBy, Instant.now());
        medicineRepository.save(medicine);
        return medicine;
    }

    @Override
    @Transactional
    public Medicine markUnmapped(UUID medicineId, UUID reviewedBy) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new MedicineNotFoundException(medicineId));

        medicine.markUnmapped(reviewedBy, Instant.now());
        medicineRepository.save(medicine);
        return medicine;
    }
}
