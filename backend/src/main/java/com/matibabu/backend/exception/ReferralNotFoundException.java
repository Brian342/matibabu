package com.matibabu.backend.exception;

import java.util.UUID;

public class ReferralNotFoundException extends RuntimeException {
    public ReferralNotFoundException(UUID id) {
        super("Referral not found: " + id);
    }
}
