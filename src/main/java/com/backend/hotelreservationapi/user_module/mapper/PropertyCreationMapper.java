package com.backend.hotelreservationapi.user_module.mapper;

import com.backend.hotelreservationapi.user_module.dto.response.AddressResponseDto;
import com.backend.hotelreservationapi.user_module.dto.response.FeatureResponseDto;
import com.backend.hotelreservationapi.user_module.dto.response.PropertyCreationResponseDto;
import com.backend.hotelreservationapi.user_module.dto.response.PropertyMediaResponseDto;
import com.backend.hotelreservationapi.user_module.entity.*;
import org.springframework.stereotype.Component;

@Component
public class PropertyCreationMapper {

    public PropertyCreationResponseDto toResponse(PropertyEntity property) {
        return new PropertyCreationResponseDto(
                property.getPropertyId(),
                property.getPrice(),
                property.getTitle(),
                property.getNumberOfBedrooms(),
                property.getCurrency(),
                property.getPropertyType(),
                property.getPropertyStatus(),
                property.getListingType(),

                property.getAddresses().stream()
                        .map(this::toAddressResponse)
                        .toList(),

                property.getPropertyFeature().stream()
                        .map(this::toPropertyFeatureResponse)
                        .toList(),

                property.getPropertyMedia().stream()
                        .map(this::toPropertyMediaResponse)
                        .toList()


        );

    }

    private AddressResponseDto toAddressResponse(PropertyAddressEntity address) {
        return new AddressResponseDto(
                address.getId(),
                address.getCity(),
                address.getCountry(),
                address.getAddress(),
                address.getCreatedAt()
        );
    }

    private PropertyMediaResponseDto toPropertyMediaResponse(PropertyMediaEntity media) {
        return new PropertyMediaResponseDto(
                media.getId(),
                media.getMediaUrl(),
                media.getMediaType()
        );
    }


    private FeatureResponseDto toPropertyFeatureResponse(PropertyFeatureEntity feature) {
        return new FeatureResponseDto(
                feature.getId(),
                feature.getFeature().getFeatureName()
        );
    }






}
