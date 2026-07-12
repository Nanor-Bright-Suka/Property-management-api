package com.backend.hotelreservationapi.auth_module.service;


import com.backend.hotelreservationapi.auth_module.entity.OtpSession;
import com.backend.hotelreservationapi.auth_module.exception.InvalidOtpException;
import com.backend.hotelreservationapi.auth_module.exception.RateLimitExceededException;
import com.backend.hotelreservationapi.auth_module.util.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;


@Slf4j
@Service
@RequiredArgsConstructor
public class OtpValidationService {

    private static final int MAX_ATTEMPTS = 3;

    private final RedisTemplate<String, OtpSession> redisOtpSessionTemplate;
    private final Utility utility;

    public void validateOtp(String email, String otp) {

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

            redisOtpSessionTemplate.opsForValue()
                    .set(
                            key,
                            session,
                            Duration.ofSeconds(ttl)
                    );
            throw new InvalidOtpException("Invalid OTP");
        }


        redisOtpSessionTemplate.delete(key);

        log.info("OTP verified successfully email={}", email);
    }
}
