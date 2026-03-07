-- Migration: V5 - Create vehicle_insurance table
-- Stores insurance records for each vehicle, supporting historical retention of
-- insurance renewals via the is_active flag (FR-1, FR-3).
-- Depends on: vehicles (V4), users (V2).

CREATE TABLE vehicle_insurance (
    id                    UUID                     PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id            UUID                     NOT NULL REFERENCES vehicles (id) ON DELETE CASCADE,
    insurer_name          VARCHAR(200)             NOT NULL,
    policy_number         VARCHAR(100)             NOT NULL,
    coverage_start_date   DATE                     NOT NULL,
    coverage_end_date     DATE                     NOT NULL,
    -- Marks the current active insurance record; only one active record per vehicle at any time
    is_active             BOOLEAN                  NOT NULL DEFAULT TRUE,
    -- Set to TRUE by the daily insurance expiry check when coverage_end_date is within 7 days or past (FR-3)
    insurance_blocked     BOOLEAN                  NOT NULL DEFAULT FALSE,
    -- Timestamp when the insurance block was most recently activated; NULL when not blocked
    block_triggered_at    TIMESTAMP WITH TIME ZONE,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    -- The user who last updated this insurance record (nullable; NULL for system-initiated updates)
    updated_by            UUID                     REFERENCES users (id),

    -- Coverage start date must be strictly before coverage end date
    CONSTRAINT chk_vehicle_insurance_coverage_dates
        CHECK (coverage_start_date < coverage_end_date)
);

-- Index to support joins and lookups from the vehicles table
CREATE INDEX idx_vehicle_insurance_vehicle_id ON vehicle_insurance (vehicle_id);

-- Index to support the daily insurance expiry scan (FR-3)
CREATE INDEX idx_vehicle_insurance_coverage_end_date ON vehicle_insurance (coverage_end_date);

-- Partial unique index: enforces at most one active insurance record per vehicle (DD-04)
CREATE UNIQUE INDEX uq_vehicle_insurance_active ON vehicle_insurance (vehicle_id) WHERE is_active = TRUE;
