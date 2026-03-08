package com.carrental;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring Boot integration smoke test.
 *
 * <p>Uses the {@code test} profile which disables real database and OAuth2 connections,
 * verifying only that the application context loads correctly.
 */
@SpringBootTest
@ActiveProfiles("test")
class CarRentalApplicationTest {

    @Test
    void contextLoads() {
        // Verifies that the Spring application context starts without errors
    }
}
