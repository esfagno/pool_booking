CREATE TABLE subscription_type
(
    id                     SERIAL PRIMARY KEY,
    name                   VARCHAR(100)   NOT NULL,
    description            TEXT,
    max_bookings_per_month INTEGER        NOT NULL CHECK (max_bookings_per_month >= 0),
    price                  NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    duration_days          INTEGER        NOT NULL CHECK (duration_days > 0),
    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);