package com.backend.hotelreservationapi.auth_module.service;

import com.backend.hotelreservationapi.auth_module.dto.*;
import com.backend.hotelreservationapi.auth_module.enums.OtpRequestStatusEnum;
import com.backend.hotelreservationapi.auth_module.exception.InvalidOtpException;
import com.backend.hotelreservationapi.auth_module.exception.RateLimitExceededException;
import com.backend.hotelreservationapi.auth_module.util.Utility;
import com.backend.hotelreservationapi.user_module.entity.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;



import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private Utility utility;

    @Mock
    private EmailService emailService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private OtpValidationService otpValidationService;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationTokenService authenticationTokenService;

    @Mock
    private CookieService cookieService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldReturnSuccessResponseWhenOtpRequestIsSuccessful(){

        EmailRequestDto email = new EmailRequestDto();
        email.setEmail("hello@gmail");

        String ip = "192.168.1.1";
        String otp = "123456";

        when(utility.extractClientIp(request)).thenReturn(ip);
        when(utility.createOtp(email.getEmail())).thenReturn(otp);

        OtpResponseDto result = authService.requestOtp(request, email);

        assertThat(result.email()).isEqualTo(email.getEmail());
        assertThat(result.message()).isEqualTo("OTP sent successfully");

        verify(utility).checkEmailLimit(email);
        verify(utility).checkIpLimit(ip);
        verify(utility).log(email.getEmail(), ip, "Request access allowed", OtpRequestStatusEnum.ALLOWED);

        verify(emailService).sendOtpEmailAsync(email.getEmail(), otp);

    }



    @Test
    void shouldBlockRequestWhenEmailRateLimitIsExceeded() {

        EmailRequestDto email = new EmailRequestDto();
        email.setEmail("hello@gmail");

        String ip = "192.168.1.1";

        when(utility.extractClientIp(request)).thenReturn(ip);
        doThrow(new RateLimitExceededException("Too many OTP requests")).when(utility).checkEmailLimit(email);

        assertThrows(RateLimitExceededException.class, () -> authService.requestOtp(request, email));

        verify(utility).log(email.getEmail(), ip, "Too many OTP requests", OtpRequestStatusEnum.BLOCKED);
        verify(utility, never()).checkIpLimit(any());
        verify(utility, never()).createOtp(any());
        verify(emailService, never()).sendOtpEmailAsync(any(), any());

    }


    @Test
    void shouldBlockRequestWhenIpRateLimitIsExceeded() {

        EmailRequestDto email = new EmailRequestDto();
        email.setEmail("hello@gmail");

        String ip = "192.168.1.1";

        when(utility.extractClientIp(request)).thenReturn(ip);
        doThrow(new RateLimitExceededException("Too many requests from IP")).when(utility).checkIpLimit(ip);

        assertThrows(RateLimitExceededException.class, () -> authService.requestOtp(request, email));

        verify(utility).checkEmailLimit(email);
        verify(utility).checkIpLimit(ip);
        verify(utility).log(email.getEmail(), ip, "Too many requests from IP", OtpRequestStatusEnum.BLOCKED);
        verify(utility, never()).createOtp(any());
        verify(emailService, never()).sendOtpEmailAsync(any(), any());
    }



    //OTP VERIFICATION

    @Test
    void shouldReturnAuthenticationResponseWhenOtpVerificationIsSuccessful() {

        String email = "hello@gmail.com";
        String otp = "123456";

        UserEntity user = new UserEntity();
        UserCreationResponseDto userResult = new UserCreationResponseDto(user, true);
        TokenPairDto tokens = new TokenPairDto("access-token", "refresh-token");


        doNothing().when(otpValidationService).validateOtp(email, otp);
        when(userService.findOrCreateUser(email)).thenReturn(userResult);
        when(authenticationTokenService.generateTokens(user)).thenReturn(tokens);

        AuthenticationResponseDto result = authService.verifyOtp(email, otp, response);


        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.isNewUser()).isTrue();

        verify(otpValidationService).validateOtp(email, otp);
        verify(userService).findOrCreateUser(email);
        verify(authenticationTokenService).generateTokens(user);
        verify(cookieService).addRefreshTokenCookie(response, "refresh-token");
    }


    @Test
    void shouldReturnInvalidOtpExceptionWhenOtpValidationFails() {
        String email = "hello@gmail.com";
        String otp = "123456";

        doThrow(new InvalidOtpException("Invalid OTP")).when(otpValidationService).validateOtp(email, otp);

        assertThatThrownBy(() -> authService.verifyOtp(email, otp, response))
                .isInstanceOf(InvalidOtpException.class)
                .hasMessage("Invalid OTP");

        verify(otpValidationService).validateOtp(email, otp);
        verify(userService, never()).findOrCreateUser(any());
        verify(authenticationTokenService, never()).generateTokens(any());
        verify(cookieService, never()).addRefreshTokenCookie(any(), any());
    }


    @Test
    void shouldThrowRuntimeExceptionWhenUserCreationFails() {
        String email = "hello@gmail.com";
        String otp = "123456";

        doNothing().when(otpValidationService).validateOtp(email, otp);

        when(userService.findOrCreateUser(email)).thenThrow(new RuntimeException("User creation failed"));

        assertThatThrownBy(() ->
                authService.verifyOtp(
                        email,
                        otp,
                        response
                ))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User creation failed");

        verify(otpValidationService).validateOtp(email, otp);
        verify(userService).findOrCreateUser(email);
        verify(authenticationTokenService, never()).generateTokens(any());
        verify(cookieService, never()).addRefreshTokenCookie(any(), any());
    }


    @Test
    void shouldThrowRuntimeExceptionWhenTokenGenerationFails() {

        String email = "hello@gmail.com";
        String otp = "123456";

        UserEntity user = new UserEntity();

        UserCreationResponseDto userResult = new UserCreationResponseDto(user, true);

        doNothing().when(otpValidationService).validateOtp(email, otp);
        when(userService.findOrCreateUser(email)).thenReturn(userResult);

        when(authenticationTokenService.generateTokens(user)).thenThrow(new RuntimeException("Token generation failed"));

        assertThatThrownBy(() ->
                authService.verifyOtp(email, otp, response))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Token generation failed");


        verify(otpValidationService).validateOtp(email, otp);
        verify(userService).findOrCreateUser(email);
        verify(authenticationTokenService).generateTokens(user);
        verify(cookieService, never()).addRefreshTokenCookie(any(), any());
    }


    @Test
    void shouldThrowExceptionWhenAddingRefreshTokenCookieFails() {
        String email = "hello@gmail.com";
        String otp = "123456";

        UserEntity user = new UserEntity();

        UserCreationResponseDto userResult = new UserCreationResponseDto(user, true);

        TokenPairDto tokens = new TokenPairDto("access-token", "refresh-token");

        doNothing().when(otpValidationService).validateOtp(email, otp);
        when(userService.findOrCreateUser(email)).thenReturn(userResult);
        when(authenticationTokenService.generateTokens(user)).thenReturn(tokens);

        doThrow(new RuntimeException("Failed to add cookie"))
                .when(cookieService)
                .addRefreshTokenCookie(response, "refresh-token");

        assertThatThrownBy(() ->
                authService.verifyOtp(email, otp, response))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to add cookie");

        verify(otpValidationService).validateOtp(email, otp);
        verify(userService).findOrCreateUser(email);
        verify(authenticationTokenService).generateTokens(user);
        verify(cookieService).addRefreshTokenCookie(response, "refresh-token");
    }


}
