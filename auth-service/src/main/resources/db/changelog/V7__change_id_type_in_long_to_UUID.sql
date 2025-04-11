ALTER TABLE auth_users ADD COLUMN new_id UUID;

ALTER TABLE auth_users DROP CONSTRAINT auth_users_pkey;
ALTER TABLE auth_users DROP COLUMN id;

ALTER TABLE auth_users RENAME COLUMN new_id TO id;

ALTER TABLE auth_users ADD PRIMARY KEY (id);

ALTER TABLE auth_users ALTER COLUMN id SET NOT NULL;