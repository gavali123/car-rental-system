-- Migration: V1 - Create PostgreSQL enum types
-- These types are shared across multiple tables and must be created before any table that references them.

-- Lifecycle status of a vehicle in the rental fleet (FR-2)
CREATE TYPE lifecycle_status_enum AS ENUM (
    'INCOMING',
    'ACTIVE',
    'MAINTENANCE',
    'DECOMMISSIONING',
    'SOLD'
);

-- Physical size of a vehicle (FR-1)
CREATE TYPE size_type_enum AS ENUM (
    'SMALL',
    'MEDIUM'
);

-- Market/pricing class of a vehicle (FR-1)
CREATE TYPE vehicle_class_enum AS ENUM (
    'ECONOMY',
    'LUXURY'
);

-- Derived category combining size_type and vehicle_class (FR-1)
CREATE TYPE vehicle_category_enum AS ENUM (
    'ECONOMY_SMALL',
    'ECONOMY_MEDIUM',
    'LUXURY_SMALL',
    'LUXURY_MEDIUM'
);

-- Fuel type of a vehicle (FR-1)
CREATE TYPE fuel_type_enum AS ENUM (
    'GAS',
    'ELECTRIC',
    'HYBRID'
);

-- Event type recorded in the insurance expiry block audit log (FR-3)
CREATE TYPE insurance_block_event_type AS ENUM (
    'BLOCK_APPLIED',
    'BLOCK_LIFTED'
);

-- What triggered an insurance block event (FR-3)
CREATE TYPE insurance_block_trigger AS ENUM (
    'SCHEDULED_CHECK',
    'MANUAL_UPDATE'
);
