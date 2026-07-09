package com.backend.hotelreservationapi.user_module.dto.response;

import com.backend.hotelreservationapi.user_module.enums.CurrencyEnum;
import com.backend.hotelreservationapi.user_module.enums.PropertyListingTypeEnum;
import com.backend.hotelreservationapi.user_module.enums.PropertyStatusEnum;
import com.backend.hotelreservationapi.user_module.enums.PropertyTypeEnum;

import java.util.List;
import java.util.UUID;

public record PropertyCreationResponseDto(
        UUID propertyId,
        double price,
        String title,
        int numberOfBedrooms,
        CurrencyEnum currency,
        PropertyTypeEnum propertyType,
        PropertyStatusEnum propertyStatus,
        PropertyListingTypeEnum listType,
        List<AddressResponseDto>addresses,
        List<FeatureResponseDto> features,
        List<PropertyMediaResponseDto> media

) {
}
