package com.backend.hotelreservationapi.auth_module.config;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SecurityEnvironment {

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private Integer mailPort;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;

    @Value("${jwt.secret}")
    private String token;

    @Value("${jwt.access-token-expiration}")
    private int accessTokenExpirationInMinutes;

    @Value("${jwt.refresh-token-expiration}")
    private int refreshTokenExpirationInDays;

}
