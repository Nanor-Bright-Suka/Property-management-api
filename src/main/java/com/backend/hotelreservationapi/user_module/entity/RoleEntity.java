package com.backend.hotelreservationapi.user_module.entity;


import com.backend.hotelreservationapi.user_module.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "roles")
public class RoleEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private RoleEnum roleName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<PermissionEntity> permissions = new HashSet<>();

    @Column(nullable = false)
    private Instant createdAt;

    public void addPermission(PermissionEntity permission) {
        this.permissions.add(permission);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleEntity)) return false;
        RoleEntity r = (RoleEntity) o;
        return id != null && id.equals(r.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }




}
