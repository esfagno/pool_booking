CREATE TABLE booking
(
    id           SERIAL PRIMARY KEY,
    user_id      INTEGER REFERENCES users (id) ON DELETE CASCADE,
    session_id   INTEGER REFERENCES session (id) ON DELETE CASCADE,
    booking_time TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    status       booking_status DEFAULT 'ACTIVE' NOT NULL,
    created_at   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, session_id)
);
