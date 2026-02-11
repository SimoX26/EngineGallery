USE engine_gallery;



INSERT INTO customers
(id, name, company_name, phone, email, notes)
VALUES
-- Cliente 1
(1, 'Mario Rossi', NULL, '3331234567', 'mario.rossi@email.it',
 'Cliente privato, BMW Serie 1'),

-- Cliente 2
(2, 'Luigi Bianchi', NULL, '3339876543', 'luigi.bianchi@email.it',
 'Cliente privato, Renault'),

-- Cliente 3 (azienda)
(3, 'Auto Sprint', 'Auto Sprint SRL', '0245678901', 'info@autosprint.it',
 'Officina partner'),

-- Cliente 4
(4, 'Carlo Verdi', NULL, '3391122334', 'carlo.verdi@email.it',
 'Cliente appassionato di motori ad alte prestazioni');



INSERT INTO engines
(engine_ref, engine_code, customer_id, status, intake_date, delivery_date, notes)
VALUES
-- Cliente 1: Mario Rossi
('ENG-2026-00001', 'N47D20A', 1, 'WAITING', '2026-01-03', NULL, 'Motore BMW appena arrivato'),
('ENG-2026-00002', 'N47D20A', 1, 'WORK_IN_PROGRESS', '2026-01-04', NULL, 'Catena in lavorazione'),

-- Cliente 2: Luigi Bianchi
('ENG-2026-00003', 'K9K', 2, 'DISASSEMBLED', '2026-01-05', NULL, 'Motore Renault smontato'),
('ENG-2026-00004', 'K9K', 2, 'READY', '2026-01-06', NULL, 'Pronto per riconsegna'),

-- Cliente 3: Officina Auto Sprint SRL
('ENG-2026-00005', '1.3 MJTD', 3, 'WORK_IN_PROGRESS', '2026-01-07', NULL, 'Motore Fiat'),
('ENG-2026-00006', 'M9R', 3, 'DELIVERED', '2026-01-02', '2026-01-10', 'Consegnato al cliente'),

-- Cliente 4: Carlo Verdi
('ENG-2026-00007', 'V8-034', 4, 'WORK_IN_PROGRESS', '2026-01-08', NULL, 'Motore ad alte prestazioni'),
('ENG-2026-00008', 'D-998', 4, 'DELIVERED', '2026-01-01', '2026-01-09', 'Motore storico consegnato');



INSERT INTO images (engine_id, filename, uploaded_by) VALUES
-- ENG-2026-00001
(1, 'n47_front.jpg', 3),
(1, 'n47_chain.jpg', 4),

-- ENG-2026-00002
(2, 'n47_block.jpg', 3),

-- ENG-2026-00003
(3, 'k9k_before.jpg', 4),
(3, 'k9k_open.jpg', 4),

-- ENG-2026-00004
(4, 'k9k_ready.jpg', 5),

-- ENG-2026-00006 (consegnato)
(6, 'm9r_final.jpg', 5),

-- ENG-2026-00008 (consegnato)
(8, 'd998_overview.jpg', 6),
(8, 'd998_detail.jpg', 6),

-- Upload anonimo (test uploaded_by NULL)
(1, 'n47_old_damage.jpg', NULL);