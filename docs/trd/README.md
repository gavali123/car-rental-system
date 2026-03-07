# TRD – Technical Requirement Documents

This directory contains Technical Requirement Documents (TRDs) for the Car Rental System.

## Platform / Foundation

| Document | PRD Reference | Description |
|---|---|---|
| [Spring Boot Project Setup](trd-spring-boot-project-setup.md) | — | Foundational architecture, Maven dependency design, layered architecture conventions, configuration, exception handling, security, logging, reactive model, database migrations, and testing strategy for all backend services. |

## Car Management

| Document | PRD Reference | Description |
|---|---|---|
| [Vehicle Onboarding](trd-car-management-vehicle-onboarding.md) | [FR-1](../prd/prd-car-management.md#fr-1-vehicle-onboarding) | Data model, API design, business rules, and validation for registering a new vehicle into the rental fleet. |
| [Vehicle Lifecycle Management](trd-car-management-lifecycle.md) | [FR-2](../prd/prd-car-management.md#fr-2-vehicle-lifecycle-management) | Lifecycle status values, transition matrix, audit history, and API design for managing vehicle lifecycle. |
| [Insurance Expiry Block](trd-car-management-fr3-insurance-expiry-block.md) | [FR-3](../prd/prd-car-management.md#fr-3-insurance-expiry-block) | Scheduled expiry check, automatic availability block, tri-channel fleet manager alert, and block-lift logic. |
| [Vehicle Retirement](trd-car-management-fr4-vehicle-retirement.md) | [FR-4](../prd/prd-car-management.md#fr-4-vehicle-retirement) | Two-step decommission-then-sold retirement flow, conflict checks, and terminal state enforcement. |
| [Home Location Assignment](trd-car-management-fr5-home-location-assignment.md) | [FR-5](../prd/prd-car-management.md#fr-5-home-location-assignment) | Data model, API design, business rules, and UI flows for assigning and viewing a vehicle's home location. |
