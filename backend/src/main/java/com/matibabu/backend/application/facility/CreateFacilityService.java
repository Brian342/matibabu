package com.matibabu.backend.application.facility;

import com.matibabu.backend.domain.facility.Facility;
import com.matibabu.backend.domain.facility.FacilityRepository;
import com.matibabu.backend.domain.facility.FacilityType;
import com.matibabu.backend.exception.DuplicateMflCodeException;
import org.springframework.stereotype.Service;

@Service
public class CreateFacilityService implements CreateFacilityUseCase {

    private final FacilityRepository facilityRepository;

    public CreateFacilityService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    @Override
    public Facility create(String name, FacilityType type, String mflCode) {
        if (mflCode != null && facilityRepository.existsByMflCode(mflCode)) {
            throw new DuplicateMflCodeException(mflCode);
        }

        Facility facility = Facility.create(name, type, mflCode);
        return facilityRepository.save(facility);
    }
}
