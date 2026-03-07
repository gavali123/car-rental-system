package com.carrental.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * Externalized configuration properties bound from {@code application.yml} under the
 * {@code app} prefix.
 *
 * <p>Inject this class wherever application-specific settings are needed instead of using
 * {@code @Value} directly, to keep configuration consistent and testable.
 */
@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    private Security security = new Security();

    @Getter
    @Setter
    public static class Security {
        /** The Spring Security role name that grants fleet management privileges. */
        @NotBlank
        private String fleetManagerRole = "FLEET_MANAGER";
    }
}
