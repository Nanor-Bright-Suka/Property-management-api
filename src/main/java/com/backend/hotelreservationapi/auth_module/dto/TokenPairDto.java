package com.backend.hotelreservationapi.auth_module.dto;

public record TokenPairDto(
        String accessToken,
        String refreshToken
) {
}
