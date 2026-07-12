package com.backend.hotelreservationapi.user_module.repository;

import com.backend.hotelreservationapi.user_module.entity.PropertyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<PropertyEntity, UUID> {
    Optional<PropertyEntity> findByPropertyIdAndDeletedFalse(UUID propertyId);
    List<PropertyEntity> findAllByDeletedFalse();
}
