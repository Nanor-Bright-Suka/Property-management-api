package com.backend.hotelreservationapi.auth_module.exception;

public class FileTypeValidationException extends RuntimeException {
    public FileTypeValidationException(String message) {
        super(message);
    }
}
