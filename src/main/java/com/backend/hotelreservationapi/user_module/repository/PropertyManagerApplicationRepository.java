package com.backend.hotelreservationapi.user_module.repository;

import com.backend.hotelreservationapi.user_module.entity.ProfileEntity;
import com.backend.hotelreservationapi.user_module.entity.PropertyManagerApplicationEntity;
import com.backend.hotelreservationapi.user_module.enums.PropertyApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;

public interface PropertyManagerApplicationRepository extends JpaRepository<PropertyManagerApplicationEntity, UUID> {
        boolean existsByProfileAndStatus(ProfileEntity profile, PropertyApplicationStatus status);

}
