package com.backend.hotelreservationapi.user_module.dto.response;

import com.backend.hotelreservationapi.user_module.enums.MediaTypeEnum;

import java.util.UUID;

public record PropertyMediaResponseDto(
        UUID featureId,
        String mediaUrl,
        MediaTypeEnum mediaTypeEnum

) {
}
