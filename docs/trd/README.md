# TRD – Technical Requirement Documents

This directory contains Technical Requirement Documents (TRDs) for the Car Rental System.

## Car Management

| Document | PRD Reference | Description |
|---|---|---|
| [Vehicle Onboarding](trd-car-management-vehicle-onboarding.md) | [FR-1](../prd/prd-car-management.md#fr-1-vehicle-onboarding) | Data model, API design, business rules, and UI flows for registering a new vehicle into the rental fleet. |
| [Database Design – Vehicle Onboarding](db-design-car-management-vehicle-onboarding.md) | [FR-1](../prd/prd-car-management.md#fr-1-vehicle-onboarding) | Detailed database schema for the `vehicles` and `vehicle_insurance` tables introduced by FR-1. |
| [Vehicle Lifecycle Management](trd-car-management-lifecycle.md) | [FR-2](../prd/prd-car-management.md#fr-2-vehicle-lifecycle-management) | Status transition rules, REST API, algorithm, and UI flows for managing the lifecycle status of a vehicle. |
| [Database Design – Vehicle Lifecycle Management](database-design-car-management-lifecycle.md) | [FR-2](../prd/prd-car-management.md#fr-2-vehicle-lifecycle-management) | Detailed database schema for lifecycle-relevant `vehicles` fields and the canonical `vehicle_status_history` audit table. |
| [Insurance Expiry Block](trd-car-management-fr3-insurance-expiry-block.md) | [FR-3](../prd/prd-car-management.md#fr-3-insurance-expiry-block) | Business rules and API design for blocking vehicle availability when insurance has expired. |
| [Vehicle Retirement](trd-car-management-fr4-vehicle-retirement.md) | [FR-4](../prd/prd-car-management.md#fr-4-vehicle-retirement) | Two-step decommission-to-sold retirement flow, conflict-check warnings, and API design. |
| [Home Location Assignment](trd-car-management-fr5-home-location-assignment.md) | [FR-5](../prd/prd-car-management.md#fr-5-home-location-assignment) | Data model, API design, business rules, and UI flows for assigning and viewing a vehicle's home location. |
