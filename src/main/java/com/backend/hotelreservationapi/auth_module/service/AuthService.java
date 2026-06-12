package com.backend.hotelreservationapi.auth_module.service;


import com.backend.hotelreservationapi.auth_module.config.SecurityEnvironment;
import com.backend.hotelreservationapi.auth_module.dto.AuthenticationResponseDto;
import com.backend.hotelreservationapi.auth_module.dto.EmailRequestDto;
import com.backend.hotelreservationapi.auth_module.dto.OtpResponseDto;
import com.backend.hotelreservationapi.auth_module.entity.OtpSession;
import com.backend.hotelreservationapi.auth_module.enums.OtpRequestStatusEnum;
import com.backend.hotelreservationapi.auth_module.exception.InvalidOtpException;
import com.backend.hotelreservationapi.auth_module.exception.RateLimitExceededException;
import com.backend.hotelreservationapi.auth_module.util.Utility;
import com.backend.hotelreservationapi.user_module.entity.UserEntity;
import com.backend.hotelreservationapi.user_module.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final Utility utility;
    private final EmailService emailService;
    private final RedisTemplate<String, OtpSession> redisOtpSessionTemplate;
    private final int MAX_ATTEMPTS = 2;
    private final UserService userService;
    private final JwtService  jwtService;
    private  final SecurityEnvironment securityEnvironment;



    public OtpResponseDto requestOtp(HttpServletRequest request, EmailRequestDto email) {

        String ip = utility.extractClientIp(request);

        try {
            utility.checkEmailLimit(email);
            utility.checkIpLimit(ip);
            utility.log(email.getEmail(), ip, "Request access allowed", OtpRequestStatusEnum.ALLOWED);
          String rawOtp = utility.createOtp(email.getEmail());
            emailService.sendOtpEmailAsync(email.getEmail(), rawOtp);
            return new  OtpResponseDto(email.getEmail(), "OTP sent successfully");
        } catch (RateLimitExceededException ex) {
            utility.log(email.getEmail(), ip, ex.getMessage(), OtpRequestStatusEnum.BLOCKED);
            throw ex;
        }

    }



    public AuthenticationResponseDto verifyOtp(String email, String otp, HttpServletResponse response) {
        String key = "otp:email:" + email;

        OtpSession session = redisOtpSessionTemplate.opsForValue().get(key);


        if (session == null) {
            log.warn("OTP verification failed - session not found email={}", email);
            throw new InvalidOtpException("OTP verification failed");
        }

        if (Instant.now().isAfter(session.getExpiresAt())) {
            log.info("OTP expired for email={}", email);
            redisOtpSessionTemplate.delete(key);
            throw new InvalidOtpException("OTP has expired");
        }

        if (session.getAttempts() >= MAX_ATTEMPTS) {
            log.warn("OTP blocked due to max attempts email={}", email);
            redisOtpSessionTemplate.delete(key);
            throw new RateLimitExceededException("Too many invalid attempts");
        }

        boolean isValid = utility.matches(otp, session.getOtpHash());
        Long ttl = redisOtpSessionTemplate.getExpire(key);

        if (!isValid) {
            session.setAttempts(session.getAttempts() + 1);
            log.warn("Invalid OTP attempt email={}, attempts={}", email, session.getAttempts());
            redisOtpSessionTemplate.opsForValue().set(key, session, Duration.ofSeconds(ttl));
            throw new InvalidOtpException("Invalid OTP");
        }

        redisOtpSessionTemplate.delete(key);
        log.info("OTP verified successfully email={}", email);


        Optional<UserEntity> existingUser = userService.findByEmail(email);

        UserEntity user;
        boolean isNewUser;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            isNewUser = false;
        } else {
            user = userService.createUser(email);
            isNewUser = true;
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken =  jwtService.generateAndStoreRefreshToken(user);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // disable for local dev
                .path("/")
                .maxAge(Duration.ofDays(securityEnvironment.getRefreshTokenExpirationInDays()))
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return new AuthenticationResponseDto(
                accessToken,
                refreshToken,
                isNewUser

        );




    }












}
