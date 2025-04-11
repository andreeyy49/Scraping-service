CREATE TABLE auth_users (
    id BIGSERIAL PRIMARY KEY,
    user_name VARCHAR(255),
    password VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    roles VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES auth_users(id) ON DELETE CASCADE
);