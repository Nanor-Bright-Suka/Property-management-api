package com.backend.hotelreservationapi.auth_module.dto;

public record OtpResponseDto(
        String email,
        String message) {
}
