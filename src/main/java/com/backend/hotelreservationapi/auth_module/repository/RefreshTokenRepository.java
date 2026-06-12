package com.backend.hotelreservationapi.auth_module.repository;

import com.backend.hotelreservationapi.auth_module.entity.RefreshTokenEntity;
import com.backend.hotelreservationapi.user_module.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    Optional<RefreshTokenEntity> findByUser(UserEntity user);

    void deleteByUser(UserEntity user);
}
