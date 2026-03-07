# TRD – Technical Requirement Documents

This directory contains Technical Requirement Documents (TRDs) for the Car Rental System.

## Car Management

| Document | PRD Reference | Description |
|---|---|---|
| [Home Location Assignment](trd-car-management-fr5-home-location-assignment.md) | [FR-5](../prd/prd-car-management.md#fr-5-home-location-assignment) | Data model, API design, business rules, and UI flows for assigning and viewing a vehicle's home location. |

## Database Design and Migrations

| Document | Feature | Description |
|---|---|---|
| [DB Design – Vehicle Onboarding](db-design-car-management-vehicle-onboarding.md) | [FR-1](../prd/prd-car-management.md#fr-1-vehicle-onboarding) | Full schema for `vehicles` and `vehicle_insurance` tables, including enum types, indexes, and design decisions. |
| [DB Design – Vehicle Lifecycle Management](database-design-car-management-lifecycle.md) | [FR-2](../prd/prd-car-management.md#fr-2-vehicle-lifecycle-management) | Schema for the `vehicles` lifecycle fields and the `vehicle_status_history` audit table. |
| [DB Migrations – Vehicle Lifecycle Management](db-migrations-car-management-lifecycle.md) | [FR-2](../prd/prd-car-management.md#fr-2-vehicle-lifecycle-management) | Flyway migration script specifications (`V2`, `V3`) for the lifecycle soft-delete columns on `vehicles` and the `vehicle_status_history` table. |
