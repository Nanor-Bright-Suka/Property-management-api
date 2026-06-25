package com.backend.hotelreservationapi.user_module.repository;

import com.backend.hotelreservationapi.user_module.entity.ProfileEntity;
import com.backend.hotelreservationapi.user_module.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<ProfileEntity, UUID> {
    Optional<ProfileEntity> findByUser(UserEntity user);
    Optional<ProfileEntity> findByUser_UserId(UUID profileUserId);
}
