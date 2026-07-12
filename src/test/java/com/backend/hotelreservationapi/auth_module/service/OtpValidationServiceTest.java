package com.backend.hotelreservationapi.auth_module.service;

import com.backend.hotelreservationapi.auth_module.entity.OtpSession;
import com.backend.hotelreservationapi.auth_module.exception.InvalidOtpException;
import com.backend.hotelreservationapi.auth_module.exception.RateLimitExceededException;
import com.backend.hotelreservationapi.auth_module.util.Utility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OtpValidationServiceTest {


    @Mock
    private RedisTemplate<String, OtpSession> redisOtpSessionTemplate;

    @Mock
    private ValueOperations<String, OtpSession> otpSessionValueOperations;

    @Mock
    private Utility utility;

    @InjectMocks
    private OtpValidationService otpValidationService;


    @Test
    void shouldDeleteOtpSessionWhenOtpIsValid() {

        String email = "hello@gmail.com";
        String otp = "123456";
        String key = "otp:email:" + email;

        OtpSession session = new OtpSession(
                "hashedOtp",
                0,
                Instant.now(),
                Instant.now().plus(Duration.ofMinutes(5))
        );

        doReturn(otpSessionValueOperations).when(redisOtpSessionTemplate).opsForValue();
        when(otpSessionValueOperations.get(key)).thenReturn(session);
        when(utility.matches(otp, session.getOtpHash())).thenReturn(true);

        otpValidationService.validateOtp(email, otp);


        verify(utility).matches(otp, session.getOtpHash());
        verify(redisOtpSessionTemplate).delete(key);
        verify(otpSessionValueOperations, never())
                .set(
                        anyString(),
                        any(OtpSession.class),
                        any(Duration.class)
                );
    }



    @Test
    void shouldThrowInvalidOtpExceptionWhenOtpSessionDoesNotExist() {
        String email = "hello@gmail.com";
        String otp = "123456";

        String key = "otp:email:" + email;

        when(redisOtpSessionTemplate.opsForValue()).thenReturn(otpSessionValueOperations);
        when(otpSessionValueOperations.get(key)).thenReturn(null);

        assertThatThrownBy(() -> otpValidationService.validateOtp(email, otp))
                .isInstanceOf(InvalidOtpException.class)
                .hasMessage("OTP verification failed");

        verify(otpSessionValueOperations).get(key);
        verify(utility, never()).matches(anyString(), anyString());
        verify(redisOtpSessionTemplate, never()).delete(anyString());
    }


    @Test
    void shouldThrowInvalidOtpExceptionWhenOtpHasExpired() {

        String email = "hello@gmail.com";
        String otp = "123456";

        String key = "otp:email:" + email;
        OtpSession expiredSession = new OtpSession(
                "hashedOtp",
                0,
                Instant.now().minus(Duration.ofMinutes(10)),
                Instant.now().minus(Duration.ofMinutes(5))
        );


        when(redisOtpSessionTemplate.opsForValue()).thenReturn(otpSessionValueOperations);
        when(otpSessionValueOperations.get(key)).thenReturn(expiredSession);

        assertThatThrownBy(() -> otpValidationService.validateOtp(email, otp))
                .isInstanceOf(InvalidOtpException.class)
                .hasMessage("OTP has expired");

        verify(redisOtpSessionTemplate).delete(key);
        verify(utility, never()).matches(anyString(), anyString());
        verify(otpSessionValueOperations, never())
                .set(
                        anyString(),
                        any(OtpSession.class),
                        any(Duration.class)
                );
    }

    @Test
    void shouldThrowExceptionWhenMaximumAttemptsIsReached() {
        String email = "hello@gmail.com";
        String otp = "123456";

        String key = "otp:email:" + email;

        OtpSession session = new OtpSession(
                "hashedOtp",
                3,
                Instant.now(),
                Instant.now().plus(Duration.ofMinutes(5))
        );


        when(redisOtpSessionTemplate.opsForValue()).thenReturn(otpSessionValueOperations);
        when(otpSessionValueOperations.get(key)).thenReturn(session);

        assertThatThrownBy(() ->
                otpValidationService.validateOtp(email, otp)
        )
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessage("Too many invalid attempts");

        verify(redisOtpSessionTemplate).delete(key);
        verify(utility, never()).matches(anyString(), anyString());
        verify(otpSessionValueOperations, never())
                .set(
                        anyString(),
                        any(OtpSession.class),
                        any(Duration.class)
                );
    }


    @Test
    void shouldIncrementAttemptsWhenOtpIsInvalid() {
        String email = "hello@gmail.com";
        String otp = "123456";
        String key = "otp:email:" + email;

        OtpSession session = new OtpSession(
                "hashedOtp",
                0,
                Instant.now(),
                Instant.now().plus(Duration.ofMinutes(5))
        );

        when(redisOtpSessionTemplate.opsForValue()).thenReturn(otpSessionValueOperations);
        when(otpSessionValueOperations.get(key)).thenReturn(session);
        when(utility.matches(otp, session.getOtpHash())).thenReturn(false);
        when(redisOtpSessionTemplate.getExpire(key)).thenReturn(300L);

        assertThatThrownBy(() ->
                otpValidationService.validateOtp(email, otp)
        )
                .isInstanceOf(InvalidOtpException.class)
                .hasMessage("Invalid OTP");


        assertThat(session.getAttempts()).isEqualTo(1);
        verify(otpSessionValueOperations)
                .set(
                        key,
                        session,
                        Duration.ofSeconds(300)
                );

        verify(redisOtpSessionTemplate, never()).delete(key);
    }


    @Test
    void shouldAllowVerificationWhenAttemptsAreTwo() {

        String email = "hello@gmail.com";
        String otp = "123456";
        String key = "otp:email:" + email;

        OtpSession session = new OtpSession(
                "hashedOtp",
                2,
                Instant.now(),
                Instant.now().plus(Duration.ofMinutes(5))
        );

        when(redisOtpSessionTemplate.opsForValue()).thenReturn(otpSessionValueOperations);
        when(otpSessionValueOperations.get(key)).thenReturn(session);
        when(utility.matches(otp, session.getOtpHash())).thenReturn(true);

        otpValidationService.validateOtp(email, otp);

        verify(redisOtpSessionTemplate).delete(key);
        verify(otpSessionValueOperations, never())
                .set(
                        anyString(),
                        any(OtpSession.class),
                        any(Duration.class)
                );
    }







































    
































}