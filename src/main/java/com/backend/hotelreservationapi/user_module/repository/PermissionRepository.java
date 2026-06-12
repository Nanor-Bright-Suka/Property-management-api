package com.backend.hotelreservationapi.user_module.repository;

import com.backend.hotelreservationapi.user_module.entity.PermissionEntity;
import com.backend.hotelreservationapi.user_module.enums.PermissionEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID> {
    Optional<PermissionEntity> findByPermissionName(PermissionEnum permissionName);
}
