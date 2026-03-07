# TRD - Car Management: Home Location Assignment

## Document Information

| Field | Details |
|---|---|
| **Feature Name** | Car Management – Home Location Assignment |
| **PRD Reference** | [FR-5: Home Location Assignment](../prd/prd-car-management.md#fr-5-home-location-assignment) |
| **Author** | @copilot |
| **Date** | 2026-03-07 |
| **Version** | 1.0 |

---

## Table of Contents

1. [Overview](#overview)
2. [Scope](#scope)
3. [Data Model](#data-model)
   - [Location Entity](#location-entity)
   - [Vehicle Entity (Home Location Relationship)](#vehicle-entity-home-location-relationship)
   - [Entity-Relationship Summary](#entity-relationship-summary)
4. [Business Rules](#business-rules)
5. [API Design](#api-design)
   - [List Locations](#1-list-locations)
   - [Get Vehicle Home Location](#2-get-vehicle-home-location)
   - [Assign Home Location During Onboarding](#3-assign-home-location-during-onboarding)
   - [List Vehicles by Home Location](#4-list-vehicles-by-home-location)
6. [UI/UX Flow](#uiux-flow)
   - [Vehicle Onboarding Form](#vehicle-onboarding-form)
   - [Vehicle Record – Location Section](#vehicle-record--location-section)
   - [Fleet Inventory – Location Filter](#fleet-inventory--location-filter)
7. [Integration Points](#integration-points)
8. [Non-Functional Requirements](#non-functional-requirements)
9. [Security & Access Control](#security--access-control)
10. [Open Questions](#open-questions)

---

## Overview

This document describes the technical design for **FR-5: Home Location Assignment** from the Car Management PRD. The feature ensures that every vehicle in the rental fleet is assigned to exactly one home location at all times. The home location is captured during vehicle onboarding and is displayed prominently on the vehicle record. Fleet managers can filter the vehicle inventory by home location to gain per-location fleet visibility.

This TRD covers only the assignment and display of the home location. Changes to the home location after onboarding (i.e., location transfers) are handled by FR-6 and documented in a separate TRD.

---

## Scope

### In Scope

- Definition of the `Location` data entity and its attributes.
- The mandatory home location selection step within the vehicle onboarding flow (FR-1).
- Display of the current home location on the vehicle record detail view.
- Filtering the fleet inventory list by home location.
- API endpoints supporting the above capabilities.

### Out of Scope

- Location transfers (changing the home location after onboarding) — covered by FR-6.
- Location transfer cost logging and history — covered by FR-6.
- GPS or physical location tracking of vehicles in transit — covered by FR-9.
- Creating, editing, or deactivating location records (Location management is an administrative operation handled separately).

---

## Data Model

### Location Entity

The `Location` entity represents a physical rental base from which vehicles are dispatched and returned. It is a system-managed reference table maintained by administrators.

| Attribute | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | Primary Key, Not Null | Unique system-generated identifier for the location. |
| `name` | String (100) | Not Null, Unique | Human-readable name for the location (e.g., "Downtown Branch", "Airport Hub"). |
| `address_line_1` | String (255) | Not Null | First line of the physical street address. |
| `address_line_2` | String (255) | Nullable | Second line of the address (unit, floor, etc.). |
| `city` | String (100) | Not Null | City where the location is based. |
| `country` | String (100) | Not Null | Country where the location is based. |
| `postal_code` | String (20) | Not Null | Postal or ZIP code. |
| `phone_number` | String (30) | Nullable | Contact phone number for the location. |
| `is_active` | Boolean | Not Null, Default: `true` | Indicates whether the location is currently operational. Only active locations may be assigned as a home location. |
| `created_at` | Timestamp | Not Null, System-set | Date and time the location record was created. |
| `updated_at` | Timestamp | Not Null, System-set | Date and time the location record was last modified. |

---

### Vehicle Entity (Home Location Relationship)

The `Vehicle` entity (defined fully in FR-1) is extended with a mandatory foreign key reference to the `Location` entity.

| Attribute | Type | Constraints | Description |
|---|---|---|---|
| `home_location_id` | UUID | Foreign Key → `Location.id`, Not Null | The location to which this vehicle is currently assigned as its home base. Set at onboarding and updated only via a transfer (FR-6). |

> **Note:** The `home_location_id` is a non-nullable attribute on the `Vehicle` entity. A vehicle record cannot be saved without a valid home location.

---

### Entity-Relationship Summary

```
Location (1) ──────< (many) Vehicle
  └── id (PK)              └── home_location_id (FK, Not Null)
  └── name
  └── is_active
  └── ...
```

- One `Location` can be the home base for zero or more vehicles.
- One `Vehicle` must have exactly one home location at any given time.
- The relationship is mandatory on the `Vehicle` side (i.e., `home_location_id` cannot be null).

---

## Business Rules

| Rule ID | Rule Description |
|---|---|
| **BR-FR5-01** | A vehicle must be assigned a home location as part of the onboarding process. The onboarding form cannot be submitted without selecting a home location. |
| **BR-FR5-02** | Only locations where `is_active = true` are available for selection during onboarding. Inactive locations must not appear in the home location dropdown. |
| **BR-FR5-03** | A vehicle's home location cannot be set to `null` at any point in its lifecycle. The field is always populated. |
| **BR-FR5-04** | Once set during onboarding, the home location can only be changed through the Location Transfer process (FR-6). It cannot be edited directly on the vehicle record. |
| **BR-FR5-05** | The vehicle inventory list must support filtering by home location. When a location filter is applied, only vehicles whose `home_location_id` matches the selected location are returned. |
| **BR-FR5-06** | The current home location name and address must be displayed on the vehicle record detail view within the dedicated "Location" section. |

---

## API Design

All endpoints are served under the base path `/api/v1`. Request and response bodies use JSON. All endpoints require a valid authenticated session.

---

### 1. List Locations

Retrieves all active rental locations for use in dropdowns and filter controls.

| Property | Value |
|---|---|
| **Method** | `GET` |
| **Path** | `/locations` |
| **Query Parameters** | `active_only` (boolean, default: `true`) — when `true`, returns only locations where `is_active = true` |
| **Access** | All authenticated users |

**Response – Success (200 OK)**

Returns a list of location objects. Each object includes:

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Unique location identifier. |
| `name` | String | Display name of the location. |
| `city` | String | City of the location. |
| `country` | String | Country of the location. |
| `is_active` | Boolean | Whether the location is currently active. |

---

### 2. Get Vehicle Home Location

Retrieves the current home location details for a specific vehicle.

| Property | Value |
|---|---|
| **Method** | `GET` |
| **Path** | `/vehicles/{vehicle_id}/home-location` |
| **Path Parameters** | `vehicle_id` — the UUID of the vehicle |
| **Access** | Fleet Manager, Operations Manager (read) |

**Response – Success (200 OK)**

Returns the full location object for the vehicle's current home location, including:

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Location identifier. |
| `name` | String | Display name. |
| `address_line_1` | String | Street address line 1. |
| `address_line_2` | String \| null | Street address line 2, if present. |
| `city` | String | City. |
| `country` | String | Country. |
| `postal_code` | String | Postal code. |
| `phone_number` | String \| null | Location contact number, if present. |

**Response – Not Found (404)**

Returned when the `vehicle_id` does not correspond to any vehicle in the system.

---

### 3. Assign Home Location During Onboarding

The home location is set as part of the vehicle onboarding request (FR-1: `POST /vehicles`). It is not a separate endpoint. The vehicle creation request body must include `home_location_id`.

**Vehicle creation request body extension:**

| Field | Type | Required | Description |
|---|---|---|---|
| `home_location_id` | UUID | **Yes** | The ID of the active location to assign as the vehicle's home base. |

**Validation:**
- If `home_location_id` is absent or null, the request is rejected with `400 Bad Request` and a descriptive error message.
- If `home_location_id` references a location that does not exist or is inactive, the request is rejected with `422 Unprocessable Entity`.

---

### 4. List Vehicles by Home Location

Retrieves vehicles filtered by a specific home location. This is an extension of the fleet inventory list endpoint.

| Property | Value |
|---|---|
| **Method** | `GET` |
| **Path** | `/vehicles` |
| **Query Parameters** | `home_location_id` (UUID, optional) — when provided, returns only vehicles assigned to that location |
| **Additional Filters** | Supports combining with existing vehicle filters (status, type, class, etc.) |
| **Access** | Fleet Manager, Operations Manager |

**Response – Success (200 OK)**

Returns a paginated list of vehicle objects matching the filter criteria. Each vehicle object includes the `home_location_id` and a nested `home_location` summary object with `id` and `name`.

---

## UI/UX Flow

### Vehicle Onboarding Form

1. The fleet manager navigates to the vehicle registration form.
2. The form includes a **"Home Location"** field, rendered as a required dropdown.
3. The dropdown is populated by calling `GET /locations?active_only=true`.
4. The dropdown displays each location's `name` and `city` for clarity.
5. The "Home Location" field is marked as mandatory. The form's submit action is disabled until a location is selected.
6. On form submission, the selected location's `id` is included as `home_location_id` in the vehicle creation request.
7. If the server rejects the location (inactive or not found), the form displays an inline error beneath the field: *"The selected location is no longer available. Please choose another."*

---

### Vehicle Record – Location Section

1. The fleet manager opens a vehicle's detail page.
2. A dedicated **"Location"** section is displayed, showing:
   - **Home Location Name** (e.g., "Downtown Branch")
   - **Full Address** (address lines, city, postal code, country)
   - **Contact Number** (if available)
3. The location data is retrieved via `GET /vehicles/{vehicle_id}/home-location`.
4. The "Location" section does not include an inline edit control. Changes to the home location are only possible through the Location Transfer workflow (FR-6), which is accessed via a separate "Transfer Vehicle" action.

---

### Fleet Inventory – Location Filter

1. The fleet manager opens the fleet inventory list view.
2. A **"Filter by Location"** control is displayed, rendered as a dropdown populated by `GET /locations?active_only=true`.
3. Selecting a location calls `GET /vehicles?home_location_id={id}`.
4. The inventory table refreshes to show only vehicles assigned to the selected location.
5. The active location filter is shown as a visible label above the table (e.g., *"Showing: Downtown Branch"*) with a clear/reset option.
6. When no filter is applied, vehicles from all locations are listed.

---

## Integration Points

| Related FR | Dependency Description |
|---|---|
| **FR-1: Vehicle Onboarding** | The home location (`home_location_id`) is a required field in the vehicle onboarding request. The onboarding workflow must surface the location selection field and enforce its presence before saving the vehicle record. |
| **FR-6: Location Transfer** | The Location Transfer process is the only mechanism by which `home_location_id` on the vehicle can be updated after onboarding. FR-6 reads the current `home_location_id` as the "previous location" and writes a new value as the "new location". FR-6 also writes a transfer history record. |
| **FR-7: Real-Time Vehicle Availability** | The availability check for real-time dispatch requires that a vehicle is "at its home location". The `home_location_id` is used to scope availability queries to the correct branch. |
| **FR-8: Planned Vehicle Availability** | Similar to FR-7, planned availability views are scoped per home location. The `home_location_id` is used to group planned availability by branch. |
| **FR-24: Utilization Dashboard** | The utilization dashboard groups fleet metrics per location using `home_location_id`. The per-location filter on the dashboard calls the vehicle list endpoint with the `home_location_id` query parameter. |
| **FR-25: Consecutive Idle Alert** | Idle alerts include the vehicle's current home location in the alert payload, sourced from `home_location_id`. |

---

## Non-Functional Requirements

| ID | Category | Requirement |
|---|---|---|
| **NFR-FR5-01** | Data Integrity | The `home_location_id` foreign key on the `Vehicle` entity must be enforced at the database level. Any attempt to assign a non-existent location must be rejected by the persistence layer, not only by application validation. |
| **NFR-FR5-02** | Data Integrity | `home_location_id` must be declared `NOT NULL` on the `Vehicle` table. No vehicle record may exist without a valid home location reference. |
| **NFR-FR5-03** | Performance | The `GET /vehicles?home_location_id={id}` query must complete within 500 ms for a fleet size of up to 10,000 vehicles. An index on `Vehicle.home_location_id` is required. |
| **NFR-FR5-04** | Performance | The `GET /locations` endpoint must return within 200 ms. Location data is relatively static and may be cached at the application layer with a TTL of 5 minutes. |
| **NFR-FR5-05** | Availability | Location reference data must be loaded and available before the vehicle onboarding form can be presented. If the location list fails to load, the form must display an error and prevent submission. |
| **NFR-FR5-06** | Auditability | Any write operation involving `home_location_id` (set during onboarding) must be recorded in the vehicle's audit log with the acting user's identity and a timestamp. |

---

## Security & Access Control

| Action | Permitted Roles |
|---|---|
| View list of active locations | All authenticated users |
| View vehicle home location (on vehicle record) | Fleet Manager, Operations Manager |
| Set home location during vehicle onboarding | Fleet Manager |
| Filter fleet inventory by home location | Fleet Manager, Operations Manager |
| Modify home location directly (bypassing transfer workflow) | **Not permitted for any role.** Changes must go through the FR-6 Location Transfer process. |

- All API requests must carry a valid authentication token. Unauthenticated requests must receive a `401 Unauthorized` response.
- Role-based access control (RBAC) must be enforced at the API gateway or service layer. Requests by users without the required role must receive a `403 Forbidden` response.

---

## Open Questions

| # | Question | Owner | Status |
|---|---|---|---|
| 1 | What is the maximum number of active locations expected at launch and in year 1? This affects caching strategy and dropdown UX design decisions. | Product / Business | Open |
| 2 | Should inactive locations still be visible (read-only) on existing vehicle records where they were previously assigned? Or should the UI substitute a placeholder (e.g., *"[Inactive] Old Location Name"*)? | Product | Open |
| 3 | Is there a need to support a vehicle being temporarily "unassigned" from a home location (e.g., while in transit between locations)? The current FR-5 design does not allow a null home location. | Product | Open |
| 4 | What should happen to a vehicle's `home_location_id` if the assigned location is deactivated? Should the system block deactivation when vehicles are still assigned, or trigger a forced transfer? | Product / Engineering | Open |
