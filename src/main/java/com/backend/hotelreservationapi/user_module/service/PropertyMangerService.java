package com.backend.hotelreservationapi.user_module.service;


import com.backend.hotelreservationapi.auth_module.exception.BusinessException;
import com.backend.hotelreservationapi.auth_module.exception.NotFoundException;
import com.backend.hotelreservationapi.auth_module.exception.ValidationException;
import com.backend.hotelreservationapi.user_module.dto.request.PropertyAddressRequestDto;
import com.backend.hotelreservationapi.user_module.dto.request.PropertyApplicationRequestDto;
import com.backend.hotelreservationapi.user_module.dto.response.ApplicationPropertyResponseDto;
import com.backend.hotelreservationapi.user_module.entity.*;
import com.backend.hotelreservationapi.user_module.enums.DocumentType;
import com.backend.hotelreservationapi.user_module.enums.PropertyApplicationStatus;
import com.backend.hotelreservationapi.user_module.mapper.PropertyApplicationMapper;
import com.backend.hotelreservationapi.user_module.repository.*;
import com.backend.hotelreservationapi.user_module.validator.FileValidator;
import com.backend.hotelreservationapi.user_module.validator.ProfileValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

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
 private final FileValidator fileValidator;
 private final CloudinaryService cloudinaryService;
 private final PropertyApplicationMapper applicationMapper;



 @Transactional
 public ApplicationPropertyResponseDto createApplicationService (PropertyApplicationRequestDto dto, List<MultipartFile> file) {

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

 List<PropertyApplicationDocumentsEntity>  createdDocuments =  createApplicationDocuments(createdApplication, file);
          createdDocuments = propertyDocumentRepository.saveAll(createdDocuments);


 return applicationMapper.toResponse(createdApplication, createdAddress, createdDocuments);
 }


 private PropertyManagerApplicationEntity createPropertyManagerApplication(ProfileEntity profile, PropertyApplicationRequestDto dto) {
  return PropertyManagerApplicationEntity.builder()
          .id(UUID.randomUUID())
          .profile(profile)
          .registrationNumber(dto.getRegistrationNumber())
          .propertyType(dto.getPropertyType())
          .propertyCount(dto.getPropertyCount())
          .yearsOfExperience(dto.getYearsOfExperience())
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


 private List<PropertyApplicationDocumentsEntity> createApplicationDocuments(PropertyManagerApplicationEntity app, List<MultipartFile> files) {

  List<DocumentType> expectedTypes = List.of(
          DocumentType.NATIONAL_ID,
          DocumentType.PROPERTY_DOCUMENT,
          DocumentType.BUSINESS_REGISTRATION,
          DocumentType.TAX_CERTIFICATE
  );
     if (files == null || files.size() != expectedTypes.size()) {
         throw new ValidationException("Invalid number of documents uploaded");
     }
  return IntStream.range(0, files.size())
          .mapToObj(i -> {

           MultipartFile file = files.get(i);
           DocumentType type = expectedTypes.get(i);

           fileValidator.validateDocument(file);

           String url = cloudinaryService.uploadImage(file, "property-documents");

           return PropertyApplicationDocumentsEntity.builder()
                   .id(UUID.randomUUID())
                   .application(app)
                   .documentType(type)
                   .fileUrl(url)
                   .uploadedAt(Instant.now())
                   .build();
          })
          .toList();
 }









}
