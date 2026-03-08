-- V2: Create the vehicles table
-- Stores the primary record for each vehicle registered in the rental fleet.

-- Enum types used by the vehicles table
CREATE TYPE size_type_enum AS ENUM ('SMALL', 'MEDIUM');
CREATE TYPE vehicle_class_enum AS ENUM ('ECONOMY', 'LUXURY');
CREATE TYPE vehicle_category_enum AS ENUM ('ECONOMY_SMALL', 'ECONOMY_MEDIUM', 'LUXURY_SMALL', 'LUXURY_MEDIUM');
CREATE TYPE fuel_type_enum AS ENUM ('GAS', 'ELECTRIC', 'HYBRID');
CREATE TYPE lifecycle_status_enum AS ENUM ('INCOMING', 'ACTIVE', 'MAINTENANCE', 'DECOMMISSIONING', 'SOLD');

CREATE TABLE IF NOT EXISTS vehicles
(
    id                      UUID                     NOT NULL DEFAULT gen_random_uuid(),
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
    home_location_id        UUID                     NOT NULL,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT vehicles_pkey PRIMARY KEY (id),
    CONSTRAINT vehicles_vin_key UNIQUE (vin),
    CONSTRAINT vehicles_license_plate_key UNIQUE (license_plate),
    CONSTRAINT vehicles_purchase_date_check CHECK (purchase_date <= CURRENT_DATE),
    CONSTRAINT vehicles_purchase_cost_check CHECK (purchase_cost > 0),
    CONSTRAINT vehicles_odometer_check CHECK (odometer_at_acquisition >= 0),
    CONSTRAINT vehicles_seats_check CHECK (number_of_seats > 0),
    CONSTRAINT vehicles_mfg_year_check CHECK (
        manufacturing_year >= 1900
        AND manufacturing_year <= EXTRACT(YEAR FROM CURRENT_DATE) + 1
    ),
    CONSTRAINT vehicles_home_location_fk
        FOREIGN KEY (home_location_id) REFERENCES locations (id) ON DELETE RESTRICT
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_vehicles_lifecycle_status
    ON vehicles (lifecycle_status);

CREATE INDEX IF NOT EXISTS idx_vehicles_home_location_id
    ON vehicles (home_location_id);

CREATE INDEX IF NOT EXISTS idx_vehicles_category_status
    ON vehicles (vehicle_category, lifecycle_status);
