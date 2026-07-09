package com.backend.hotelreservationapi.user_module.repository;

import com.backend.hotelreservationapi.user_module.entity.FeatureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FeatureRepository extends JpaRepository<FeatureEntity, UUID> {
    Optional<FeatureEntity> findByFeatureNameIgnoreCase(String featureName);
}
