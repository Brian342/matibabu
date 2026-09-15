package com.matibabu.backend.domain.referral;

public class ReferralNotPendingException extends RuntimeException {
    public ReferralNotPendingException(String message) {
        super(message);
    }
}
