package com.carrental.service;

import com.carrental.domain.entity.Location;
import com.carrental.dto.response.LocationResponse;
import com.carrental.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Business logic for location queries used in vehicle onboarding and fleet filtering.
 */
@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    /**
     * Returns all locations, optionally restricted to active ones.
     *
     * @param activeOnly when {@code true}, only active locations are returned
     * @return a {@link Flux} of {@link LocationResponse} objects
     */
    public Flux<LocationResponse> listLocations(boolean activeOnly) {
        log.debug("Fetching locations, activeOnly={}", activeOnly);
        Flux<Location> locations = activeOnly
                ? locationRepository.findByActiveTrue()
                : locationRepository.findAll();
        return locations.map(this::toResponse);
    }

    // ── Mapping ──────────────────────────────────────────────────────────────

    private LocationResponse toResponse(Location location) {
        return LocationResponse.builder()
                .id(location.getId())
                .name(location.getName())
                .city(location.getCity())
                .country(location.getCountry())
                .active(location.isActive())
                .build();
    }
}
