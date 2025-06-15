CREATE TABLE user_subscription
(
    id                 SERIAL PRIMARY KEY,
    user_id            INTEGER REFERENCES users (id),
    subscription_id    INTEGER REFERENCES subscription (id),
    remaining_bookings INTEGER NOT NULL,
    assigned_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);