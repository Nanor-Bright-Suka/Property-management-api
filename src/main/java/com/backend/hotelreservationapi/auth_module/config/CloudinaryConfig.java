package com.backend.hotelreservationapi.auth_module.config;




import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class CloudinaryConfig {

    private final SecurityEnvironment environment;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(Map.of(
                "cloud_name", environment.getCloudName(),
                "api_key", environment.getCloudApiKey(),
                "api_secret", environment.getApiSecret(),
                "secure", true
        ));
    }



}
