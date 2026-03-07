package com.carrental.domain.entity;

import com.carrental.domain.enums.FuelType;
import com.carrental.domain.enums.LifecycleStatus;
import com.carrental.domain.enums.SizeType;
import com.carrental.domain.enums.VehicleCategory;
import com.carrental.domain.enums.VehicleClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Primary entity representing a vehicle registered in the rental fleet.
 *
 * <p>A vehicle is created through the onboarding process (FR-1) and maintains its
 * current lifecycle status (FR-2). Each vehicle must reference an active home location
 * (FR-5) and have at least one active insurance record (FR-3).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("vehicles")
public class Vehicle {

    @Id
    private UUID id;

    /** Vehicle Identification Number – exactly 17 alphanumeric characters; immutable. */
    private String vin;

    /** License plate number, stored in uppercase; immutable after creation. */
    @Column("license_plate")
    private String licensePlate;

    @Column("purchase_date")
    private LocalDate purchaseDate;

    @Column("purchase_cost")
    private BigDecimal purchaseCost;

    @Column("odometer_at_acquisition")
    private Integer odometerAtAcquisition;

    private String brand;

    private String model;

    @Column("manufacturing_year")
    private Short manufacturingYear;

    @Column("size_type")
    private SizeType sizeType;

    @Column("vehicle_class")
    private VehicleClass vehicleClass;

    /**
     * Derived from {@link #sizeType} and {@link #vehicleClass}; computed and stored
     * at write time for query efficiency.
     */
    @Column("vehicle_category")
    private VehicleCategory vehicleCategory;

    @Column("number_of_seats")
    private Short numberOfSeats;

    @Column("fuel_type")
    private FuelType fuelType;

    /** Current operational state of the vehicle; defaults to {@code INCOMING} at creation. */
    @Column("lifecycle_status")
    private LifecycleStatus lifecycleStatus;

    /** Foreign key to {@code locations.id}; must reference an active location. */
    @Column("home_location_id")
    private UUID homeLocationId;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
