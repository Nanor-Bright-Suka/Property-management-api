package com.backend.hotelreservationapi.auth_module.controller;

import com.backend.hotelreservationapi.auth_module.dto.EmailRequestDto;
import com.backend.hotelreservationapi.auth_module.entity.OtpRequestLog;
import com.backend.hotelreservationapi.auth_module.entity.OtpSession;
import com.backend.hotelreservationapi.auth_module.enums.OtpRequestStatusEnum;
import com.backend.hotelreservationapi.auth_module.repository.OtpRequestLogRepository;
import com.backend.hotelreservationapi.auth_module.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
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











}