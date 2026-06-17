package com.backend.hotelreservationapi.user_module.dto;


import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileDto {

    @Size(min = 2, max = 20, message = "Invalid lastname")
    private String firstName;

    @Size(min = 2, max = 20, message =  "Invalid lastname")
    private String lastName;

    @Size(min = 10, max = 15)
    @Pattern(regexp = "\\d+", message = "Phone number must contain only digits")
    private String phoneNumber;

    @Size(min = 2, max = 6, message = "Invalid gender")
    private String gender;

}
