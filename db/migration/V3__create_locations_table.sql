-- Migration: V3 - Create locations table
-- Stores the physical rental branches from which vehicles are dispatched and returned.
-- Referenced by the vehicles table via home_location_id (FR-5).

CREATE TABLE locations (
    id              UUID                     PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100)             NOT NULL,
    address_line_1  VARCHAR(255)             NOT NULL,
    address_line_2  VARCHAR(255),
    city            VARCHAR(100)             NOT NULL,
    country         VARCHAR(100)             NOT NULL,
    postal_code     VARCHAR(20)              NOT NULL,
    phone_number    VARCHAR(30),
    is_active       BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Unique constraint: location names must be distinct across the system
CREATE UNIQUE INDEX locations_name_key ON locations (name);

-- Index to support filtering active locations for dropdowns and onboarding forms (FR-5, NFR-FR5-04)
CREATE INDEX idx_locations_is_active ON locations (is_active);
