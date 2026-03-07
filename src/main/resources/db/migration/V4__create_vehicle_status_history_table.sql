-- V4: Create the vehicle_status_history table
-- Immutable audit log of every lifecycle status transition for each vehicle.
-- Records are append-only; no updates or deletes are permitted.
-- This is the CANONICAL table for lifecycle transition history (see database-design-car-management-lifecycle.md).

CREATE TABLE IF NOT EXISTS vehicle_status_history
(
    id                  UUID                     NOT NULL DEFAULT gen_random_uuid(),
    vehicle_id          UUID                     NOT NULL,
    previous_status     lifecycle_status_enum    NOT NULL,
    new_status          lifecycle_status_enum    NOT NULL,
    changed_by_user_id  UUID                     NOT NULL,
    changed_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    notes               TEXT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by          TEXT                     NOT NULL,
    updated_by          TEXT                     NOT NULL,
    deleted             BOOLEAN                  NOT NULL DEFAULT FALSE,

    CONSTRAINT vehicle_status_history_pkey PRIMARY KEY (id),
    CONSTRAINT vehicle_status_history_vehicle_fk
        FOREIGN KEY (vehicle_id) REFERENCES vehicles (id)
);

-- Index: support lookups by vehicle
CREATE INDEX IF NOT EXISTS idx_vehicle_status_history_vehicle_id
    ON vehicle_status_history (vehicle_id);

-- Index: support time-ordered history queries
CREATE INDEX IF NOT EXISTS idx_vehicle_status_history_changed_at
    ON vehicle_status_history (changed_at);
