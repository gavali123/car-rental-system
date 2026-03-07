-- Migration: V2 - Create users table
-- Stores authenticated users of the system.
-- This table is referenced as a foreign key by vehicle_status_history,
-- retirement_records, vehicle_insurance, and insurance_expiry_block_log.

CREATE TABLE users (
    id                  UUID                     PRIMARY KEY DEFAULT gen_random_uuid(),
    username            VARCHAR(100)             NOT NULL,
    email               VARCHAR(255)             NOT NULL,
    password_hash       TEXT                     NOT NULL,
    role                TEXT                     NOT NULL,
    is_active           BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Unique constraint: no two users may share a username
CREATE UNIQUE INDEX users_username_key ON users (username);

-- Unique constraint: no two users may share an email address
CREATE UNIQUE INDEX users_email_key ON users (email);

-- Index to support role-based access control queries
CREATE INDEX idx_users_role ON users (role);
