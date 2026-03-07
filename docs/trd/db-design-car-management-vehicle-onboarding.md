# Database Design - Car Management: Vehicle Onboarding (FR-1)

## Document Information

| Field | Details |
|---|---|
| **Feature Name** | Vehicle Onboarding |
| **Module** | Car Management |
| **Related TRD** | [TRD - Car Management: Vehicle Onboarding](./trd-car-management-vehicle-onboarding.md) |
| **Related PRD** | [PRD - Car Management, FR-1: Vehicle Onboarding](../prd/prd-car-management.md#fr-1-vehicle-onboarding) |
| **Author** | @copilot |
| **Date** | 2026-03-07 |
| **Version** | 1.0 |

---

## Table of Contents

1. [Overview](#overview)
2. [Enum Definitions](#enum-definitions)
3. [Table: `vehicles`](#table-vehicles)
4. [Table: `vehicle_insurance`](#table-vehicle_insurance)
5. [Entity Relationship Diagram](#entity-relationship-diagram)
6. [Foreign Key Relationships](#foreign-key-relationships)
7. [Index Summary](#index-summary)
8. [Design Decisions](#design-decisions)

---

## Overview

This document describes the database schema introduced to support Vehicle Onboarding (FR-1) of the Car Management module. Two tables are created:

- **`vehicles`** — the primary record for each vehicle registered in the rental fleet.
- **`vehicle_insurance`** — insurance records linked to a vehicle, supporting historical retention of insurance renewals.

These tables depend on the `locations` table, which is managed by the Location module and is referenced here via a foreign key only.

---

## Enum Definitions

The following database-level enum types are used across the tables in this design.

### `size_type_enum`

| Value | Description |
|---|---|
| `SMALL` | 4-seat vehicle |
| `MEDIUM` | 7-seat vehicle |

### `vehicle_class_enum`

| Value | Description |
|---|---|
| `ECONOMY` | Economy-tier vehicle |
| `LUXURY` | Luxury-tier vehicle |

### `vehicle_category_enum`

| Value | Description |
|---|---|
| `ECONOMY_SMALL` | 4-seat economy sedan |
| `ECONOMY_MEDIUM` | 7-seat economy MPV / SUV |
| `LUXURY_SMALL` | 4-seat luxury sedan |
| `LUXURY_MEDIUM` | Luxury MPV |

`vehicle_category` is always derived from the combination of `size_type` and `vehicle_class`. See [Design Decisions](#design-decisions) for the rationale behind storing it.

### `fuel_type_enum`

| Value | Description |
|---|---|
| `GAS` | Internal combustion engine |
| `ELECTRIC` | Battery electric vehicle |
| `HYBRID` | Hybrid engine (gas + electric) |

### `lifecycle_status_enum`

| Value | Description | Is Terminal? |
|---|---|---|
| `INCOMING` | Acquired and registered; not yet ready for rental | No |
| `ACTIVE` | Available for reservation assignment | No |
| `MAINTENANCE` | Undergoing service; unavailable for rental | No |
| `DECOMMISSIONING` | Being phased out; unavailable for new reservations | No |
| `SOLD` | Removed from fleet | **Yes** |

The `INCOMING` status is the only value assigned by the onboarding process. Transitions to all other statuses are governed by FR-2 (Lifecycle Management).

---

## Table: `vehicles`

**Description:** Stores the primary record for each vehicle registered in the rental fleet.

| Column | Data Type | Nullable | Default | Constraints | Notes |
|---|---|---|---|---|---|
| `id` | `UUID` | No | `gen_random_uuid()` | `PRIMARY KEY` | System-generated identifier |
| `vin` | `VARCHAR(17)` | No | — | `UNIQUE`, `NOT NULL` | Exactly 17 alphanumeric characters; immutable after creation |
| `license_plate` | `VARCHAR(20)` | No | — | `UNIQUE`, `NOT NULL` | Normalized to uppercase; immutable after creation |
| `purchase_date` | `DATE` | No | — | `NOT NULL`, `CHECK (purchase_date <= CURRENT_DATE)` | Must not be a future date |
| `purchase_cost` | `DECIMAL(12,2)` | No | — | `NOT NULL`, `CHECK (purchase_cost > 0)` | In local currency; must be positive |
| `odometer_at_acquisition` | `INTEGER` | No | — | `NOT NULL`, `CHECK (odometer_at_acquisition >= 0)` | In kilometres; zero is valid for brand-new vehicles |
| `brand` | `VARCHAR(100)` | No | — | `NOT NULL` | Vehicle manufacturer (e.g., Toyota, BMW) |
| `model` | `VARCHAR(100)` | No | — | `NOT NULL` | Vehicle model name (e.g., Camry, 3 Series) |
| `manufacturing_year` | `SMALLINT` | No | — | `NOT NULL`, `CHECK (manufacturing_year >= 1900 AND manufacturing_year <= EXTRACT(YEAR FROM CURRENT_DATE) + 1)` | Year of manufacture |
| `size_type` | `size_type_enum` | No | — | `NOT NULL` | `SMALL` or `MEDIUM` |
| `vehicle_class` | `vehicle_class_enum` | No | — | `NOT NULL` | `ECONOMY` or `LUXURY` |
| `vehicle_category` | `vehicle_category_enum` | No | — | `NOT NULL` | Derived from `size_type` + `vehicle_class`; computed and stored at write time |
| `number_of_seats` | `SMALLINT` | No | — | `NOT NULL`, `CHECK (number_of_seats > 0)` | Typical values: 4 for `SMALL`, 7 for `MEDIUM` |
| `fuel_type` | `fuel_type_enum` | No | — | `NOT NULL` | `GAS`, `ELECTRIC`, or `HYBRID` |
| `lifecycle_status` | `lifecycle_status_enum` | No | `'INCOMING'` | `NOT NULL` | Set to `INCOMING` by the system on insert; transitions managed by FR-2 |
| `home_location_id` | `UUID` | No | — | `NOT NULL`, `FOREIGN KEY → locations(id)` | Required at onboarding; managed by FR-5 for subsequent changes |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | No | `NOW()` | `NOT NULL` | UTC; set once at insert; never updated |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | No | `NOW()` | `NOT NULL` | UTC; updated on every write via trigger or ORM hook |

### Indexes on `vehicles`

| Index Name | Type | Columns | Purpose |
|---|---|---|---|
| `vehicles_pkey` | Unique (PK) | `id` | Primary key lookup |
| `vehicles_vin_key` | Unique | `vin` | Prevents duplicate VIN registrations |
| `vehicles_license_plate_key` | Unique | `license_plate` | Prevents duplicate license plate registrations (case-insensitive collation) |
| `idx_vehicles_lifecycle_status` | B-tree | `lifecycle_status` | Supports filtering by availability status across all vehicle queries |
| `idx_vehicles_home_location_id` | B-tree | `home_location_id` | Supports inventory-per-location queries |
| `idx_vehicles_category_status` | B-tree (composite) | `vehicle_category`, `lifecycle_status` | Supports reservation allocation queries filtering by category and status |

---

## Table: `vehicle_insurance`

**Description:** Stores insurance records for each vehicle. Supports historical retention of insurance renewals by maintaining multiple records per vehicle with an `is_active` flag.

| Column | Data Type | Nullable | Default | Constraints | Notes |
|---|---|---|---|---|---|
| `id` | `UUID` | No | `gen_random_uuid()` | `PRIMARY KEY` | System-generated identifier |
| `vehicle_id` | `UUID` | No | — | `NOT NULL`, `FOREIGN KEY → vehicles(id) ON DELETE CASCADE` | Links insurance to its vehicle |
| `insurer_name` | `VARCHAR(200)` | No | — | `NOT NULL` | Name of the insurance provider |
| `policy_number` | `VARCHAR(100)` | No | — | `NOT NULL` | Insurance policy reference number |
| `coverage_start_date` | `DATE` | No | — | `NOT NULL`, `CHECK (coverage_start_date < coverage_end_date)` | Start of insurance coverage period |
| `coverage_end_date` | `DATE` | No | — | `NOT NULL` | End of insurance coverage period; monitored by FR-3 for expiry blocking |
| `is_active` | `BOOLEAN` | No | `TRUE` | `NOT NULL` | Marks the current insurance record; only one active record per vehicle at any time |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | No | `NOW()` | `NOT NULL` | UTC; set once at insert |

### Indexes on `vehicle_insurance`

| Index Name | Type | Columns | Condition | Purpose |
|---|---|---|---|---|
| `vehicle_insurance_pkey` | Unique (PK) | `id` | — | Primary key lookup |
| `idx_vehicle_insurance_vehicle_id` | B-tree | `vehicle_id` | — | Supports joins and lookups from the vehicles table |
| `idx_vehicle_insurance_coverage_end_date` | B-tree | `coverage_end_date` | — | Supports daily insurance expiry scan (FR-3) |
| `uq_vehicle_insurance_active` | Unique (Partial) | `vehicle_id` | `WHERE is_active = TRUE` | Enforces at most one active insurance record per vehicle at the database level |

---

## Entity Relationship Diagram

```
┌───────────────────────┐           ┌─────────────────────────┐
│       locations        │           │     vehicle_insurance    │
│ ─────────────────────  │           │ ──────────────────────── │
│ id (PK)               │           │ id (PK)                  │
│ name                  │           │ vehicle_id (FK) ───────┐ │
│ ...                   │           │ insurer_name           │ │
└───────────┬───────────┘           │ policy_number          │ │
            │ 1                     │ coverage_start_date    │ │
            │                       │ coverage_end_date      │ │
            │                       │ is_active              │ │
            │ N                     │ created_at             │ │
┌───────────┴───────────┐           └─────────────────────────┘
│        vehicles        │                      │ 0..*
│ ─────────────────────  │                      │
│ id (PK)               │◄─────────────────────┘
│ vin (UNIQUE)          │ 1
│ license_plate (UNIQUE)│
│ purchase_date         │
│ purchase_cost         │
│ odometer_at_acq.      │
│ brand                 │
│ model                 │
│ manufacturing_year    │
│ size_type             │
│ vehicle_class         │
│ vehicle_category      │
│ number_of_seats       │
│ fuel_type             │
│ lifecycle_status      │
│ home_location_id (FK) │
│ created_at            │
│ updated_at            │
└───────────────────────┘
```

---

## Foreign Key Relationships

| From Table | From Column | To Table | To Column | On Delete | Notes |
|---|---|---|---|---|---|
| `vehicles` | `home_location_id` | `locations` | `id` | `RESTRICT` | A location cannot be deleted while vehicles are assigned to it |
| `vehicle_insurance` | `vehicle_id` | `vehicles` | `id` | `CASCADE` | Insurance records are deleted when the parent vehicle record is deleted (hard-delete is not expected; see NFR-08 in the TRD) |

---

## Index Summary

| Table | Index Name | Columns | Unique? | Partial Condition | Purpose |
|---|---|---|---|---|---|
| `vehicles` | `vehicles_pkey` | `id` | Yes | — | PK lookup |
| `vehicles` | `vehicles_vin_key` | `vin` | Yes | — | Duplicate VIN prevention |
| `vehicles` | `vehicles_license_plate_key` | `license_plate` | Yes | — | Duplicate license plate prevention |
| `vehicles` | `idx_vehicles_lifecycle_status` | `lifecycle_status` | No | — | Availability filtering |
| `vehicles` | `idx_vehicles_home_location_id` | `home_location_id` | No | — | Location-based inventory |
| `vehicles` | `idx_vehicles_category_status` | `vehicle_category`, `lifecycle_status` | No | — | Reservation allocation |
| `vehicle_insurance` | `vehicle_insurance_pkey` | `id` | Yes | — | PK lookup |
| `vehicle_insurance` | `idx_vehicle_insurance_vehicle_id` | `vehicle_id` | No | — | Vehicle-to-insurance join |
| `vehicle_insurance` | `idx_vehicle_insurance_coverage_end_date` | `coverage_end_date` | No | — | Daily expiry scan (FR-3) |
| `vehicle_insurance` | `uq_vehicle_insurance_active` | `vehicle_id` | Yes | `is_active = TRUE` | One active record per vehicle |

---

## Design Decisions

| # | Decision | Rationale |
|---|---|---|
| DD-01 | **`vehicle_insurance` is a separate table** | Insurance details require historical retention. When a policy is renewed, the old record is kept with `is_active = FALSE` for audit and compliance purposes. Embedding insurance data in the `vehicles` table would prevent this. |
| DD-02 | **`vehicle_category` is stored (not only computed)** | Although derivable from `size_type` and `vehicle_class`, storing `vehicle_category` avoids a runtime derivation on every reservation allocation query. The composite index on `(vehicle_category, lifecycle_status)` then becomes highly selective and efficient. |
| DD-03 | **`lifecycle_status` defaults to `INCOMING` at the DB level** | Enforcing the default at the database layer ensures that even if the application layer fails to set the status, the record is always created in a safe, non-rentable state. |
| DD-04 | **Partial unique index on `vehicle_insurance.vehicle_id` where `is_active = TRUE`** | A standard unique index on `vehicle_id` alone would prevent storing historical insurance records. A partial index scoped to active records achieves the uniqueness guarantee only where it matters, while still allowing multiple historical (inactive) rows per vehicle. |
| DD-05 | **`ON DELETE RESTRICT` for `vehicles → locations`** | A location record must not be deleted while vehicles are assigned to it, as this would leave orphaned fleet records. `RESTRICT` forces the Location module to reassign or transfer all vehicles before deletion. |
| DD-06 | **`ON DELETE CASCADE` for `vehicle_insurance → vehicles`** | Insurance records have no meaning independent of their vehicle. Cascade deletion ensures no orphaned insurance rows remain if a vehicle record is ever hard-deleted. In normal operations, vehicle records are soft-deleted / archived (NFR-08), so cascade will not fire in regular use. |
| DD-07 | **UUIDs for primary keys** | UUIDs avoid sequential ID exposure (no enumeration risk), allow client-side pre-generation if needed, and are compatible with distributed or multi-region architectures in the future. |
| DD-08 | **`license_plate` stored in uppercase** | License plate lookups are case-insensitive by business rule. Normalizing to uppercase at write time simplifies index design (a standard B-tree unique index is sufficient rather than a case-insensitive function index). |
