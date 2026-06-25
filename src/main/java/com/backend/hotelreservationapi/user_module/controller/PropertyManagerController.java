package com.backend.hotelreservationapi.user_module.controller;


import com.backend.hotelreservationapi.user_module.dto.request.UpdateApplicationStatusRequestDto;
import com.backend.hotelreservationapi.user_module.dto.response.ApplicationPropertyResponseDto;
import com.backend.hotelreservationapi.user_module.dto.request.PropertyApplicationRequestDto;
import com.backend.hotelreservationapi.user_module.dto.response.UpdateApplicationStatusResponseDto;
import com.backend.hotelreservationapi.user_module.service.PropertyMangerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class PropertyManagerController {

    private final PropertyMangerService service;

    @PreAuthorize("hasAuthority('APPLICATION_CREATE')")
    @PostMapping("/property-manager-application")
    public ResponseEntity<ApplicationPropertyResponseDto> createApplication (@Valid @RequestBody PropertyApplicationRequestDto dto){
        return ResponseEntity.ok(service.createApplicationService(dto));
    }

    @PreAuthorize("hasAuthority('APPLICATION_VIEW')")
    @GetMapping("/property-manager-application/{applicationId}")
    public ResponseEntity<ApplicationPropertyResponseDto> getMyApplication(@PathVariable UUID applicationId){
        return ResponseEntity.ok(service.getMyApplicationService(applicationId));
    }

    @PreAuthorize("hasAuthority('APPLICATION_VIEW_ALL')")
    @GetMapping("/property-manager-application/my-applications")
    public ResponseEntity<List<ApplicationPropertyResponseDto>> getMyApplications(){
        return ResponseEntity.ok(service.getAllApplicationsService());
    }

    @PreAuthorize("hasAuthority('ADMIN_APPLICATION_VIEW_ALL')")
    @GetMapping("/admin/property-manager-application/my-applications")
    public ResponseEntity<List<ApplicationPropertyResponseDto>> getMyAdminApplications(){
        return ResponseEntity.ok(service.getAllApplicationsAdminService());
    }


    @PreAuthorize("hasAuthority('ADMIN_APPLICATION_VIEW_SINGLE')")
    @GetMapping("/admin/property-manager-application/{applicationId}")
    public ResponseEntity<ApplicationPropertyResponseDto> getMyAdminApplication(@PathVariable UUID applicationId){
        return ResponseEntity.ok(service.getMyAdminApplicationService(applicationId));
    }


    @PreAuthorize("hasAuthority('ADMIN_APPLICATION_UPDATE')")
    @PatchMapping("/admin/property-manager-applications/{applicationId}/status")
    public ResponseEntity<UpdateApplicationStatusResponseDto> updateUserApplication(@PathVariable UUID applicationId, UpdateApplicationStatusRequestDto dto){
        return ResponseEntity.ok(service.updateUserApplicationService(applicationId, dto));

    }
















}
