package com.backend.hotelreservationapi.auth_module.util;

import java.time.Instant;


public record ApiErrorResponse(
         int status,
         String message,
         String path,
         Instant timestamp

) {

    public ApiErrorResponse(int status, String message, String path) {
        this(status, message, path, Instant.now());
    }
}
