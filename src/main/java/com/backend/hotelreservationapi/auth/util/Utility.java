package com.backend.hotelreservationapi.auth.util;

import com.backend.hotelreservationapi.auth.dto.EmailRequestDto;
import com.backend.hotelreservationapi.auth.entity.OtpRequestLog;
import com.backend.hotelreservationapi.auth.entity.OtpSession;
import com.backend.hotelreservationapi.auth.enums.OtpRequestStatusEnum;
import com.backend.hotelreservationapi.auth.exception.RateLimitExceededException;
import com.backend.hotelreservationapi.auth.repository.OtpRequestLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    private static final int OTP_LENGTH = 6;


    private final RedisTemplate<String, OtpSession> redisOtpSessionTemplate;
    private static final long OTP_TTL_MINUTES = 5;


    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final StringRedisTemplate redisTemplate;
    private final OtpRequestLogRepository otpRequestLogRepository;


    //private final JavaMailSender mailSender;

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

        String key = "rate:email:" + email.email();

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

        int min = (int) Math.pow(10, OTP_LENGTH - 1);
        int max = (int) Math.pow(10, OTP_LENGTH) - 1;

        int otp = secureRandom.nextInt(max - min + 1) + min;

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



//    public void sendOtpEmail(String email, String otp) {
//
//        SimpleMailMessage message = new SimpleMailMessage();
//
//        message.setTo(email);
//        message.setSubject("Your OTP Code");
//        message.setText(buildMessage(otp));
//
//        mailSender.send(message);
//    }
//
//    private String buildMessage(String otp) {
//        return "Your OTP code is: " + otp +
//                "\n\nThis code expires in 5 minutes." +
//                "\nIf you did not request this, ignore this email.";
//    }
//
//





}
