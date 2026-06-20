package com.backend.hotelreservationapi.auth_module.config;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

@Component
@Getter
public class SecurityEnvironment {

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${jwt.secret}")
    private String token;

    @Value("${jwt.access-token-expiration}")
    private int accessTokenExpirationInMinutes;

    @Value("${jwt.refresh-token-expiration}")
    private int refreshTokenExpirationInDays;

    @Value("${file.upload.max-size-profile-picture}")
    private DataSize maxImageSize;

    @Value("${file.upload.max-document-size}")
    private DataSize maxDocumentSize;


    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.cloud-api-key}")
    private String cloudApiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;
}
