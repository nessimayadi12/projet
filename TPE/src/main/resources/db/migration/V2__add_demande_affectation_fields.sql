-- Migration: Ajout des champs de demande d'affectation TPE
-- Date: 2026-01-29
-- Description: Ajout des champs nécessaires pour le formulaire de demande et validation Monétique

-- Ajout des champs de demande Agence (TPE Physique)
ALTER TABLE demandes ADD COLUMN raison_sociale VARCHAR(255);
ALTER TABLE demandes ADD COLUMN activite VARCHAR(255);
ALTER TABLE demandes ADD COLUMN numero_compte VARCHAR(50);
ALTER TABLE demandes ADD COLUMN adresse VARCHAR(500);
ALTER TABLE demandes ADD COLUMN code_postal VARCHAR(10);
ALTER TABLE demandes ADD COLUMN code_agence VARCHAR(20);
ALTER TABLE demandes ADD COLUMN telephone VARCHAR(20);
ALTER TABLE demandes ADD COLUMN rne_file_path VARCHAR(500);

-- Ajout des champs de validation Monétique (TPE Physique)
ALTER TABLE demandes ADD COLUMN mcc VARCHAR(4);
ALTER TABLE demandes ADD COLUMN taux_commission DECIMAL(5,2);
ALTER TABLE demandes ADD COLUMN taux_commission_inter DECIMAL(5,2);
ALTER TABLE demandes ADD COLUMN loyer DECIMAL(10,2);
ALTER TABLE demandes ADD COLUMN serie_tpe VARCHAR(50);
ALTER TABLE demandes ADD COLUMN numero_terminal VARCHAR(20);
ALTER TABLE demandes ADD COLUMN value_date INT DEFAULT 1;

-- Ajout des champs spécifiques E-commerce
ALTER TABLE demandes ADD COLUMN localite VARCHAR(255);
ALTER TABLE demandes ADD COLUMN rib VARCHAR(50);
ALTER TABLE demandes ADD COLUMN webmaster VARCHAR(255);
ALTER TABLE demandes ADD COLUMN contact_technique VARCHAR(255);
ALTER TABLE demandes ADD COLUMN url_site_marchand VARCHAR(500);

-- Modification du champ urgence de BOOLEAN à ENUM
-- Changer le type de la colonne urgence pour accepter les valeurs d'enum
ALTER TABLE demandes MODIFY COLUMN urgence VARCHAR(20) DEFAULT 'NORMALE';

-- Création d'un index sur le code_agence pour améliorer les performances
CREATE INDEX idx_demandes_code_agence ON demandes(code_agence);
CREATE INDEX idx_demandes_numero_terminal ON demandes(numero_terminal);

-- Commentaires sur les colonnes
-- EXEC sp_addextendedproperty 
--     @name = N'MS_Description', 
--     @value = N'Raison sociale du commerçant (demande agence)', 
--     @level0type = N'Schema', @level0name = 'dbo',
--     @level1type = N'Table', @level1name = 'demandes',
--     @level2type = N'Column', @level2name = 'raison_sociale';

PRINT 'Migration terminée: Colonnes de demande d''affectation TPE ajoutées avec succès';
