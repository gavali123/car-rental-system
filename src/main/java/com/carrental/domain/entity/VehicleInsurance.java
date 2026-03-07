package com.carrental.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Stores insurance records for a vehicle.
 *
 * <p>Multiple records may exist for the same vehicle over its lifetime (renewals). Only
 * one record may have {@link #active} set to {@code true} at a time, enforced by a
 * partial unique index on {@code vehicle_id WHERE is_active = TRUE}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("vehicle_insurance")
public class VehicleInsurance {

    @Id
    private UUID id;

    @Column("vehicle_id")
    private UUID vehicleId;

    @Column("insurer_name")
    private String insurerName;

    @Column("policy_number")
    private String policyNumber;

    @Column("coverage_start_date")
    private LocalDate coverageStartDate;

    @Column("coverage_end_date")
    private LocalDate coverageEndDate;

    /** {@code true} for the current/active insurance record; {@code false} for historical records. */
    @Column("is_active")
    private boolean active;

    @Column("created_at")
    private OffsetDateTime createdAt;
}
