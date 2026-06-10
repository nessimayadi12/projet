-- ============================================
-- CREATE MISSING TABLES FOR HIBERNTE
-- ============================================

-- Table: commercants
CREATE TABLE IF NOT EXISTS commercants (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    created_by VARCHAR(255),
    created_date DATETIME(6) NOT NULL,
    last_modified_by VARCHAR(255),
    last_modified_date DATETIME(6),
    version BIGINT,
    
    raison_sociale VARCHAR(255) NOT NULL,
    activite VARCHAR(255) NOT NULL,
    numero_compte VARCHAR(255) NOT NULL,
    adresse VARCHAR(255),
    localite VARCHAR(255),
    code_postal VARCHAR(255),
    code_agence VARCHAR(255) NOT NULL,
    telephone VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    statut VARCHAR(50) NOT NULL DEFAULT 'ACTIF',
    loyer DOUBLE,
    rne_file_path VARCHAR(255),
    type_commerce VARCHAR(50),
    url_site_marchand VARCHAR(255),
    webhook_url VARCHAR(255),
    webmaster VARCHAR(255),
    contact_technique VARCHAR(255),
    type_cartes_acceptees VARCHAR(255),
    mode_test BIT(1) DEFAULT 0
);

-- Table: tpes
CREATE TABLE IF NOT EXISTS tpes (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    created_by VARCHAR(255),
    created_date DATETIME(6) NOT NULL,
    last_modified_by VARCHAR(255),
    last_modified_date DATETIME(6),
    version BIGINT,
    
    numero_terminal VARCHAR(255) UNIQUE,
    statut VARCHAR(50) NOT NULL DEFAULT 'ACTIF',
    type_devise VARCHAR(10),
    commercant_id BIGINT,
    agence_id BIGINT,
    
    FOREIGN KEY (commercant_id) REFERENCES commercants(id) ON DELETE SET NULL
);

-- Table: taux
CREATE TABLE IF NOT EXISTS taux (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    created_by VARCHAR(255),
    created_date DATETIME(6) NOT NULL,
    last_modified_by VARCHAR(255),
    last_modified_date DATETIME(6),
    version BIGINT,
    
    commercant_id BIGINT,
    ancien_taux_commission DOUBLE,
    nouveau_taux_commission DOUBLE NOT NULL,
    ancien_taux_commission_inter DOUBLE,
    nouveau_taux_commission_inter DOUBLE,
    statut VARCHAR(50) NOT NULL DEFAULT 'BROUILLON',
    inputer_id BIGINT,
    inputer_nom VARCHAR(255),
    authorizer_id BIGINT,
    authorizer_nom VARCHAR(255),
    date_saisie DATETIME(6),
    date_validation DATETIME(6),
    date_application DATETIME(6),
    motif_rejet TEXT,
    commentaire TEXT,
    actif BIT(1) DEFAULT 0,
    
    FOREIGN KEY (commercant_id) REFERENCES commercants(id) ON DELETE SET NULL,
    FOREIGN KEY (inputer_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (authorizer_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Table: demandes (si nécessaire)
CREATE TABLE IF NOT EXISTS demandes (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    created_by VARCHAR(255),
    created_date DATETIME(6) NOT NULL,
    last_modified_by VARCHAR(255),
    last_modified_date DATETIME(6),
    version BIGINT,
    
    commercant_id BIGINT,
    statut VARCHAR(50),
    type_demande VARCHAR(50),
    
    FOREIGN KEY (commercant_id) REFERENCES commercants(id) ON DELETE SET NULL
);

-- Verify
SELECT 'TABLES CREATED' as status;
SHOW TABLES LIKE '%';
