CREATE TABLE pool_schedule
(
    id           SERIAL PRIMARY KEY,
    pool_id      INTEGER  NOT NULL REFERENCES pool (id) ON DELETE CASCADE,
    day_of_week  SMALLINT NOT NULL,
    opening_time TIME     NOT NULL,
    closing_time TIME     NOT NULL,
    UNIQUE (pool_id, day_of_week)
);