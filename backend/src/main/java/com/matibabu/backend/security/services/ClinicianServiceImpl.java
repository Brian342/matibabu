package com.matibabu.backend.security.services;

import com.matibabu.backend.exception.ClinicianAlreadyExistsException;
import com.matibabu.backend.exception.UserNotFoundException;
import com.matibabu.backend.security.entity.Clinician;
import com.matibabu.backend.security.repository.ClinicianRepository;
import com.matibabu.backend.security.dto.RegistrationRequest;
import com.matibabu.backend.security.entity.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ClinicianServiceImpl implements ClinicianService {

    private final ClinicianRepository clinicianRepository;
    private final PasswordEncoder passwordEncoder;

    public ClinicianServiceImpl(ClinicianRepository clinicianRepository, PasswordEncoder passwordEncoder) {
        this.clinicianRepository = clinicianRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public Clinician registerClinician(RegistrationRequest registrationRequest) {
        if (clinicianRepository.existsByEmail(registrationRequest.email())) {
            throw new ClinicianAlreadyExistsException("A clinician with this email already exists");
        }

        Clinician clinician = new Clinician(
                registrationRequest.email(),
                passwordEncoder.encode(registrationRequest.password()),
                Role.USER
        );

        return clinicianRepository.save(clinician);
    }

    @Transactional
    @Override
    public Clinician promoteToAdmin(UUID id) {

        Clinician clinician = clinicianRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                                "Clinician not found"
                ));

        clinician.setRole(Role.ADMIN);

        return clinicianRepository.save(clinician);
    }
}
