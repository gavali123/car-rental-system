# TRD - Car Management: Vehicle Onboarding (FR-1)

## Document Information

| Field | Details |
|---|---|
| **Feature Name** | Vehicle Onboarding |
| **Module** | Car Management |
| **Related PRD** | [PRD - Car Management, FR-1: Vehicle Onboarding](../prd/prd-car-management.md#fr-1-vehicle-onboarding) |
| **Author** | @copilot |
| **Date** | 2026-03-07 |
| **Version** | 1.0 |

---

## Table of Contents

1. [Overview](#overview)
2. [Assumptions & Constraints](#assumptions--constraints)
3. [Data Model](#data-model)
   - [Vehicle Entity](#vehicle-entity)
   - [Vehicle Insurance Entity](#vehicle-insurance-entity)
   - [Entity Relationships](#entity-relationships)
4. [API Design](#api-design)
   - [Register New Vehicle](#register-new-vehicle)
   - [Get Vehicle Details](#get-vehicle-details)
5. [Business Rules & Validation](#business-rules--validation)
   - [Field Validation Rules](#field-validation-rules)
   - [Uniqueness Rules](#uniqueness-rules)
   - [Derived & Computed Fields](#derived--computed-fields)
   - [Initial Lifecycle Status](#initial-lifecycle-status)
6. [Lifecycle Status Design](#lifecycle-status-design)
7. [Integration Points](#integration-points)
8. [Non-Functional Requirements](#non-functional-requirements)
9. [Open Questions & Future Considerations](#open-questions--future-considerations)

---

## Overview

This document defines the technical design for **FR-1: Vehicle Onboarding** as specified in the [Car Management PRD](../prd/prd-car-management.md#fr-1-vehicle-onboarding).

Vehicle Onboarding is the entry point for adding a new asset into the rental fleet. It captures all essential vehicle attributes — identity, physical properties, insurance, and acquisition data — and assigns the vehicle its initial lifecycle status of **Incoming**. All downstream modules (reservation allocation, GPS tracking, maintenance scheduling, availability management) depend on vehicles being correctly registered through this process.

**Scope of this TRD:**
- Data model for persisting vehicle registration information.
- API specification for the vehicle registration endpoint.
- Business rules governing field validation, uniqueness checks, and initial status assignment.
- Integration surface with other modules that depend on vehicle data.

**Out of scope for this TRD:**
- Lifecycle status transitions after onboarding (see FR-2).
- Insurance expiry blocking logic (see FR-3).
- Home location assignment details (see FR-5).
- Vehicle availability computation (see FR-7, FR-8).

---

## Assumptions & Constraints

| # | Assumption / Constraint |
|---|---|
| A1 | Only authenticated users with the **Fleet Manager** role can register new vehicles. |
| A2 | The system uses a relational database. All entities described in this document map to database tables. |
| A3 | All API interactions use REST over HTTPS with JSON payloads. |
| A4 | The interface for vehicle registration is a desktop web form (see PRD dependency: desktop web only). |
| A5 | Vehicle ownership at onboarding is always the company; no third-party or individual owner registrations are supported in this phase. |
| A6 | A vehicle type (brand + model + manufacturing year) may already exist in the system from a prior onboarding; the system reuses existing type records rather than creating duplicates. |
| A7 | The home location field is required at onboarding (see FR-5). The location entity is managed by a separate Location module and is referenced here by its identifier. |
| A8 | Insurance details are captured as a single record at onboarding; subsequent insurance renewals are handled through a separate update operation. |
| A9 | Audit logging (who created the record and when) is handled by a shared infrastructure-level audit mechanism and does not need to be explicitly modelled in the Vehicle entity. |

---

## Data Model

### Vehicle Entity

**Table name:** `vehicles`

| Field | Type | Nullable | Constraints / Notes |
|---|---|---|---|
| `id` | UUID | No | Primary key, system-generated |
| `vin` | VARCHAR(17) | No | Unique; must be exactly 17 alphanumeric characters; immutable after creation |
| `license_plate` | VARCHAR(20) | No | Unique; case-insensitive; immutable after creation |
| `purchase_date` | DATE | No | Must not be a future date |
| `purchase_cost` | DECIMAL(12,2) | No | Must be > 0 |
| `odometer_at_acquisition` | INTEGER | No | In kilometres; must be ≥ 0 |
| `brand` | VARCHAR(100) | No | Vehicle manufacturer name (e.g., Toyota, BMW) |
| `model` | VARCHAR(100) | No | Vehicle model name (e.g., Camry, 3 Series) |
| `manufacturing_year` | SMALLINT | No | Must be between 1900 and current calendar year + 1 |
| `size_type` | ENUM | No | Allowed values: `SMALL`, `MEDIUM` |
| `vehicle_class` | ENUM | No | Allowed values: `ECONOMY`, `LUXURY` |
| `vehicle_category` | ENUM | No | Derived from `size_type` + `vehicle_class`; see [Derived & Computed Fields](#derived--computed-fields); stored for query efficiency |
| `number_of_seats` | SMALLINT | No | Must be > 0; expected values: 4 for `SMALL`, 7 for `MEDIUM` (enforced by business rule, not DB constraint) |
| `fuel_type` | ENUM | No | Allowed values: `GAS`, `ELECTRIC`, `HYBRID` |
| `lifecycle_status` | ENUM | No | Default: `INCOMING`; allowed values: `INCOMING`, `ACTIVE`, `MAINTENANCE`, `DECOMMISSIONING`, `SOLD` |
| `home_location_id` | UUID | No | Foreign key to `locations.id`; assigned at onboarding |
| `created_at` | TIMESTAMP | No | Set by system at record creation; UTC |
| `updated_at` | TIMESTAMP | No | Updated by system on every record change; UTC |

**Indexes:**
- Unique index on `vin`
- Unique index on `license_plate` (case-insensitive collation)
- Index on `lifecycle_status` (supports availability queries)
- Index on `home_location_id` (supports location-based inventory queries)
- Composite index on `vehicle_category`, `lifecycle_status` (supports reservation allocation queries)

---

### Vehicle Insurance Entity

**Table name:** `vehicle_insurance`

Insurance details are stored in a separate entity to allow historical tracking of insurance records (renewals over the vehicle's lifetime).

| Field | Type | Nullable | Constraints / Notes |
|---|---|---|---|
| `id` | UUID | No | Primary key, system-generated |
| `vehicle_id` | UUID | No | Foreign key to `vehicles.id`; cascades on delete |
| `insurer_name` | VARCHAR(200) | No | Name of the insurance company |
| `policy_number` | VARCHAR(100) | No | Unique per insurer; used for verification |
| `coverage_start_date` | DATE | No | Must not be after `coverage_end_date` |
| `coverage_end_date` | DATE | No | Must be after `coverage_start_date` |
| `is_active` | BOOLEAN | No | Default: `TRUE`; only one active record per vehicle at a time |
| `created_at` | TIMESTAMP | No | Set by system at record creation; UTC |

**Indexes:**
- Index on `vehicle_id`
- Index on `coverage_end_date` (supports daily insurance expiry check in FR-3)
- Partial unique index on `vehicle_id` where `is_active = TRUE` (ensures one active insurance record per vehicle)

---

### Entity Relationships

```
vehicles (1) ──── (0..*) vehicle_insurance
   │
   └── home_location_id ──── (1) locations
```

| Relationship | Cardinality | Description |
|---|---|---|
| `vehicles` → `vehicle_insurance` | One-to-Many | A vehicle has one current (active) insurance record. Historical records are retained with `is_active = FALSE`. |
| `vehicles` → `locations` | Many-to-One | A vehicle belongs to one home location at a time. The location record is managed by the Location module. |

---

## API Design

All endpoints require an `Authorization` header with a valid bearer token belonging to a user with the **Fleet Manager** role. Unauthorized requests return HTTP `403 Forbidden`.

### Register New Vehicle

**Endpoint:** `POST /api/v1/vehicles`

**Purpose:** Registers a new vehicle into the rental fleet. On success, assigns the vehicle lifecycle status `INCOMING` and persists the insurance record.

#### Request Body

| Field | Type | Required | Validation |
|---|---|---|---|
| `vin` | String | Yes | Exactly 17 alphanumeric characters; no spaces |
| `licensePlate` | String | Yes | 1–20 characters; must not already exist in the system |
| `purchaseDate` | String (ISO 8601 date) | Yes | Must not be a future date |
| `purchaseCost` | Number | Yes | Must be > 0 |
| `odometerAtAcquisition` | Integer | Yes | Must be ≥ 0 |
| `brand` | String | Yes | 1–100 characters |
| `model` | String | Yes | 1–100 characters |
| `manufacturingYear` | Integer | Yes | Between 1900 and current year + 1 |
| `sizeType` | String (Enum) | Yes | One of: `SMALL`, `MEDIUM` |
| `vehicleClass` | String (Enum) | Yes | One of: `ECONOMY`, `LUXURY` |
| `numberOfSeats` | Integer | Yes | Must be > 0 |
| `fuelType` | String (Enum) | Yes | One of: `GAS`, `ELECTRIC`, `HYBRID` |
| `homeLocationId` | UUID | Yes | Must reference an existing location |
| `insurance.insurerName` | String | Yes | 1–200 characters |
| `insurance.policyNumber` | String | Yes | 1–100 characters |
| `insurance.coverageStartDate` | String (ISO 8601 date) | Yes | Must not be after `coverageEndDate` |
| `insurance.coverageEndDate` | String (ISO 8601 date) | Yes | Must be after `coverageStartDate` |

#### Response: Success

**HTTP Status:** `201 Created`

| Field | Type | Description |
|---|---|---|
| `id` | UUID | System-generated vehicle identifier |
| `vin` | String | Registered VIN |
| `licensePlate` | String | Registered license plate |
| `lifecycleStatus` | String | Always `INCOMING` on successful creation |
| `vehicleCategory` | String | Derived category (e.g., `ECONOMY_SMALL`) |
| `createdAt` | String (ISO 8601 datetime) | Timestamp of registration (UTC) |

#### Response: Errors

| HTTP Status | Error Code | Condition |
|---|---|---|
| `400 Bad Request` | `VALIDATION_ERROR` | One or more required fields are missing or fail validation; response body includes a list of field-level error messages |
| `409 Conflict` | `DUPLICATE_VIN` | A vehicle with the same VIN already exists |
| `409 Conflict` | `DUPLICATE_LICENSE_PLATE` | A vehicle with the same license plate already exists |
| `404 Not Found` | `LOCATION_NOT_FOUND` | The provided `homeLocationId` does not match any existing location |
| `403 Forbidden` | `UNAUTHORIZED` | The caller does not have Fleet Manager privileges |

---

### Get Vehicle Details

**Endpoint:** `GET /api/v1/vehicles/{vehicleId}`

**Purpose:** Retrieves the full details of a registered vehicle, including its current insurance record. Useful for confirming a successful onboarding or viewing the vehicle profile.

#### Path Parameters

| Parameter | Type | Description |
|---|---|---|
| `vehicleId` | UUID | The unique identifier of the vehicle |

#### Response: Success

**HTTP Status:** `200 OK`

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Vehicle identifier |
| `vin` | String | VIN |
| `licensePlate` | String | License plate number |
| `purchaseDate` | String (ISO 8601 date) | Date of acquisition |
| `purchaseCost` | Number | Acquisition cost |
| `odometerAtAcquisition` | Integer | Odometer reading at acquisition (km) |
| `brand` | String | Vehicle manufacturer |
| `model` | String | Vehicle model |
| `manufacturingYear` | Integer | Year of manufacture |
| `sizeType` | String | `SMALL` or `MEDIUM` |
| `vehicleClass` | String | `ECONOMY` or `LUXURY` |
| `vehicleCategory` | String | Derived category |
| `numberOfSeats` | Integer | Total seating capacity |
| `fuelType` | String | `GAS`, `ELECTRIC`, or `HYBRID` |
| `lifecycleStatus` | String | Current lifecycle status |
| `homeLocation.id` | UUID | Home location identifier |
| `homeLocation.name` | String | Home location display name |
| `insurance.id` | UUID | Active insurance record identifier |
| `insurance.insurerName` | String | Insurance company name |
| `insurance.policyNumber` | String | Policy number |
| `insurance.coverageStartDate` | String | Insurance start date |
| `insurance.coverageEndDate` | String | Insurance end date |
| `createdAt` | String (ISO 8601 datetime) | Registration timestamp (UTC) |
| `updatedAt` | String (ISO 8601 datetime) | Last update timestamp (UTC) |

#### Response: Errors

| HTTP Status | Error Code | Condition |
|---|---|---|
| `404 Not Found` | `VEHICLE_NOT_FOUND` | No vehicle exists with the given `vehicleId` |
| `403 Forbidden` | `UNAUTHORIZED` | The caller does not have Fleet Manager privileges |

---

## Business Rules & Validation

### Field Validation Rules

| Rule ID | Field | Rule Description |
|---|---|---|
| BRV-01 | `vin` | Must be exactly 17 alphanumeric characters (letters A–Z, digits 0–9; no special characters or spaces). |
| BRV-02 | `licensePlate` | Must be between 1 and 20 characters. Comparison and storage are case-insensitive (normalized to uppercase). |
| BRV-03 | `purchaseDate` | Must be a valid calendar date that is not in the future (≤ today). |
| BRV-04 | `purchaseCost` | Must be a positive decimal number greater than zero. |
| BRV-05 | `odometerAtAcquisition` | Must be a non-negative integer (zero is valid for new vehicles). |
| BRV-06 | `manufacturingYear` | Must be an integer between 1900 and the current calendar year + 1 (allowing pre-production year). |
| BRV-07 | `sizeType` | Must be one of the enumerated values: `SMALL` or `MEDIUM`. |
| BRV-08 | `vehicleClass` | Must be one of the enumerated values: `ECONOMY` or `LUXURY`. |
| BRV-09 | `numberOfSeats` | Must be a positive integer greater than zero. The system expects 4 seats for `SMALL` and 7 seats for `MEDIUM`; a warning is surfaced in the UI if the value deviates, but the system does not block registration. |
| BRV-10 | `fuelType` | Must be one of the enumerated values: `GAS`, `ELECTRIC`, or `HYBRID`. |
| BRV-11 | `homeLocationId` | Must reference an existing, active location record in the system. |
| BRV-12 | `insurance.coverageStartDate` | Must be a valid calendar date. Must not be after `insurance.coverageEndDate`. |
| BRV-13 | `insurance.coverageEndDate` | Must be a valid calendar date. Must be strictly after `insurance.coverageStartDate`. |

### Uniqueness Rules

| Rule ID | Field(s) | Rule Description |
|---|---|---|
| BRU-01 | `vin` | Must be unique across all vehicles in the system. Duplicate VIN submissions are rejected with error `DUPLICATE_VIN`. |
| BRU-02 | `licensePlate` | Must be unique across all vehicles in the system (case-insensitive). Duplicate submissions are rejected with error `DUPLICATE_LICENSE_PLATE`. |

### Derived & Computed Fields

The `vehicleCategory` field is derived from the combination of `sizeType` and `vehicleClass`. It is computed by the system at the time of registration and stored for query efficiency. Fleet managers do not input this field directly.

| `sizeType` | `vehicleClass` | Derived `vehicleCategory` | Description |
|---|---|---|---|
| `SMALL` | `ECONOMY` | `ECONOMY_SMALL` | 4-seat economy sedan |
| `MEDIUM` | `ECONOMY` | `ECONOMY_MEDIUM` | 7-seat economy MPV / SUV |
| `SMALL` | `LUXURY` | `LUXURY_SMALL` | 4-seat luxury sedan |
| `MEDIUM` | `LUXURY` | `LUXURY_MEDIUM` | Luxury MPV |

### Initial Lifecycle Status

Upon successful vehicle registration, the system **always** assigns the lifecycle status `INCOMING`. This is a system-enforced default; the fleet manager cannot specify a different status at the point of registration.

The `INCOMING` status signals that the vehicle has been formally acquired and recorded in the system but is not yet cleared for rental operations. Transitioning from `INCOMING` to `ACTIVE` is governed by the lifecycle management rules defined in FR-2.

---

## Lifecycle Status Design

The full lifecycle is defined in FR-2. The table below shows the status introduced by this TRD and the statuses a vehicle can transition to next.

| Status | Description | Transitions To |
|---|---|---|
| `INCOMING` | Vehicle acquired and registered; not yet available for rental | `ACTIVE` (via FR-2) |
| `ACTIVE` | *(Out of scope for this TRD — see FR-2)* | — |
| `MAINTENANCE` | *(Out of scope for this TRD — see FR-2)* | — |
| `DECOMMISSIONING` | *(Out of scope for this TRD — see FR-2)* | — |
| `SOLD` | *(Out of scope for this TRD — see FR-2)* | — |

Only the `INCOMING` status is introduced and assigned by the Vehicle Onboarding process.

---

## Integration Points

| Module | Dependency Type | Description |
|---|---|---|
| **Location Module** | Consumed at onboarding | The `homeLocationId` provided during registration must reference an existing location managed by the Location module. The Vehicle module reads location data but does not write to it. |
| **Reservation & Allocation Module** (FR-11) | Downstream consumer | The Reservation module queries vehicle records by `lifecycleStatus` and `vehicleCategory` to identify eligible vehicles. Vehicles in `INCOMING` status are excluded from allocation since only `ACTIVE` vehicles can be assigned. |
| **Vehicle Lifecycle Module** (FR-2) | Downstream consumer | FR-2 reads the vehicle record created by onboarding and manages subsequent status transitions from `INCOMING` to `ACTIVE`, `MAINTENANCE`, etc. |
| **Insurance Expiry Block Module** (FR-3) | Downstream consumer | FR-3 reads the `vehicle_insurance.coverage_end_date` for all active vehicles and triggers availability blocks. The insurance record created during onboarding is the initial record that FR-3 monitors. |
| **Home Location Assignment Module** (FR-5) | Downstream consumer | FR-5 uses the `home_location_id` set during onboarding as the vehicle's initial home location and manages location changes via the location transfer process. |
| **GPS Tracking Module** (FR-9) | Downstream consumer | Once a vehicle is set to `ACTIVE`, the GPS module tracks it. The vehicle record (specifically its `id`) is used as the identifier in GPS tracking data. |
| **Maintenance Scheduling Module** (FR-19) | Downstream consumer | Maintenance scheduling uses the `odometer_at_acquisition` value as the baseline for calculating service intervals. |
| **Audit / Activity Log** | Infrastructure dependency | All creation and modification events on vehicle and insurance records are captured by the shared audit log. No additional implementation is required in the Vehicle Onboarding module. |

---

## Non-Functional Requirements

| ID | Category | Requirement |
|---|---|---|
| NFR-01 | **Security** | Only users with the `FLEET_MANAGER` role may invoke the vehicle registration endpoint. Attempts by other authenticated roles return HTTP `403`. Unauthenticated requests return HTTP `401`. |
| NFR-02 | **Data Integrity** | VIN and license plate uniqueness must be enforced at both the application layer (pre-validation) and the database layer (unique index) to prevent race conditions in concurrent submissions. |
| NFR-03 | **Immutability** | VIN and license plate must not be editable after initial registration. Update operations that attempt to modify these fields must be rejected by the API. |
| NFR-04 | **Auditability** | Every vehicle registration event must produce an audit log entry capturing: the acting user's ID, the vehicle ID created, and the timestamp (UTC). |
| NFR-05 | **Performance** | The vehicle registration API should respond within 500 ms under normal load for a single registration request (excluding network latency). |
| NFR-06 | **Availability** | Vehicle registration is a core operational function. The endpoint should be available as part of the overall system availability SLA (to be defined at platform level). |
| NFR-07 | **Validation Feedback** | When a submission fails validation, the API must return all field-level errors in a single response (not just the first error found) so that fleet managers can correct all issues in one step. |
| NFR-08 | **Data Retention** | Vehicle records must not be hard-deleted. Even after a vehicle reaches `SOLD` status, its registration record is retained for historical and audit purposes. |

---

## Open Questions & Future Considerations

| # | Topic | Detail |
|---|---|---|
| OQ-01 | **VIN Format Validation** | Should the system validate the VIN checksum (digit 9 verification algorithm as per ISO 3779) in addition to length and character checks, or is a format-only check sufficient for this phase? |
| OQ-02 | **License Plate Format** | License plate formats vary by country and region. Should the system enforce a specific format pattern, or accept any 1–20 character string? |
| OQ-03 | **Bulk Onboarding** | Is there a need to support bulk vehicle registration (e.g., CSV import) for the initial fleet intake, or is one-at-a-time registration via the form sufficient? |
| OQ-04 | **Vehicle Photos** | Should photos of the vehicle (exterior, interior) be uploaded at the time of onboarding, or is photo capture deferred to the first inspection workflow? |
| OQ-05 | **Ownership Evidence** | The PRD states ownership must be the company. Is there a document (e.g., title deed, purchase invoice) that needs to be attached to the vehicle record at onboarding for compliance purposes? |
| OQ-06 | **Manufacturing Year vs. Model Year** | Some markets distinguish between manufacturing year and model year. Should both be captured, or is a single year field sufficient? |
| OQ-07 | **Insurance Policy Document** | Should a digital copy (PDF/image) of the insurance policy document be attached to the insurance record at onboarding? |
