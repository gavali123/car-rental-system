package com.carrental.controller;

import com.carrental.dto.request.RegisterVehicleRequest;
import com.carrental.dto.request.UpdateLifecycleStatusRequest;
import com.carrental.dto.response.LifecycleHistoryResponse;
import com.carrental.dto.response.LifecycleStatusUpdateResponse;
import com.carrental.dto.response.VehicleCreatedResponse;
import com.carrental.dto.response.VehicleDetailResponse;
import com.carrental.service.VehicleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller for vehicle management endpoints.
 *
 * <p>Base path: {@code /api/v1/vehicles}
 *
 * <p>All endpoints require an authenticated user. Write operations additionally require
 * the {@code FLEET_MANAGER} role (enforced in {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private static final Logger log = LoggerFactory.getLogger(VehicleController.class);

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    /**
     * Registers a new vehicle into the rental fleet.
     *
     * @param request the vehicle registration payload
     * @return {@code 201 Created} with the registered vehicle summary
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<VehicleCreatedResponse> registerVehicle(
            @Valid @RequestBody RegisterVehicleRequest request) {
        log.info("POST /api/v1/vehicles - VIN: {}", request.getVin());
        return vehicleService.registerVehicle(request);
    }

    /**
     * Retrieves full details for a specific vehicle.
     *
     * @param vehicleId the vehicle UUID
     * @return {@code 200 OK} with the vehicle detail
     */
    @GetMapping("/{vehicleId}")
    public Mono<VehicleDetailResponse> getVehicleDetail(
            @PathVariable UUID vehicleId) {
        log.debug("GET /api/v1/vehicles/{}", vehicleId);
        return vehicleService.getVehicleDetail(vehicleId);
    }

    /**
     * Updates the lifecycle status of a vehicle.
     *
     * <p>Only permitted transitions (per FR-2 transition matrix) are accepted.
     *
     * @param vehicleId the vehicle UUID
     * @param request   the status update payload
     * @param jwt       the authenticated principal's JWT (provides user identity)
     * @return {@code 200 OK} with the status transition result
     */
    @PatchMapping("/{vehicleId}/lifecycle-status")
    public Mono<LifecycleStatusUpdateResponse> updateLifecycleStatus(
            @PathVariable UUID vehicleId,
            @Valid @RequestBody UpdateLifecycleStatusRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID actingUserId = extractUserId(jwt);
        String actingUserEmail = jwt.getClaimAsString("email");
        log.info("PATCH /api/v1/vehicles/{}/lifecycle-status -> {}", vehicleId, request.getStatus());
        return vehicleService.updateLifecycleStatus(vehicleId, request, actingUserId, actingUserEmail);
    }

    /**
     * Returns the paginated lifecycle history for a vehicle.
     *
     * @param vehicleId the vehicle UUID
     * @param page      1-based page number (default: 1)
     * @param pageSize  records per page, max 100 (default: 20)
     * @return {@code 200 OK} with the paginated history
     */
    @GetMapping("/{vehicleId}/lifecycle-history")
    public Mono<LifecycleHistoryResponse> getLifecycleHistory(
            @PathVariable UUID vehicleId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.debug("GET /api/v1/vehicles/{}/lifecycle-history page={} pageSize={}", vehicleId, page, pageSize);
        return vehicleService.getLifecycleHistory(vehicleId, page, pageSize);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private UUID extractUserId(Jwt jwt) {
        String subject = jwt.getSubject();
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            // Fall back to a deterministic UUID derived from the subject string
            return UUID.nameUUIDFromBytes(subject.getBytes());
        }
    }
}
