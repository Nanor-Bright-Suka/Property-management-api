package com.backend.hotelreservationapi.auth.util;

import com.backend.hotelreservationapi.auth.dto.EmailRequestDto;
import com.backend.hotelreservationapi.auth.entity.OtpRequestLog;
import com.backend.hotelreservationapi.auth.entity.OtpSession;
import com.backend.hotelreservationapi.auth.enums.OtpRequestStatusEnum;
import com.backend.hotelreservationapi.auth.exception.InvalidOtpException;
import com.backend.hotelreservationapi.auth.exception.RateLimitExceededException;
import com.backend.hotelreservationapi.auth.repository.OtpRequestLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class Utility {

    private static final int MAX_IP_REQUESTS = 10;
    private static final int MAX_EMAIL_REQUESTS = 3;

    private static final long WINDOW_IP_MINUTES = 10;
    private static final long WINDOW_EMAIL_MINUTES = 5;


    private static final SecureRandom secureRandom = new SecureRandom();

    private final RedisTemplate<String, OtpSession> redisOtpSessionTemplate;
    private static final long OTP_TTL_MINUTES = 5;


    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final StringRedisTemplate redisTemplate;
    private final OtpRequestLogRepository otpRequestLogRepository;




    public  String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");

        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    public  void checkIpLimit(String ip){

        String key = "rate:ip:" + ip;

        String currentIpValue = redisTemplate.opsForValue().get(key);

        if (currentIpValue == null) {
            redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(WINDOW_IP_MINUTES));
            return;
        }

        int count = Integer.parseInt(currentIpValue);

        if (count >= MAX_IP_REQUESTS) {
            throw new RateLimitExceededException("Too many requests from this IP");
        }

        redisTemplate.opsForValue().increment(key);
    }

 public  void checkEmailLimit(EmailRequestDto email){

        String key = "rate:email:" + email.getEmail();

        String currentEmailValue = redisTemplate.opsForValue().get(key);

        if (currentEmailValue == null) {
            redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(WINDOW_EMAIL_MINUTES));
            return;
        }

        int count = Integer.parseInt(currentEmailValue);

        if (count >= MAX_EMAIL_REQUESTS) {
            throw new RateLimitExceededException("Too many requests from this email");
        }

        redisTemplate.opsForValue().increment(key);
    }


    public void log(String email, String ip,
                    String reason,
                    OtpRequestStatusEnum status) {

        OtpRequestLog log = OtpRequestLog.builder()
                .email(email)
                .ip(ip)
                .reason(reason)
                .status(status)
                .build();

        otpRequestLogRepository.save(log);

    }


    public String generateOtp() {
        int otp = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(otp);
    }


    public String hashOtp(String otp) {
        return encoder.encode(otp);
    }

    public boolean matches(String rawOtp, String hashedOtp) {
        return encoder.matches(rawOtp, hashedOtp);
    }


    public void storeOtp(String email, String otpHash) {

        String key =  "otp:email:" + email;

        Instant now = Instant.now();

        OtpSession session = new OtpSession(
                otpHash,
                0,
                now,
                now.plus(Duration.ofMinutes(OTP_TTL_MINUTES))
        );

        redisOtpSessionTemplate.opsForValue().set(
                key,
                session,
                Duration.ofMinutes(OTP_TTL_MINUTES)
        );
    }

    public String createOtp(String email) {
        String otp = generateOtp();
        String otpHash = hashOtp(otp);
        storeOtp(email, otpHash);
        return otp;
    }









}
