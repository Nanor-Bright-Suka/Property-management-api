package com.backend.hotelreservationapi.user_module.seeder;

import com.backend.hotelreservationapi.user_module.entity.PermissionEntity;
import com.backend.hotelreservationapi.user_module.entity.RoleEntity;
import com.backend.hotelreservationapi.user_module.enums.PermissionEnum;
import com.backend.hotelreservationapi.user_module.enums.RoleEnum;
import com.backend.hotelreservationapi.user_module.repository.PermissionRepository;
import com.backend.hotelreservationapi.user_module.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;


@Component
@RequiredArgsConstructor
@Order(value=2)
public class RoleSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(ApplicationArguments args) {

        // USER
        RoleEntity normalUser = roleRepository.findByRoleName(RoleEnum.ROLE_USER)
                .orElseGet(() -> roleRepository.save(
                        RoleEntity.builder()
                                .id(UUID.randomUUID())
                                .roleName(RoleEnum.ROLE_USER)
                                .createdAt(Instant.now())
                                .build()
                ));

        add(normalUser,
                PermissionEnum.BOOKING_CREATE,
                PermissionEnum.BOOKING_VIEW_OWN,
                PermissionEnum.BOOKING_CANCEL_OWN

        );


        // HOTEL MANAGER
        RoleEntity hotelManager = roleRepository.findByRoleName(RoleEnum.ROLE_HOTEL_MANAGER)
                .orElseGet(() -> roleRepository.save(
                        RoleEntity.builder()
                                .id(UUID.randomUUID())
                                .roleName(RoleEnum.ROLE_HOTEL_MANAGER)
                                .createdAt(Instant.now())
                                .build()
                ));

        add(hotelManager,
                PermissionEnum.ROOM_MANAGE,
                PermissionEnum.BOOKING_MANAGE,
                PermissionEnum.REPORT_VIEW
        );


        // ADMIN
        RoleEntity admin = roleRepository.findByRoleName(RoleEnum.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(
                        RoleEntity.builder()
                                .id(UUID.randomUUID())
                                .roleName(RoleEnum.ROLE_ADMIN)
                                .createdAt(Instant.now())
                                .build()
                ));

        add(admin,
                PermissionEnum.USER_MANAGE,
                PermissionEnum.HOTEL_MANAGE,
                PermissionEnum.ROLE_MANAGE
        );





    }

        private void add(RoleEntity role, PermissionEnum... perms) {
            for (PermissionEnum p : perms) {
                PermissionEntity perm = permissionRepository.findByPermissionName(p).orElseThrow(() ->
                        new IllegalStateException("Permission not found" + p.name()));
                role.addPermission(perm);
            }
            roleRepository.save(role);
        }



    }
