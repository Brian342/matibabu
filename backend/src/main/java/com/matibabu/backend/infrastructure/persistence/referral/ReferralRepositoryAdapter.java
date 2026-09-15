package com.matibabu.backend.infrastructure.persistence.referral;

import com.matibabu.backend.domain.referral.Referral;
import com.matibabu.backend.domain.referral.ReferralRepository;
import com.matibabu.backend.domain.referral.ReferralStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ReferralRepositoryAdapter implements ReferralRepository {

    private final SpringDataReferralsRepository jpaRepository;
    private final ReferralMapper mapper;

    public ReferralRepositoryAdapter(
            SpringDataReferralsRepository jpaRepository,
            ReferralMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Referral save(Referral referral) {
        ReferralEntity entity = mapper.toEntity(referral);
        ReferralEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Referral> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Referral> findByEncounterId(UUID encounterId) {
        return jpaRepository.findByEncounterId(encounterId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Referral> findByPatientId(UUID patientId) {
        return jpaRepository.findByPatientId(patientId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Referral> findByStatus(ReferralStatus status) {
        return jpaRepository.findByStatus(status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
