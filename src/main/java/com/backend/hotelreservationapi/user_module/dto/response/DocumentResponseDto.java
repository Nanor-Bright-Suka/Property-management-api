package com.backend.hotelreservationapi.user_module.dto.response;

import com.backend.hotelreservationapi.user_module.enums.DocumentType;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponseDto(
        UUID documentId,
        DocumentType documentType,
        String fileUrl,
        Instant createdAt
) {
}
