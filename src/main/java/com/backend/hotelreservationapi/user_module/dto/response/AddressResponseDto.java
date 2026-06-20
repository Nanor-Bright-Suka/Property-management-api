package com.backend.hotelreservationapi.user_module.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AddressResponseDto(
        UUID addressId,
        String city,
        String country,
        String address,
        Instant createdAt
) {
}
