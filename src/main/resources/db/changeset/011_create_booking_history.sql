CREATE TABLE booking_history
(
    id         SERIAL PRIMARY KEY,
    booking_id INTEGER REFERENCES booking (id) ON DELETE CASCADE,
    action     booking_action NOT NULL,
    changed_by INTEGER REFERENCES users (id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes      TEXT
);