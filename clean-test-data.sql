-- Nettoyer les données de test avant de réessayer
USE TPE_Managements;
GO

-- Supprimer toutes les écritures de test
DELETE FROM dbo.TPE_POSTING_comp WHERE sessiondate = '180226' OR sessiondate = '20260226';

PRINT 'Écritures de test supprimées';

-- Vérifier qu'il ne reste rien
SELECT COUNT(*) as nombre_ecritures FROM dbo.TPE_POSTING_comp WHERE sessiondate IN ('180226', '20260226');
GO
