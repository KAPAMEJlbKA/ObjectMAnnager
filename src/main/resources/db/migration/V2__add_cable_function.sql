ALTER TABLE cable_types
    ADD COLUMN function VARCHAR(255) DEFAULT 'SIGNAL' NOT NULL;

UPDATE cable_types
SET function = 'SIGNAL'
WHERE function IS NULL;
