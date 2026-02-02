-- Script de création de la base de données TPE Management
-- SQL Server

-- Créer la base de données
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'tpe_management')
BEGIN
    CREATE DATABASE tpe_management;
END
GO

USE tpe_management;
GO

-- Les tables seront créées automatiquement par Hibernate
-- Ce script contient les données initiales

-- Insertion des rôles
IF NOT EXISTS (SELECT * FROM roles WHERE name = 'ROLE_ADMIN')
BEGIN
    INSERT INTO roles (name, description) VALUES
    ('ROLE_ADMIN', 'Administrateur système'),
    ('ROLE_MONETIQUE', 'Service Monétique'),
    ('ROLE_AGENCE', 'Agence bancaire'),
    ('ROLE_INPUTER', 'Saisie des taux (Monétique)'),
    ('ROLE_AUTHORIZER', 'Validation des taux (Monétique)');
END
GO

PRINT 'Base de données initialisée avec succès';
