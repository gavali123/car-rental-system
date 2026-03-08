package com.carrental.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request body for registering a new vehicle into the rental fleet
 * ({@code POST /api/v1/vehicles}).
 *
 * <p>Validation annotations enforce field-level rules as defined in BRV-01 through
 * BRV-13 of the Vehicle Onboarding TRD. Business-level validation (e.g., VIN
 * uniqueness, location existence) is performed in the service layer.
 */
@Value
@Builder
@Jacksonized
public class RegisterVehicleRequest {

    /** Exactly 17 alphanumeric characters; no spaces. */
    @NotBlank
    String vin;

    /** 1–20 characters; stored and compared in uppercase. */
    @NotBlank
    String licensePlate;

    /** Must not be a future date. */
    @NotNull
    LocalDate purchaseDate;

    /** Must be greater than zero. */
    @NotNull
    @Positive
    BigDecimal purchaseCost;

    /** In kilometres; zero is valid for brand-new vehicles. */
    @NotNull
    @PositiveOrZero
    Integer odometerAtAcquisition;

    @NotBlank
    String brand;

    @NotBlank
    String model;

    /** Must be between 1900 and current year + 1. */
    @NotNull
    Short manufacturingYear;

    /** Must be {@code SMALL} or {@code MEDIUM}. */
    @NotBlank
    String sizeType;

    /** Must be {@code ECONOMY} or {@code LUXURY}. */
    @NotBlank
    String vehicleClass;

    /** Must be greater than zero. */
    @NotNull
    @Positive
    Short numberOfSeats;

    /** Must be {@code GAS}, {@code ELECTRIC}, or {@code HYBRID}. */
    @NotBlank
    String fuelType;

    /** Must reference an existing, active location. */
    @NotNull
    UUID homeLocationId;

    /** Insurance details for the vehicle at time of registration. */
    @NotNull
    InsuranceRequest insurance;
}
