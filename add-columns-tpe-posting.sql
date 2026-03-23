-- Ajout des colonnes PROFIT_CENTER et SEQ_NO à la table TPE_POSTING_comp
USE TPE_Managements;
GO

-- Vérifier si la colonne PROFIT_CENTER existe déjà
IF NOT EXISTS (SELECT * FROM sys.columns 
               WHERE object_id = OBJECT_ID(N'[dbo].[TPE_POSTING_comp]') 
               AND name = 'PROFIT_CENTER')
BEGIN
    ALTER TABLE dbo.TPE_POSTING_comp
    ADD PROFIT_CENTER NVARCHAR(10) NULL;
    PRINT 'Colonne PROFIT_CENTER ajoutée avec succès';
END
ELSE
BEGIN
    PRINT 'Colonne PROFIT_CENTER existe déjà';
END
GO

-- Vérifier si la colonne SEQ_NO existe déjà
IF NOT EXISTS (SELECT * FROM sys.columns 
               WHERE object_id = OBJECT_ID(N'[dbo].[TPE_POSTING_comp]') 
               AND name = 'SEQ_NO')
BEGIN
    ALTER TABLE dbo.TPE_POSTING_comp
    ADD SEQ_NO NVARCHAR(10) NULL;
    PRINT 'Colonne SEQ_NO ajoutée avec succès';
END ELSE
BEGIN
    PRINT 'Colonne SEQ_NO existe déjà';
END
GO

-- Vérifier la structure de la table
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'TPE_POSTING_comp'
ORDER BY ORDINAL_POSITION;
GO
