-- Correctif manuel pour l'erreur :
-- Data truncated for column 'type_commerce'
--
-- Cause probable : la base contient encore un ancien type ENUM limite a
-- PHYSIQUE / ECOMMERCE, alors que le code actuel utilise TPE / MOBILE.
-- A executer une seule fois sur la base TPE_Managements, application arretee.

USE TPE_Managements;

-- Verification avant correction.
SHOW COLUMNS FROM commercants LIKE 'type_commerce';
SELECT id, CAST(type_commerce AS CHAR) AS type_commerce
FROM commercants
ORDER BY id
LIMIT 20;

-- Sauvegarde des tables touchees par le renommage des types.
CREATE TABLE IF NOT EXISTS commercants_backup_before_type_fix_20260601 AS
SELECT *
FROM commercants;

CREATE TABLE IF NOT EXISTS demandes_backup_before_type_fix_20260601 AS
SELECT *
FROM demandes;

CREATE TABLE IF NOT EXISTS tpes_backup_before_type_fix_20260601 AS
SELECT *
FROM tpes;

-- Elargir les colonnes avant de remplacer les anciennes valeurs.
ALTER TABLE commercants
    MODIFY COLUMN type_commerce VARCHAR(50) NULL;

ALTER TABLE demandes
    MODIFY COLUMN type_demande VARCHAR(50) NOT NULL;

ALTER TABLE tpes
    MODIFY COLUMN typetpe VARCHAR(50) NOT NULL;

-- Normalisation des anciennes valeurs vers les valeurs metier actuelles.
UPDATE commercants
SET type_commerce = 'TPE'
WHERE type_commerce = 'PHYSIQUE';

UPDATE commercants
SET type_commerce = 'MOBILE'
WHERE type_commerce = 'ECOMMERCE';

UPDATE demandes
SET type_demande = 'TPE'
WHERE type_demande = 'PHYSIQUE';

UPDATE demandes
SET type_demande = 'MOBILE'
WHERE type_demande = 'ECOMMERCE';

UPDATE tpes
SET typetpe = 'TPE'
WHERE typetpe = 'PHYSIQUE';

UPDATE tpes
SET typetpe = 'MOBILE'
WHERE typetpe = 'ECOMMERCE';

-- Verification apres correction.
SHOW COLUMNS FROM commercants LIKE 'type_commerce';
SELECT id, type_commerce
FROM commercants
ORDER BY id
LIMIT 20;
