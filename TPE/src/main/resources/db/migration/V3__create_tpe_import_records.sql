-- Table de staging pour conserver toutes les lignes importées du fichier Excel
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[tpe_import_records]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[tpe_import_records] (
        [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [n_affiliation] VARCHAR(100) NOT NULL UNIQUE,
        [source_row_number] INT NOT NULL,
        [source_file_name] VARCHAR(255),
        [type_tpe] VARCHAR(50),
        [numero_serie] VARCHAR(100),
        [numero_terminal] VARCHAR(100),
        [raison_sociale] VARCHAR(255),
        [activite] VARCHAR(255),
        [mcc] VARCHAR(20),
        [numero_compte] VARCHAR(100),
        [code_agence] VARCHAR(50),
        [adresse] VARCHAR(500),
        [code_postal] VARCHAR(20),
        [telephone] VARCHAR(50),
        [email] VARCHAR(255),
        [privilege_secteur] VARCHAR(255),
        [taux_commission] VARCHAR(50),
        [taux_commission_inter] VARCHAR(50),
        [loyer] VARCHAR(50),
        [n_compte_intern] VARCHAR(100),
        [groupe] VARCHAR(100),
        [num_seq] VARCHAR(100),
        [active] BIT,
        [value_date] DATE,
        [date_affiliation] DATE,
        [raw_data_json] TEXT,
        [created_date] DATETIME NOT NULL DEFAULT GETDATE(),
        [last_modified_date] DATETIME,
        [created_by] VARCHAR(50),
        [last_modified_by] VARCHAR(50),
        [version] BIGINT
    );
END
GO