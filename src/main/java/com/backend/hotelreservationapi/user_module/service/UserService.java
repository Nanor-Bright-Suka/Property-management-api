package com.backend.hotelreservationapi.user_module.service;

import com.backend.hotelreservationapi.user_module.entity.UserEntity;

import java.util.Optional;

public interface UserService {

    Optional<UserEntity> findByEmail(String email);
    UserEntity createUser(String email);
}
