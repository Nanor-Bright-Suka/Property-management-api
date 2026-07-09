package com.backend.hotelreservationapi.user_module.dto.request;


import com.backend.hotelreservationapi.user_module.enums.CurrencyEnum;
import com.backend.hotelreservationapi.user_module.enums.PropertyListingTypeEnum;
import com.backend.hotelreservationapi.user_module.enums.PropertyTypeEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PropertyCreateRequestDto {
    @NotNull(message = "Property type is required")
    private PropertyTypeEnum propertyType;

    @NotEmpty(message = "address cannot be empty")
    @Valid
    private List<PropertyAddressRequestDto> addresses;

    @NotNull(message = "Price cannot be blank")
    @PositiveOrZero(message = "invalid price")
    private double price;

    @NotNull(message = "Property list type is required")
    private PropertyListingTypeEnum listType;

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 4, message = "Must be at least 4 characters")
    private String title;

    @NotNull(message = "number of bedrooms cannot be blank")
    @Positive(message = "invalid number of bedrooms")
    private int numberOfBedrooms;

    @NotNull(message = "Currency type is required")
    private CurrencyEnum currency;

    @NotEmpty(message = "Features cannot be empty")
    @Valid
    private List<FeatureRequestDto> features;

    @NotEmpty(message = "address cannot be empty")
    @Valid
    private List<PropertyMediaRequestDto> media;

}
