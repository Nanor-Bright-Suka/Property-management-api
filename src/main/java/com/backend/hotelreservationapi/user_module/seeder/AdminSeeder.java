package com.backend.hotelreservationapi.user_module.seeder;

import com.backend.hotelreservationapi.auth_module.exception.NotFoundException;
import com.backend.hotelreservationapi.user_module.repository.UserRepository;
import com.backend.hotelreservationapi.user_module.entity.RoleEntity;
import com.backend.hotelreservationapi.user_module.entity.UserEntity;
import com.backend.hotelreservationapi.user_module.enums.RoleEnum;
import com.backend.hotelreservationapi.user_module.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Order(value = 3)
@Slf4j
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;


    @Value("${admin.email}")
    private String adminEmail;

    @Override
    public void run(ApplicationArguments args) {

        if (userRepository.existsByEmail(adminEmail)) return;

       UserEntity admin = UserEntity.builder()
                .email(adminEmail)
                .build();
        RoleEntity adminRole = roleRepository.findByRoleName(RoleEnum.ROLE_ADMIN)
                .orElseThrow(() -> new NotFoundException("Admin role not found!" + adminEmail));

        admin.addRole(adminRole);
        userRepository.save(admin);
        log.info("Pre-seeded ADMIN created, {} ", adminEmail);
    }
}
