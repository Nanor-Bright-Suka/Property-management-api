package com.backend.hotelreservationapi.user_module.service;


import com.backend.hotelreservationapi.auth_module.exception.BusinessException;
import com.backend.hotelreservationapi.auth_module.exception.NotFoundException;
import com.backend.hotelreservationapi.auth_module.exception.ValidationException;
import com.backend.hotelreservationapi.user_module.dto.request.PropertyAddressRequestDto;
import com.backend.hotelreservationapi.user_module.dto.request.PropertyApplicationRequestDto;
import com.backend.hotelreservationapi.user_module.dto.request.PropertyDocumentRequestDto;
import com.backend.hotelreservationapi.user_module.dto.response.ApplicationPropertyResponseDto;
import com.backend.hotelreservationapi.user_module.entity.*;
import com.backend.hotelreservationapi.user_module.enums.DocumentType;
import com.backend.hotelreservationapi.user_module.enums.PropertyApplicationStatus;
import com.backend.hotelreservationapi.user_module.mapper.PropertyApplicationMapper;
import com.backend.hotelreservationapi.user_module.repository.*;
import com.backend.hotelreservationapi.user_module.validator.ProfileValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyMangerService {


 private final PropertyManagerApplicationRepository applicationRepository;
 private final AuthenticatedUser authenticatedUser;
 private final UserRepository userRepository;
 private final ProfileRepository profileRepository;
 private final ProfileValidator profileValidator;
 private final PropertyAddressRepository propertyAddressRepository;
 private final ApplicationStatusHistoryRepository applicationStatusRepository;
 private final PropertyDocumentRepository propertyDocumentRepository;
 private final PropertyApplicationMapper applicationMapper;



 @Transactional
 public ApplicationPropertyResponseDto createApplicationService (PropertyApplicationRequestDto dto) {

  UUID authUserId = authenticatedUser.getUserId();

  UserEntity user = userRepository.findById(authUserId)
          .orElseThrow(() -> new NotFoundException("User not found"));

  ProfileEntity profile = profileRepository.findByUser(user)
          .orElseThrow(() -> new NotFoundException("Profile not found"));

  boolean hasPending = applicationRepository
          .existsByProfileAndStatus(profile, PropertyApplicationStatus.PENDING_REVIEW);

  if (hasPending) {
   throw new BusinessException("You cannot submit a new application while another application is pending review");
  }

  profileValidator.validateProfileForPropertyManagerApplication(profile);

 PropertyManagerApplicationEntity createdApplication = createPropertyManagerApplication(profile, dto);
    createdApplication = applicationRepository.save(createdApplication);

  List<PropertyAddressEntity> createdAddress = createPropertyAddress(createdApplication, dto.getPropertyAddresses());
      createdAddress = propertyAddressRepository.saveAll(createdAddress);

  ApplicationStatusHistoryEntity createdHistory = createApplicationStatusHistory(createdApplication, user);
        applicationStatusRepository.save(createdHistory);

 List<PropertyApplicationDocumentsEntity>  createdDocuments =  createApplicationDocuments(createdApplication, dto);
          createdDocuments = propertyDocumentRepository.saveAll(createdDocuments);

 return applicationMapper.toResponse(createdApplication, createdAddress, createdDocuments);
 }


 private PropertyManagerApplicationEntity createPropertyManagerApplication(ProfileEntity profile, PropertyApplicationRequestDto dto) {
  return PropertyManagerApplicationEntity.builder()
          .id(UUID.randomUUID())
          .profile(profile)
          .propertyType(dto.getPropertyType())
          .description(dto.getDescription())
          .status(PropertyApplicationStatus.PENDING_REVIEW)
          .submittedAt(Instant.now())
          .build();

 }

 private List<PropertyAddressEntity> createPropertyAddress(PropertyManagerApplicationEntity app, List<PropertyAddressRequestDto> addresses) {
      return  addresses.stream()
          .map(addressDto -> PropertyAddressEntity.builder()
                  .id(UUID.randomUUID())
                  .application(app)
                  .city(addressDto.getCity())
                  .country(addressDto.getCountry())
                  .address(addressDto.getAddress())
                  .createdAt(Instant.now())
                  .build())
          .toList();

 }


 private ApplicationStatusHistoryEntity createApplicationStatusHistory(PropertyManagerApplicationEntity application, UserEntity user) {
    return  ApplicationStatusHistoryEntity.builder()
                  .id(UUID.randomUUID())
                  .application(application)
                  .fromState(null)
                  .toState(PropertyApplicationStatus.PENDING_REVIEW)
                  .changedByUser(user)
                  .comment("Application submitted")
                  .changedAt(Instant.now())
                  .build();
 }


    private List<PropertyApplicationDocumentsEntity> createApplicationDocuments(
            PropertyManagerApplicationEntity app,
            PropertyApplicationRequestDto dto
    ) {
        List<DocumentType> expectedTypes = List.of(
                DocumentType.GHANA_CARD,
                DocumentType.PROPERTY_DOCUMENT
        );

        List<PropertyDocumentRequestDto> documents = dto.getDocuments();

        if (documents == null || documents.size() != expectedTypes.size()) {
            throw new ValidationException("Exactly 2 documents are required");
        }

        Set<DocumentType> providedTypes = documents.stream()
                .map(PropertyDocumentRequestDto::getDocumentType)
                .collect(Collectors.toSet());

        if (providedTypes.size() != expectedTypes.size()) {
            throw new ValidationException("Duplicate document types detected");
        }

        if (!providedTypes.containsAll(expectedTypes)) {
            throw new ValidationException("Missing required document types");
        }

        return documents.stream()
                .map(doc -> PropertyApplicationDocumentsEntity.builder()
                        .id(UUID.randomUUID())
                        .application(app)
                        .documentType(doc.getDocumentType())
                        .fileUrl(doc.getFileUrl())
                        .uploadedAt(Instant.now())
                        .build()
                )
                .toList();
    }





}
