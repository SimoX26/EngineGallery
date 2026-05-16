-- Migration: update credenziali utenti e allineamento account
-- Data: 2026-05-16
-- Compatibilità: MySQL 8+

USE engine_gallery;

-- 1) Imposta la password di tutte le utenze esistenti a "rml" (hash legacy SHA-256)
UPDATE users
SET password_hash = SHA2('rml', 256);

-- 2) Rimuove completamente l'utenza richiesta
DELETE FROM users
WHERE username = 'giggianuel';

-- 3) Aggiunge le nuove utenze con ruolo standard OPERATOR, se mancanti
INSERT INTO users (username, password_hash, role)
SELECT 'Marco', SHA2('rml', 256), 'OPERATOR'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'Marco'
);

INSERT INTO users (username, password_hash, role)
SELECT 'Larissa', SHA2('rml', 256), 'OPERATOR'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'Larissa'
);
