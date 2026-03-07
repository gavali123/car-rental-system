package com.carrental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Car Rental System Spring Boot application.
 *
 * <p>This service uses Spring WebFlux for reactive HTTP endpoints and R2DBC for
 * non-blocking database access against a PostgreSQL database. Database migrations
 * are managed by Flyway using a dedicated JDBC data source.
 */
@SpringBootApplication
public class CarRentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarRentalApplication.class, args);
    }
}
