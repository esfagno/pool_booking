CREATE TABLE role (
                      id SERIAL PRIMARY KEY,
                      name role_type UNIQUE NOT NULL,
                      description TEXT
);