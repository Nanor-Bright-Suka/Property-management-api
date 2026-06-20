package com.backend.hotelreservationapi.user_module.dto.request;


import com.backend.hotelreservationapi.user_module.enums.PropertyType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class PropertyApplicationRequestDto {

    @NotBlank(message = "Invalid registration number")
    @Size(message="Verification number too short", min = 5)
    private String registrationNumber;

    @NotNull(message = "Invalid property type")
    private PropertyType propertyType;

    @NotNull(message = "Invalid property count")
    @Min(value = 1, message = "Property Count must be at least one")
    private Integer propertyCount;

    @NotNull(message = "Invalid experience")
    @Min(value = 0, message = "Experience cannot be negative")
    private Integer yearsOfExperience;

    @NotBlank(message = "Invalid description")
    @Size(message="Description  too short", min = 6)
    private String description;

    @NotEmpty(message = "address cannot be empty")
    @Valid
    List<PropertyAddressRequestDto> propertyAddresses;

}
