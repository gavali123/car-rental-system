package com.carrental.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Spring Security configuration for the Car Rental System.
 *
 * <p>All protected endpoints require a valid JWT bearer token. Mutation endpoints
 * (POST, PATCH) are further restricted to users with the {@code FLEET_MANAGER} role.
 * The JWT is validated against the issuer URI configured in {@code application.yml}.
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final ApplicationProperties properties;

    public SecurityConfig(ApplicationProperties properties) {
        this.properties = properties;
    }

    /**
     * Defines the security filter chain that protects all API routes.
     *
     * @param http the reactive HTTP security builder
     * @return the configured {@link SecurityWebFilterChain}
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        String fleetManagerRole = properties.getSecurity().getFleetManagerRole();

        return http
                /*
                 * CSRF protection is explicitly disabled because this is a stateless REST API
                 * that uses JWT bearer tokens for authentication (no session cookies).
                 * CSRF attacks rely on cookie-based authentication and are not applicable here.
                 * See: https://docs.spring.io/spring-security/reference/features/exploits/csrf.html#csrf-when-to-use
                 */
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Vehicle registration and lifecycle mutations require Fleet Manager role
                        .pathMatchers(HttpMethod.POST, "/api/v1/vehicles").hasRole(fleetManagerRole)
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/vehicles/*/lifecycle-status")
                                .hasRole(fleetManagerRole)
                        // Read endpoints require any authenticated user
                        .pathMatchers(HttpMethod.GET, "/api/v1/**").authenticated()
                        // All other requests must be authenticated
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> {})
                )
                .build();
    }
}
