package com.backend.hotelreservationapi.user_module.dto.response;

import com.backend.hotelreservationapi.user_module.enums.PropertyApplicationStatus;

import java.time.Instant;
import java.util.UUID;

public record UpdateApplicationStatusResponseDto(
        UUID applicationId,
        PropertyApplicationStatus previousStatus,
        PropertyApplicationStatus currentStatus,
        String comment,
        Instant updatedAt
) {
}
