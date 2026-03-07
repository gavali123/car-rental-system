-- Migration: V7 - Create retirement_records table
-- Stores disposal-specific details captured when a vehicle is set to Sold (FR-4).
-- One record per vehicle; a vehicle may have at most one retirement record.
-- Depends on: vehicles (V4), users (V2).

CREATE TABLE retirement_records (
    id                    UUID                     PRIMARY KEY DEFAULT gen_random_uuid(),
    -- One-to-one relationship: each vehicle has at most one retirement record
    vehicle_id            UUID                     NOT NULL UNIQUE REFERENCES vehicles (id),
    -- Formal date of disposal; must be >= the date of the Decommissioning transition (enforced at application layer)
    disposal_date         DATE                     NOT NULL,
    -- Amount received from sale in the base currency; optional
    sale_price            DECIMAL(12, 2),
    -- Name of the buyer or disposal party; optional
    buyer_name            VARCHAR(255),
    -- The fleet manager who submitted the disposal details
    recorded_by_user_id   UUID                     NOT NULL REFERENCES users (id),
    recorded_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Index to support lookup of a vehicle's retirement record
CREATE INDEX idx_retirement_records_vehicle_id ON retirement_records (vehicle_id);

-- Index to support audit queries by the recording user
CREATE INDEX idx_retirement_records_recorded_by ON retirement_records (recorded_by_user_id);
