package com.backend.hotelreservationapi.user_module.controller;


import com.backend.hotelreservationapi.user_module.dto.response.ProfileResponseDto;
import com.backend.hotelreservationapi.user_module.dto.request.UpdateProfileRequestDto;
import com.backend.hotelreservationapi.user_module.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PreAuthorize("hasAuthority('PROFILE_VIEW')")
    @GetMapping
    public ResponseEntity<ProfileResponseDto> myProfile() {
        return ResponseEntity.ok(profileService.getProfile());
    }


    @PreAuthorize("hasAuthority('PROFILE_UPDATE')")
    @PutMapping
    public ResponseEntity<Map<String, String>> updateProfile(@Valid @RequestBody UpdateProfileRequestDto dto) {
        profileService.updateMyProfileService(dto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Profile updated successfully");
        return ResponseEntity.ok(response);
    }



}
