-- Applicare dopo 2026-10-25_create_workshop_maintenance.sql.
ALTER TABLE workshop_machines ADD COLUMN image_filename VARCHAR(255) NULL;
