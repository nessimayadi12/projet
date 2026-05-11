-- Created by GitHub Copilot in SSMS - review carefully before executing
-- Script d'initialisation du système Screen/ScreenRole pour SQL Server
-- Ce script insère les données de base (les tables existent déjà)

-- ============================================
-- Insertion des rôles manquants
-- ============================================
DELETE FROM dbo.screen_roles
WHERE role_id IN (SELECT id FROM dbo.roles WHERE name IN ('ROLE_TECHNICIEN', 'ROLE_LOGISTIQUE'));

DELETE FROM dbo.user_roles
WHERE role_id IN (SELECT id FROM dbo.roles WHERE name IN ('ROLE_TECHNICIEN', 'ROLE_LOGISTIQUE'));

DELETE FROM dbo.roles
WHERE name IN ('ROLE_TECHNICIEN', 'ROLE_LOGISTIQUE');

IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE name = 'ROLE_COMMERCANT')
    INSERT INTO dbo.roles (name, description) VALUES ('ROLE_COMMERCANT', 'Rôle pour les commerçants');

-- ============================================
-- Nettoyage des données existantes (optionnel)
-- ============================================
-- DELETE FROM dbo.screen_roles;
-- DELETE FROM dbo.screens;

-- ============================================
-- Insertion des screens de base
-- ============================================

-- Dashboard
INSERT INTO dbo.screens (code, libelle, description, route, icon, ordre, actif, created_date) VALUES
('DASHBOARD', 'Tableau de bord', 'Vue d''ensemble des statistiques et indicateurs', '/dashboard', 'dashboard', 1, 1, SYSDATETIME());

-- Gestion des TPE
INSERT INTO dbo.screens (code, libelle, description, route, icon, ordre, actif, created_date) VALUES
('TPE_LIST', 'Liste des TPE', 'Consultation de la liste des terminaux de paiement', '/tpe', 'devices', 2, 1, SYSDATETIME()),
('TPE_CREATE', 'Créer un TPE', 'Ajouter un nouveau terminal de paiement', '/tpe/new', 'add_circle', 3, 1, SYSDATETIME()),
('TPE_EDIT', 'Modifier un TPE', 'Modifier les informations d''un terminal', '/tpe/:id/edit', 'edit', 4, 1, SYSDATETIME()),
('TPE_VIEW', 'Détail TPE', 'Voir les détails d''un terminal', '/tpe/:id', 'visibility', 5, 1, SYSDATETIME());

-- Gestion des Commerçants
INSERT INTO dbo.screens (code, libelle, description, route, icon, ordre, actif, created_date) VALUES
('COMMERCANT_LIST', 'Liste des commerçants', 'Consultation de la liste des commerçants', '/commercants', 'store', 6, 1, SYSDATETIME()),
('COMMERCANT_CREATE', 'Créer un commerçant', 'Ajouter un nouveau commerçant', '/commercants/new', 'person_add', 7, 1, SYSDATETIME()),
('COMMERCANT_EDIT', 'Modifier un commerçant', 'Modifier les informations d''un commerçant', '/commercants/:id/edit', 'edit', 8, 1, SYSDATETIME()),
('COMMERCANT_VIEW', 'Détail commerçant', 'Voir les détails d''un commerçant', '/commercants/:id', 'visibility', 9, 1, SYSDATETIME());

-- Gestion des Demandes
INSERT INTO dbo.screens (code, libelle, description, route, icon, ordre, actif, created_date) VALUES
('DEMANDE_LIST', 'Liste des demandes', 'Consultation des demandes de TPE', '/demandes', 'assignment', 10, 1, SYSDATETIME()),
('DEMANDE_CREATE', 'Créer une demande', 'Créer une nouvelle demande de TPE', '/demandes/new', 'add_box', 11, 1, SYSDATETIME()),
('DEMANDE_EDIT', 'Modifier une demande', 'Modifier une demande existante', '/demandes/:id/edit', 'edit', 12, 1, SYSDATETIME()),
('DEMANDE_VIEW', 'Détail demande', 'Voir les détails d''une demande', '/demandes/:id', 'visibility', 13, 1, SYSDATETIME()),
('DEMANDE_AFFECTER', 'Affecter un TPE', 'Affecter un TPE à une demande', '/demandes/:id/affecter', 'link', 14, 1, SYSDATETIME());

-- Gestion de la Maintenance
INSERT INTO dbo.screens (code, libelle, description, route, icon, ordre, actif, created_date) VALUES
('PANNE_LIST', 'Liste des pannes', 'Consultation et gestion des pannes', '/pannes', 'build', 15, 1, SYSDATETIME());

-- Gestion des Taux
INSERT INTO dbo.screens (code, libelle, description, route, icon, ordre, actif, created_date) VALUES
('TAUX_GESTION', 'Gestion des taux', 'Configuration des taux de commission', '/taux', 'percent', 16, 1, SYSDATETIME());

-- Profil utilisateur
INSERT INTO dbo.screens (code, libelle, description, route, icon, ordre, actif, created_date) VALUES
('USER_PROFILE', 'Profil utilisateur', 'Voir et modifier son profil', '/user-profile', 'person', 17, 1, SYSDATETIME());

-- Administration
INSERT INTO dbo.screens (code, libelle, description, route, icon, ordre, actif, created_date) VALUES
('SCREEN_MANAGEMENT', 'Gestion des permissions', 'Configuration des permissions par rôle', '/admin/screens', 'security', 18, 1, SYSDATETIME());

