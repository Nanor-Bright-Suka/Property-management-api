package com.backend.hotelreservationapi.user_module.repository;

import com.backend.hotelreservationapi.user_module.entity.PropertyApplicationAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PropertyAddressRepository  extends JpaRepository<PropertyApplicationAddressEntity, UUID> {
}
