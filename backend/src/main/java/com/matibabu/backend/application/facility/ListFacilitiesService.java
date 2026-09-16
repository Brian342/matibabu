package com.matibabu.backend.application.facility;

import com.matibabu.backend.domain.facility.Facility;
import com.matibabu.backend.domain.facility.FacilityRepository;
import com.matibabu.backend.exception.FacilityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListFacilitiesService implements ListFacilitiesUseCase {

    private final FacilityRepository facilityRepository;

    public ListFacilitiesService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    @Override
    public Facility getById(UUID id) {
        return facilityRepository.findById(id)
                .orElseThrow(() -> new FacilityNotFoundException(id));
    }

    @Override
    public List<Facility> listActive() {
        return facilityRepository.findAllActive();
    }

    @Override
    public List<Facility> search(String query) {
        return facilityRepository.search(query);
    }
}
