package com.backend.hotelreservationapi.auth_module.repository;

import com.backend.hotelreservationapi.auth_module.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
}
