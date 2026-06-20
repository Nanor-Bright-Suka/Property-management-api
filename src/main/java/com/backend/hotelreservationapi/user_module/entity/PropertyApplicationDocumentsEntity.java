package com.backend.hotelreservationapi.user_module.entity;


import com.backend.hotelreservationapi.user_module.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "property_application_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyApplicationDocumentsEntity {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private PropertyManagerApplicationEntity application;

    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    private String fileUrl;

    private Instant uploadedAt;

    private Instant updatedAt;


}
