package com.backend.hotelreservationapi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record VerifyOtpRequestDto(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Length(max = 6)
        String otp
) {

}
