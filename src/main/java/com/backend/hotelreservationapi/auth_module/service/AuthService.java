package com.backend.hotelreservationapi.auth_module.service;


import com.backend.hotelreservationapi.auth_module.config.SecurityEnvironment;
import com.backend.hotelreservationapi.auth_module.dto.*;
import com.backend.hotelreservationapi.auth_module.enums.OtpRequestStatusEnum;
import com.backend.hotelreservationapi.auth_module.exception.RateLimitExceededException;
import com.backend.hotelreservationapi.auth_module.util.Utility;
import com.backend.hotelreservationapi.user_module.entity.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final Utility utility;
    private final EmailService emailService;
    private final UserService userService;
    private final OtpValidationService otpValidationService;
    private final AuthenticationTokenService authenticationTokenService;
    private final CookieService cookieService;



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
        otpValidationService.validateOtp(email, otp);

       UserCreationResponseDto results =  userService.findOrCreateUser(email);

      TokenPairDto tokens = authenticationTokenService.generateTokens(results.user());

      cookieService.addRefreshTokenCookie(response, tokens.refreshToken());

        return new AuthenticationResponseDto(
                tokens.accessToken(),
                tokens.refreshToken(),
                results.isNewUser()

        );




    }












}
