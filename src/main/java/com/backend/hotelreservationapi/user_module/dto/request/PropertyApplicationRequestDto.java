package com.backend.hotelreservationapi.user_module.dto.request;


import com.backend.hotelreservationapi.user_module.enums.PropertyTypeEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class PropertyApplicationRequestDto {

    @NotNull(message = "Invalid property type")
    private PropertyTypeEnum propertyType;

    @NotBlank(message = "Invalid description")
    @Size(message="Description  too short", min = 6)
    private String description;

    @NotEmpty(message = "address cannot be empty")
    @Valid
    List<PropertyApplicationAddressRequestDto> propertyAddresses;

    @NotEmpty(message = "Documents are required")
    @Size(min = 2, max = 2, message = "Exactly 2 documents are required")
    @Valid
    private List<PropertyDocumentRequestDto> documents;




}
