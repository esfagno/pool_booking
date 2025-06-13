CREATE TABLE pool (
                      id SERIAL PRIMARY KEY,
                      name VARCHAR(255) NOT NULL,
                      address TEXT NOT NULL,
                      description TEXT,
                      max_capacity INTEGER NOT NULL,
                      session_duration_minutes INTEGER NOT NULL,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);