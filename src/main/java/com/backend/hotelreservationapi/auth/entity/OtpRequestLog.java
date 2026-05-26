package com.backend.hotelreservationapi.auth.entity;


import com.backend.hotelreservationapi.auth.dto.EmailRequestDto;
import com.backend.hotelreservationapi.auth.enums.OtpRequestStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "otp_request_log")
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class OtpRequestLog {

    @Id
    private UUID id;

    private String email;

    private String ip;

    private String reason;

    @Enumerated(EnumType.STRING)
    private OtpRequestStatusEnum status;

    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }


}
