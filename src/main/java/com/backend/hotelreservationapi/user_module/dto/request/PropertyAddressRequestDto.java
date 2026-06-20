package com.backend.hotelreservationapi.user_module.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyAddressRequestDto {

    @NotBlank(message = "City cannot be blank")
    @Size(min = 4, message = "Must be at least 4 characters")
    private String city;

    @NotBlank(message = "Country cannot be blank")
    @Size(min = 4, message = "Must be at least 4 characters")
    private String country;

    @NotBlank(message = "Address cannot be blank")
    @Size(min = 4, message = "Must be at least 4 characters")
    private String address;


}
