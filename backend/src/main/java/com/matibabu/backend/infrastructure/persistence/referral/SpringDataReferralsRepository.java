package com.matibabu.backend.infrastructure.persistence.referral;

import com.matibabu.backend.domain.referral.ReferralStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataReferralsRepository extends JpaRepository<ReferralEntity, UUID> {

    List<ReferralEntity> findByEncounterId(UUID encounterId);

    List<ReferralEntity> findByPatientId(UUID patientId);

    List<ReferralEntity> findByStatus(ReferralStatus status);
}
