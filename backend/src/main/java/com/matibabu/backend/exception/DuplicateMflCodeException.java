package com.matibabu.backend.exception;

public class DuplicateMflCodeException extends RuntimeException {
    public DuplicateMflCodeException(String mflCode) {
        super("A facility with MFL code " + mflCode + " already exists");
    }
}
