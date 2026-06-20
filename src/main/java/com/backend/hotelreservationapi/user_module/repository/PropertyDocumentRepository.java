package com.backend.hotelreservationapi.user_module.repository;

import com.backend.hotelreservationapi.user_module.entity.PropertyApplicationDocumentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PropertyDocumentRepository extends JpaRepository<PropertyApplicationDocumentsEntity, UUID> {
}
