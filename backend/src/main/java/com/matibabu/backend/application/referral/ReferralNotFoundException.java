package com.matibabu.backend.application.referral;

import java.util.UUID;

public class ReferralNotFoundException extends RuntimeException {
    public ReferralNotFoundException(UUID id) {
        super("Referral not found: " + id);
    }
}
