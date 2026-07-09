package com.backend.hotelreservationapi.user_module.dto.response;


import java.util.UUID;

public record FeatureResponseDto(
        UUID featureId,
        String featureName
) {
}
