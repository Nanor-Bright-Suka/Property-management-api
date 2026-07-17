package com.backend.hotelreservationapi.auth_module.service;

import com.backend.hotelreservationapi.auth_module.config.SecurityEnvironment;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CookieServiceTest {

    @Mock
    private SecurityEnvironment securityEnvironment;

    @InjectMocks
    private CookieService cookieService;


    @Test
    void shouldAddRefreshTokenCookieSuccessfully() {

        HttpServletResponse response = mock(HttpServletResponse.class);

        when(securityEnvironment.getRefreshTokenExpirationInDays()).thenReturn(7);

        String refreshToken = "test-refresh-token";

        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);

        cookieService.addRefreshTokenCookie(response, refreshToken);

        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());

        String cookieHeader = headerCaptor.getValue();

        assertTrue(cookieHeader.contains("refreshToken=" + refreshToken));
        assertTrue(cookieHeader.contains("HttpOnly"));
        assertTrue(cookieHeader.contains("Path=/"));
        assertTrue(cookieHeader.contains("SameSite=Lax"));
    }

}