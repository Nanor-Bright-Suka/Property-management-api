package com.backend.hotelreservationapi.user_module.entity;


import com.backend.hotelreservationapi.user_module.enums.PermissionEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Data
@Table(name = "permission")
public class PermissionEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private PermissionEnum permissionName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PermissionEntity)) return false;
        PermissionEntity p = (PermissionEntity) o;
        return id != null && id.equals(p.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
