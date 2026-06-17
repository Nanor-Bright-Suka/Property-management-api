package com.backend.hotelreservationapi.user_module.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileEntity {

    @Id
    private UUID profileId;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    private String firstName;

    private String lastName;

    private String gender;

    private String verifiedEmail;

    private String profilePicUrl;

   private String phoneNumber;

    private Instant createdAt;

    private Instant updatedAt;




}
