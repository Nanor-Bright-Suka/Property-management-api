package com.backend.hotelreservationapi.auth_module.controller;

import com.backend.hotelreservationapi.auth_module.dto.EmailRequestDto;
import com.backend.hotelreservationapi.auth_module.entity.OtpRequestLog;
import com.backend.hotelreservationapi.auth_module.entity.OtpSession;
import com.backend.hotelreservationapi.auth_module.enums.OtpRequestStatusEnum;
import com.backend.hotelreservationapi.auth_module.repository.OtpRequestLogRepository;
import com.backend.hotelreservationapi.auth_module.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisTemplate<String, OtpSession> redisOtpSessionTemplate;

    @Autowired
    private OtpRequestLogRepository otpRequestLogRepository;

    @MockitoBean
    private EmailService emailService;


    @Test
    @DisplayName(value = "This test test the request otp controller")
    void shouldRequestOtpSuccessfully() throws Exception {

        EmailRequestDto request = new EmailRequestDto();
        request.setEmail("john@gmail.com");

        mockMvc.perform(
                        post("/api/v1/auth/request-otp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsBytes(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@gmail.com"))
                .andExpect(jsonPath("$.message").value("OTP sent successfully"));

        OtpSession session = redisOtpSessionTemplate.opsForValue().get("otp:email:john@gmail.com");

        assertThat(session).isNotNull();
        assertThat(session.getOtpHash()).isNotBlank();
        assertThat(session.getAttempts()).isZero();

        List<OtpRequestLog> logs = otpRequestLogRepository.findAll();

        assertThat(logs).hasSize(1);

        OtpRequestLog log = logs.getFirst();

        assertThat(log.getEmail()).isEqualTo("john@gmail.com");
        assertThat(log.getStatus()).isEqualTo(OtpRequestStatusEnum.ALLOWED);

        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailService).sendOtpEmailAsync(eq("john@gmail.com"), otpCaptor.capture());

        String sentOtp = otpCaptor.getValue();
        assertThat(sentOtp).hasSize(6);

    }


    @Test
    @DisplayName(value = "This test blocks request when email rate limit is exceeded")
    void shouldBlockRequestWhenEmailRateLimitExceeded() throws Exception {

        EmailRequestDto request = new EmailRequestDto();
        request.setEmail("john@gmail.com");

        String requestBody = mapper.writeValueAsString(request);

        for (int i = 0; i < 3; i++) {

            mockMvc.perform(post("/api/v1/auth/request-otp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/v1/auth/request-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Too many requests from this email"));

        List<OtpRequestLog> logs = otpRequestLogRepository.findAll();

        assertThat(logs).hasSize(4);

        OtpRequestLog lastLog = logs.getLast();

        assertThat(lastLog.getStatus()).isEqualTo(OtpRequestStatusEnum.BLOCKED);
        assertThat(lastLog.getReason()).isEqualTo("Too many requests from this email");

        verify(emailService, times(3)).sendOtpEmailAsync(eq("john@gmail.com"), anyString());
    }



    @Test
    void shouldBlockRequestWhenIpRateLimitExceeded() throws Exception {

        String ipAddress = "192.168.1.100";

        for (int i = 1; i <= 10; i++) {
            EmailRequestDto request = new EmailRequestDto();
            request.setEmail("user" + i + "@gmail.com");

            String requestBody = mapper.writeValueAsString(request);

            mockMvc.perform(post("/api/v1/auth/request-otp")
                            .header("X-Forwarded-For", ipAddress)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk());
        }


        EmailRequestDto blockedRequest = new EmailRequestDto();
        blockedRequest.setEmail("user11@gmail.com");

        String blockRequestBody = mapper.writeValueAsString(blockedRequest);

        mockMvc.perform(post("/api/v1/auth/request-otp")
                        .header("X-Forwarded-For", ipAddress)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(blockRequestBody))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message")
                        .value("Too many requests from this IP"));

        List<OtpRequestLog> logs = otpRequestLogRepository.findAll();

        assertThat(logs).hasSize(11);

        OtpRequestLog blockedLog = logs.getLast();

        assertThat(blockedLog.getEmail()).isEqualTo("user11@gmail.com");
        assertThat(blockedLog.getIp()).isEqualTo(ipAddress);

        assertThat(blockedLog.getStatus())
                .isEqualTo(OtpRequestStatusEnum.BLOCKED);

        assertThat(blockedLog.getReason()).isEqualTo("Too many requests from this IP");

        verify(emailService, times(10)).sendOtpEmailAsync(anyString(), anyString());
    }






































}