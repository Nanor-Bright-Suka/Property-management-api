package com.backend.hotelreservationapi.auth_module.service;

import com.backend.hotelreservationapi.auth_module.config.SecurityEnvironment;
import com.backend.hotelreservationapi.auth_module.entity.RefreshTokenEntity;
import com.backend.hotelreservationapi.auth_module.repository.RefreshTokenRepository;
import com.backend.hotelreservationapi.user_module.entity.PermissionEntity;
import com.backend.hotelreservationapi.user_module.entity.RoleEntity;
import com.backend.hotelreservationapi.user_module.entity.UserEntity;
import com.backend.hotelreservationapi.user_module.enums.PermissionEnum;
import com.backend.hotelreservationapi.user_module.enums.RoleEnum;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private  RefreshTokenRepository refreshTokenRepository;

    @Mock
    private SecurityEnvironment securityEnvironment;

    @InjectMocks
    private TokenService tokenService;

    @Test
    void shouldGenerateValidAccessTokenForUser() {

        UserEntity user = new UserEntity();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@gmail.com");

        PermissionEntity permission = new PermissionEntity();
        permission.setPermissionName(PermissionEnum.PROFILE_VIEW);

        RoleEntity role = new RoleEntity();
        role.setRoleName(RoleEnum.ROLE_USER);
        role.setPermissions(Set.of(permission));

        user.setRoles(Set.of(role));

        when(securityEnvironment.getToken()).thenReturn("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!!");
        when(securityEnvironment.getAccessTokenExpirationInMinutes()).thenReturn(15);

        String token = tokenService.generateAccessToken(user);

        Claims claims = tokenService.extractAllClaims(token);

        assertEquals(user.getEmail(), claims.getSubject());
        assertEquals(user.getUserId().toString(), claims.getId());
        assertEquals(List.of("ROLE_USER"), claims.get("roles", List.class));
        assertEquals(List.of("PROFILE_VIEW"), claims.get("permissions", List.class));

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }


    @Test
    void shouldGenerateAndStoreRefreshTokenSuccessfully() {

        UserEntity user = new UserEntity();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@gmail.com");

        when(securityEnvironment.getRefreshTokenExpirationInDays()).thenReturn(7);
        ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);

        String refreshToken = tokenService.generateAndStoreRefreshToken(user);

        assertNotNull(refreshToken);

        verify(refreshTokenRepository).save(captor.capture());

        RefreshTokenEntity savedToken = captor.getValue();
        assertEquals(user, savedToken.getUser());
        assertNotNull(savedToken.getId());
        assertNotNull(savedToken.getTokenHash());
        assertNotEquals(refreshToken, savedToken.getTokenHash());
        assertNotNull(savedToken.getCreatedAt());
        assertNotNull(savedToken.getExpiresAt());
        assertFalse(savedToken.getIsRevoked());
    }










}