-- Migration: V8 - Create insurance_expiry_block_log table
-- Immutable audit log of every activation and deactivation of the insurance block
-- for traceability and compliance purposes (FR-3).
-- Records are append-only; each row is written once per block event.
-- Depends on: vehicles (V4), users (V2), enum types (V1).

CREATE TABLE insurance_expiry_block_log (
    id                          UUID                         PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id                  UUID                         NOT NULL REFERENCES vehicles (id),
    -- Whether the block was applied or lifted for this event
    event_type                  insurance_block_event_type   NOT NULL,
    -- The coverage_end_date value on the active insurance record at the time of the event
    coverage_end_date_at_event  DATE                         NOT NULL,
    -- Days between the event date and coverage_end_date; negative values indicate days since expiry
    days_to_expiry              INTEGER                      NOT NULL,
    -- Whether the event was triggered by the daily scheduled job or by a manual fleet manager update
    triggered_by                insurance_block_trigger      NOT NULL,
    -- The user who performed the action; NULL for SCHEDULED_CHECK events
    actor_id                    UUID                         REFERENCES users (id),
    occurred_at                 TIMESTAMP WITH TIME ZONE     NOT NULL DEFAULT NOW()
);

-- Index to support retrieval of all block events for a given vehicle
CREATE INDEX idx_insurance_expiry_block_log_vehicle_id ON insurance_expiry_block_log (vehicle_id);

-- Index to support time-range queries over block events
CREATE INDEX idx_insurance_expiry_block_log_occurred_at ON insurance_expiry_block_log (occurred_at);

-- Index to support filtering events by type (e.g. all BLOCK_APPLIED events)
CREATE INDEX idx_insurance_expiry_block_log_event_type ON insurance_expiry_block_log (event_type);
