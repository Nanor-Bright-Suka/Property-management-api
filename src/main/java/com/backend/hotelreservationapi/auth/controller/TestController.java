package com.backend.hotelreservationapi.auth.controller;





import com.backend.hotelreservationapi.auth.dto.EmailRequestDto;
import com.backend.hotelreservationapi.auth.service.RateLimittingService;
import com.backend.hotelreservationapi.auth.util.Utility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class TestController {

    private final RateLimittingService rateLimittingService;
    private final Utility utility;


    @GetMapping("/hello")
    public String hello(){
        return "hello, is working";
    }



    @PostMapping("/request-code")
    public ResponseEntity<String> requestCode(
            @Valid
            @RequestBody EmailRequestDto email,
            HttpServletRequest request) {

        String ip = utility.extractClientIp(request);
            rateLimittingService.checkLimits(email, ip);
          String value =  utility.createOtp(email.email());

        return ResponseEntity.ok("OTP is sent " + value);
    }







}
