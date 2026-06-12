package com.backend.hotelreservationapi.auth_module.dto;

public record AuthenticationResponseDto(

        String accessToken,
        String refreshToken,
        boolean isNewUser
) {
}
