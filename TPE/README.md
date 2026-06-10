# Système de Gestion du Parc TPE Bancaire - Backend

Application Spring Boot pour la gestion du parc TPE et E-commerce de la Banque ABC.

## 📋 Table des matières

- [Prérequis](#prérequis)
- [Configuration](#configuration)
- [Installation](#installation)
- [Démarrage](#démarrage)
- [API Documentation](#api-documentation)
- [Utilisateurs par défaut](#utilisateurs-par-défaut)
- [Architecture](#architecture)
- [Fonctionnalités](#fonctionnalités)

## 🔧 Prérequis

- Java 17+
- Maven 3.8+
- SQL Server 2019+ (ou SQL Server Express)
- IDE (IntelliJ IDEA, Eclipse, VS Code)

## ⚙️ Configuration

### 1. Configuration de la base de données

Créer une base de données SQL Server :

```sql
CREATE DATABASE tpe_management;
```

### 2. Configuration de l'application

Modifier le fichier `src/main/resources/application.properties` :

```properties
# SQL Server Configuration
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=tpe_management;encrypt=true;trustServerCertificate=true
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

# JWT Configuration
jwt.secret=YOUR_SECRET_KEY_MINIMUM_32_CHARACTERS

```

## 📦 Installation

### 1. Cloner ou récupérer le projet

```bash
cd TPE
```

### 2. Installer les dépendances

```bash
mvn clean install
```

### 3. Compilation

```bash
mvn compile
```

## 🚀 Démarrage

### Démarrer l'application

```bash
mvn spring-boot:run
```

L'application démarre sur `http://localhost:8080`

### Vérification

```bash
curl http://localhost:8080/api/auth/login
```

## 📚 API Documentation

Une fois l'application démarrée, accéder à :

- **Swagger UI** : http://localhost:8080/swagger-ui.html
- **API Docs** : http://localhost:8080/api-docs

## 👥 Utilisateurs par défaut

L'application crée automatiquement les utilisateurs suivants :

| Username    | Password         | Rôle        | Description                    |
|-------------|------------------|-------------|--------------------------------|
| admin       | Admin@123        | ADMIN       | Administrateur système         |
| monetique   | Monetique@123    | MONETIQUE   | Service Monétique              |
| agence      | Agence@123       | AGENCE      | Agence bancaire                |
| inputer     | Inputer@123      | INPUTER     | Saisie des taux                |
| authorizer  | Authorizer@123   | AUTHORIZER  | Validation des taux (4 yeux)   |

## 🏗️ Architecture

### Structure du projet

```
src/main/java/com/banque/abc/tpe/
├── config/              # Configuration (Security, CORS, etc.)
├── controller/          # API REST Controllers
├── dto/                 # Data Transfer Objects
├── entity/              # JPA Entities
│   └── enums/          # Enumerations
├── exception/           # Gestion des exceptions
├── repository/          # JPA Repositories
├── security/            # JWT & Spring Security
├── service/             # Services métier
└── util/                # Utilitaires (TID Generator, etc.)
```

### Technologies utilisées

- **Spring Boot 3.2.1** - Framework
- **Spring Security** - Authentification & Autorisation
- **JWT** - Tokens
- **JPA/Hibernate** - ORM
- **SQL Server** - Base de données
- **ModelMapper** - Mapping DTO/Entity
- **Lombok** - Réduction du boilerplate
- **Swagger/OpenAPI** - Documentation API

## ✨ Fonctionnalités

### 1. Gestion des TPE
- ✅ Création, modification, suppression de TPE
- ✅ Gestion des statuts (Disponible, Affecté, En panne, etc.)
- ✅ Génération automatique du TID avec algorithme Luhn
- ✅ Historique complet des changements de statut
- ✅ Support TPE Physique et E-commerce

### 2. Gestion des Commerçants
- ✅ CRUD complet
- ✅ Gestion multi-TPE par commerçant
- ✅ Support E-commerce (URL, Webhook, API Keys)
- ✅ Recherche multicritère

### 3. Gestion des Demandes
- ✅ Workflow complet : Nouvelle → En cours → Validée → Affectée → Clôturée
- ✅ Validation par la Monétique
- ✅ Gestion des pièces jointes
- ✅ Commentaires et suivi

### 4. Affectation TPE
- ✅ Liaison TPE ↔ Commerçant
- ✅ Génération TID automatique
- ✅ Génération documents (bon de livraison, contrat)
- ✅ Validation des règles métier

### 5. Gestion des Pannes
- ✅ Déclaration de panne
- ✅ Workflow diagnostic → réparation → test
- ✅ TPE de remplacement
- ✅ Gestion pièces détachées
- ✅ Coûts et garantie

### 6. Gestion des Taux (Règle 4 yeux)
- ✅ Saisie par Inputer
- ✅ Validation par Authorizer (différent de l'Inputer)
- ✅ Workflow : Brouillon → En attente → Validé/Rejeté
- ✅ Traçabilité complète (ancien/nouveau taux)
- ✅ Historique des modifications

### 7. Sécurité & Audit
- ✅ Authentification JWT
- ✅ RBAC (Role-Based Access Control)
- ✅ 5 rôles différents avec permissions granulaires
- ✅ Audit complet (qui/quoi/quand)
- ✅ Logging détaillé

### 8. Règles Métier Implémentées
- ✅ Un TPE = un seul commerçant à la fois
- ✅ Numéro de série unique
- ✅ Affectation seulement si statut = Disponible
- ✅ Inputer ≠ Authorizer (règle 4 yeux)
- ✅ Validation Monétique obligatoire pour affectation

## 📖 Guide d'utilisation

### 1. Authentification

**Login**
```bash
POST /api/auth/login
{
    "username": "monetique",
    "password": "Monetique@123"
}
```

**Réponse**
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "username": "monetique",
    "email": "monetique@banque.com",
    "roles": ["ROLE_MONETIQUE"]
}
```

### 2. Créer un TPE

```bash
POST /api/tpes
Authorization: Bearer {token}
{
    "typeTPE": "PHYSIQUE",
    "numeroSerie": "TPE-2026-001",
    "marque": "Ingenico",
    "modele": "Move 5000"
}
```

### 3. Créer un Commerçant

```bash
POST /api/commercants
Authorization: Bearer {token}
{
    "raisonSociale": "Café Central",
    "activite": "Restauration",
    "numeroCompte": "12345678901234567890",
    "codeAgence": "041",
    "telephone": "0600000000",
    "email": "cafe.central@example.com"
}
```

### 4. Créer une Demande TPE

```bash
POST /api/demandes
Authorization: Bearer {token}
{
    "typeDemande": "PHYSIQUE",
    "commercantId": 1,
    "description": "Demande TPE pour nouveau commerçant",
    "urgence": false
}
```

### 5. Générer un TID

```bash
POST /api/tpes/1/generate-tid?rib=2304512345&codeAgence=041
Authorization: Bearer {token}
```

### 6. Saisir des Taux (Inputer)

```bash
POST /api/taux
Authorization: Bearer {token_inputer}
{
    "commercantId": 1,
    "nouveauTauxCommission": 1.5,
    "nouveauTauxCommissionInter": 0.8,
    "commentaire": "Nouveau taux commercial"
}
```

### 7. Valider des Taux (Authorizer)

```bash
POST /api/taux/1/valider
Authorization: Bearer {token_authorizer}
{
    "approuver": true
}
```

## 🔍 Génération du TID

### Règle de génération

Le TID (Terminal ID) est généré selon la formule :

**Structure** : `RR AAA CCC K`
- **RR** (2 chiffres) = 2 premiers chiffres du RIB
- **AAA** (3 chiffres) = Code agence
- **CCC** (3 chiffres) = Compteur terminal
- **K** (1 chiffre) = Clé de Luhn

**Exemple** :
- RIB: `23045...`
- Code Agence: `041`
- Compteur: `008`
- → TID généré: `23041008` + clé Luhn → `230410085`

### Algorithme Luhn

L'algorithme Luhn est utilisé pour la clé de contrôle :
1. Doubler un chiffre sur deux de droite à gauche
2. Si le résultat > 9, soustraire 9
3. Additionner tous les chiffres
4. La clé = ce qu'il faut ajouter pour atteindre le prochain multiple de 10

## 📊 Base de données

### Tables principales

- **users** - Utilisateurs
- **roles** - Rôles
- **user_roles** - Association users/roles
- **tpes** - Terminaux de paiement
- **commercants** - Commerçants
- **demandes** - Demandes TPE
- **affectations** - Affectations TPE/Commerçant
- **pannes** - Pannes et réparations
- **taux** - Taux de commission
- **historique_statuts** - Historique changements TPE
- **audit_logs** - Logs d'audit
- **commentaires** - Commentaires sur demandes
- **pieces_jointes** - Fichiers joints
- **pieces_detachees** - Pièces de réparation

## 🛡️ Sécurité

### Endpoints publics
- `/api/auth/login`
- `/api/auth/register`
- `/swagger-ui/**`
- `/api-docs/**`

### Endpoints protégés
Tous les autres endpoints nécessitent un JWT valide et les rôles appropriés.

### Permissions par rôle

| Fonctionnalité | ADMIN | MONETIQUE | AGENCE | INPUTER | AUTHORIZER |
|----------------|-------|-----------|--------|---------|------------|
| Créer TPE      | ✅    | ✅        | ❌     | ❌      | ❌         |
| Voir TPE       | ✅    | ✅        | ✅     | ❌      | ❌         |
| Créer Demande  | ✅    | ❌        | ✅     | ❌      | ❌         |
| Valider Demande| ✅    | ✅        | ❌     | ❌      | ❌         |
| Saisir Taux    | ✅    | ❌        | ❌     | ✅      | ❌         |
| Valider Taux   | ✅    | ❌        | ❌     | ❌      | ✅         |

## 🐛 Dépannage

### Erreur de connexion à la base de données
```
Vérifier que SQL Server est démarré
Vérifier les credentials dans application.properties
```

### Port 8080 déjà utilisé
```properties
# Changer le port dans application.properties
server.port=8081
```

### JWT Secret trop court
```
Le secret JWT doit faire au moins 32 caractères
```

## 📝 Notes importantes

1. **Règle 4 yeux** : Un Inputer ne peut JAMAIS valider ses propres saisies de taux
2. **TID unique** : Chaque TID doit être unique dans tout le système
3. **Affectation** : Un TPE ne peut être affecté que s'il est DISPONIBLE
4. **Audit** : Toutes les actions critiques sont loggées

## 🔄 Workflow complet

### Création d'un nouveau commerçant avec TPE

1. **Agence** : Crée le commerçant
2. **Agence** : Crée une demande TPE
3. **Monétique** : Valide la demande
4. **Monétique** : Sélectionne un TPE disponible
5. **Monétique** : Génère le TID
6. **Monétique** : Crée l'affectation
7. **Système** : Change le statut TPE → AFFECTE
8. **Système** : Notifie l'agence

### Modification de taux (4 yeux)

1. **Inputer** : Saisit les nouveaux taux (statut: BROUILLON)
2. **Inputer** : Soumet à validation (statut: EN_ATTENTE_VALIDATION)
3. **Authorizer** : Valide ou rejette
4. Si validé : Taux activé, ancien taux désactivé
5. **Système** : Log complet dans audit_logs

## 📞 Support

Pour toute question ou problème, contactez l'équipe de développement.

## 📄 Licence

© 2026 Banque ABC - Tous droits réservés
