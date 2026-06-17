package com.backend.hotelreservationapi.user_module.service;


import com.backend.hotelreservationapi.auth_module.config.SecurityEnvironment;
import com.backend.hotelreservationapi.auth_module.exception.FileTypeValidationException;
import com.backend.hotelreservationapi.auth_module.exception.NotFoundException;
import com.backend.hotelreservationapi.user_module.dto.ProfileResponseDto;
import com.backend.hotelreservationapi.user_module.dto.UpdateProfileDto;
import com.backend.hotelreservationapi.user_module.entity.ProfileEntity;
import com.backend.hotelreservationapi.user_module.entity.UserEntity;
import com.backend.hotelreservationapi.user_module.mapper.ProfileMapper;
import com.backend.hotelreservationapi.user_module.repository.ProfileRepository;
import com.backend.hotelreservationapi.user_module.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final AuthenticatedUser authenticatedUser;
    private final ProfileMapper profileMapper;
    private final SecurityEnvironment environment;
    private final CloudinaryService cloudinaryService;



    public ProfileResponseDto getProfile() {

        UUID authUserId = authenticatedUser.getUserId();

        UserEntity user = userRepository.findById(authUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ProfileEntity profile = profileRepository.findByUser(user)
                .orElseGet(() -> createDefaultProfile(user));

    return profileMapper.toDto(profile);
    }


    private ProfileEntity createDefaultProfile(UserEntity user) {
        ProfileEntity profile = new ProfileEntity();

        profile.setProfileId(UUID.randomUUID());
        profile.setUser(user);
        profile.setVerifiedEmail(user.getEmail());
        profile.setCreatedAt(Instant.now());
        profile.setUpdatedAt(Instant.now());

        return profileRepository.save(profile);
    }



    @Transactional
    public void updateMyProfileService(UpdateProfileDto dto, MultipartFile imageFile) {

        UUID authUserId = authenticatedUser.getUserId();

        UserEntity user = userRepository.findById(authUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ProfileEntity profile = profileRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.info("Profile not found for user with email={}", user.getEmail());
                    return new NotFoundException("Profile not found for user");
                });

        if (imageFile == null || imageFile.isEmpty()) {
            throw new FileTypeValidationException("Image file is required");
        }

        if (imageFile.getOriginalFilename() == null ||
                (!imageFile.getOriginalFilename().toLowerCase().endsWith(".png") &&
                        !imageFile.getOriginalFilename().toLowerCase().endsWith(".jpg") &&
                        !imageFile.getOriginalFilename().toLowerCase().endsWith(".jpeg"))) {

            log.warn("Invalid file type for profile image {}", imageFile.getOriginalFilename());
            throw new FileTypeValidationException("Invalid file tpe");
        }

        if (imageFile.getSize() > environment.getMaxFileSize().toBytes()) {
            log.warn("File size for profile picture {} exceeds max allowed {}", imageFile.getSize(), environment.getMaxFileSize());
            throw new FileTypeValidationException("File too large");
        }

        String contentType = imageFile.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/png") && !contentType.equals("image/jpeg"))) {
            throw new FileTypeValidationException("Only PNG and JPEG are allowed");
        }

        String imageUrl = cloudinaryService.uploadImage(imageFile);

        profile.setProfilePicUrl(imageUrl);
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setGender(dto.getGender());
        profile.setPhoneNumber(dto.getPhoneNumber());
        profile.setUpdatedAt(Instant.now());

        profileRepository.save(profile);
        log.info("Profile updated successfully for user with email={}", user.getEmail());
    }











}
