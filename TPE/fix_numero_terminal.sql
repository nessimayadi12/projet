-- Script pour permettre les valeurs NULL dans la colonne numero_terminal
-- À exécuter dans SQL Server Management Studio ou via sqlcmd

USE TPE_Managements;
GO

-- Modifier la colonne pour permettre les NULL
ALTER TABLE tpes
ALTER COLUMN numero_terminal VARCHAR(50) NULL;
GO

PRINT 'Colonne numero_terminal modifiée avec succès pour permettre les NULL';
GO
