package com.backend.hotelreservationapi.user_module.repository;

import com.backend.hotelreservationapi.user_module.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);
        boolean existsByEmail(String email);


}
