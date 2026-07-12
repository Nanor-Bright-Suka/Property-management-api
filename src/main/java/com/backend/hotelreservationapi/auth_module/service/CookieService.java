package com.backend.hotelreservationapi.auth_module.service;


import com.backend.hotelreservationapi.auth_module.config.SecurityEnvironment;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
@RequiredArgsConstructor
public class CookieService {

    private final SecurityEnvironment securityEnvironment;

    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // change to true in production with HTTPS
                .path("/")
                .maxAge(Duration.ofDays(securityEnvironment.getRefreshTokenExpirationInDays()))
                .sameSite("Lax")
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }







}
