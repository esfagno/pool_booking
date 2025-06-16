CREATE TABLE subscription
(
    id                   SERIAL PRIMARY KEY,
    subscription_type_id INTEGER REFERENCES subscription_type (id),
    status               subscription_status NOT NULL,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);