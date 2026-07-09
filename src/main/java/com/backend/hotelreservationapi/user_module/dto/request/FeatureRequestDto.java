package com.backend.hotelreservationapi.user_module.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeatureRequestDto {

    @NotBlank(message = "feature is required")
    private String featureName;
}
