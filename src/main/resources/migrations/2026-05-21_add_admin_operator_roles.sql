-- Introduce role model ADMIN/OPERATOR preserving existing accounts.
-- Existing INSPECTOR users are promoted to ADMIN.

ALTER TABLE users
    MODIFY COLUMN role ENUM('ADMIN', 'OPERATOR', 'INSPECTOR') NOT NULL;

UPDATE users
SET role = 'ADMIN'
WHERE role = 'INSPECTOR';

ALTER TABLE users
    MODIFY COLUMN role ENUM('ADMIN', 'OPERATOR') NOT NULL;
