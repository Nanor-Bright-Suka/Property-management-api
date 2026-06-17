package com.backend.hotelreservationapi.user_module.service;


import com.backend.hotelreservationapi.auth_module.config.CloudinaryConfig;
import com.backend.hotelreservationapi.auth_module.exception.FileStorageException;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final CloudinaryConfig cloudinaryConfig;

    public String uploadImage(MultipartFile file) {

        try {
            Map uploadResult = cloudinaryConfig.cloudinary().uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "profile-pictures"
                    )
            );

            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new FileStorageException("Cloudinary upload failed");
        }
    }



}
