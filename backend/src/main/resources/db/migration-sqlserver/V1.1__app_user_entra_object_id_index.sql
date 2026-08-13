-- Plain UNIQUE constraints in SQL Server reject a second NULL (unlike Postgres/MySQL,
-- which treat each NULL as distinct), and most users won't have an Entra object id
-- until their first login. A filtered index enforces uniqueness only when it's set.
CREATE UNIQUE INDEX uk_app_user_entra_object_id ON app_user(entra_object_id) WHERE entra_object_id IS NOT NULL;
