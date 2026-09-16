package com.matibabu.backend.security;

import com.matibabu.backend.exception.UserNotFoundException;
import com.matibabu.backend.security.entity.Clinician;
import com.matibabu.backend.security.repository.ClinicianRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ClinicianRepository clinicianRepository;

    public CustomUserDetailsService(ClinicianRepository clinicianRepository) {
        this.clinicianRepository = clinicianRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Clinician clinician = clinicianRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("Clinician not found"));

        return new CustomUserDetails(clinician);
    }
}
