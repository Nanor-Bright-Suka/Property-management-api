package com.backend.hotelreservationapi.auth.service;


import com.backend.hotelreservationapi.auth.dto.EmailRequestDto;
import com.backend.hotelreservationapi.auth.dto.OtpResponseDto;
import com.backend.hotelreservationapi.auth.entity.OtpSession;
import com.backend.hotelreservationapi.auth.enums.OtpRequestStatusEnum;
import com.backend.hotelreservationapi.auth.exception.InvalidOtpException;
import com.backend.hotelreservationapi.auth.exception.RateLimitExceededException;
import com.backend.hotelreservationapi.auth.util.Utility;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final Utility utility;
    private final EmailService emailService;
    private final RedisTemplate<String, OtpSession> redisOtpSessionTemplate;
    private final int MAX_ATTEMPTS = 2;



    public OtpResponseDto requestOtpService(HttpServletRequest request, EmailRequestDto email) {

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



    public String verifyOtp(String email, String otp){
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

        if (!isValid) {
            session.setAttempts(session.getAttempts() + 1);
            log.warn("Invalid OTP attempt email={}, attempts={}", email, session.getAttempts());
            redisOtpSessionTemplate.opsForValue().set(key, session);
            throw new InvalidOtpException("Invalid OTP");
        }

        redisOtpSessionTemplate.delete(key);
        log.info("OTP verified successfully email={}", email);

        return "Bro, access granted";

    }












}
