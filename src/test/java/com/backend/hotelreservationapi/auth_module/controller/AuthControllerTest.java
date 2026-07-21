package com.backend.hotelreservationapi.auth_module.controller;

import com.backend.hotelreservationapi.auth_module.dto.EmailRequestDto;
import com.backend.hotelreservationapi.auth_module.dto.VerifyOtpRequestDto;
import com.backend.hotelreservationapi.auth_module.entity.OtpRequestLog;
import com.backend.hotelreservationapi.auth_module.entity.OtpSession;
import com.backend.hotelreservationapi.auth_module.entity.RefreshTokenEntity;
import com.backend.hotelreservationapi.auth_module.enums.OtpRequestStatusEnum;
import com.backend.hotelreservationapi.auth_module.repository.OtpRequestLogRepository;
import com.backend.hotelreservationapi.auth_module.repository.RefreshTokenRepository;
import com.backend.hotelreservationapi.auth_module.service.EmailService;
import com.backend.hotelreservationapi.auth_module.util.Utility;
import com.backend.hotelreservationapi.user_module.entity.RoleEntity;
import com.backend.hotelreservationapi.user_module.entity.UserEntity;
import com.backend.hotelreservationapi.user_module.enums.RoleEnum;
import com.backend.hotelreservationapi.user_module.repository.RoleRepository;
import com.backend.hotelreservationapi.user_module.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private Utility utility;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void cleanDatabase() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }


    @AfterEach
    void cleanUp() {

        refreshTokenRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();
        otpRequestLogRepository.deleteAllInBatch();

        redisOtpSessionTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushDb();
    }


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
    @DisplayName(value = "This test block request when ip rate limit is exceeded")
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



    @Test
    @Transactional
    void shouldVerifyOtpAndCreateNewUserSuccessfully() throws Exception {

        String email = "john@gmail.com";
        String rawOtp = "123456";

        RoleEntity userRole = RoleEntity.builder()
                .id(UUID.randomUUID())
                .roleName(RoleEnum.ROLE_USER)
                .createdAt(Instant.now())
                .build();
        roleRepository.save(userRole);


        String key = "otp:email:" + email;

        OtpSession otpSession = new OtpSession(
                utility.hashOtp(rawOtp),
                0,
                Instant.now(),
                Instant.now().plus(Duration.ofMinutes(5))
        );


        redisOtpSessionTemplate.opsForValue()
                .set(
                        key,
                        otpSession,
                        Duration.ofMinutes(5)
                );

        VerifyOtpRequestDto request = new VerifyOtpRequestDto(email, rawOtp);
        String mapperContent  = mapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(
                        post("/api/v1/auth/verify-otp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapperContent))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.isNewUser").value(true))
                .andReturn();

        List<UserEntity> users = userRepository.findAll();

        assertThat(users).hasSize(1);
        UserEntity createdUser = users.getFirst();
        assertThat(createdUser.getEmail()).isEqualTo(email);

        List<RefreshTokenEntity> refreshTokens = refreshTokenRepository.findAll();
        assertThat(refreshTokens).hasSize(1);

        RefreshTokenEntity refreshToken = refreshTokens.getFirst();
        assertThat(refreshToken.getUser().getEmail()).isEqualTo(email);

        assertThat(refreshToken.getTokenHash()).isNotBlank();
        assertThat(refreshToken.getIsRevoked()).isFalse();

        OtpSession deletedSession = redisOtpSessionTemplate.opsForValue().get(key);
        assertThat(deletedSession).isNull();


        String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie)
                .contains("refreshToken=")
                .contains("HttpOnly");

    }







