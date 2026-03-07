package com.carrental.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response body returned after a successful vehicle registration
 * ({@code POST /api/v1/vehicles} → {@code 201 Created}).
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleCreatedResponse {

    UUID id;
    String vin;
    String licensePlate;

    /** Always {@code INCOMING} for a newly registered vehicle. */
    String lifecycleStatus;

    /** Category derived from {@code sizeType} and {@code vehicleClass} (e.g., {@code ECONOMY_SMALL}). */
    String vehicleCategory;

    OffsetDateTime createdAt;
}
