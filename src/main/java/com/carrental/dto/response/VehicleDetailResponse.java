package com.carrental.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Full vehicle detail response for {@code GET /api/v1/vehicles/{vehicleId}}.
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleDetailResponse {

    UUID id;
    String vin;
    String licensePlate;
    LocalDate purchaseDate;
    BigDecimal purchaseCost;
    Integer odometerAtAcquisition;
    String brand;
    String model;
    Short manufacturingYear;
    String sizeType;
    String vehicleClass;
    String vehicleCategory;
    Short numberOfSeats;
    String fuelType;
    String lifecycleStatus;

    /** Summary of the vehicle's home location. */
    LocationSummaryResponse homeLocation;

    /** Current active insurance record. */
    InsuranceResponse insurance;

    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
