package com.backend.hotelreservationapi.auth_module.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
