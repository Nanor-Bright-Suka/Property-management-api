package com.backend.hotelreservationapi.user_module.service;


import com.backend.hotelreservationapi.auth_module.exception.BusinessException;
import com.backend.hotelreservationapi.auth_module.exception.NotFoundException;
import com.backend.hotelreservationapi.auth_module.exception.ValidationException;
import com.backend.hotelreservationapi.user_module.dto.request.*;
import com.backend.hotelreservationapi.user_module.dto.response.ApplicationPropertyResponseDto;
import com.backend.hotelreservationapi.user_module.dto.response.PropertyCreationResponseDto;
import com.backend.hotelreservationapi.user_module.dto.response.UpdateApplicationStatusResponseDto;
import com.backend.hotelreservationapi.user_module.entity.*;
import com.backend.hotelreservationapi.user_module.enums.DocumentType;
import com.backend.hotelreservationapi.user_module.enums.PropertyApplicationStatus;
import com.backend.hotelreservationapi.user_module.enums.PropertyStatusEnum;
import com.backend.hotelreservationapi.user_module.enums.RoleEnum;
import com.backend.hotelreservationapi.user_module.mapper.PropertyApplicationMapper;
import com.backend.hotelreservationapi.user_module.mapper.PropertyCreationMapper;
import com.backend.hotelreservationapi.user_module.repository.*;
import com.backend.hotelreservationapi.user_module.validator.ProfileValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
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
 private final RoleRepository roleRepository;
 private final FeatureRepository featureRepository;
 private final PropertyCreationMapper propertyCreationMapper;
 private final PropertyRepository propertyRepository;



 @Transactional
 public ApplicationPropertyResponseDto createApplicationService (PropertyApplicationRequestDto dto) {

  UUID authUserId = authenticatedUser.getUserId();

  UserEntity user = userRepository.findById(authUserId)
          .orElseThrow(() -> new NotFoundException("User not found"));

  ProfileEntity profile = profileRepository.findByUser(user)
          .orElseThrow(() -> new NotFoundException("Profile is not found"));

  boolean hasPending = applicationRepository
          .existsByProfileAndStatus(profile, PropertyApplicationStatus.PENDING_REVIEW);

  if (hasPending) {
   throw new BusinessException("You cannot submit a new application while another application is pending review");
  }

  profileValidator.validateProfileForPropertyManagerApplication(profile);

 PropertyManagerApplicationEntity createdApplication = createPropertyManagerApplication(profile, dto);
 applicationRepository.save(createdApplication);

  List<PropertyApplicationAddressEntity> createdAddress = createPropertyAddress(createdApplication, dto.getPropertyAddresses());
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

 private List<PropertyApplicationAddressEntity> createPropertyAddress(PropertyManagerApplicationEntity app, List<PropertyApplicationAddressRequestDto> addresses) {
      List<PropertyApplicationAddressEntity> address = addresses.stream()
          .map(addressDto -> PropertyApplicationAddressEntity.builder()
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


    @Transactional(readOnly = true)
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



    @Transactional(readOnly = true)
    public List<ApplicationPropertyResponseDto> getAllApplicationsService(){
        UUID authUserId = authenticatedUser.getUserId();

        ProfileEntity profile = profileRepository.findByUser_UserId(authUserId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        List<PropertyManagerApplicationEntity> foundApplications = applicationRepository.findByProfile_ProfileId(profile.getProfileId());

        return foundApplications.stream()
                .map(applicationMapper::toResponse)
                .toList();

    }


    @Transactional(readOnly = true)
    public List<ApplicationPropertyResponseDto> getAllApplicationsAdminService() {

        List<PropertyManagerApplicationEntity> applications =
                applicationRepository.findAll();

        return applications.stream()
                .map(applicationMapper::toResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public ApplicationPropertyResponseDto getMyAdminApplicationService(UUID applicationId) {
        PropertyManagerApplicationEntity  foundApplication = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        return applicationMapper.toResponse(foundApplication);

    }

    @Transactional
    public UpdateApplicationStatusResponseDto updateUserApplicationService(UUID applicationId, UpdateApplicationStatusRequestDto dto) {

        RoleEntity propertyManagerRole = roleRepository.findByRoleName(RoleEnum.ROLE_PROPERTY_MANAGER)
                .orElseThrow(() -> new NotFoundException("Role not found"));

        PropertyManagerApplicationEntity application = applicationRepository.findByApplicationId(applicationId)
                        .orElseThrow(() -> new NotFoundException("Application not found"));

        PropertyApplicationStatus oldStatus = application.getStatus();
        PropertyApplicationStatus newStatus = dto.getApplicationStatus();

        UserEntity foundUser  = application.getProfile().getUser();

        if (oldStatus.equals(newStatus)) {
            throw new ValidationException("Application already has status " + newStatus);}

        if(newStatus == PropertyApplicationStatus.APPROVED) {
            foundUser.addRole(propertyManagerRole);
            log.info("Role assigned successfully");
        }
        application.setStatus(newStatus);
        application.setUpdatedAt(Instant.now());

        ApplicationStatusHistoryEntity history = new ApplicationStatusHistoryEntity();
        history.setId(UUID.randomUUID());
        history.setApplication(application);
        history.setFromState(oldStatus);
        history.setToState(newStatus);
        history.setComment(dto.getComment());
        history.setChangedByUser(foundUser);
        history.setChangedAt(Instant.now());

        applicationStatusRepository.save(history);


        return new UpdateApplicationStatusResponseDto(
                application.getApplicationId(),
                oldStatus,
                newStatus,
                dto.getComment(),
                application.getUpdatedAt()
                       );
    }



    @Transactional
    public PropertyCreationResponseDto createPropertyService(PropertyCreateRequestDto dto){

        UUID authId = authenticatedUser.getUserId();

        PropertyManagerApplicationEntity manager = applicationRepository.findByProfileUserUserId(authId)
                        .orElseThrow(() -> new NotFoundException("Property manager not found"));

        PropertyEntity property = buildProperty(dto, manager);
     addAddress(dto, property);
     addPropertyFeatures(dto, property);
     addMedia(dto, property);
     propertyRepository.save(property);
     return propertyCreationMapper.toResponse(property);

    }

    private PropertyEntity buildProperty(PropertyCreateRequestDto dto, PropertyManagerApplicationEntity  manager) {
        return PropertyEntity.builder()
                .propertyId(UUID.randomUUID())
                .price(dto.getPrice())
                .currency(dto.getCurrency())
                .listingType(dto.getListType())
                .propertyType(dto.getPropertyType())
                .title(dto.getTitle())
                .propertyManager(manager)
                .numberOfBedrooms(dto.getNumberOfBedrooms())
                .propertyStatus(PropertyStatusEnum.AVAILABLE)
                .build();

    }

    private void addAddress(PropertyCreateRequestDto dto, PropertyEntity property ) {
         List<PropertyAddressEntity>  addresses =  dto.getAddresses().stream()
        .map(addr-> PropertyAddressEntity.builder()
                .id(UUID.randomUUID())
                .property(property)
                .city(addr.getCity())
                .country(addr.getCountry())
                .address(addr.getAddress())
                .createdAt(Instant.now())
                .build()
        )
             .toList();
         property.setAddresses(addresses);

    }

    private void addPropertyFeatures(PropertyCreateRequestDto dto, PropertyEntity property) {
        List<PropertyFeatureEntity> propertyFeatures = new ArrayList<>();
        List<FeatureRequestDto> features = dto.getFeatures();

        for (FeatureRequestDto featureRequestDto : features) {
            String name = featureRequestDto.getFeatureName().trim();

            FeatureEntity feature = featureRepository.findByFeatureNameIgnoreCase(name)
                    .orElseGet(() -> {
                        FeatureEntity newFeature = new FeatureEntity();
                        newFeature.setFeatureId(UUID.randomUUID());
                      newFeature.setFeatureName(name);
                     return  newFeature;
                    });

            PropertyFeatureEntity pf = new PropertyFeatureEntity();
            pf.setId(UUID.randomUUID());
            pf.setProperty(property);
            pf.setFeature(feature);

            propertyFeatures.add(pf);

        }

        property.setPropertyFeature(propertyFeatures);
    }


    private void addMedia(PropertyCreateRequestDto dto, PropertyEntity property){
     List<PropertyMediaEntity> media = dto.getMedia().stream()
             .map(med-> PropertyMediaEntity.builder()
                     .id(UUID.randomUUID())
                     .mediaUrl(med.getMediaUrl())
                     .mediaType(med.getMediaType())
                     .property(property)
                     .build())
             .toList();
     property.setPropertyMedia(media);

    }

    @Transactional
    public String updatePropertyPostService(UUID propertyId, UpdatePropertyRequestDto dto) {


        PropertyEntity foundProperty = propertyRepository.findByPropertyIdAndDeletedFalse(propertyId)
                .orElseThrow(() -> new NotFoundException("Property not found"));

        foundProperty.setPropertyType(dto.getPropertyType());
        foundProperty.setPrice(dto.getPrice());
        foundProperty.setListingType(dto.getListType());
        foundProperty.setTitle(dto.getTitle());
        foundProperty.setNumberOfBedrooms(dto.getNumberOfBedrooms());
        foundProperty.setCurrency(dto.getCurrency());

        updateMedia(foundProperty,dto);
        updateAddress(foundProperty, dto);
        updatePropertyFeature(foundProperty, dto);

        propertyRepository.save(foundProperty);

    return "Property Post updated successfully";

    }


    private void updateMedia(PropertyEntity foundProperty, UpdatePropertyRequestDto dto) {

            List<PropertyMediaEntity> newMediaList = new ArrayList<>();
            for (PropertyMediaRequestDto mediaRequestDto : dto.getMedia()) {

                PropertyMediaEntity newPropMedia = new PropertyMediaEntity();
                newPropMedia.setId(UUID.randomUUID());
                newPropMedia.setMediaUrl(mediaRequestDto.getMediaUrl());
                newPropMedia.setMediaType(mediaRequestDto.getMediaType());
                newPropMedia.setProperty(foundProperty);
                newMediaList.add(newPropMedia);
            }

            foundProperty.getPropertyMedia().clear();
            foundProperty.getPropertyMedia().addAll(newMediaList);

    }


    private void updateAddress(PropertyEntity foundProperty, UpdatePropertyRequestDto dto) {
        List<PropertyAddressEntity> newAdddressList = new ArrayList<>();

        for(PropertyAddressRequestDto addressRequestDto : dto.getAddresses()) {
            PropertyAddressEntity propAddress = new PropertyAddressEntity();
            propAddress.setId(UUID.randomUUID());
            propAddress.setCity(addressRequestDto.getCity());
            propAddress.setCountry(addressRequestDto.getCountry());
            propAddress.setAddress(addressRequestDto.getAddress());
            propAddress.setProperty(foundProperty);
            propAddress.setCreatedAt(Instant.now());
            propAddress.setUpdatedAt(Instant.now());
            newAdddressList.add(propAddress);
        }

        foundProperty.getAddresses().clear();
        foundProperty.getAddresses().addAll(newAdddressList);
    }


    private void updatePropertyFeature(PropertyEntity foundProperty, UpdatePropertyRequestDto dto) {

     Set<PropertyFeatureEntity> newPropertyFeature = new HashSet<>();
     for(FeatureRequestDto featureRequestDto : dto.getFeatures()) {
         String name = featureRequestDto.getFeatureName().trim();
         FeatureEntity feature = featureRepository.findByFeatureNameIgnoreCase(name)
                 .orElseGet(() -> {
                     FeatureEntity f = new FeatureEntity();
                     f.setFeatureId(UUID.randomUUID());
                     f.setFeatureName(name);
                     return featureRepository.save(f);
                 });


         PropertyFeatureEntity updatedPf = new PropertyFeatureEntity();
         updatedPf.setId(UUID.randomUUID());
         updatedPf.setProperty(foundProperty);
         updatedPf.setFeature(feature);

         newPropertyFeature.add(updatedPf);
     }
     foundProperty.getPropertyFeature().clear();
     foundProperty.getPropertyFeature().addAll(newPropertyFeature);

    }


    @Transactional(readOnly = true)
    public PropertyCreationResponseDto viewPropertyService(UUID propertyId){

     PropertyEntity foundProperty = propertyRepository.findByPropertyIdAndDeletedFalse(propertyId)
            .orElseThrow(() -> new NotFoundException("Property does not exist"));

      UUID authId = authenticatedUser.getUserId();
      UUID propertyOwnerId = foundProperty.getPropertyManager().getProfile().getUser().getUserId();

      if(!authId.equals(propertyOwnerId)){
          throw new AccessDeniedException("Access Denied");
      }

      return propertyCreationMapper.toResponse(foundProperty);
    }



    @Transactional(readOnly = true)
    public List<PropertyCreationResponseDto> viewAllPropertyPostsService() {
    log.info("Method executed successfully");
        List<PropertyEntity> properties = propertyRepository.findAllByDeletedFalse();

        return properties.stream()
                .map(propertyCreationMapper::toResponse)
                .toList();
    }


    @Transactional
    public String deletePropertyPostService(UUID propertyId){

        PropertyEntity foundProperty = propertyRepository.findByPropertyIdAndDeletedFalse(propertyId)
                .orElseThrow(() -> new NotFoundException("Property post does not exist"));

        UUID authId = authenticatedUser.getUserId();
        UUID propertyOwnerId = foundProperty.getPropertyManager().getProfile().getUser().getUserId();

        if(!authId.equals(propertyOwnerId)){
            throw new AccessDeniedException("Access Denied");
        }

        foundProperty.setDeleted(true);
        return "Property post deleted successfully";
    }

}