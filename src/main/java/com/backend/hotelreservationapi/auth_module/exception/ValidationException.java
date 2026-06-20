package com.backend.hotelreservationapi.auth_module.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
