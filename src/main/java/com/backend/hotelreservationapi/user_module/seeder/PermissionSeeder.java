package com.backend.hotelreservationapi.user_module.seeder;

import com.backend.hotelreservationapi.user_module.entity.PermissionEntity;
import com.backend.hotelreservationapi.user_module.enums.PermissionEnum;
import com.backend.hotelreservationapi.user_module.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
@RequiredArgsConstructor
@Order(value = 1)
public class PermissionSeeder implements ApplicationRunner {

    private final PermissionRepository permissionRepository;

    @Override
    public void run(ApplicationArguments args) {
        for (PermissionEnum p : PermissionEnum.values()) {
            permissionRepository.findByPermissionName(p)
                    .orElseGet(() -> permissionRepository.save(
                            PermissionEntity.builder()
                                    .id(UUID.randomUUID())
                                    .permissionName(p)
                                    .build()
                    ));

        }
    }

}
