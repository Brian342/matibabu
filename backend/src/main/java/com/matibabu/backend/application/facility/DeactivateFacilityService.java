package com.matibabu.backend.application.facility;

import com.matibabu.backend.domain.facility.Facility;
import com.matibabu.backend.domain.facility.FacilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.matibabu.backend.exception.FacilityNotFoundException;

import java.util.UUID;

@Service
public class DeactivateFacilityService implements DeactivateFacilityUseCase {

    private final FacilityRepository facilityRepository;

    public DeactivateFacilityService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    @Override
    @Transactional
    public Facility deactivate(UUID id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new FacilityNotFoundException(id));

        facility.deactivate();
        return facilityRepository.save(facility);
    }
}
