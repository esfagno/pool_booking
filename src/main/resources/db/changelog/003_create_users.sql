CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash TEXT NOT NULL,
                       first_name VARCHAR(20),
                       last_name VARCHAR(50),
                       phone_number VARCHAR(20),
                       role_id INTEGER NOT NULL REFERENCES role(id),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);