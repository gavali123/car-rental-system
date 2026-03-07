# Database Migrations – Car Management: Vehicle Lifecycle Management (FR-2)

## Document Information

| Field | Details |
|---|---|
| **Feature Name** | Car Management – Vehicle Lifecycle Management |
| **Module** | Car Management |
| **Related TRD** | [TRD - Car Management: Vehicle Lifecycle Management](./trd-car-management-lifecycle.md) |
| **Related DB Design** | [Database Design – Car Management: Vehicle Lifecycle Management](./database-design-car-management-lifecycle.md) |
| **Related PRD** | [PRD - Car Management, FR-2: Vehicle Lifecycle Management](../prd/prd-car-management.md#fr-2-vehicle-lifecycle-management) |
| **Author** | @copilot |
| **Date** | 2026-03-07 |
| **Version** | 1.0 |

---

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Migration Files](#migration-files)
   - [V2\_\_alter\_vehicles\_add\_lifecycle\_audit\_columns.sql](#v2__alter_vehicles_add_lifecycle_audit_columnssql)
   - [V3\_\_create\_vehicle\_status\_history.sql](#v3__create_vehicle_status_historysql)
4. [Index Summary](#index-summary)
5. [Design Notes](#design-notes)

---

## Overview

This document specifies the Flyway database migration scripts for the **Vehicle Lifecycle Management** feature (FR-2) of the Car Management module. The migrations implement the database schema described in [database-design-car-management-lifecycle.md](./database-design-car-management-lifecycle.md).

Two migration files are required:

| Migration File | Purpose |
|---|---|
| `V2__alter_vehicles_add_lifecycle_audit_columns.sql` | Extends the existing `vehicles` table with soft-delete and audit trail columns introduced by the lifecycle feature |
| `V3__create_vehicle_status_history.sql` | Creates the `vehicle_status_history` table — the canonical, immutable audit trail for all vehicle lifecycle status transitions |

> **Note on version numbers:** Version numbers `V2` and `V3` are assigned relative to the Vehicle Onboarding migration (`V1`). The implementing team must assign definitive version numbers based on the full, ordered sequence of all applied migrations across the system.

---

## Prerequisites

The following tables must exist before applying these migrations:

| Table | Created By | Notes |
|---|---|---|
| `vehicles` | FR-1 Vehicle Onboarding migration (`V1__create_vehicles.sql`) | The lifecycle migrations extend this table; the base table must already exist |
| `users` | User Management module migration | Referenced by `vehicle_status_history.changed_by_user_id` as a foreign key |

---

## Migration Files

### V2\_\_alter\_vehicles\_add\_lifecycle\_audit\_columns.sql

**Purpose:** Adds soft-delete and audit trail columns to the existing `vehicles` table. These columns are required by the lifecycle feature for tracking record modification history and enabling soft deletion of vehicles.

> This migration must run **after** `V1__create_vehicles.sql` (the Vehicle Onboarding migration).

```sql
-- Migration: V2__alter_vehicles_add_lifecycle_audit_columns.sql
-- Feature: FR-2 Vehicle Lifecycle Management
-- Description: Adds soft-delete and audit trail columns to the vehicles table.

ALTER TABLE vehicles
    ADD COLUMN created_by TEXT        NOT NULL DEFAULT 'system',
    ADD COLUMN updated_by TEXT        NOT NULL DEFAULT 'system',
    ADD COLUMN deleted     BOOLEAN    NOT NULL DEFAULT false,
    ADD COLUMN deleted_at  TIMESTAMPTZ;
```

**Column descriptions:**

| Column | Data Type | Nullable | Default | Description |
|---|---|---|---|---|
| `created_by` | `TEXT` | No | `'system'` | Username (email) of the user who created the record; populated at write time by the application layer |
| `updated_by` | `TEXT` | No | `'system'` | Username (email) of the user who last updated the record; updated on every write by the application layer |
| `deleted` | `BOOLEAN` | No | `false` | Soft-delete flag; when `true`, the vehicle is considered deleted and excluded from all normal queries |
| `deleted_at` | `TIMESTAMPTZ` | Yes | — | Timestamp when the vehicle was soft-deleted; `NULL` for active records |

> **Note on default values:** The column defaults (`'system'` for `created_by` and `updated_by`) are migration-time defaults only. The application layer must always supply the acting user's email address at runtime. The defaults exist solely to satisfy the `NOT NULL` constraint for any pre-existing rows at the time of migration.

---

### V3\_\_create\_vehicle\_status\_history.sql

**Purpose:** Creates the `vehicle_status_history` table — the single, canonical, append-only audit log for all vehicle lifecycle status transitions. Every status change recorded by the system is persisted here. No other table should duplicate this purpose (see the [canonical ownership notice](./database-design-car-management-lifecycle.md#vehicle_status_history) in the DB design).

> This migration must run **after** `V2__alter_vehicles_add_lifecycle_audit_columns.sql`.

```sql
-- Migration: V3__create_vehicle_status_history.sql
-- Feature: FR-2 Vehicle Lifecycle Management
-- Description: Creates the vehicle_status_history table for immutable
--              lifecycle status transition audit records.

CREATE TABLE vehicle_status_history (
    id                   UUID         NOT NULL DEFAULT gen_random_uuid(),
    vehicle_id           UUID         NOT NULL,
    previous_status      TEXT         NOT NULL,
    new_status           TEXT         NOT NULL,
    changed_by_user_id   UUID         NOT NULL,
    changed_at           TIMESTAMPTZ  NOT NULL,
    notes                TEXT,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at           TIMESTAMPTZ,
    created_by           TEXT         NOT NULL,
    updated_by           TEXT         NOT NULL,
    deleted              BOOLEAN      NOT NULL DEFAULT false,

    CONSTRAINT vehicle_status_history_pkey
        PRIMARY KEY (id),

    CONSTRAINT fk_vsh_vehicle_id
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(id),

    CONSTRAINT fk_vsh_changed_by_user_id
        FOREIGN KEY (changed_by_user_id)
        REFERENCES users(id),

    CONSTRAINT chk_vsh_previous_status
        CHECK (previous_status IN ('Incoming', 'Active', 'Maintenance', 'Decommissioning', 'Sold')),

    CONSTRAINT chk_vsh_new_status
        CHECK (new_status IN ('Incoming', 'Active', 'Maintenance', 'Decommissioning', 'Sold'))
);

-- Index: vehicle_id (foreign key; supports joins and per-vehicle history retrieval)
CREATE INDEX idx_vsh_vehicle_id
    ON vehicle_status_history (vehicle_id);

-- Index: changed_by_user_id (foreign key; supports joins when auditing by user)
CREATE INDEX idx_vsh_changed_by_user_id
    ON vehicle_status_history (changed_by_user_id);

-- Index: changed_at (supports ordered retrieval of history and time-range queries)
CREATE INDEX idx_vsh_changed_at
    ON vehicle_status_history (changed_at);
```

**Column descriptions:**

| Column | Data Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `UUID` | No | `gen_random_uuid()` | System-generated unique identifier for the history record |
| `vehicle_id` | `UUID` | No | — | Foreign key referencing the vehicle whose status changed |
| `previous_status` | `TEXT` | No | — | Lifecycle status before the transition. Allowed values: `Incoming`, `Active`, `Maintenance`, `Decommissioning`, `Sold` |
| `new_status` | `TEXT` | No | — | Lifecycle status after the transition. Allowed values: `Incoming`, `Active`, `Maintenance`, `Decommissioning`, `Sold` |
| `changed_by_user_id` | `UUID` | No | — | Foreign key referencing the user (fleet manager) who performed the transition; derived from the JWT `sub` claim |
| `changed_at` | `TIMESTAMPTZ` | No | — | Timestamp when the status transition occurred |
| `notes` | `TEXT` | Yes | — | Optional free-text reason for the status change provided by the fleet manager |
| `created_at` | `TIMESTAMPTZ` | No | `NOW()` | Record creation timestamp; set once at insert |
| `updated_at` | `TIMESTAMPTZ` | No | `NOW()` | Record last-update timestamp; updated on every write |
| `deleted_at` | `TIMESTAMPTZ` | Yes | — | Soft-delete timestamp; `NULL` for non-deleted records |
| `created_by` | `TEXT` | No | — | Email address of the user who created the record (from JWT `email` claim) |
| `updated_by` | `TEXT` | No | — | Email address of the user who last updated the record |
| `deleted` | `BOOLEAN` | No | `false` | Soft-delete flag |

---

## Index Summary

| Table | Index Name | Columns | Unique? | Purpose |
|---|---|---|---|---|
| `vehicle_status_history` | `vehicle_status_history_pkey` | `id` | Yes | Primary key lookup |
| `vehicle_status_history` | `idx_vsh_vehicle_id` | `vehicle_id` | No | Per-vehicle history retrieval; foreign key join support |
| `vehicle_status_history` | `idx_vsh_changed_by_user_id` | `changed_by_user_id` | No | Audit queries by acting user; foreign key join support |
| `vehicle_status_history` | `idx_vsh_changed_at` | `changed_at` | No | Ordered history retrieval; time-range queries |

---

## Design Notes

| # | Note |
|---|---|
| DN-01 | **`vehicle_status_history` is canonical.** Per the lifecycle TRD and its [canonical ownership notice](./database-design-car-management-lifecycle.md), the `vehicle_status_history` table is the single authoritative store for lifecycle status transition history. FR-4 (Vehicle Retirement) and any other feature that causes a status transition must write to this table and must not define a separate duplicate history table. |
| DN-02 | **Append-only enforcement.** Records in `vehicle_status_history` must never be updated or deleted by application logic. Immutability is enforced at the application layer (the repository / service must not expose update or delete operations for this table). |
| DN-03 | **Status value consistency.** The `previous_status` and `new_status` columns use `TEXT` with `CHECK` constraints aligned with the allowed values defined in the lifecycle TRD. Note that the `vehicles.lifecycle_status` column (defined in the Vehicle Onboarding TRD) uses a PostgreSQL enum type with uppercase values (`INCOMING`, `ACTIVE`, etc.). If a unified enum type is preferred across both tables, the implementing team should align the `vehicle_status_history` constraints to reference the same enum type and update the lifecycle TRD accordingly before implementation. |
| DN-04 | **`changed_by_user_id` must come from the JWT.** The value stored in `vehicle_status_history.changed_by_user_id` must always be derived from the authenticated user's JWT `sub` claim. It must never be accepted as a client-supplied value in the request body. |
| DN-05 | **Soft-delete columns on `vehicle_status_history`.** While `vehicle_status_history` records are logically append-only, the soft-delete columns (`deleted`, `deleted_at`) are included to maintain schema consistency with other tables in the system. These columns must not be used for any purpose other than exceptional administrative corrections, subject to separate approval. |
| DN-06 | **Migration ordering.** `V2` (alter vehicles) must be applied before `V3` (create vehicle_status_history) because `vehicle_status_history` carries a foreign key on `vehicles`. Both must be applied after `V1` (create vehicles, from FR-1). |
