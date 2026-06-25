package com.backend.hotelreservationapi.user_module.entity;


import com.backend.hotelreservationapi.auth_module.entity.RefreshTokenEntity;
import com.backend.hotelreservationapi.user_module.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.*;

@Table(name = "appUsers")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    private UUID userId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "isNew", nullable = false)
    private Boolean isNewUser;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<RefreshTokenEntity> refreshTokens = new HashSet<>();



    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<RoleEntity> roles = new HashSet<>();


    @Column(name = "created_at",  nullable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        this.userId = UUID.randomUUID();
        this.isNewUser = true;
        this.createdAt = Instant.now();
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity)) return false;
        UserEntity u = (UserEntity) o;
        return userId != null && userId.equals(u.userId);
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }


    public void addRole(RoleEntity role) {
        this.roles.add(role);
    }
}
