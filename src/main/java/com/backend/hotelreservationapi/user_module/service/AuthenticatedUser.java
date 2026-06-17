package com.backend.hotelreservationapi.user_module.service;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthenticatedUser {

    public CustomUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = auth.getPrincipal();

        if (principal == null || principal.equals("anonymousUser")) {
            throw new IllegalStateException("No authenticated user found");
        }

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails;
        }

        throw new IllegalStateException("Unexpected principal type: " + principal.getClass());
    }

    public UUID getUserId() {
        return getCurrentUser().getUserId();
    }

    public String getEmail() {
        return getCurrentUser().getUsername();
    }



}
