package com.carrental.domain.entity;

import com.carrental.domain.enums.LifecycleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Immutable audit record of a vehicle lifecycle status transition.
 *
 * <p>A new row is appended on every status change. Records in this table are
 * append-only — no updates or deletes are permitted (FR-2 TRD).
 *
 * <p>This is the <em>canonical</em> table for lifecycle transition history. No other
 * table must duplicate this purpose (see database-design-car-management-lifecycle.md).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("vehicle_status_history")
public class VehicleStatusHistory {

    @Id
    private UUID id;

    @Column("vehicle_id")
    private UUID vehicleId;

    @Column("previous_status")
    private LifecycleStatus previousStatus;

    @Column("new_status")
    private LifecycleStatus newStatus;

    /** UUID of the fleet manager who performed the transition. */
    @Column("changed_by_user_id")
    private UUID changedByUserId;

    @Column("changed_at")
    private OffsetDateTime changedAt;

    /** Optional free-text note explaining the reason for the transition. */
    private String notes;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;

    @Column("created_by")
    private String createdBy;

    @Column("updated_by")
    private String updatedBy;

    private boolean deleted;
}
