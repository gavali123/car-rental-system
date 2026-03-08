-- V3: Create the vehicle_insurance table
-- Stores insurance records for each vehicle. Supports historical retention of renewals.

CREATE TABLE IF NOT EXISTS vehicle_insurance
(
    id                  UUID                     NOT NULL DEFAULT gen_random_uuid(),
    vehicle_id          UUID                     NOT NULL,
    insurer_name        VARCHAR(200)             NOT NULL,
    policy_number       VARCHAR(100)             NOT NULL,
    coverage_start_date DATE                     NOT NULL,
    coverage_end_date   DATE                     NOT NULL,
    is_active           BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT vehicle_insurance_pkey PRIMARY KEY (id),
    CONSTRAINT vehicle_insurance_vehicle_fk
        FOREIGN KEY (vehicle_id) REFERENCES vehicles (id) ON DELETE CASCADE,
    CONSTRAINT vehicle_insurance_dates_check
        CHECK (coverage_start_date < coverage_end_date)
);

-- Index: support joins from the vehicles table
CREATE INDEX IF NOT EXISTS idx_vehicle_insurance_vehicle_id
    ON vehicle_insurance (vehicle_id);

-- Index: support daily insurance expiry scan (FR-3)
CREATE INDEX IF NOT EXISTS idx_vehicle_insurance_coverage_end_date
    ON vehicle_insurance (coverage_end_date);

-- Partial unique index: enforce at most one active insurance record per vehicle
CREATE UNIQUE INDEX IF NOT EXISTS uq_vehicle_insurance_active
    ON vehicle_insurance (vehicle_id)
    WHERE is_active = TRUE;
