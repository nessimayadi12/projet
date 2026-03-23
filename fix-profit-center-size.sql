-- Fix PROFIT_CENTER column size to accommodate longer codes if needed
-- Current: varchar(10), Changing to: varchar(50)

USE TPE_Managements;
GO

-- Alter the column size
ALTER TABLE tpe_posting_comp
ALTER COLUMN PROFIT_CENTER varchar(50) NULL;
GO

-- Verify the change
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'tpe_posting_comp' 
AND COLUMN_NAME = 'PROFIT_CENTER';
GO

PRINT 'PROFIT_CENTER column size updated to varchar(50)';
