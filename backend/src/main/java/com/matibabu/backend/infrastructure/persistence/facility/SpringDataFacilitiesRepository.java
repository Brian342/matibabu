package com.matibabu.backend.infrastructure.persistence.facility;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataFacilitiesRepository extends JpaRepository<FacilityEntity, UUID> {

    Optional<FacilityEntity> findByMflCode(String mflCode);

    boolean existsByMflCode(String mflCode);

    List<FacilityEntity> findByActiveTrue();

    List<FacilityEntity> findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndMflCodeContainingIgnoreCase(
            String name, String mflCode
    );
}
