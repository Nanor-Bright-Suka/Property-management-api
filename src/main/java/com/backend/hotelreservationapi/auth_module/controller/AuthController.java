package com.backend.hotelreservationapi.auth_module.controller;





import com.backend.hotelreservationapi.auth_module.dto.AuthenticationResponseDto;
import com.backend.hotelreservationapi.auth_module.dto.EmailRequestDto;
import com.backend.hotelreservationapi.auth_module.dto.OtpResponseDto;
import com.backend.hotelreservationapi.auth_module.dto.VerifyOtpRequestDto;
import com.backend.hotelreservationapi.auth_module.service.AuthService;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Key;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/hello")
    public String hello(){
        return "hello, is working";
    }



    @PostMapping("/request-code")
    public ResponseEntity<OtpResponseDto> requestOtp(@Valid @RequestBody EmailRequestDto email, HttpServletRequest request) {
          return ResponseEntity.ok(authService.requestOtp(request, email));
    }



    @PostMapping("/verify-otp")
    public ResponseEntity<AuthenticationResponseDto> verifyOtp(@RequestBody VerifyOtpRequestDto request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.verifyOtp(request.email(), request.otp(),  response));
    }





}
