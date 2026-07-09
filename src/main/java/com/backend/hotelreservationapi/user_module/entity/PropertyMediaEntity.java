package com.backend.hotelreservationapi.user_module.entity;


import com.backend.hotelreservationapi.user_module.enums.MediaTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "property_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyMediaEntity {

    @Id
    private UUID id;

    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    private MediaTypeEnum mediaType;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private PropertyEntity property;



}
