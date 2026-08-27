-- Aggiunge data/ora e autore di inserimento ai record mostrati negli elenchi.
-- I valori storici vengono ricostruiti dai log quando disponibili. Gli autori
-- non verificabili restano NULL; per i motori la data di ingresso è il solo
-- fallback attendibile disponibile per la data di creazione.

ALTER TABLE engines
    ADD COLUMN created_at TIMESTAMP NULL DEFAULT NULL,
    ADD COLUMN created_by VARCHAR(100) NULL;

UPDATE engines e
SET e.created_at = (
        SELECT l.created_at
        FROM user_activity_log l
        WHERE l.entity_type = 'MOTOR'
          AND l.action_type = 'CREATE'
          AND l.entity_id = e.engine_ref
        ORDER BY l.created_at, l.id
        LIMIT 1
    ),
    e.created_by = (
        SELECT l.username
        FROM user_activity_log l
        WHERE l.entity_type = 'MOTOR'
          AND l.action_type = 'CREATE'
          AND l.entity_id = e.engine_ref
        ORDER BY l.created_at, l.id
        LIMIT 1
    );

UPDATE engines
SET created_at = TIMESTAMP(intake_date, '00:00:00')
WHERE created_at IS NULL;

ALTER TABLE engines
    MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE warehouse_items
    ADD COLUMN created_at TIMESTAMP NULL DEFAULT NULL,
    ADD COLUMN created_by VARCHAR(100) NULL;

UPDATE warehouse_items w
SET w.created_at = (
        SELECT l.created_at
        FROM user_activity_log l
        WHERE l.entity_type = 'WAREHOUSE_ITEM'
          AND l.action_type = 'CREATE'
          AND l.entity_id = CAST(w.id AS CHAR)
        ORDER BY l.created_at, l.id
        LIMIT 1
    ),
    w.created_by = (
        SELECT l.username
        FROM user_activity_log l
        WHERE l.entity_type = 'WAREHOUSE_ITEM'
          AND l.action_type = 'CREATE'
          AND l.entity_id = CAST(w.id AS CHAR)
        ORDER BY l.created_at, l.id
        LIMIT 1
    );

ALTER TABLE warehouse_items
    MODIFY COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE hydraulic_tests
    ADD COLUMN created_by VARCHAR(100) NULL;

UPDATE hydraulic_tests h
SET h.created_by = (
    SELECT l.username
    FROM user_activity_log l
    WHERE l.entity_type = 'HYDRAULIC_TEST'
      AND l.action_type = 'CREATE'
      AND l.entity_id = CAST(h.id AS CHAR)
    ORDER BY l.created_at, l.id
    LIMIT 1
);
