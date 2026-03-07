# TRD - Car Management: FR-4 Vehicle Retirement

## Document Information

| Field | Details |
|---|---|
| **Feature Name** | Vehicle Retirement |
| **Parent PRD** | [PRD - Car Management, FR-4](../prd/prd-car-management.md#fr-4-vehicle-retirement) |
| **Author** | @copilot |
| **Date** | 2026-03-07 |
| **Version** | 1.0 |

---

## Table of Contents

1. [Overview](#overview)
2. [Scope](#scope)
3. [Actors & Roles](#actors--roles)
4. [Business Rules](#business-rules)
5. [Data Model](#data-model)
   - [Vehicle Table (relevant fields)](#vehicle-table-relevant-fields)
   - [Vehicle Status History Table](#vehicle-status-history-table)
   - [Retirement Record Table](#retirement-record-table)
   - [Lifecycle Status Enum](#lifecycle-status-enum)
   - [Entity Relationships](#entity-relationships)
6. [Functional Flows](#functional-flows)
   - [Flow 1: Decommission a Vehicle](#flow-1-decommission-a-vehicle)
   - [Flow 2: Mark a Decommissioned Vehicle as Sold](#flow-2-mark-a-decommissioned-vehicle-as-sold)
7. [API Design](#api-design)
   - [PATCH /vehicles/{vehicleId}/status](#patch-vehiclesvehicleidstatus)
   - [GET /vehicles/{vehicleId}/retirement](#get-vehiclesvehicleidretirement)
   - [GET /vehicles/{vehicleId}/reservations/conflicts](#get-vehiclesvehicleidreservationsconflicts)
8. [UI / UX Requirements](#ui--ux-requirements)
   - [Vehicle Detail Page — Status Panel](#vehicle-detail-page--status-panel)
   - [Conflicting Reservations Warning Dialog](#conflicting-reservations-warning-dialog)
   - [Sold Confirmation Dialog](#sold-confirmation-dialog)
9. [Validation Rules](#validation-rules)
10. [Error Handling](#error-handling)
11. [Non-Functional Requirements](#non-functional-requirements)
12. [Dependencies](#dependencies)
13. [Open Questions](#open-questions)

---

## Overview

Vehicle Retirement defines the two-step process by which a rental vehicle is permanently removed from active fleet operations:

1. **Decommissioning** — the fleet manager sets the vehicle's lifecycle status to `Decommissioning`. From this point forward, the vehicle is excluded from all new reservation allocations.
2. **Sold** — once all physical disposal formalities are complete, the fleet manager sets the status to `Sold`. `Sold` is a terminal, immutable state; no further status changes are permitted.

Both transitions are manual actions initiated by the fleet manager. No automated rule or scheduled job triggers retirement.

---

## Scope

**In scope for this TRD:**

- Transitioning a vehicle from any eligible source status to `Decommissioning`.
- Transitioning a vehicle from `Decommissioning` to `Sold`.
- Surfacing a warning when conflicting active or upcoming reservations exist before allowing the `Decommissioning` transition.
- Persisting a timestamped audit trail of every status change.
- Capturing final disposal details when a vehicle is marked `Sold`.
- Excluding `Decommissioning` and `Sold` vehicles from reservation allocation.
- Preventing any further status change once a vehicle is `Sold`.

**Out of scope:**

- Automated triggering of retirement based on mileage, age, or maintenance thresholds.
- Financial settlement of asset value (handled by the Accounting module).
- Notifying external parties (e.g., insurance provider, registration authority) — to be defined in a future integration TRD.

---

## Actors & Roles

| Actor | Permissions |
|---|---|
| **Fleet Manager** | Can initiate both the `Decommissioning` and `Sold` transitions; views retirement records. |
| **Operations Manager** | Read-only access to vehicle status and retirement records; cannot initiate transitions. |
| **System** | Enforces business rules (conflict check, terminal-state guard, timestamp recording). |

---

## Business Rules

| ID | Rule |
|---|---|
| BR-01 | Only a Fleet Manager may initiate a retirement status transition. |
| BR-02 | A vehicle may be set to `Decommissioning` only from an eligible source status (any status except `Sold`). |
| BR-03 | A vehicle may be set to `Sold` only from `Decommissioning`. |
| BR-04 | `Sold` is a terminal state. No subsequent status change is permitted on a `Sold` vehicle. |
| BR-05 | Before setting a vehicle to `Decommissioning`, the system must check for active or upcoming reservations associated with that vehicle. |
| BR-06 | When conflicting reservations exist, the system must display a warning listing each conflicting reservation. The fleet manager may still proceed after acknowledging the warning. |
| BR-07 | A vehicle in `Decommissioning` status must not be included in any new automatic or manual reservation allocation. |
| BR-08 | Every status transition must be recorded with: the actor's user ID, the previous status, the new status, and a UTC timestamp. |
| BR-09 | When a vehicle is marked `Sold`, the fleet manager must supply the disposal date and may optionally supply a sale price and buyer name. |
| BR-10 | The disposal date for a `Sold` vehicle must not be earlier than the date of the `Decommissioning` transition. |

---

## Data Model

### Vehicle Table (relevant fields)

The following fields on the `vehicles` table are directly relevant to Vehicle Retirement. Full table definition is owned by the Vehicle Onboarding TRD.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | Primary Key | Unique vehicle identifier. |
| `lifecycle_status` | ENUM | Not Null | Current lifecycle status of the vehicle. See [Lifecycle Status Enum](#lifecycle-status-enum). |

### Vehicle Status History Table

Stores a complete, immutable audit log of every lifecycle status change across all vehicles.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | Primary Key | Unique record identifier. |
| `vehicle_id` | UUID | Not Null, Foreign Key → `vehicles.id` | The vehicle whose status changed. |
| `previous_status` | ENUM | Not Null | Lifecycle status before the change. |
| `new_status` | ENUM | Not Null | Lifecycle status after the change. |
| `changed_by_user_id` | UUID | Not Null, Foreign Key → `users.id` | The user who performed the change. |
| `changed_at` | TIMESTAMP (UTC) | Not Null, Default: now() | When the change occurred. |
| `notes` | TEXT | Nullable | Optional free-text note provided at the time of transition. |

**Constraints:**
- Records in this table are append-only; no updates or deletes are permitted.

### Retirement Record Table

Stores disposal-specific details captured when a vehicle is set to `Sold`. One record per vehicle; a vehicle may have at most one retirement record.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | Primary Key | Unique record identifier. |
| `vehicle_id` | UUID | Not Null, Unique, Foreign Key → `vehicles.id` | The retired vehicle. |
| `disposal_date` | DATE | Not Null | The formal date of disposal. Must be ≥ the `Decommissioning` transition date. |
| `sale_price` | DECIMAL(12,2) | Nullable | Amount received from sale, in the base currency. |
| `buyer_name` | VARCHAR(255) | Nullable | Name of the buyer or disposal party. |
| `recorded_by_user_id` | UUID | Not Null, Foreign Key → `users.id` | The fleet manager who submitted the disposal details. |
| `recorded_at` | TIMESTAMP (UTC) | Not Null, Default: now() | When the retirement record was created. |

### Lifecycle Status Enum

The `lifecycle_status` column on the `vehicles` table uses the following enumeration, as defined in the Car Management PRD (FR-2):

| Value | Description |
|---|---|
| `Incoming` | Vehicle acquired but not yet ready for rental. |
| `Active` | Vehicle available for reservation allocation. |
| `Maintenance` | Vehicle undergoing maintenance; unavailable for rental. |
| `Decommissioning` | Vehicle being phased out; unavailable for new reservations. |
| `Sold` | Vehicle permanently removed from fleet. Terminal state. |

### Entity Relationships

```text
vehicles (1) ──── (0..*) vehicle_status_history
vehicles (1) ──── (0..1) retirement_records
```

---

## Functional Flows

### Flow 1: Decommission a Vehicle

**Trigger:** Fleet manager selects a vehicle and chooses the "Decommission" action.

**Preconditions:**
- The acting user has the Fleet Manager role.
- The vehicle's current `lifecycle_status` is not `Sold`.

**Steps:**

1. Fleet manager opens the vehicle detail page and clicks **Decommission**.
2. The system queries the reservations service for any active or upcoming reservations linked to this vehicle.
   - An "active" reservation is one that has started and not yet ended.
   - An "upcoming" reservation is one with a start date in the future.
3. **If conflicting reservations exist:**
   a. The system displays the [Conflicting Reservations Warning Dialog](#conflicting-reservations-warning-dialog) listing all conflicting reservation IDs, customer names, and date ranges.
   b. The fleet manager must explicitly acknowledge the warning by clicking **Proceed Anyway** or cancel by clicking **Cancel**.
   c. If the manager clicks **Cancel**, the flow terminates with no changes.
4. The system atomically:
   a. Updates `vehicles.lifecycle_status` to `Decommissioning`.
   b. Inserts a record into `vehicle_status_history` with `previous_status`, `new_status = Decommissioning`, `changed_by_user_id`, and `changed_at = now()`.
5. The UI reflects the updated status immediately.

**Postconditions:**
- `vehicles.lifecycle_status = Decommissioning`.
- A status history record exists for this transition.
- The vehicle no longer appears in reservation allocation results.

---

### Flow 2: Mark a Decommissioned Vehicle as Sold

**Trigger:** Fleet manager selects a vehicle in `Decommissioning` status and chooses the "Mark as Sold" action.

**Preconditions:**
- The acting user has the Fleet Manager role.
- The vehicle's current `lifecycle_status` is `Decommissioning`.

**Steps:**

1. Fleet manager opens the vehicle detail page and clicks **Mark as Sold**.
2. The system displays the [Sold Confirmation Dialog](#sold-confirmation-dialog) prompting for:
   - **Disposal date** (required)
   - **Sale price** (optional)
   - **Buyer name** (optional)
3. Fleet manager fills in the required field(s) and clicks **Confirm**.
4. The system validates:
   - Disposal date is not blank.
   - Disposal date is ≥ the date of the `Decommissioning` status transition (retrieved from `vehicle_status_history`).
5. The system atomically:
   a. Updates `vehicles.lifecycle_status` to `Sold`.
   b. Inserts a record into `vehicle_status_history` with `previous_status = Decommissioning`, `new_status = Sold`, `changed_by_user_id`, and `changed_at = now()`.
   c. Inserts a record into `retirement_records` with the provided disposal details.
6. The UI reflects the updated status immediately. All status-change controls on the vehicle detail page are disabled, and a "Sold" badge replaces any action buttons.

**Postconditions:**
- `vehicles.lifecycle_status = Sold`.
- A status history record exists for this transition.
- A `retirement_records` entry exists for this vehicle.
- No further status changes are possible for this vehicle.

---

## API Design

### PATCH /vehicles/{vehicleId}/status

Updates the lifecycle status of a vehicle. Used for both the `Decommissioning` and `Sold` transitions.

**Authorization:** Fleet Manager role required.

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `vehicleId` | UUID | The unique identifier of the vehicle. |

**Request Body:**

```json
{
  "newStatus": "Decommissioning" | "Sold",
  "acknowledgedConflicts": boolean,   // required when newStatus = "Decommissioning"
  "disposalDetails": {                // required when newStatus = "Sold"
    "disposalDate": "YYYY-MM-DD",
    "salePrice": number | null,
    "buyerName": string | null
  },
  "notes": string | null
}
```

**Response — Success (200 OK):**

```json
{
  "vehicleId": "UUID",
  "previousStatus": "string",
  "newStatus": "string",
  "changedAt": "ISO-8601 UTC timestamp"
}
```

**Response — Conflict Warning (409 Conflict):**

Returned when `newStatus = "Decommissioning"` and `acknowledgedConflicts = false` (or omitted) but conflicting reservations exist.

```json
{
  "error": "CONFLICTING_RESERVATIONS",
  "message": "Vehicle has active or upcoming reservations.",
  "conflicts": [
    {
      "reservationId": "UUID",
      "customerName": "string",
      "startDate": "YYYY-MM-DD",
      "endDate": "YYYY-MM-DD"
    }
  ]
}
```

**Response — Forbidden (403 Forbidden):**

```json
{
  "error": "FORBIDDEN",
  "message": "Only Fleet Managers may perform status transitions."
}
```

**Response — Validation Error (422 Unprocessable Entity):**

```json
{
  "error": "VALIDATION_ERROR",
  "message": "string",
  "fields": ["string"]
}
```

---

### GET /vehicles/{vehicleId}/retirement

Returns the retirement record for a vehicle that has been marked `Sold`.

**Authorization:** Fleet Manager or Operations Manager role required.

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `vehicleId` | UUID | The unique identifier of the vehicle. |

**Response — Success (200 OK):**

```json
{
  "vehicleId": "UUID",
  "disposalDate": "YYYY-MM-DD",
  "salePrice": number | null,
  "buyerName": string | null,
  "recordedByUserId": "UUID",
  "recordedAt": "ISO-8601 UTC timestamp"
}
```

**Response — Not Found (404 Not Found):**

Returned when the vehicle has no retirement record (i.e., is not yet `Sold`).

```json
{
  "error": "NOT_FOUND",
  "message": "No retirement record found for this vehicle."
}
```

---

### GET /vehicles/{vehicleId}/reservations/conflicts

Returns a list of active or upcoming reservations for a vehicle. Used by the UI to pre-check for conflicts before showing the decommission action.

**Authorization:** Fleet Manager role required.

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `vehicleId` | UUID | The unique identifier of the vehicle. |

**Response — Success (200 OK):**

```json
{
  "vehicleId": "UUID",
  "conflicts": [
    {
      "reservationId": "UUID",
      "customerName": "string",
      "startDate": "YYYY-MM-DD",
      "endDate": "YYYY-MM-DD"
    }
  ]
}
```

An empty `conflicts` array indicates no conflicting reservations.

---

## UI / UX Requirements

### Vehicle Detail Page — Status Panel

- The current lifecycle status is displayed prominently (e.g., as a labelled badge).
- The available action button(s) shown depend on the current status:

  | Current Status | Available Actions |
  |---|---|
  | `Incoming` | Decommission |
  | `Active` | Decommission |
  | `Maintenance` | Decommission |
  | `Decommissioning` | Mark as Sold |
  | `Sold` | *(none — all retirement actions are disabled)* |

- When the vehicle status is `Sold`, a read-only **Retirement Details** section is displayed showing: disposal date, sale price (if provided), buyer name (if provided), and the date/time the record was created.

### Conflicting Reservations Warning Dialog

Displayed when the fleet manager attempts to set a vehicle to `Decommissioning` and one or more active or upcoming reservations exist.

**Content:**
- Title: "Active or Upcoming Reservations Exist"
- Explanatory text: "This vehicle has the following reservations that will be affected. You may still proceed, but you should coordinate with the reservations team to reassign or cancel these bookings."
- A table listing each conflicting reservation: Reservation ID, Customer Name, Start Date, End Date.
- Two buttons: **Proceed Anyway** (destructive action, styled prominently) and **Cancel**.

**Behaviour:**
- If the fleet manager clicks **Proceed Anyway**, the status transition proceeds (Flow 1, Step 4).
- If the fleet manager clicks **Cancel**, the dialog closes and no change is made.

### Sold Confirmation Dialog

Displayed when the fleet manager clicks **Mark as Sold** on a vehicle in `Decommissioning` status.

**Content:**
- Title: "Confirm Vehicle Disposal"
- **Disposal Date** field (required, date picker, defaults to today).
- **Sale Price** field (optional, numeric, currency label).
- **Buyer Name** field (optional, free text).
- Two buttons: **Confirm** and **Cancel**.

**Behaviour:**
- The **Confirm** button is disabled until a valid disposal date is entered.
- If the fleet manager clicks **Confirm**, Flow 2 Step 4 onward executes.
- If the fleet manager clicks **Cancel**, the dialog closes and no change is made.

---

## Validation Rules

| Field | Rule |
|---|---|
| `newStatus` | Must be `Decommissioning` or `Sold`. Any other value is rejected with a 422 error. |
| `newStatus = "Sold"` | Only allowed if the vehicle's current status is `Decommissioning`; otherwise rejected with a 422 error. |
| `newStatus = "Decommissioning"` | Not allowed if the vehicle's current status is `Sold`; otherwise allowed from any other status. |
| `disposalDetails.disposalDate` | Required when `newStatus = "Sold"`. Must be a valid date and must be ≥ the date of the `Decommissioning` transition. |
| `disposalDetails.salePrice` | Optional. If provided, must be a non-negative decimal. |
| `disposalDetails.buyerName` | Optional. If provided, must not exceed 255 characters. |
| `acknowledgedConflicts` | When `newStatus = "Decommissioning"` and conflicts exist, the request is rejected with a 409 response unless `acknowledgedConflicts = true`. |

---

## Error Handling

| Scenario | HTTP Status | Error Code | User-Facing Message |
|---|---|---|---|
| User does not have Fleet Manager role | 403 | `FORBIDDEN` | "You do not have permission to perform this action." |
| Vehicle not found | 404 | `NOT_FOUND` | "The specified vehicle could not be found." |
| Attempt to change status of a `Sold` vehicle | 422 | `TERMINAL_STATE` | "This vehicle has been sold and its status can no longer be changed." |
| `Sold` transition attempted from a status other than `Decommissioning` | 422 | `INVALID_TRANSITION` | "A vehicle can only be marked as Sold from Decommissioning status." |
| Disposal date earlier than decommission date | 422 | `INVALID_DISPOSAL_DATE` | "The disposal date cannot be earlier than the decommissioning date." |
| Conflicting reservations exist and not acknowledged | 409 | `CONFLICTING_RESERVATIONS` | *(see API response above; list of conflicts is returned)* |
| Concurrent status update conflict | 409 | `CONCURRENT_MODIFICATION` | "Another update is in progress. Please refresh and try again." |

---

## Non-Functional Requirements

| ID | Category | Requirement |
|---|---|---|
| NFR-01 | Auditability | Every status transition must be durably recorded in `vehicle_status_history` within the same database transaction as the `vehicles` table update. A transition that succeeds must always have a corresponding history record. |
| NFR-02 | Data Integrity | The `Sold` terminal-state constraint must be enforced at the database level (e.g., via a check constraint or trigger) in addition to the application layer, to prevent accidental direct-database updates. |
| NFR-03 | Consistency | The `vehicles.lifecycle_status` update, the `vehicle_status_history` insert, and (where applicable) the `retirement_records` insert must be performed within a single atomic database transaction. |
| NFR-04 | Performance | The conflict check query (active/upcoming reservations for a vehicle) must return results within 500 ms under normal load conditions. |
| NFR-05 | Availability | The retirement endpoints must be available whenever the core Car Management service is available; no separate deployment dependency is introduced. |

---

## Dependencies

| Dependency | Type | Description |
|---|---|---|
| Vehicle Lifecycle Management (FR-2) | Internal — same module | Defines the full set of lifecycle statuses and the `vehicles` table. The `lifecycle_status` column and its enum are owned by FR-2. |
| Reservation Service | Internal — cross-module | Required for the conflict check (Flow 1, Step 2). The retirement flow must query active and upcoming reservations by vehicle ID. |
| User / Auth Service | Internal — cross-module | Required to authenticate and authorise the fleet manager role for status transitions. |
| Accounting Module | Internal — cross-module | Asset write-down or financial settlement triggered by vehicle disposal is handled by the Accounting module. The retirement record (specifically `disposal_date` and `sale_price`) serves as the source event for that process. |

---

## Open Questions

| ID | Question | Owner | Status |
|---|---|---|---|
| OQ-01 | Should the system allow a fleet manager to set a vehicle to `Decommissioning` when it has an *ongoing* (started but not ended) active rental? Or only block on *upcoming* reservations? | Product | Open |
| OQ-02 | Is there a requirement to notify the customer when their reservation is affected by a decommission event? | Product | Open |
| OQ-03 | Should the `Sold` record be immutable, or should the fleet manager be able to correct disposal details (e.g., wrong sale price) after saving? | Product | Open |
| OQ-04 | What access level should read-only users (e.g., Operations Manager) have to the `vehicle_status_history` audit log? Should it be exposed in the UI? | Product | Open |
