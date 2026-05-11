-- Script d'initialisation du système Screen/ScreenRole pour SQL Server
-- Ce script crée les tables et insère les données de base

-- ============================================
-- Création de la table screens
-- ============================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[screens]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[screens] (
        [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [code] VARCHAR(50) NOT NULL UNIQUE,
        [libelle] VARCHAR(100) NOT NULL,
        [description] VARCHAR(500),
        [route] VARCHAR(200) NOT NULL UNIQUE,
        [icon] VARCHAR(50),
        [ordre] INT,
        [parent_id] BIGINT,
        [actif] BIT NOT NULL DEFAULT 1,
        [created_date] DATETIME NOT NULL DEFAULT GETDATE(),
        [created_by] VARCHAR(50),
        [updated_by] VARCHAR(50),
        CONSTRAINT FK_screens_parent FOREIGN KEY ([parent_id]) REFERENCES [screens]([id]) ON DELETE NO ACTION
    );
END
GO

-- ============================================
-- Création de la table screen_roles
-- ============================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[screen_roles]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[screen_roles] (
        [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [screen_id] BIGINT NOT NULL,
        [role_id] BIGINT NOT NULL,
        [can_view] BIT DEFAULT 1,
        [can_create] BIT DEFAULT 0,
        [can_edit] BIT DEFAULT 0,
        [can_delete] BIT DEFAULT 0,
        [can_export] BIT DEFAULT 0,
        [created_date] DATETIME NOT NULL DEFAULT GETDATE(),
        CONSTRAINT UK_screen_role UNIQUE ([screen_id], [role_id]),
        CONSTRAINT FK_screen_roles_screen FOREIGN KEY ([screen_id]) REFERENCES [screens]([id]) ON DELETE CASCADE,
        CONSTRAINT FK_screen_roles_role FOREIGN KEY ([role_id]) REFERENCES [roles]([id]) ON DELETE CASCADE
    );
END
GO

-- ============================================
-- Insertion des screens de base
-- ============================================

-- Dashboard
IF NOT EXISTS (SELECT 1 FROM screens WHERE code = 'DASHBOARD')
INSERT INTO screens (code, libelle, description, route, icon, ordre, actif, created_date) VALUES
('DASHBOARD', 'Tableau de bord', 'Vue d''ensemble des statistiques et indicateurs', '/dashboard', 'dashboard', 1, 1, GETDATE());

-- Gestion des TPE
IF NOT EXISTS (SELECT 1 FROM screens WHERE code IN ('LISTE_TPE', 'CREER_TPE', 'MODIFIER_TPE', 'DETAIL_TPE'))
INSERT INTO screens (code, libelle, description, route, icon, ordre, actif, created_date) VALUES
('LISTE_TPE', 'Liste des TPE', 'Consultation de la liste des terminaux de paiement', '/tpe', 'devices', 2, 1, GETDATE()),
('CREER_TPE', 'Créer un TPE', 'Ajouter un nouveau terminal de paiement', '/tpe/new', 'add_circle', 3, 1, GETDATE()),
('MODIFIER_TPE', 'Modifier un TPE', 'Modifier les informations d''un terminal', '/tpe/:id/edit', 'edit', 4, 1, GETDATE()),
('DETAIL_TPE', 'Détail TPE', 'Voir les détails d''un terminal', '/tpe/:id', 'visibility', 5, 1, GETDATE());

-- Gestion des Commerçants
IF NOT EXISTS (SELECT 1 FROM screens WHERE code IN ('LISTE_COMMERCANTS', 'CREER_COMMERCANT', 'MODIFIER_COMMERCANT', 'DETAIL_COMMERCANT'))
INSERT INTO screens (code, libelle, description, route, icon, ordre, actif, created_date) VALUES
('LISTE_COMMERCANTS', 'Liste des commerçants', 'Consultation de la liste des commerçants', '/commercants', 'store', 6, 1, GETDATE()),
('CREER_COMMERCANT', 'Créer un commerçant', 'Ajouter un nouveau commerçant', '/commercants/new', 'person_add', 7, 1, GETDATE()),
('MODIFIER_COMMERCANT', 'Modifier un commerçant', 'Modifier les informations d''un commerçant', '/commercants/:id/edit', 'edit', 8, 1, GETDATE()),
('DETAIL_COMMERCANT', 'Détail commerçant', 'Voir les détails d''un commerçant', '/commercants/:id', 'visibility', 9, 1, GETDATE());

-- Gestion des Demandes
IF NOT EXISTS (SELECT 1 FROM screens WHERE code IN ('LISTE_DEMANDES', 'CREER_DEMANDE', 'MODIFIER_DEMANDE', 'DETAIL_DEMANDE', 'AFFECTER_TPE'))
INSERT INTO screens (code, libelle, description, route, icon, ordre, actif, created_date) VALUES
('LISTE_DEMANDES', 'Liste des demandes', 'Consultation des demandes de TPE', '/demandes', 'assignment', 10, 1, GETDATE()),
('CREER_DEMANDE', 'Créer une demande', 'Créer une nouvelle demande de TPE', '/demandes/new', 'add_box', 11, 1, GETDATE()),
('MODIFIER_DEMANDE', 'Modifier une demande', 'Modifier une demande existante', '/demandes/:id/edit', 'edit', 12, 1, GETDATE()),
('DETAIL_DEMANDE', 'Détail demande', 'Voir les détails d''une demande', '/demandes/:id', 'visibility', 13, 1, GETDATE()),
('AFFECTER_TPE', 'Affecter un TPE', 'Affecter un TPE à une demande', '/demandes/:id/affecter', 'link', 14, 1, GETDATE());

-- Gestion de la Maintenance
IF NOT EXISTS (SELECT 1 FROM screens WHERE code = 'LISTE_PANNES')
INSERT INTO screens (code, libelle, description, route, icon, ordre, actif, created_date) VALUES
('LISTE_PANNES', 'Liste des pannes', 'Consultation et gestion des pannes', '/pannes', 'build', 15, 1, GETDATE());

-- Gestion des Taux
IF NOT EXISTS (SELECT 1 FROM screens WHERE code = 'GESTION_TAUX')
INSERT INTO screens (code, libelle, description, route, icon, ordre, actif, created_date) VALUES
('GESTION_TAUX', 'Gestion des taux', 'Configuration des taux de commission', '/taux', 'percent', 16, 1, GETDATE());

-- Profil utilisateur
IF NOT EXISTS (SELECT 1 FROM screens WHERE code = 'PROFIL_UTILISATEUR')
INSERT INTO screens (code, libelle, description, route, icon, ordre, actif, created_date) VALUES
('PROFIL_UTILISATEUR', 'Profil utilisateur', 'Voir et modifier son profil', '/user-profile', 'person', 17, 1, GETDATE());

-- Administration
IF NOT EXISTS (SELECT 1 FROM screens WHERE code = 'GESTION_PERMISSIONS')
INSERT INTO screens (code, libelle, description, route, icon, ordre, actif, created_date) VALUES
('GESTION_PERMISSIONS', 'Gestion des permissions', 'Configuration des permissions par rôle', '/admin/screens', 'security', 18, 1, GETDATE());

GO

-- ============================================
-- Assignation des permissions par rôle
-- ============================================

-- ADMIN : Tous les droits sur tous les écrans
DELETE FROM screen_roles
WHERE role_id IN (SELECT id FROM roles WHERE name IN ('ROLE_TECHNICIEN', 'ROLE_LOGISTIQUE'));

DELETE FROM user_roles
WHERE role_id IN (SELECT id FROM roles WHERE name IN ('ROLE_TECHNICIEN', 'ROLE_LOGISTIQUE'));

DELETE FROM roles
WHERE name IN ('ROLE_TECHNICIEN', 'ROLE_LOGISTIQUE');

INSERT INTO screen_roles (screen_id, role_id, can_view, can_create, can_edit, can_delete, can_export)
SELECT s.id, r.id, 1, 1, 1, 1, 1
FROM screens s
CROSS JOIN roles r
WHERE r.name = 'ROLE_ADMIN'
AND NOT EXISTS (SELECT 1 FROM screen_roles sr WHERE sr.screen_id = s.id AND sr.role_id = r.id);

-- MONETIQUE : Accès au dashboard, TPE, demandes, pannes
INSERT INTO screen_roles (screen_id, role_id, can_view, can_create, can_edit, can_delete, can_export)
SELECT s.id, r.id, 
    1, 
    CASE WHEN s.code IN ('AFFECTER_TPE', 'LISTE_PANNES') THEN 1 ELSE 0 END,
    CASE WHEN s.code IN ('AFFECTER_TPE', 'LISTE_PANNES') THEN 1 ELSE 0 END,
    0,
    1
FROM screens s
CROSS JOIN roles r
WHERE r.name = 'ROLE_MONETIQUE'
AND s.code IN ('DASHBOARD', 'LISTE_TPE', 'DETAIL_TPE', 'LISTE_DEMANDES', 'DETAIL_DEMANDE', 'AFFECTER_TPE', 'LISTE_PANNES', 'PROFIL_UTILISATEUR')
AND NOT EXISTS (SELECT 1 FROM screen_roles sr WHERE sr.screen_id = s.id AND sr.role_id = r.id);

-- AGENCE : Gestion des commerçants et demandes (lecture seule)
INSERT INTO screen_roles (screen_id, role_id, can_view, can_create, can_edit, can_delete, can_export)
SELECT s.id, r.id, 
    1,
    0,
    0,
    0,
    0
FROM screens s
CROSS JOIN roles r
WHERE r.name = 'ROLE_AGENCE'
AND s.code IN ('LISTE_COMMERCANTS', 'DETAIL_COMMERCANT', 'LISTE_DEMANDES', 'DETAIL_DEMANDE', 'LISTE_PANNES', 'PROFIL_UTILISATEUR')
AND NOT EXISTS (SELECT 1 FROM screen_roles sr WHERE sr.screen_id = s.id AND sr.role_id = r.id);

-- INPUTER : Saisie des demandes
INSERT INTO screen_roles (screen_id, role_id, can_view, can_create, can_edit, can_delete, can_export)
SELECT s.id, r.id, 
    1,
    CASE WHEN s.code = 'CREER_DEMANDE' THEN 1 ELSE 0 END,
    0,
    0,
    0
FROM screens s
CROSS JOIN roles r
WHERE r.name = 'ROLE_INPUTER'
AND s.code IN ('LISTE_DEMANDES', 'CREER_DEMANDE', 'DETAIL_DEMANDE', 'PROFIL_UTILISATEUR')
AND NOT EXISTS (SELECT 1 FROM screen_roles sr WHERE sr.screen_id = s.id AND sr.role_id = r.id);

-- AUTHORIZER : Validation des demandes
INSERT INTO screen_roles (screen_id, role_id, can_view, can_create, can_edit, can_delete, can_export)
SELECT s.id, r.id, 
    1,
    0,
    CASE WHEN s.code IN ('MODIFIER_DEMANDE', 'GESTION_TAUX') THEN 1 ELSE 0 END,
    0,
    1
FROM screens s
CROSS JOIN roles r
WHERE r.name = 'ROLE_AUTHORIZER'
AND s.code IN ('LISTE_DEMANDES', 'DETAIL_DEMANDE', 'MODIFIER_DEMANDE', 'GESTION_TAUX', 'PROFIL_UTILISATEUR')
AND NOT EXISTS (SELECT 1 FROM screen_roles sr WHERE sr.screen_id = s.id AND sr.role_id = r.id);

-- COMMERCANT : Consultation de ses informations
INSERT INTO screen_roles (screen_id, role_id, can_view, can_create, can_edit, can_delete, can_export)
SELECT s.id, r.id, 
    1,
    0,
    0,
    0,
    0
FROM screens s
CROSS JOIN roles r
WHERE r.name = 'ROLE_COMMERCANT'
AND s.code IN ('DETAIL_COMMERCANT', 'DETAIL_DEMANDE', 'PROFIL_UTILISATEUR')
AND NOT EXISTS (SELECT 1 FROM screen_roles sr WHERE sr.screen_id = s.id AND sr.role_id = r.id);

GO

-- ============================================
-- Vérification
-- ============================================
SELECT 'Screens créés:' as Message, COUNT(*) as [Count] FROM screens;
SELECT 'Permissions créées:' as Message, COUNT(*) as [Count] FROM screen_roles;

-- Afficher les permissions par rôle
SELECT 
    r.name as [Role],
    COUNT(DISTINCT sr.screen_id) as NombreEcrans,
    SUM(CASE WHEN sr.can_create = 1 THEN 1 ELSE 0 END) as AvecCreation,
    SUM(CASE WHEN sr.can_edit = 1 THEN 1 ELSE 0 END) as AvecModification,
    SUM(CASE WHEN sr.can_delete = 1 THEN 1 ELSE 0 END) as AvecSuppression
FROM roles r
LEFT JOIN screen_roles sr ON sr.role_id = r.id
GROUP BY r.name
ORDER BY r.name;
