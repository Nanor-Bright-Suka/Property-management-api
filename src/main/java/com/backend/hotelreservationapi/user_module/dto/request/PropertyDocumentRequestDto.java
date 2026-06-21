package com.backend.hotelreservationapi.user_module.dto.request;

import com.backend.hotelreservationapi.user_module.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyDocumentRequestDto {

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotBlank(message = "File URL is required")
    private String fileUrl;
}
