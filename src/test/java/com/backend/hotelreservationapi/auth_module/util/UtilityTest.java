package com.backend.hotelreservationapi.auth_module.util;


import com.backend.hotelreservationapi.auth_module.config.SecurityEnvironment;
import com.backend.hotelreservationapi.auth_module.dto.EmailRequestDto;
import com.backend.hotelreservationapi.auth_module.entity.OtpRequestLog;
import com.backend.hotelreservationapi.auth_module.entity.OtpSession;
import com.backend.hotelreservationapi.auth_module.enums.OtpRequestStatusEnum;
import com.backend.hotelreservationapi.auth_module.exception.InvalidOtpException;
import com.backend.hotelreservationapi.auth_module.exception.RateLimitExceededException;
import com.backend.hotelreservationapi.auth_module.repository.OtpRequestLogRepository;
import com.backend.hotelreservationapi.auth_module.service.EmailService;
import com.backend.hotelreservationapi.auth_module.service.OtpValidationService;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilityTest {
    @Mock
    private HttpServletRequest request;

    @Mock
    private RedisTemplate<String, OtpSession> redisOtpSessionTemplate;

    @Mock
    private ValueOperations<String,String> valueOperations;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private OtpRequestLogRepository otpRequestLogRepository;

    @Mock
    private ValueOperations<String, OtpSession> otpSessionValueOperations;

    @Mock
    private SecureRandom secureRandom;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private SendGrid sendGrid;

    @Mock
    private SecurityEnvironment securityEnvironment;


    @InjectMocks
    private EmailService emailService;

    @InjectMocks
    private Utility utility;


    @Test
    void shouldReturnFirstIpFromXForwardedForHeaderWhenHeaderExists() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.10, 10.0.0.1, 172.16.0.1");
        String result = utility.extractClientIp(request);

        assertThat(result).isEqualTo("192.168.1.10");
    }


    @Test
    void shouldReturnRemoteAddressWhenForwardedHeaderIsMissing() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.50");

        String result = utility.extractClientIp(request);
        assertThat(result).isEqualTo("192.168.1.50");
    }



    @Test
    void shouldCreateEmailRateLimitWhenEmailHasNoPreviousRequests() {

        EmailRequestDto email = new EmailRequestDto();
        email.setEmail("hello@gmail.com");

        String key = "rate:email:hello@gmail.com";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);

        utility.checkEmailLimit(email);

        verify(valueOperations).set(key, "1", Duration.ofMinutes(5));
        verify(valueOperations, never()).increment(anyString());
    }


    @Test
    void shouldIncrementEmailRequestCountWhenEmailIsWithinRateLimit() {

        EmailRequestDto email = new EmailRequestDto();
        email.setEmail("hello@gmail.com");

        String key = "rate:email:" + email.getEmail();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn("1");

        utility.checkEmailLimit(email);
        verify(valueOperations).increment(key);
    }


    @Test
    void shouldThrowExceptionWhenEmailRequestLimitIsExceeded() {

        EmailRequestDto email = new EmailRequestDto();
        email.setEmail("hello@gmail.com");

        String key = "rate:email:" + email.getEmail();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn("3");

        assertThrows(RateLimitExceededException.class, () -> utility.checkEmailLimit(email));

        verify(valueOperations, never()).increment(key);
    }

    @Test
    void shouldCreateIpRateLimitWhenIpHasNoPreviousRequests(){

        String ip = "192.168.1.1";
        String key = "rate:ip:" + ip;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);

        utility.checkIpLimit(ip);

        verify(valueOperations).set(key, "1", Duration.ofMinutes(10));
        verify(valueOperations, never()).increment(key);

    }

    @Test
    void shouldIncrementIpRateLimitWhenIpIsWithinRateLimit() {

        String ip = "192.168.1.1";
        String key = "rate:ip:" + ip;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn("7");

        utility.checkIpLimit(ip);

        verify(valueOperations).increment(key);

    }



    @Test
    void shouldThrowExceptionWhenIpRequestLimitIsExceeded(){

        String ip = "192.168.1.1";
        String key = "rate:ip:" + ip;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn("11");

        assertThrows(RateLimitExceededException.class, () -> utility.checkIpLimit(ip));

        verify(valueOperations, never()).increment(key);

    }


    @Test
    @DisplayName(value = "This test should generate a random 6 digit otp")
    void shouldGenerateOtp() {
        when(secureRandom.nextInt(900000)).thenReturn(234567);

        String otp = utility.generateOtp();

        assertThat(otp).isEqualTo("334567");
        assertThat(otp).hasSize(6);

    }


    @Test
    void shouldReturnHashedOtp() {
    String rawOtp = "123456";
    String hashedOtp = "$2a$10$somehashedvalue";

    when(encoder.encode(rawOtp)).thenReturn(hashedOtp);

    String result = utility.hashOtp(rawOtp);

    assertThat(result).isEqualTo(hashedOtp);
    verify(encoder).encode(rawOtp);
    }



    @Test
    void shouldStoreOtpSessionInRedis() {
    String email = "hello@gmail.com";
    String otpHash = "hashedOtp";

   doReturn(otpSessionValueOperations).when(redisOtpSessionTemplate).opsForValue();

    utility.storeOtp(email, otpHash);

    verify(otpSessionValueOperations).set(
            eq("otp:email:" + email),
            any(OtpSession.class),
            eq(Duration.ofMinutes(5)));
    }


    @Test
    void shouldSaveOtpRequestLogWithCorrectDetails() {
        String email = "hello@gmail.com";
        String ip = "192.168.1.1";
        String reason = "Request access allowed";
        OtpRequestStatusEnum status = OtpRequestStatusEnum.ALLOWED;

        utility.log(email, ip, reason, status);

        ArgumentCaptor<OtpRequestLog> captor = ArgumentCaptor.forClass(OtpRequestLog.class);

        verify(otpRequestLogRepository).save(captor.capture());
        OtpRequestLog savedLog = captor.getValue();

        assertThat(savedLog.getEmail()).isEqualTo(email);
        assertThat(savedLog.getIp()).isEqualTo(ip);
        assertThat(savedLog.getReason()).isEqualTo(reason);
        assertThat(savedLog.getStatus()).isEqualTo(status);
    }

    @Test
    void shouldSendOtpEmailSuccessfully() throws IOException {

        when(securityEnvironment.getFromEmail()).thenReturn("noreply@test.com");

        Response response = new Response();
        response.setStatusCode(202);

        when(sendGrid.api(any(Request.class))).thenReturn(response);

        emailService.sendOtpEmailAsync("user@gmail.com", "123456");

        verify(sendGrid).api(any(Request.class));
    }

    @Test
    void shouldThrowInvalidOtpExceptionWhenEmailSendingFails() throws IOException {
        when(securityEnvironment.getFromEmail()).thenReturn("noreply@test.com");
        when(sendGrid.api(any(Request.class))).thenThrow(new IOException("SendGrid unavailable"));

        assertThatThrownBy(() ->
                emailService.sendOtpEmailAsync("user@gmail.com", "123456"))
                .isInstanceOf(InvalidOtpException.class)
                .hasMessage("Failed to send OTP email");

        verify(sendGrid).api(any(Request.class));
    }










































}

