package com.backend.hotelreservationapi.user_module.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "features")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureEntity {

    @Id
    private UUID featureId;

    @Column(unique = true)
    private String featureName;

    @OneToMany(mappedBy = "feature")
    private List<PropertyFeatureEntity> propertyFeature = new ArrayList<>();
}
