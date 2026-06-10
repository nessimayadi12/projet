-- Correctif manuel pour l'erreur Hibernate :
-- alter table demandes modify column value_date integer
-- Data truncation: Out of range value for column 'value_date'
--
-- Dans l'application, demandes.value_date est une valeur metier entiere :
-- 1 = J+1, 2 = J+2. Les anciennes donnees peuvent contenir une date,
-- un timestamp ou une valeur trop grande pour un INT.
--
-- A executer une seule fois sur la base TPE_Managements avant de relancer
-- l'application.

USE TPE_Managements;

-- Verification avant correction.
SHOW COLUMNS FROM demandes LIKE 'value_date';
SELECT id, CAST(value_date AS CHAR) AS value_date
FROM demandes
ORDER BY id
LIMIT 20;

-- Sauvegarde complete avant modification destructive de colonne.
CREATE TABLE demandes_backup_before_value_date_fix_20260601 AS
SELECT *
FROM demandes;

-- Recreation de la colonne au bon type, en conservant seulement les valeurs
-- valides du domaine metier. Toute ancienne valeur differente de 2 devient 1.
ALTER TABLE demandes ADD COLUMN value_date_normalized INT DEFAULT 1;

UPDATE demandes
SET value_date_normalized = CASE
    WHEN value_date IS NULL THEN 1
    WHEN TRIM(CAST(value_date AS CHAR)) = '2' THEN 2
    ELSE 1
END;

ALTER TABLE demandes DROP COLUMN value_date;

ALTER TABLE demandes
    CHANGE COLUMN value_date_normalized value_date INT NOT NULL DEFAULT 1;

-- Verification apres correction.
SHOW COLUMNS FROM demandes LIKE 'value_date';
SELECT id, value_date
FROM demandes
ORDER BY id
LIMIT 20;
