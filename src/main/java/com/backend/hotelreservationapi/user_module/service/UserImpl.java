package com.backend.hotelreservationapi.user_module.service;

import com.backend.hotelreservationapi.auth_module.exception.NotFoundException;
import com.backend.hotelreservationapi.user_module.entity.RoleEntity;
import com.backend.hotelreservationapi.user_module.enums.RoleEnum;
import com.backend.hotelreservationapi.user_module.repository.RoleRepository;
import com.backend.hotelreservationapi.user_module.repository.UserRepository;
import com.backend.hotelreservationapi.user_module.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserImpl implements UserService {


    private final UserRepository userRepository;
    private final RoleRepository  roleRepository;

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserEntity createUser(String email) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        RoleEntity defaultRole = roleRepository.findByRoleName(RoleEnum.ROLE_USER)
                .orElseThrow(() -> new NotFoundException("Role not found"));
        user.addRole(defaultRole);
        return userRepository.save(user);
    }





}
