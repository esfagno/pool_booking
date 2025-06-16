CREATE TABLE subscription_type
(
    id                     SERIAL PRIMARY KEY,
    name                   VARCHAR(100)   NOT NULL,
    description            TEXT,
    max_bookings_per_month INTEGER        NOT NULL,
    price                  NUMERIC(10, 2) NOT NULL,
    duration_days          INTEGER        NOT NULL,
    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);