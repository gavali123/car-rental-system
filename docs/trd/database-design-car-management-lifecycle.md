# Database Design - Car Management: Vehicle Lifecycle Management (FR-2)

> **Canonical ownership notice:** The `vehicle_status_history` table defined in this document is the single authoritative table for recording all vehicle lifecycle status transitions. Other TRDs that involve lifecycle status changes (e.g., FR-4 Vehicle Retirement) **must reference this table** rather than defining a separate history table.

## Document Information

| Field | Details |
|---|---|
| **Feature Name** | Vehicle Lifecycle Management |
| **Module** | Car Management |
| **Related TRD** | [TRD - Car Management: Vehicle Lifecycle Management](./trd-car-management-lifecycle.md) |
| **Related PRD** | [PRD - Car Management, FR-2: Vehicle Lifecycle Management](../prd/prd-car-management.md#fr-2-vehicle-lifecycle-management) |
| **Author** | @copilot |
| **Date** | 2026-03-07 |
| **Version** | 1.0 |

---

## Table of Contents

1. [Overview](#overview)
2. [Enum Definitions](#enum-definitions)
3. [Table: `vehicles` (lifecycle fields)](#table-vehicles-lifecycle-fields)
4. [Table: `vehicle_status_history`](#table-vehicle_status_history)
5. [Entity Relationship Diagram](#entity-relationship-diagram)
6. [Foreign Key Relationships](#foreign-key-relationships)
7. [Index Summary](#index-summary)
8. [Design Decisions](#design-decisions)

---

## Overview

This document describes the database schema changes introduced to support Vehicle Lifecycle Management (FR-2) of the Car Management module. Two tables are relevant:

- **`vehicles`** — the `lifecycle_status` column on the existing vehicle record represents the vehicle's current operational state. Only the lifecycle-relevant columns are described here; all other columns are defined in [Database Design - Vehicle Onboarding (FR-1)](./db-design-car-management-vehicle-onboarding.md).
- **`vehicle_status_history`** — a new append-only audit table that records every lifecycle status transition for every vehicle, including who made the change and when. This is the single canonical table for lifecycle transition history across the entire Car Management module.

The `users` table referenced by `vehicle_status_history` is managed by the Authentication/User Management module and is referenced here via a foreign key only.

---

## Enum Definitions

### `lifecycle_status_enum`

The `lifecycle_status_enum` type is defined in [Database Design - Vehicle Onboarding (FR-1)](./db-design-car-management-vehicle-onboarding.md#lifecycle_status_enum) and is reproduced here for reference.

| Value | Description | Is Terminal? |
|---|---|---|
| `INCOMING` | Acquired and registered; not yet ready for rental | No |
| `ACTIVE` | Available for reservation assignment | No |
| `MAINTENANCE` | Undergoing service; unavailable for rental | No |
| `DECOMMISSIONING` | Being phased out; unavailable for new reservations | No |
| `SOLD` | Removed from fleet | **Yes** |

The `INCOMING` value is assigned by the onboarding process (FR-1). Transitions to all other values are governed by this feature (FR-2) and the retirement process (FR-4). `SOLD` is a terminal state; no further transitions are permitted once a vehicle reaches it.

---

## Table: `vehicles` (lifecycle fields)

**Description:** The `vehicles` table stores the primary record for each vehicle in the rental fleet. Only the columns added or directly relevant to lifecycle management (FR-2) are described here. All other columns are defined in [Database Design - Vehicle Onboarding (FR-1)](./db-design-car-management-vehicle-onboarding.md#table-vehicles).

| Column | Data Type | Nullable | Default | Constraints | Notes |
|---|---|---|---|---|---|
| `id` | `UUID` | No | `gen_random_uuid()` | `PRIMARY KEY` | System-generated identifier; defined in FR-1 |
| `lifecycle_status` | `lifecycle_status_enum` | No | `'INCOMING'` | `NOT NULL` | Set to `INCOMING` on insert by the system; transitions managed by FR-2 |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | No | `NOW()` | `NOT NULL` | UTC; set once at insert; never updated; defined in FR-1 |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | No | `NOW()` | `NOT NULL` | UTC; updated on every write via trigger or ORM hook; defined in FR-1 |
| `deleted_at` | `TIMESTAMP WITH TIME ZONE` | Yes | `NULL` | — | Populated on soft delete; `NULL` while record is active |
| `created_by` | `TEXT` | No | — | `NOT NULL` | Email of the user who created the record; set once at insert |
| `updated_by` | `TEXT` | No | — | `NOT NULL` | Email of the user who last updated the record |
| `deleted` | `BOOLEAN` | No | `FALSE` | `NOT NULL` | Soft delete flag; `TRUE` means the record is logically deleted |

> **Note:** Additional columns (`vin`, `license_plate`, `brand`, `model`, `manufacturing_year`, `size_type`, `vehicle_class`, `vehicle_category`, `number_of_seats`, `fuel_type`, `odometer_at_acquisition`, `purchase_date`, `purchase_cost`, `home_location_id`) are defined in [Database Design - Vehicle Onboarding (FR-1)](./db-design-car-management-vehicle-onboarding.md).

### Indexes on `vehicles` (lifecycle-relevant)

| Index Name | Type | Columns | Purpose |
|---|---|---|---|
| `vehicles_pkey` | Unique (PK) | `id` | Primary key lookup; defined in FR-1 |
| `idx_vehicles_lifecycle_status` | B-tree | `lifecycle_status` | Supports filtering vehicles by current lifecycle status (availability queries, reservation allocation) |

---

## Table: `vehicle_status_history`

**Description:** Stores an immutable, ordered audit trail of every lifecycle status transition for each vehicle. A new row is inserted on every status change. This is the **canonical** and **single** table for lifecycle transition history; no other table should duplicate this purpose.

Records in this table are **append-only** — no `UPDATE` or `DELETE` operations are permitted after a row is inserted.

| Column | Data Type | Nullable | Default | Constraints | Notes |
|---|---|---|---|---|---|
| `id` | `UUID` | No | `gen_random_uuid()` | `PRIMARY KEY` | System-generated identifier |
| `vehicle_id` | `UUID` | No | — | `NOT NULL`, `FOREIGN KEY → vehicles(id) ON DELETE RESTRICT` | The vehicle whose status changed |
| `previous_status` | `lifecycle_status_enum` | No | — | `NOT NULL` | Lifecycle status of the vehicle before this transition |
| `new_status` | `lifecycle_status_enum` | No | — | `NOT NULL` | Lifecycle status of the vehicle after this transition |
| `changed_by_user_id` | `UUID` | No | — | `NOT NULL`, `FOREIGN KEY → users(id) ON DELETE RESTRICT` | UUID of the fleet manager who performed the transition; derived from the JWT `sub` claim — never supplied by the client |
| `changed_at` | `TIMESTAMP WITH TIME ZONE` | No | `NOW()` | `NOT NULL` | UTC timestamp when the transition was recorded |
| `notes` | `TEXT` | Yes | `NULL` | — | Optional free-text reason for the transition; supplied by the fleet manager at the time of the change |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | No | `NOW()` | `NOT NULL` | UTC; set once at insert; never updated |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | No | `NOW()` | `NOT NULL` | UTC; set to `NOW()` at insert; not updated thereafter (append-only record) |
| `deleted_at` | `TIMESTAMP WITH TIME ZONE` | Yes | `NULL` | — | Populated on soft delete; expected to remain `NULL` for all history records |
| `created_by` | `TEXT` | No | — | `NOT NULL` | Email of the user who created the record; derived from the JWT `email` claim |
| `updated_by` | `TEXT` | No | — | `NOT NULL` | Email of the user who last updated the record; set equal to `created_by` at insert |
| `deleted` | `BOOLEAN` | No | `FALSE` | `NOT NULL` | Soft delete flag; expected to remain `FALSE` for all history records |

### Indexes on `vehicle_status_history`

| Index Name | Type | Columns | Purpose |
|---|---|---|---|
| `vehicle_status_history_pkey` | Unique (PK) | `id` | Primary key lookup |
| `idx_vehicle_status_history_vehicle_id` | B-tree | `vehicle_id` | Supports fetching all history records for a given vehicle; also satisfies FK constraint lookup |
| `idx_vehicle_status_history_changed_by_user_id` | B-tree | `changed_by_user_id` | Supports FK constraint lookup on the `users` table |
| `idx_vehicle_status_history_changed_at` | B-tree | `changed_at` | Supports time-range queries and chronological ordering of history records |
| `idx_vehicle_status_history_vehicle_id_changed_at` | B-tree (composite) | `vehicle_id`, `changed_at DESC` | Supports the paginated lifecycle history API, which fetches all history records for a vehicle ordered by most recent first |

---

## Entity Relationship Diagram

```
┌─────────────────────────┐
│          users           │
│ ──────────────────────── │
│ id (PK)                  │
│ email                    │
│ role                     │
│ ...                      │
└───────────┬─────────────┘
            │ 1
            │ (changed_by_user_id)
            │ 0..*
┌───────────┴─────────────┐           ┌───────────────────────────────┐
│         vehicles         │           │     vehicle_status_history     │
│ ──────────────────────── │           │ ───────────────────────────── │
│ id (PK)                  │◄──────────┤ vehicle_id (FK)               │
│ lifecycle_status         │ 1   0..*  │ id (PK)                       │
│ ...                      │           │ previous_status               │
│ (other fields in FR-1)   │           │ new_status                    │
└──────────────────────────┘           │ changed_by_user_id (FK)       │
                                       │ changed_at                    │
                                       │ notes                         │
                                       │ created_at                    │
                                       │ updated_at                    │
                                       │ deleted_at                    │
                                       │ created_by                    │
                                       │ updated_by                    │
                                       │ deleted                       │
                                       └───────────────────────────────┘
```

---

## Foreign Key Relationships

| From Table | From Column | To Table | To Column | On Delete | Notes |
|---|---|---|---|---|---|
| `vehicle_status_history` | `vehicle_id` | `vehicles` | `id` | `RESTRICT` | A vehicle record cannot be hard-deleted while history rows reference it. In normal operations, vehicles are soft-deleted (`deleted = TRUE`); hard deletion is not expected. |
| `vehicle_status_history` | `changed_by_user_id` | `users` | `id` | `RESTRICT` | A user record cannot be hard-deleted while they have associated history records. Ensures the audit trail remains traceable to a user identity. |

---

## Index Summary

| Table | Index Name | Columns | Unique? | Purpose |
|---|---|---|---|---|
| `vehicles` | `vehicles_pkey` | `id` | Yes | PK lookup |
| `vehicles` | `idx_vehicles_lifecycle_status` | `lifecycle_status` | No | Availability filtering and reservation allocation |
| `vehicle_status_history` | `vehicle_status_history_pkey` | `id` | Yes | PK lookup |
| `vehicle_status_history` | `idx_vehicle_status_history_vehicle_id` | `vehicle_id` | No | History retrieval per vehicle; FK lookup |
| `vehicle_status_history` | `idx_vehicle_status_history_changed_by_user_id` | `changed_by_user_id` | No | FK lookup on users table |
| `vehicle_status_history` | `idx_vehicle_status_history_changed_at` | `changed_at` | No | Time-range queries and chronological ordering |
| `vehicle_status_history` | `idx_vehicle_status_history_vehicle_id_changed_at` | `vehicle_id`, `changed_at DESC` | No | Paginated history API (vehicle history ordered most recent first) |

---

## Design Decisions

| # | Decision | Rationale |
|---|---|---|
| DD-01 | **`vehicle_status_history` is a separate, append-only table** | Storing history in a dedicated table ensures the audit trail is immutable and cannot be corrupted by updates to the parent `vehicles` record. A single-column history field on `vehicles` would not be sufficient for an ordered, multi-entry audit log. |
| DD-02 | **`lifecycle_status_enum` is used for both `previous_status` and `new_status`** | Using the shared enum type enforces that only valid status values can be stored in the history table at the database level, preventing orphaned or inconsistent history records. |
| DD-03 | **`changed_by_user_id` is derived from the JWT `sub` claim, not from the request body** | Trusting the authenticated token rather than client-supplied data prevents a malicious actor from attributing a status change to another user. The application layer must never allow the client to override this value. |
| DD-04 | **`ON DELETE RESTRICT` for both foreign keys on `vehicle_status_history`** | History records are part of a permanent audit trail. `RESTRICT` prevents orphaning history rows if a vehicle or user record is accidentally hard-deleted. Both vehicles and users are expected to be soft-deleted (`deleted = TRUE`) rather than hard-deleted in normal operations. |
| DD-05 | **Composite index on `(vehicle_id, changed_at DESC)`** | The primary query pattern for history retrieval is "all status changes for vehicle X, ordered most recent first". A composite index on these two columns serves this query directly without a separate sort step, making pagination efficient even for vehicles with large history sets. |
| DD-06 | **`updated_at` and `updated_by` are present on `vehicle_status_history` despite the append-only nature** | These audit columns follow the standard row-level audit pattern used consistently across all tables in the system. They are set at insert time and are never subsequently changed; their presence ensures that application-layer ORM hooks and audit frameworks work uniformly without special-casing this table. |
| DD-07 | **`notes` is nullable** | A reason for the status change is operationally useful but not required for every transition. Making `notes` nullable avoids forcing fleet managers to supply a placeholder value for routine status changes, while still allowing meaningful notes when they are relevant (e.g., pre-rental inspection passed, insurance lapsed). |
| DD-08 | **`lifecycle_status` defaults to `INCOMING` at the database level** | Enforcing the default at the database layer ensures that even if the application layer fails to set the status during onboarding, the record is always created in a safe, non-rentable state. This is defined in FR-1 and referenced here for completeness. |
