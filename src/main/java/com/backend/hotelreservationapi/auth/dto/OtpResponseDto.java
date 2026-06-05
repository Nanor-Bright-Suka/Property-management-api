package com.backend.hotelreservationapi.auth.dto;

public record OtpResponseDto(
        String email,
        String message) {
}
