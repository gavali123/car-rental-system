-- Migration: V6 - Create vehicle_status_history table
-- Immutable, append-only audit log of every lifecycle status transition for each vehicle.
-- This is the canonical table for lifecycle transition history (FR-2, FR-4).
-- No updates or deletes are permitted on this table; each row is written once on each status change.
-- Depends on: vehicles (V4), users (V2), enum types (V1).

CREATE TABLE vehicle_status_history (
    id                  UUID                     PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id          UUID                     NOT NULL REFERENCES vehicles (id),
    previous_status     lifecycle_status_enum    NOT NULL,
    new_status          lifecycle_status_enum    NOT NULL,
    changed_by_user_id  UUID                     NOT NULL REFERENCES users (id),
    changed_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    -- Optional free-text note explaining the reason for the transition
    notes               TEXT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by          TEXT                     NOT NULL,
    updated_by          TEXT                     NOT NULL,
    deleted             BOOLEAN                  NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP WITH TIME ZONE
);

-- Index to support retrieval of a vehicle's full lifecycle history (FR-2 API)
CREATE INDEX idx_vehicle_status_history_vehicle_id ON vehicle_status_history (vehicle_id);

-- Index to support queries filtering history by the acting user
CREATE INDEX idx_vehicle_status_history_changed_by ON vehicle_status_history (changed_by_user_id);

-- Index to support time-ordered retrieval of status changes (FR-2 API, FR-4)
CREATE INDEX idx_vehicle_status_history_changed_at ON vehicle_status_history (changed_at);
