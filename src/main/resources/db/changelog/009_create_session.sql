CREATE TABLE session (
                         id SERIAL PRIMARY KEY,
                         pool_id INTEGER REFERENCES pool(id) ON DELETE CASCADE,
                         start_time TIMESTAMP NOT NULL,
                         end_time TIMESTAMP NOT NULL,
                         current_capacity INTEGER DEFAULT 0,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);