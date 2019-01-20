-- migrate:up
CREATE ROLE storage_user;
GRANT USAGE ON SCHEMA fs TO storage_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA fs TO storage_user;
ALTER USER storage_user WITH encrypted password 'storage_password';
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA fs TO storage_user;

-- migrate:down

REVOKE ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA fs FROM storage_user;
REVOKE USAGE ON SCHEMA fs FROM storage_user;
REVOKE SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA fs FROM storage_user;
DROP ROLE storage_user;
