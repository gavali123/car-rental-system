-- Migration: V4 - Create vehicles table
-- Primary record for each vehicle registered in the rental fleet.
-- Combines fields from FR-1 (onboarding), FR-2 (lifecycle status), and FR-5 (home location).
-- Depends on: locations (V3), enum types (V1).

CREATE TABLE vehicles (
    id                      UUID                     PRIMARY KEY DEFAULT gen_random_uuid(),
    vin                     VARCHAR(17)              NOT NULL,
    license_plate           VARCHAR(20)              NOT NULL,
    purchase_date           DATE                     NOT NULL,
    purchase_cost           DECIMAL(12, 2)           NOT NULL,
    odometer_at_acquisition INTEGER                  NOT NULL,
    brand                   VARCHAR(100)             NOT NULL,
    model                   VARCHAR(100)             NOT NULL,
    manufacturing_year      SMALLINT                 NOT NULL,
    size_type               size_type_enum           NOT NULL,
    vehicle_class           vehicle_class_enum       NOT NULL,
    vehicle_category        vehicle_category_enum    NOT NULL,
    number_of_seats         SMALLINT                 NOT NULL,
    fuel_type               fuel_type_enum           NOT NULL,
    lifecycle_status        lifecycle_status_enum    NOT NULL DEFAULT 'INCOMING',
    home_location_id        UUID                     NOT NULL REFERENCES locations (id) ON DELETE RESTRICT,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by              TEXT                     NOT NULL,
    updated_by              TEXT                     NOT NULL,
    deleted                 BOOLEAN                  NOT NULL DEFAULT FALSE,
    deleted_at              TIMESTAMP WITH TIME ZONE,

    -- purchase_date must not be a future date
    CONSTRAINT chk_vehicles_purchase_date
        CHECK (purchase_date <= CURRENT_DATE),

    -- purchase cost must be positive
    CONSTRAINT chk_vehicles_purchase_cost
        CHECK (purchase_cost > 0),

    -- odometer reading is non-negative (zero is valid for brand-new vehicles)
    CONSTRAINT chk_vehicles_odometer
        CHECK (odometer_at_acquisition >= 0),

    -- manufacturing year must be plausible (between 1900 and next calendar year)
    CONSTRAINT chk_vehicles_manufacturing_year
        CHECK (manufacturing_year >= 1900 AND manufacturing_year <= EXTRACT(YEAR FROM CURRENT_DATE) + 1),

    -- number of seats must be positive
    CONSTRAINT chk_vehicles_number_of_seats
        CHECK (number_of_seats > 0)
);

-- Unique constraint: VIN must be globally unique across all registered vehicles
CREATE UNIQUE INDEX vehicles_vin_key ON vehicles (vin);

-- Unique constraint: license plate must be unique (stored in uppercase per DD-08)
CREATE UNIQUE INDEX vehicles_license_plate_key ON vehicles (license_plate);

-- Index to support filtering vehicles by availability status (FR-2, FR-7, FR-8)
CREATE INDEX idx_vehicles_lifecycle_status ON vehicles (lifecycle_status);

-- Index to support per-location fleet inventory queries (FR-5, NFR-FR5-03)
CREATE INDEX idx_vehicles_home_location_id ON vehicles (home_location_id);

-- Composite index for reservation allocation queries filtering by category and status (DD-02)
CREATE INDEX idx_vehicles_category_status ON vehicles (vehicle_category, lifecycle_status);
