package com.backend.hotelreservationapi.auth_module.dto;

import com.backend.hotelreservationapi.user_module.entity.UserEntity;

public record UserCreationResponseDto(
        UserEntity user,
        boolean isNewUser
) {
}
