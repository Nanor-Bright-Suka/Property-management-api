package com.backend.hotelreservationapi.user_module.controller;


import com.backend.hotelreservationapi.user_module.dto.response.ApplicationPropertyResponseDto;
import com.backend.hotelreservationapi.user_module.dto.request.PropertyApplicationRequestDto;
import com.backend.hotelreservationapi.user_module.service.PropertyMangerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/property-manager-application")
@RequiredArgsConstructor
public class PropertyManagerController {

    private final PropertyMangerService service;

    @PostMapping
    public ResponseEntity<ApplicationPropertyResponseDto> createApplication (@Valid @ModelAttribute PropertyApplicationRequestDto dto, @RequestParam("documents") List<MultipartFile> files){
        return ResponseEntity.ok(service.createApplicationService(dto, files));
    }










}
