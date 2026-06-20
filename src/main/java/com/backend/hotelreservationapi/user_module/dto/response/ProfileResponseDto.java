package com.backend.hotelreservationapi.user_module.dto.response;

import java.util.UUID;

public record ProfileResponseDto(
        UUID profileId,
        String verifiedEmail,
        String firstName,
        String lastName,
        String phoneNumber,
        String gender,
        String profilePicUrl
) {
}
