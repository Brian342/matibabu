package com.matibabu.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ClinicianAlreadyExistsException extends RuntimeException {
    public ClinicianAlreadyExistsException(String message) {
        super(message);
    }
}
