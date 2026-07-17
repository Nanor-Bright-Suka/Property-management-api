package com.backend.hotelreservationapi.auth_module.service;


import com.backend.hotelreservationapi.auth_module.dto.TokenPairDto;
import com.backend.hotelreservationapi.user_module.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationTokenServiceTest {

    @Mock
    private TokenService jwtService;


    @InjectMocks
    private AuthenticationTokenService authenticationTokenService;

    @Test
    void shouldGenerateBothAccessAndRefreshTokensSuccessfully() {
        UserEntity user = new UserEntity();

        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        when(jwtService.generateAccessToken(user)).thenReturn(accessToken);
        when(jwtService.generateAndStoreRefreshToken(user)).thenReturn(refreshToken);

        TokenPairDto result = authenticationTokenService.generateTokens(user);

        assertEquals(accessToken, result.accessToken());
        assertEquals(refreshToken, result.refreshToken());

        verify(jwtService).generateAccessToken(user);
        verify(jwtService).generateAndStoreRefreshToken(user);
    }























}