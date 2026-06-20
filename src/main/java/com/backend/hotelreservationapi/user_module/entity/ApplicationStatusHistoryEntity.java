package com.backend.hotelreservationapi.user_module.entity;


import com.backend.hotelreservationapi.user_module.enums.PropertyApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "application_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStatusHistoryEntity {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private PropertyManagerApplicationEntity application;

    @Enumerated(EnumType.STRING)
    private PropertyApplicationStatus fromState;

    @Enumerated(EnumType.STRING)
    private PropertyApplicationStatus toState;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity changedByUser;

    private String comment;

    private Instant changedAt;


}
