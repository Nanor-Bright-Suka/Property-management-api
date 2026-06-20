package com.backend.hotelreservationapi.user_module.validator;


import com.backend.hotelreservationapi.auth_module.exception.FileTypeValidationException;
import com.backend.hotelreservationapi.user_module.entity.ProfileEntity;
import org.springframework.stereotype.Service;

@Service
public class ProfileValidator {

    public void validateProfileForPropertyManagerApplication(
            ProfileEntity profile) {

        if (profile.getFirstName() == null ||
                profile.getFirstName().isBlank()) {
            throw new FileTypeValidationException("Complete your profile before applying");
        }

        if (profile.getLastName() == null ||
                profile.getLastName().isBlank()) {
            throw new FileTypeValidationException("Complete your profile before applying");
        }

        if (profile.getPhoneNumber() == null ||
                profile.getPhoneNumber().isBlank()) {
            throw new FileTypeValidationException("Complete your profile before applying");
        }

        if (profile.getGender() == null ||
                profile.getGender().isBlank()) {
            throw new FileTypeValidationException("Complete your profile before applying");
        }
        if (profile.getProfilePicUrl() == null ||
                profile.getProfilePicUrl().isBlank()) {
            throw new FileTypeValidationException("Complete your profile picture before applying");
        }

    }
}
