-- Update utenti/ruoli richiesti.
-- Compatibile con schema role ENUM('ADMIN','OPERATOR').
-- Nota: query idempotenti dove possibile.

USE engine_gallery;

-- 1) Rinomina utente: capo -> RML (solo se RML non esiste già)
UPDATE users u
SET u.username = 'RML'
WHERE LOWER(u.username) = 'capo'
  AND NOT EXISTS (
      SELECT 1
      FROM users x
      WHERE LOWER(x.username) = 'rml'
  );

-- 2) Rimuove utente giggi
DELETE FROM users
WHERE LOWER(username) = 'giggi';

-- 3) Inserisce nuova utenza federica2 come ADMIN con password temporanea
-- Password temporanea in chiaro (da comunicare all'utente): RML!2026#F2x9QpL7
-- Hash legacy SHA-256: il sistema la aggiorna automaticamente a PBKDF2 al primo login riuscito.
INSERT INTO users (username, password_hash, role)
SELECT 'federica2', SHA2('RML!2026#F2x9QpL7', 256), 'ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE LOWER(username) = 'federica2'
);

-- 4) Promozione ad ADMIN degli utenti richiesti (case-insensitive)
UPDATE users
SET role = 'ADMIN'
WHERE LOWER(username) IN (
    'marco',
    'larissa',
    'emanuele',
    'simone',
    'rml',
    'federica',
    'giordano'
);
