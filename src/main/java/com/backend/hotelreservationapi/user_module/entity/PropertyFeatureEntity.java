package com.backend.hotelreservationapi.user_module.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "property_feature")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyFeatureEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private PropertyEntity property;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "feature_id")
    private FeatureEntity feature;
}
