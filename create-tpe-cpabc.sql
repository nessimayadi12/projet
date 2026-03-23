-- Créer des TPE pour tester avec le fichier CPABC049.txt
USE TPE_Managements;
GO

-- Récupérer l'ID du commercant TEST
DECLARE @commercant_id INT;
SELECT @commercant_id = id FROM commercants WHERE raison_sociale = 'COMMERCE TEST';

-- Supprimer les anciens TPE de test s'ils existent
DELETE FROM tpes WHERE numero_terminal IN (
    '2800000164', '2800000180', '2800000206', '2800001121', 
    '2800001766', '2800001774', '2800001816', '2800001840'
);

-- Créer les TPE basés sur CPABC049.txt - Première série (branch 000, client 000501)
INSERT INTO tpes (numero_terminal, numero_serie, numero_affiliation, commercant_id, statut, typetpe, marque, modele, created_date, version)
VALUES 
('2800000164', 'SN164', '2800000164', @commercant_id, 'AFFECTE', 'PHYSIQUE', 'INGENICO', 'iWL250', GETDATE(), 0),
('2800000180', 'SN180', '2800000180', @commercant_id, 'AFFECTE', 'PHYSIQUE', 'INGENICO', 'iWL250', GETDATE(), 0),
('2800000206', 'SN206', '2800000206', @commercant_id, 'AFFECTE', 'PHYSIQUE', 'INGENICO', 'iWL250', GETDATE(), 0),
('2800001121', 'SN1121', '2800001121', @commercant_id, 'AFFECTE', 'PHYSIQUE', 'INGENICO', 'iWL250', GETDATE(), 0);

-- Vérifier la création
SELECT 
    t.numero_terminal,
    t.statut,
    c.raison_sociale,
    c.numero_compte
FROM tpes t
LEFT JOIN commercants c ON t.commercant_id = c.id
WHERE t.numero_terminal IN ('2800000164', '2800000180', '2800000206', '2800001121');

PRINT '✅ TPE créés pour fichier CPABC049.txt';
GO
