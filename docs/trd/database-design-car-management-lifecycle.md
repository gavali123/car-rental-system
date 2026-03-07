# Database Design - Car Management: Vehicle Lifecycle Management

## Table of Contents

1. [Entity Relationship Diagram](#entity-relationship-diagram)
2. [Tables](#tables)
   - [vehicles](#vehicles)
   - [vehicle_lifecycle_histories](#vehicle_lifecycle_histories)

---

## Entity Relationship Diagram

```mermaid
erDiagram
    vehicles ||--o{ vehicle_lifecycle_histories : "has"
```

---

## Tables

### vehicles

Stores each vehicle registered in the rental fleet. The `lifecycle_status` field records the vehicle's current operational state as defined in FR-2.

| Field | Data Type | Index | Constraints | Description |
|---|---|---|---|---|
| id | UUID | Primary Key | NOT NULL | Unique identifier for the vehicle |
| lifecycle_status | TEXT | Index | NOT NULL, DEFAULT 'Incoming' | Current lifecycle status of the vehicle. Allowed values: `Incoming`, `Active`, `Maintenance`, `Decommissioning`, `Sold` |
| created_at | TIMESTAMP WITH TIME ZONE | | NOT NULL | Record creation timestamp |
| updated_at | TIMESTAMP WITH TIME ZONE | | NOT NULL | Record last update timestamp |
| deleted_at | TIMESTAMP WITH TIME ZONE | | | Soft delete timestamp |
| created_by | TEXT | | NOT NULL | Username of the user who created the record |
| updated_by | TEXT | | NOT NULL | Username of the user who last updated the record |
| deleted | BOOLEAN | | NOT NULL, DEFAULT false | Soft delete flag |

> **Note:** The `vehicles` table contains additional fields defined in FR-1 (Vehicle Onboarding). Only the fields relevant to lifecycle management are listed here.

---

### vehicle_lifecycle_histories

Stores an immutable, ordered audit trail of every lifecycle status change for each vehicle. A new row is inserted on each status transition.

| Field | Data Type | Index | Constraints | Description |
|---|---|---|---|---|
| id | UUID | Primary Key | NOT NULL | Unique identifier for the history record |
| vehicle_id | UUID | Index (Foreign Key) | NOT NULL, REFERENCES vehicles(id) | The vehicle whose status changed |
| previous_status | TEXT | | NOT NULL | Lifecycle status before the transition. Allowed values: `Incoming`, `Active`, `Maintenance`, `Decommissioning`, `Sold` |
| new_status | TEXT | | NOT NULL | Lifecycle status after the transition. Allowed values: `Incoming`, `Active`, `Maintenance`, `Decommissioning`, `Sold` |
| changed_at | TIMESTAMP WITH TIME ZONE | Index | NOT NULL | Timestamp when the transition occurred |
| changed_by | TEXT | | NOT NULL | Username of the fleet manager who performed the transition |
| notes | TEXT | | | Optional free-text note explaining the reason for the transition |
| created_at | TIMESTAMP WITH TIME ZONE | | NOT NULL | Record creation timestamp |
| updated_at | TIMESTAMP WITH TIME ZONE | | NOT NULL | Record last update timestamp |
| deleted_at | TIMESTAMP WITH TIME ZONE | | | Soft delete timestamp |
| created_by | TEXT | | NOT NULL | Username of the user who created the record |
| updated_by | TEXT | | NOT NULL | Username of the user who last updated the record |
| deleted | BOOLEAN | | NOT NULL, DEFAULT false | Soft delete flag |
