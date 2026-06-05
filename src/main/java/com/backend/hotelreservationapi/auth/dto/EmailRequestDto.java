package com.backend.hotelreservationapi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class EmailRequestDto {


    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
