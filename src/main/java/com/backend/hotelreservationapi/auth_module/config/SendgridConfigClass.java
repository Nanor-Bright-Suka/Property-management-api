package com.backend.hotelreservationapi.auth_module.config;


import com.sendgrid.SendGrid;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendgridConfigClass {


    @Bean
    public SendGrid sendGrid(SecurityEnvironment securityEnvironment) {
        return new SendGrid(securityEnvironment.getApiKey());
    }
}
