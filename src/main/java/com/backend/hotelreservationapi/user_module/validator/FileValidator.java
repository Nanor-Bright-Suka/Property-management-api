package com.backend.hotelreservationapi.user_module.validator;


import com.backend.hotelreservationapi.auth_module.config.SecurityEnvironment;
import com.backend.hotelreservationapi.auth_module.exception.FileTypeValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileValidator {

    private final SecurityEnvironment environment;

    public void validateImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new FileTypeValidationException("Image file is required");
        }

        String filename = file.getOriginalFilename();

        if (filename == null ||
                (!filename.toLowerCase().endsWith(".png")
                        && !filename.toLowerCase().endsWith(".jpg")
                        && !filename.toLowerCase().endsWith(".jpeg"))) {
            log.warn("Invalid file type for profile image {}", file.getOriginalFilename());
            throw new FileTypeValidationException("Invalid file type");
        }

        if (file.getSize() > environment.getMaxImageSize().toBytes()) {
            throw new FileTypeValidationException("File too large");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                (!contentType.equals("image/png")
                        && !contentType.equals("image/jpeg"))) {

            throw new FileTypeValidationException("Only PNG and JPEG are allowed");
        }
    }


    public void validateDocument(MultipartFile file) {



        if (file == null || file.isEmpty()) {
            throw new FileTypeValidationException("Document file is required");
        }

        String filename = file.getOriginalFilename();

        if (filename == null ||
                !filename.toLowerCase().endsWith(".pdf")) {

            log.warn("Invalid file type for document {}", file.getOriginalFilename());

            throw new FileTypeValidationException(
                    "Only PDF documents are allowed"
            );
        }

        if (file.getSize() > environment.getMaxDocumentSize().toBytes()) {
            throw new FileTypeValidationException("File too large");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !contentType.equals("application/pdf")) {

            throw new FileTypeValidationException(
                    "Only PDF documents are allowed"
            );
        }


    }







}
