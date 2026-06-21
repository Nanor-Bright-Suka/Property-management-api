package com.backend.hotelreservationapi.user_module.mapper;


import com.backend.hotelreservationapi.user_module.dto.response.AddressResponseDto;
import com.backend.hotelreservationapi.user_module.dto.response.ApplicationPropertyResponseDto;
import com.backend.hotelreservationapi.user_module.dto.response.DocumentResponseDto;
import com.backend.hotelreservationapi.user_module.entity.PropertyAddressEntity;
import com.backend.hotelreservationapi.user_module.entity.PropertyApplicationDocumentsEntity;
import com.backend.hotelreservationapi.user_module.entity.PropertyManagerApplicationEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PropertyApplicationMapper {

    public ApplicationPropertyResponseDto toResponse(
            PropertyManagerApplicationEntity application,
            List<PropertyAddressEntity> addresses,
            List<PropertyApplicationDocumentsEntity> documents) {

        return new ApplicationPropertyResponseDto(
                application.getId(),
                application.getPropertyType(),
                application.getDescription(),
                application.getSubmittedAt(),
                application.getStatus(),

                addresses.stream()
                        .map(this::toAddressResponse)
                        .toList(),

                documents.stream()
                        .map(this::toDocumentResponse)
                        .toList()
        );
    }

    private AddressResponseDto toAddressResponse(
            PropertyAddressEntity address) {

        return new AddressResponseDto(
                address.getId(),
                address.getCity(),
                address.getCountry(),
                address.getAddress(),
                address.getCreatedAt()
        );
    }

    private DocumentResponseDto toDocumentResponse(
            PropertyApplicationDocumentsEntity document) {

        return new DocumentResponseDto(
                document.getId(),
                document.getDocumentType(),
                document.getFileUrl(),
                document.getUploadedAt()
        );
    }


}
