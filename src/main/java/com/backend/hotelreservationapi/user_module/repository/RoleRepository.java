package com.backend.hotelreservationapi.user_module.repository;

import com.backend.hotelreservationapi.user_module.entity.RoleEntity;
import com.backend.hotelreservationapi.user_module.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByRoleName(RoleEnum roleName);
}
