package com.backend.hotelreservationapi.auth_module.service;


import com.backend.hotelreservationapi.auth_module.config.SecurityEnvironment;
import com.backend.hotelreservationapi.auth_module.entity.RefreshTokenEntity;
import com.backend.hotelreservationapi.auth_module.repository.RefreshTokenRepository;
import com.backend.hotelreservationapi.auth_module.util.Utility;
import com.backend.hotelreservationapi.user_module.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class JwtService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityEnvironment  securityEnvironment;


//    private SecretKey getSigningKey() {
//        return Keys.hmacShaKeyFor(
//                securityEnvironment.getToken()
//                        .getBytes(StandardCharsets.UTF_8)
//        );
//
//    }

    private SecretKey getSigningKey() {
        String token = securityEnvironment.getToken();
        byte[] bytes = token.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }


    public static Set<String> extractRoles(UserEntity user) {
        return user.getRoles().stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toSet());
    }

    public static Set<String> extractPermissions(UserEntity user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(perm -> perm.getPermissionName().name())
                .collect(Collectors.toSet());
    }


    public String generateAccessToken(UserEntity user) {
        Map<String, Object> claims = new HashMap<>();

        // Use the new helpers
       Set<String> roles = extractRoles(user);
        Set<String> permissions = extractPermissions(user);

           claims.put("roles", roles);
         claims.put("permissions", permissions);
        return Jwts.builder()
                .id(String.valueOf(user.getUserId()))
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(securityEnvironment.getAccessTokenExpirationInMinutes(), ChronoUnit.MINUTES)))
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public String generateAndStoreRefreshToken(UserEntity user) {

        String plainToken = UUID.randomUUID().toString();
        String tokenHash = Utility.hashToken(plainToken);


        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plus(securityEnvironment.getRefreshTokenExpirationInDays(), ChronoUnit.DAYS))
                .createdAt(Instant.now())
                .isRevoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return plainToken;
    }













}
