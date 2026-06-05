package com.backend.hotelreservationapi.auth.controller;





import com.backend.hotelreservationapi.auth.dto.EmailRequestDto;
import com.backend.hotelreservationapi.auth.dto.OtpResponseDto;
import com.backend.hotelreservationapi.auth.dto.VerifyOtpRequestDto;
import com.backend.hotelreservationapi.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<OtpResponseDto> requestOtp(
            @Valid
            @RequestBody EmailRequestDto email,
            HttpServletRequest request) {

          return ResponseEntity.ok(authService.requestOtpService(request, email));

    }



    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpRequestDto request) {
       String value =  authService.verifyOtp(request.email(), request.otp());
        return ResponseEntity.ok(value);
    }




}
