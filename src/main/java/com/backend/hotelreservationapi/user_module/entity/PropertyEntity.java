package com.backend.hotelreservationapi.user_module.entity;


import com.backend.hotelreservationapi.user_module.enums.CurrencyEnum;
import com.backend.hotelreservationapi.user_module.enums.PropertyListingTypeEnum;
import com.backend.hotelreservationapi.user_module.enums.PropertyStatusEnum;
import com.backend.hotelreservationapi.user_module.enums.PropertyTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



@Entity
@Table(name = "my_property")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyEntity {

    @Id
    private UUID propertyId;

    private double price;

    @Enumerated(EnumType.STRING)
    private CurrencyEnum currency;

    @Enumerated(EnumType.STRING)
    private PropertyListingTypeEnum listingType;

    @Enumerated(EnumType.STRING)
    private PropertyTypeEnum propertyType;

    private String title;

    @Column(nullable = false)
    private boolean deleted = false;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<PropertyAddressEntity> addresses = new ArrayList<>();
    
    @OneToMany(mappedBy = "property", cascade =  CascadeType.ALL,  orphanRemoval = true)
    private List<PropertyFeatureEntity> propertyFeature = new ArrayList<>();

    private Integer numberOfBedrooms;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<PropertyMediaEntity> propertyMedia = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private PropertyStatusEnum propertyStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_manager_id")
    private PropertyManagerApplicationEntity propertyManager;


}
