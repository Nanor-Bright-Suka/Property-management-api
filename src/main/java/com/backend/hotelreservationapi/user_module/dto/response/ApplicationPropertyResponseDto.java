package com.backend.hotelreservationapi.user_module.dto.response;

import com.backend.hotelreservationapi.user_module.enums.PropertyApplicationStatus;
import com.backend.hotelreservationapi.user_module.enums.PropertyType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


public record ApplicationPropertyResponseDto(
         UUID applicationId,
         String registrationNumber,
         PropertyType propertyType,
         Integer propertyCount,
         Integer yearsOfExperience,
         String description,
         Instant submittedAt,
         PropertyApplicationStatus  propertyApplicationStatus,
         List<AddressResponseDto> addresses,
         List<DocumentResponseDto> documents


) {
}
