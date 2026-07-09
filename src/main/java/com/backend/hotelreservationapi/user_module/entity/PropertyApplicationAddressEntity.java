package com.backend.hotelreservationapi.user_module.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "property_application_address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyApplicationAddressEntity {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private PropertyManagerApplicationEntity application;


    private String city;

    private String country;

    private String address;

    private Instant createdAt;

    private Instant updatedAt;

}
