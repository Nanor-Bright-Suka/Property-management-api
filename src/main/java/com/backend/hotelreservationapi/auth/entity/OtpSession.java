package com.backend.hotelreservationapi.auth.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class OtpSession {

    private String otpHash;
    private int attempts;
    private Instant createdAt;
    private Instant expiresAt;


}
