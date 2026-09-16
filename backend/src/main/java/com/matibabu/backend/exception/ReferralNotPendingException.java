package com.matibabu.backend.exception;

public class ReferralNotPendingException extends RuntimeException {
    public ReferralNotPendingException(String message) {
        super(message);
    }
}
