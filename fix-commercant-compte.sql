-- Corriger le numéro de compte du commercant pour correspondre au fichier CPABC049.txt
USE TPE_Managements;
GO

-- Mettre à jour le compte pour correspondre au format dans le fichier
UPDATE commercants 
SET numero_compte = '28000000501100000181'
WHERE raison_sociale = 'COMMERCE TEST';

-- Vérifier la mise à jour
SELECT 
    raison_sociale,
    numero_compte,
    SUBSTRING(numero_compte, 3, 3) AS BRANCH,
    SUBSTRING(numero_compte, 6, 6) AS CLIENT
FROM commercants 
WHERE raison_sociale = 'COMMERCE TEST';

PRINT '✅ Compte commercant mis à jour: 28000000501100000181';
PRINT '   BRANCH: 000 (positions 3-5)';
PRINT '   CLIENT: 000501 (positions 6-11)';
GO
