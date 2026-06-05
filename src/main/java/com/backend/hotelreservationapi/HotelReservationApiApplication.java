package com.backend.hotelreservationapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class HotelReservationApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelReservationApiApplication.class, args);

    }



}
