-- V1: Create the locations table
-- Stores physical rental locations (branches/hubs) to which vehicles are assigned.

CREATE TABLE IF NOT EXISTS locations
(
    id             UUID                     NOT NULL DEFAULT gen_random_uuid(),
    name           VARCHAR(100)             NOT NULL,
    address_line_1 VARCHAR(255)             NOT NULL,
    address_line_2 VARCHAR(255),
    city           VARCHAR(100)             NOT NULL,
    country        VARCHAR(100)             NOT NULL,
    postal_code    VARCHAR(20)              NOT NULL,
    phone_number   VARCHAR(30),
    is_active      BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT locations_pkey PRIMARY KEY (id),
    CONSTRAINT locations_name_key UNIQUE (name)
);

-- Index: support filtering of active locations used during vehicle onboarding
CREATE INDEX IF NOT EXISTS idx_locations_is_active ON locations (is_active);