-- ============================================
-- Assignation des permissions par rôle
-- ============================================

-- ADMIN : Tous les droits sur tous les écrans
INSERT INTO dbo.screen_roles (screen_id, role_id, can_view, can_create, can_edit, can_delete, can_export, created_at)
SELECT s.id, r.id, 1, 1, 1, 1, 1, SYSDATETIME()
FROM dbo.screens s
CROSS JOIN dbo.roles r
WHERE r.name = 'ROLE_ADMIN';

-- MONETIQUE : Accès au dashboard, TPE, demandes, pannes
INSERT INTO dbo.screen_roles (screen_id, role_id, can_view, can_create, can_edit, can_delete, can_export, created_at)
SELECT s.id, r.id, 
    1, 
    CASE WHEN s.code IN ('DEMANDE_AFFECTER', 'PANNE_LIST') THEN 1 ELSE 0 END,
    CASE WHEN s.code IN ('DEMANDE_AFFECTER', 'PANNE_LIST') THEN 1 ELSE 0 END,
    0,
    1,
    SYSDATETIME()
FROM dbo.screens s
CROSS JOIN dbo.roles r
WHERE r.name = 'ROLE_MONETIQUE'
AND s.code IN ('DASHBOARD', 'TPE_LIST', 'TPE_VIEW', 'DEMANDE_LIST', 'DEMANDE_VIEW', 'DEMANDE_AFFECTER', 'PANNE_LIST', 'USER_PROFILE');

-- AGENCE : Gestion des commerçants et demandes
INSERT INTO dbo.screen_roles (screen_id, role_id, can_view, can_create, can_edit, can_delete, can_export, created_at)
SELECT s.id, r.id, 
    1,
    CASE WHEN s.code IN ('COMMERCANT_CREATE', 'DEMANDE_CREATE') THEN 1 ELSE 0 END,
    CASE WHEN s.code IN ('COMMERCANT_EDIT', 'DEMANDE_EDIT') THEN 1 ELSE 0 END,
    0,
    1,
    SYSDATETIME()
FROM dbo.screens s
CROSS JOIN dbo.roles r
WHERE r.name = 'ROLE_AGENCE'
AND s.code IN ('COMMERCANT_LIST', 'COMMERCANT_CREATE', 'COMMERCANT_EDIT', 'COMMERCANT_VIEW', 
               'DEMANDE_LIST', 'DEMANDE_CREATE', 'DEMANDE_EDIT', 'DEMANDE_VIEW', 'PANNE_LIST', 'USER_PROFILE');

-- INPUTER : Saisie des demandes
INSERT INTO dbo.screen_roles (screen_id, role_id, can_view, can_create, can_edit, can_delete, can_export, created_at)
SELECT s.id, r.id, 
    1,
    CASE WHEN s.code = 'DEMANDE_CREATE' THEN 1 ELSE 0 END,
    0,
    0,
    0,
    SYSDATETIME()
FROM dbo.screens s
CROSS JOIN dbo.roles r
WHERE r.name = 'ROLE_INPUTER'
AND s.code IN ('DEMANDE_LIST', 'DEMANDE_CREATE', 'DEMANDE_VIEW', 'USER_PROFILE');

-- AUTHORIZER : Validation des demandes
INSERT INTO dbo.screen_roles (screen_id, role_id, can_view, can_create, can_edit, can_delete, can_export, created_at)
SELECT s.id, r.id, 
    1,
    0,
    CASE WHEN s.code IN ('DEMANDE_EDIT', 'TAUX_GESTION') THEN 1 ELSE 0 END,
    0,
    1,
    SYSDATETIME()
FROM dbo.screens s
CROSS JOIN dbo.roles r
WHERE r.name = 'ROLE_AUTHORIZER'
AND s.code IN ('DEMANDE_LIST', 'DEMANDE_VIEW', 'DEMANDE_EDIT', 'TAUX_GESTION', 'USER_PROFILE');

-- COMMERCANT : Consultation de ses informations
INSERT INTO dbo.screen_roles (screen_id, role_id, can_view, can_create, can_edit, can_delete, can_export, created_at)
SELECT s.id, r.id, 
    1,
    0,
    0,
    0,
    0,
    SYSDATETIME()
FROM dbo.screens s
CROSS JOIN dbo.roles r
WHERE r.name = 'ROLE_COMMERCANT'
AND s.code IN ('COMMERCANT_VIEW', 'DEMANDE_VIEW', 'USER_PROFILE');

-- ============================================
-- Vérification
-- ============================================
SELECT 'Screens créés:' AS Message, COUNT(*) AS Count FROM dbo.screens;
SELECT 'Permissions créées:' AS Message, COUNT(*) AS Count FROM dbo.screen_roles;

-- Afficher les permissions par rôle
SELECT 
    r.name AS Role,
    COUNT(DISTINCT sr.screen_id) AS NombreEcrans,
    SUM(CASE WHEN sr.can_create = 1 THEN 1 ELSE 0 END) AS AvecCreation,
    SUM(CASE WHEN sr.can_edit = 1 THEN 1 ELSE 0 END) AS AvecModification,
    SUM(CASE WHEN sr.can_delete = 1 THEN 1 ELSE 0 END) AS AvecSuppression
FROM dbo.roles r
LEFT JOIN dbo.screen_roles sr ON sr.role_id = r.id
GROUP BY r.name
ORDER BY r.name;
