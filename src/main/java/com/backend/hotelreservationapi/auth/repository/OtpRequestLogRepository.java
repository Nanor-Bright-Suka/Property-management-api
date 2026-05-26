package com.backend.hotelreservationapi.auth.repository;

import com.backend.hotelreservationapi.auth.entity.OtpRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;



public interface OtpRequestLogRepository extends JpaRepository<OtpRequestLog, UUID> {
}
