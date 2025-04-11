CREATE TABLE roles (
    id          SERIAL PRIMARY KEY,
    authorities VARCHAR(255),
    user_id     UUID REFERENCES "users" (id) ON DELETE CASCADE
)