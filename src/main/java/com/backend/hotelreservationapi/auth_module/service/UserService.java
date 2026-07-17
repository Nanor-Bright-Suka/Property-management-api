package com.backend.hotelreservationapi.auth_module.service;

import com.backend.hotelreservationapi.auth_module.dto.UserCreationResponseDto;
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
public class  UserService {


    private final UserRepository userRepository;
    private final RoleRepository  roleRepository;


    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    private UserEntity createUser(String email) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        RoleEntity defaultRole = roleRepository.findByRoleName(RoleEnum.ROLE_USER)
                .orElseThrow(() -> new NotFoundException("Role not found"));
        user.addRole(defaultRole);
        return userRepository.save(user);
    }


    public UserCreationResponseDto findOrCreateUser(String email) {
        Optional<UserEntity> existingUser = findByEmail(email);

        if (existingUser.isPresent()) {
            return new UserCreationResponseDto(existingUser.get(), false);
        }

        UserEntity user = createUser(email);
        return new UserCreationResponseDto(user, true);
    }



}
