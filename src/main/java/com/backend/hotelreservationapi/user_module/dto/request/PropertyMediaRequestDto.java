package com.backend.hotelreservationapi.user_module.dto.request;


import com.backend.hotelreservationapi.user_module.enums.MediaTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyMediaRequestDto {

    @NotBlank(message = "File URL is required")
    private String mediaUrl;

    @NotNull(message = "Document type is required")
    private MediaTypeEnum mediaType;
}
