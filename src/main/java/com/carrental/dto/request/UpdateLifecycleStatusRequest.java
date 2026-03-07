package com.carrental.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Request body for updating a vehicle's lifecycle status
 * ({@code PATCH /api/v1/vehicles/{vehicleId}/lifecycle-status}).
 */
@Value
@Builder
@Jacksonized
public class UpdateLifecycleStatusRequest {

    /**
     * Target lifecycle status. Must be one of:
     * {@code INCOMING}, {@code ACTIVE}, {@code MAINTENANCE}, {@code DECOMMISSIONING}, {@code SOLD}.
     */
    @NotBlank
    String status;

    /** Optional free-text explanation for the status change. */
    String notes;
}
