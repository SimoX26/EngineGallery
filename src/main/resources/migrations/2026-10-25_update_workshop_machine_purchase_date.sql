-- Applicare dopo 2026-10-25_create_workshop_maintenance.sql.
ALTER TABLE workshop_machines ADD COLUMN purchase_date DATE NULL AFTER location;
