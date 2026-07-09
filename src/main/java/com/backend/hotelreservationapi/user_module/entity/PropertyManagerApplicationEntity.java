package com.backend.hotelreservationapi.user_module.entity;


import com.backend.hotelreservationapi.user_module.enums.PropertyApplicationStatus;
import com.backend.hotelreservationapi.user_module.enums.PropertyTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
    private UUID applicationId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfileEntity profile;

    @Enumerated(EnumType.STRING)
    private PropertyTypeEnum propertyType;

    private String description;

    @Enumerated(EnumType.STRING)
    private PropertyApplicationStatus status;

    private Instant submittedAt;

    private Instant updatedAt;


    @OneToMany(mappedBy = "application")
    private List<PropertyApplicationDocumentsEntity> documents = new ArrayList<>();

    @OneToMany(mappedBy = "application")
    private List<PropertyApplicationAddressEntity> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "application")
    private List<ApplicationStatusHistoryEntity> histories = new ArrayList<>();

    @OneToMany(mappedBy = "propertyManager", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertyEntity> properties;

}
