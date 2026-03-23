-- Create TPE records with 10-digit terminal numbers matching the test file
USE TPE_Managements;
GO

-- Get the commercant_id for COMMERCE TEST
DECLARE @commercant_id INT;
SELECT @commercant_id = id FROM commercants WHERE raison_sociale = 'COMMERCE TEST';

-- Insert TPE 2101400600
INSERT INTO tpes (numero_terminal, numero_serie, marque, modele, typetpe, statut, date_acquisition, last_modified_date, created_date, commercant_id)
VALUES ('2101400600', 'SN2101400600', 'INGENICO', 'iCT250', 'PHYSIQUE', 'AFFECTE', GETDATE(), GETDATE(), GETDATE(), @commercant_id);

-- Insert TPE 2401700750  
INSERT INTO tpes (numero_terminal, numero_serie, marque, modele, typetpe, statut, date_acquisition, last_modified_date, created_date, commercant_id)
VALUES ('2401700750', 'SN2401700750', 'INGENICO', 'iCT250', 'PHYSIQUE', 'AFFECTE', GETDATE(), GETDATE(), GETDATE(), @commercant_id);

-- Insert TPE 2501400570
INSERT INTO tpes (numero_terminal, numero_serie, marque, modele, typetpe, statut, date_acquisition, last_modified_date, created_date, commercant_id)
VALUES ('2501400570', 'SN2501400570', 'INGENICO', 'iCT250', 'PHYSIQUE', 'AFFECTE', GETDATE(), GETDATE(), GETDATE(), @commercant_id);

-- Verify the inserts
SELECT numero_terminal, marque, modele, statut, commercant_id 
FROM tpes 
WHERE numero_terminal IN ('2101400600', '2401700750', '2501400570');

PRINT 'TPE records created successfully with 10-digit terminal numbers';
GO
