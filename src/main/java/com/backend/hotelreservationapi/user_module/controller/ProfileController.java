package com.backend.hotelreservationapi.user_module.controller;


import com.backend.hotelreservationapi.user_module.dto.ProfileResponseDto;
import com.backend.hotelreservationapi.user_module.dto.UpdateProfileDto;
import com.backend.hotelreservationapi.user_module.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponseDto> myProfile() {
        return ResponseEntity.ok(profileService.getProfile());
    }

    @PutMapping
    public ResponseEntity<Map<String, String>> updateProfile(@ModelAttribute UpdateProfileDto dto, @RequestParam("imageFile") MultipartFile imageFile) {
        profileService.updateMyProfileService(dto, imageFile);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Profile updated successfully");
        return ResponseEntity.ok(response);
    }



}