@Test
@Transactional
 void shouldVerifyOtpAndLoginExistingUserSuccessfully() throws Exception {

    String email = "john@gmail.com";
    String rawOtp = "123456";

    UserEntity user = new  UserEntity();
    user.setEmail(email);

    RoleEntity userRole = RoleEntity.builder()
            .id(UUID.randomUUID())
            .roleName(RoleEnum.ROLE_USER)
            .createdAt(Instant.now())
            .build();
    roleRepository.saveAndFlush(userRole);

    user.addRole(userRole);
    userRepository.saveAndFlush(user);

    String key = "otp:email:" + email;

    OtpSession otpSession = new OtpSession(
            utility.hashOtp(rawOtp),
            0,
            Instant.now(),
            Instant.now().plus(Duration.ofMinutes(5))
    );


    redisOtpSessionTemplate.opsForValue()
            .set(
                    key,
                    otpSession,
                    Duration.ofMinutes(5)
            );

    VerifyOtpRequestDto request = new VerifyOtpRequestDto(email, rawOtp);
    String mapperContent  = mapper.writeValueAsString(request);

    MvcResult result = mockMvc.perform(
                    post("/api/v1/auth/verify-otp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapperContent))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.isNewUser").value(false))
            .andReturn();

    List<UserEntity> users = userRepository.findAll();

    assertThat(users).hasSize(1);
    UserEntity createdUser = users.getFirst();
    assertThat(createdUser.getEmail()).isEqualTo(email);

    List<RefreshTokenEntity> refreshTokens = refreshTokenRepository.findAll();
    assertThat(refreshTokens).hasSize(1);

    RefreshTokenEntity refreshToken = refreshTokens.getFirst();
    assertThat(refreshToken.getUser().getEmail()).isEqualTo(email);

    assertThat(refreshToken.getTokenHash()).isNotBlank();
    assertThat(refreshToken.getIsRevoked()).isFalse();

    OtpSession deletedSession = redisOtpSessionTemplate.opsForValue().get(key);
    assertThat(deletedSession).isNull();

    String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
    assertThat(setCookie)
            .contains("refreshToken=")
            .contains("HttpOnly");




}



    @Test
    @DisplayName(value = "Throws and error when otp is invalid")
    void shouldRejectInvalidOtp() throws Exception {

        String email = "john@gmail.com";
        String correctOtp = "123456";
        String wrongOtp = "999999";


        String key = "otp:email:" + email;


        OtpSession otpSession = new OtpSession(
                utility.hashOtp(correctOtp),
                0,
                Instant.now(),
                Instant.now().plus(Duration.ofMinutes(5))
        );


        redisOtpSessionTemplate.opsForValue()
                .set(
                        key,
                        otpSession,
                        Duration.ofMinutes(5)
                );


        VerifyOtpRequestDto request = new VerifyOtpRequestDto(email, wrongOtp);


        String mapperContent = mapper.writeValueAsString(request);


        mockMvc.perform(
                        post("/api/v1/auth/verify-otp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapperContent)
                )
                .andExpect(status().isUnauthorized())
                .andDo(print())
                .andExpect(jsonPath("$.message")
                        .value("Invalid OTP"));

        OtpSession updatedSession = redisOtpSessionTemplate.opsForValue().get(key);

        assertThat(updatedSession).isNotNull();
        assertThat(updatedSession.getAttempts()).isEqualTo(1);

    }




    @Test
    @DisplayName(value = "Throws an error when otp expires")
    void shouldRejectExpiredOtp() throws Exception {

        String email = "john@gmail.com";
        String rawOtp = "123456";

        String key = "otp:email:" + email;


        OtpSession expiredSession = new OtpSession(
                utility.hashOtp(rawOtp),
                0,
                Instant.now().minus(Duration.ofMinutes(10)),
                Instant.now().minus(Duration.ofMinutes(5))
        );

        redisOtpSessionTemplate.opsForValue()
                .set(
                        key,
                        expiredSession,
                        Duration.ofMinutes(5)
                );

        VerifyOtpRequestDto request = new VerifyOtpRequestDto(email, rawOtp);

        String mapperContent = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/v1/auth/verify-otp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapperContent)
                )
                .andExpect(status().isUnauthorized())
                .andDo(print())
                .andExpect(jsonPath("$.message").value("OTP has expired"));

        OtpSession deletedSession = redisOtpSessionTemplate.opsForValue().get(key);
        assertThat(deletedSession).isNull();

    }















}