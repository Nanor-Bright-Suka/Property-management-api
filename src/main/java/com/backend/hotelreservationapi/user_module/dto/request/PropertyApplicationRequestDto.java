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
    @NotNull(message = "Invalid property type")
    private PropertyType propertyType;

    @NotBlank(message = "Invalid description")
    @Size(message="Description  too short", min = 6)
    private String description;

    @NotEmpty(message = "address cannot be empty")
    @Valid
    List<PropertyAddressRequestDto> propertyAddresses;

    @NotEmpty(message = "Documents are required")
    @Size(min = 2, max = 2, message = "Exactly 2 documents are required")
    private List<PropertyDocumentRequestDto> documents;




}
