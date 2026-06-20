package com.backend.hotelreservationapi.user_module.entity;


import com.backend.hotelreservationapi.user_module.enums.PropertyApplicationStatus;
import com.backend.hotelreservationapi.user_module.enums.PropertyType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "property_manager_application")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyManagerApplicationEntity {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfileEntity profile;

    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    private PropertyType propertyType;

    private int propertyCount;

    private Integer yearsOfExperience;

    private String description;

    @Enumerated(EnumType.STRING)
    private PropertyApplicationStatus status;

    private Instant submittedAt;

    private Instant updatedAt;


}
