CREATE TABLE booking_history
(
    id                 SERIAL PRIMARY KEY,
    booking_user_id    INTEGER,
    booking_session_id INTEGER,
    action             booking_action NOT NULL,
    changed_by         INTEGER REFERENCES users (id),
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes              TEXT,
    FOREIGN KEY (booking_user_id, booking_session_id)
        REFERENCES booking (user_id, session_id) ON DELETE CASCADE
);