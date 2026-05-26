package com.backend.hotelreservationapi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;



public record EmailRequestDto (
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email

){}
