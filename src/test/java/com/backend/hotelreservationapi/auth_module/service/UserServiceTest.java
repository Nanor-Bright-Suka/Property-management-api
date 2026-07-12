package com.backend.hotelreservationapi.auth_module.service;

import com.backend.hotelreservationapi.auth_module.dto.UserCreationResponseDto;
import com.backend.hotelreservationapi.user_module.entity.RoleEntity;
import com.backend.hotelreservationapi.user_module.entity.UserEntity;
import com.backend.hotelreservationapi.user_module.enums.RoleEnum;
import com.backend.hotelreservationapi.user_module.repository.RoleRepository;
import com.backend.hotelreservationapi.user_module.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;


    @Test
    void shouldReturnExistingUserWhenEmailExists() {
        String email = "hello@gmail.com";

        UserEntity user = new UserEntity();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserCreationResponseDto result = userService.findOrCreateUser(email);

        assertThat(result.user()).isEqualTo(user);
        assertThat(result.isNewUser()).isFalse();

        verify(userRepository).findByEmail(email);
        verify(roleRepository, never()).findByRoleName(any(RoleEnum.class));
        verify(userRepository, never()).save(any(UserEntity.class));
    }



    @Test
    void shouldCreateNewUserWhenEmailDoesNotExist() {

        String email = "hello@gmail.com";

        RoleEntity role = new RoleEntity();
        role.setRoleName(RoleEnum.ROLE_USER);

        UserEntity savedUser = new UserEntity();
        savedUser.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(RoleEnum.ROLE_USER)).thenReturn(Optional.of(role));
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        UserCreationResponseDto result = userService.findOrCreateUser(email);

        assertThat(result.user()).isEqualTo(savedUser);
        assertThat(result.isNewUser()).isTrue();

        verify(userRepository).findByEmail(email);
        verify(roleRepository).findByRoleName(RoleEnum.ROLE_USER);
        verify(userRepository).save(any(UserEntity.class));
    }


























}