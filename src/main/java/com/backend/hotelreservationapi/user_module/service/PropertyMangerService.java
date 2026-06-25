package com.backend.hotelreservationapi.user_module.service;


import com.backend.hotelreservationapi.auth_module.exception.BusinessException;
import com.backend.hotelreservationapi.auth_module.exception.NotFoundException;
import com.backend.hotelreservationapi.auth_module.exception.ValidationException;
import com.backend.hotelreservationapi.user_module.dto.request.PropertyAddressRequestDto;
import com.backend.hotelreservationapi.user_module.dto.request.PropertyApplicationRequestDto;
import com.backend.hotelreservationapi.user_module.dto.request.PropertyDocumentRequestDto;
import com.backend.hotelreservationapi.user_module.dto.request.UpdateApplicationStatusRequestDto;
import com.backend.hotelreservationapi.user_module.dto.response.ApplicationPropertyResponseDto;
import com.backend.hotelreservationapi.user_module.dto.response.UpdateApplicationStatusResponseDto;
import com.backend.hotelreservationapi.user_module.entity.*;
import com.backend.hotelreservationapi.user_module.enums.DocumentType;
import com.backend.hotelreservationapi.user_module.enums.PropertyApplicationStatus;
import com.backend.hotelreservationapi.user_module.mapper.PropertyApplicationMapper;
import com.backend.hotelreservationapi.user_module.repository.*;
import com.backend.hotelreservationapi.user_module.validator.ProfileValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
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
 private final ProfileValidator profileValidator;
 private final PropertyAddressRepository propertyAddressRepository;
 private final ApplicationStatusHistoryRepository applicationStatusRepository;
 private final PropertyDocumentRepository propertyDocumentRepository;
 private final PropertyApplicationMapper applicationMapper;
 private final ProfileRepository profileRepository;



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
 applicationRepository.save(createdApplication);

  List<PropertyAddressEntity> createdAddress = createPropertyAddress(createdApplication, dto.getPropertyAddresses());
  propertyAddressRepository.saveAll(createdAddress);

  ApplicationStatusHistoryEntity createdHistory = createApplicationStatusHistory(createdApplication, user);
  applicationStatusRepository.save(createdHistory);

 List<PropertyApplicationDocumentsEntity>  createdDocuments =  createApplicationDocuments(createdApplication, dto);
  propertyDocumentRepository.saveAll(createdDocuments);

 return applicationMapper.toResponse(createdApplication);
 }


 private PropertyManagerApplicationEntity createPropertyManagerApplication(ProfileEntity profile, PropertyApplicationRequestDto dto) {
  return PropertyManagerApplicationEntity.builder()
          .applicationId(UUID.randomUUID())
          .profile(profile)
          .propertyType(dto.getPropertyType())
          .description(dto.getDescription())
          .status(PropertyApplicationStatus.PENDING_REVIEW)
          .submittedAt(Instant.now())
          .build();

 }

 private List<PropertyAddressEntity> createPropertyAddress(PropertyManagerApplicationEntity app, List<PropertyAddressRequestDto> addresses) {
      List<PropertyAddressEntity> address = addresses.stream()
          .map(addressDto -> PropertyAddressEntity.builder()
                  .id(UUID.randomUUID())
                  .application(app)
                  .city(addressDto.getCity())
                  .country(addressDto.getCountry())
                  .address(addressDto.getAddress())
                  .createdAt(Instant.now())
                  .build())
          .toList();
      app.setAddresses(address);
      return address;

 }


 private ApplicationStatusHistoryEntity createApplicationStatusHistory(PropertyManagerApplicationEntity application, UserEntity user) {
    return ApplicationStatusHistoryEntity.builder()
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

        List<PropertyApplicationDocumentsEntity> documentBuild =  documents.stream()
                .map(doc -> PropertyApplicationDocumentsEntity.builder()
                        .id(UUID.randomUUID())
                        .application(app)
                        .documentType(doc.getDocumentType())
                        .fileUrl(doc.getFileUrl())
                        .uploadedAt(Instant.now())
                        .build()
                )
                .toList();
        app.setDocuments(documentBuild);
        return documentBuild;
    }


    @Transactional
    public ApplicationPropertyResponseDto getMyApplicationService(UUID applicationId) {
        UUID authUserId = authenticatedUser.getUserId();

        PropertyManagerApplicationEntity  foundApplication = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new NotFoundException("User application not found"));

        UUID applicationUserId =  foundApplication.getProfile().getUser().getUserId();
        if (!authUserId.equals(applicationUserId)) {
            throw new AccessDeniedException("Access Denied");
        }

       return applicationMapper.toResponse(foundApplication);

    }



    @Transactional
    public List<ApplicationPropertyResponseDto> getAllApplicationsService(){
        UUID authUserId = authenticatedUser.getUserId();

        ProfileEntity profile = profileRepository.findByUser_UserId(authUserId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        List<PropertyManagerApplicationEntity> foundApplications = applicationRepository.findByProfile_ProfileId(profile.getProfileId());

        return foundApplications.stream()
                .map(applicationMapper::toResponse)
                .toList();

    }


    @Transactional
    public List<ApplicationPropertyResponseDto> getAllApplicationsAdminService() {

        List<PropertyManagerApplicationEntity> applications =
                applicationRepository.findAll();

        return applications.stream()
                .map(applicationMapper::toResponse)
                .toList();
    }


    @Transactional
    public ApplicationPropertyResponseDto getMyAdminApplicationService(UUID applicationId) {
        PropertyManagerApplicationEntity  foundApplication = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        return applicationMapper.toResponse(foundApplication);

    }

    @Transactional
    public UpdateApplicationStatusResponseDto updateUserApplicationService(UUID applicationId, UpdateApplicationStatusRequestDto dto) {
        UUID authUserId = authenticatedUser.getUserId();

        PropertyManagerApplicationEntity application = applicationRepository.findByApplicationId(applicationId)
                        .orElseThrow(() -> new NotFoundException("Application not found"));

        PropertyApplicationStatus oldStatus = application.getStatus();
        PropertyApplicationStatus newStatus = dto.getApplicationStatus();

        if (oldStatus.equals(newStatus)) {
            throw new ValidationException("Application already has status " + newStatus);}

        application.setStatus(newStatus);
        application.setUpdatedAt(Instant.now());

        UserEntity foundUser = userRepository.findByUserId(authUserId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        ApplicationStatusHistoryEntity history = new ApplicationStatusHistoryEntity();
        history.setId(UUID.randomUUID());
        history.setApplication(application);
        history.setFromState(oldStatus);
        history.setToState(newStatus);
        history.setComment(dto.getComment());
        history.setChangedByUser(foundUser);
        history.setChangedAt(Instant.now());

        applicationStatusRepository.save(history);
        applicationRepository.save(application);


        return new UpdateApplicationStatusResponseDto(
                application.getApplicationId(),
                oldStatus,
                newStatus,
                dto.getComment(),
                application.getUpdatedAt()
                       );
    }









}
