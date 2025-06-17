CREATE TABLE session (
                         id SERIAL PRIMARY KEY,
                         pool_id INTEGER REFERENCES pool(id) ON DELETE CASCADE,
                         start_time TIMESTAMP NOT NULL,
                         end_time TIMESTAMP NOT NULL CHECK (end_time > start_time),
                         current_capacity INTEGER DEFAULT 0 CHECK (current_capacity >= 0),
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
