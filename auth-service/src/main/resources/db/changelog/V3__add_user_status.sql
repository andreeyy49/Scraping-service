ALTER TABLE auth_users
ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'REGISTRATION';