-- Migration: rimozione stato DISASSEMBLED senza reset dati
-- Data: 2026-02-26
-- Compatibilità: MySQL 8+

USE engine_gallery;

-- 1) Riallinea i dati esistenti
UPDATE engines
SET status = 'WORK_IN_PROGRESS'
WHERE status = 'DISASSEMBLED';

-- 2) Aggiorna il tipo ENUM rimuovendo DISASSEMBLED
ALTER TABLE engines
    MODIFY COLUMN status ENUM(
        'WAITING',
        'WORK_IN_PROGRESS',
        'READY',
        'DELIVERED'
    ) NOT NULL;
