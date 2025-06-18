CREATE TABLE pool
(
    id                       SERIAL PRIMARY KEY,
    name                     VARCHAR(255) NOT NULL UNIQUE,
    address                  TEXT         NOT NULL,
    description              TEXT,
    max_capacity             INTEGER      NOT NULL CHECK (max_capacity > 0),
    session_duration_minutes INTEGER      NOT NULL CHECK (session_duration_minutes BETWEEN 10 AND 480),
    created_at               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
