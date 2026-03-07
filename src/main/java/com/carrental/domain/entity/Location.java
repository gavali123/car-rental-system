package com.carrental.domain.entity;

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
 * Represents a physical rental location (branch or hub).
 *
 * <p>A vehicle must be assigned to an active location at all times (FR-5). Locations
 * are managed by an administrative module; this service only reads from this table.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("locations")
public class Location {

    @Id
    private UUID id;

    private String name;

    @Column("address_line_1")
    private String addressLine1;

    @Column("address_line_2")
    private String addressLine2;

    private String city;

    private String country;

    @Column("postal_code")
    private String postalCode;

    @Column("phone_number")
    private String phoneNumber;

    @Column("is_active")
    private boolean active;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
