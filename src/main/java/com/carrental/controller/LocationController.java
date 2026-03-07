package com.carrental.controller;

import com.carrental.dto.response.LocationResponse;
import com.carrental.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * REST controller exposing location endpoints used in vehicle onboarding and fleet filtering.
 *
 * <p>Base path: {@code /api/v1/locations}
 */
@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    private static final Logger log = LoggerFactory.getLogger(LocationController.class);

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * Lists all rental locations.
     *
     * @param activeOnly when {@code true} (default), only active locations are returned
     * @return a {@link Flux} of {@link LocationResponse}
     */
    @GetMapping
    public Flux<LocationResponse> listLocations(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        log.debug("GET /api/v1/locations activeOnly={}", activeOnly);
        return locationService.listLocations(activeOnly);
    }
}
