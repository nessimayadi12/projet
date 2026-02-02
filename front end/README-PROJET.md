# Système de Gestion du Parc TPE Bancaire - Frontend

## Description du Projet

Application web Angular pour la gestion complète du parc TPE (Terminaux de Paiement Électronique) et E-commerce de la banque ABC.

## Objectifs Principaux

- ✅ Centraliser le stock, les affectations, la maintenance et l'historique complet
- ✅ Fluidifier les échanges entre Agence et Monétique
- ✅ Digitaliser les workflows : demande → validation → affectation → mise en service
- ✅ Automatiser les processus manuels (génération TID, traçabilité, reporting)
- ✅ Améliorer la qualité de service pour les commerçants

## Architecture et Technologies

### Stack Technique
- **Framework** : Angular 14+
- **UI Framework** : Material Dashboard (Bootstrap + Material Design)
- **State Management** : RxJS
- **Authentification** : JWT
- **HTTP Client** : Angular HttpClient
- **Charts** : Chartist.js

### Structure du Projet

```
src/app/
├── models/                    # Modèles de données TypeScript
│   ├── tpe.model.ts          # TPE (Physique & E-commerce)
│   ├── commercant.model.ts   # Commerçants
│   ├── demande-tpe.model.ts  # Demandes TPE
│   ├── panne.model.ts        # Pannes et maintenance
│   ├── taux-tpe.model.ts     # Gestion des taux
│   ├── utilisateur.model.ts  # Utilisateurs et rôles
│   └── dashboard.model.ts    # Statistiques dashboard
│
├── services/                  # Services Angular
│   ├── tpe.service.ts        # CRUD et opérations TPE
│   ├── commercant.service.ts # Gestion commerçants
│   ├── demande.service.ts    # Workflow demandes
│   ├── panne.service.ts      # Maintenance et pannes
│   ├── taux-tpe.service.ts   # Système Inputer/Authorizer
│   ├── auth.service.ts       # Authentification
│   ├── dashboard.service.ts  # Statistiques
│   └── notification.service.ts # Notifications
│
├── components/                # Composants réutilisables
│   ├── navbar/               # Barre de navigation
│   ├── sidebar/              # Menu latéral
│   └── footer/               # Pied de page
│
├── tpe/                       # Module TPE
│   ├── tpe-list/             # Liste des TPE
│   ├── tpe-form/             # Formulaire TPE (Physique/E-commerce)
│   └── gestion-taux/         # Gestion des taux
│
├── commercants/               # Module Commerçants
│   ├── commercant-list/      # Liste commerçants
│   └── commercant-form/      # Formulaire commerçant
│
├── demandes/                  # Module Demandes
│   ├── demande-list/         # Liste demandes
│   ├── demande-form/         # Création demande
│   └── affectation-tpe/      # Affectation TPE
│
├── maintenance/               # Module Maintenance
│   └── panne-list/           # Gestion pannes
│
├── dashboard/                 # Tableau de bord
│   └── dashboard.component   # Vue d'ensemble
│
├── guards/                    # Guards de routage
│   └── auth.guard.ts         # Protection routes
│
├── interceptors/              # HTTP Interceptors
│   └── auth.interceptor.ts   # Injection token JWT
│
└── layouts/                   # Layouts
    └── admin-layout/         # Layout principal
```

## Acteurs du Système

### Monétique
**Rôles** : `MONETIQUE`, `INPUTER`, `AUTHORIZER`

**Permissions** :
- ✅ Gestion complète du stock TPE
- ✅ Validation des demandes Agence
- ✅ Affectation des TPE aux commerçants
- ✅ Gestion des contrats
- ✅ Supervision des pannes
- ✅ Mise à jour des statuts techniques
- ✅ Validation des taux (Authorizer)
- ✅ Saisie des taux (Inputer)
- ✅ Accès dashboards complets
- ✅ Génération TID (Numéro Terminal)

### Agence
**Rôle** : `AGENCE`

**Permissions** :
- ✅ Création de demandes TPE
- ✅ Signalement de pannes
- ✅ Suivi des demandes
- ✅ Accès dashboard Agence
- ✅ Création/modification commerçants

## Fonctionnalités Principales

### 1. Gestion du Stock TPE

#### TPE Physique
**Champs Monétiques** :
- Type TPE
- Raison sociale
- Activité
- MCC (Merchant Category Code)
- Taux commission
- Taux commission inter
- Numéro de compte (RIB)
- Code Agence
- Série TPE
- Value Date
- **N° Terminal (TID)** - Généré automatiquement

**Règle de génération TID** :
```
Structure : XX XXX XXX X
├─ 2 premiers chiffres : RIB (2 premiers du compte)
├─ 3 chiffres suivants : Code agence
├─ 3 chiffres suivants : Compteur terminal
└─ Dernier chiffre : Clé Luhn

Exemple : 23 041 008 5
```

#### TPE E-Commerce
**Champs spécifiques** :
- URL du Site Marchand *
- Webhook / URL de Callback
- Clé d'API / Identifiant terminal
- Numéro d'Affiliation
- Type de commerce (Marketplace, Retail, Services...)
- Cartes acceptées (Visa, Mastercard, DCI...)
- Mode Test / Production

**Statuts TPE** :
- `DISPONIBLE` - TPE prêt à être affecté
- `RESERVE` - Réservé pour une affectation
- `AFFECTE` - Affecté à un commerçant
- `EN_PANNE` - Panne déclarée
- `EN_MAINTENANCE` - En cours de réparation
- `HORS_SERVICE` - Hors d'usage

**Fonctionnalités** :
- ✅ Recherche multicritère
- ✅ Import massif (Excel / CSV)
- ✅ Export Excel
- ✅ Alertes stock bas
- ✅ Historique complet des modifications
- ✅ Affectation/Libération commerçant

### 2. Gestion des Commerçants

**Informations** :
- Raison Sociale
- Identifiant Unique RNE (upload fichier)
- Email / Téléphone
- Adresse, Code Postal, Ville
- Activité
- Numéro de compte
- Code Agence
- Loyer mensuel
- MCC
- Webmaster / Contact Technique (pour E-commerce)
- Email de Notification

**Statuts** :
- `ACTIF` - Commerçant actif
- `INACTIF` - Temporairement inactif
- `SUSPENDU` - Suspendu

**Fonctionnalités** :
- ✅ CRUD complet
- ✅ Historique des TPE affectés
- ✅ Upload fichier RNE
- ✅ Import/Export massif
- ✅ Top commerçants (plus de TPE)

### 3. Workflow des Demandes TPE

**Cycle de vie** :
```
NOUVELLE → EN_COURS → VALIDEE → AFFECTEE → CLOTUREE
                              ↓
                          REJETEE
```

**Processus** :

1️⃣ **Création par Agence**
- Sélection commerçant
- Type de demande (TPE Physique / E-Commerce)
- Type de TPE requis
- Niveau d'urgence
- Description détaillée
- Pièces jointes

2️⃣ **Notification Monétique**
- Email automatique
- Dashboard alerte

3️⃣ **Validation Monétique**
- Examen de la demande
- Validation ou Rejet avec motif
- Commentaires

4️⃣ **Affectation TPE**
- Sélection TPE disponible
- Génération TID (si nécessaire)
- Liaison commerçant ↔ TPE
- Génération documents :
  - Bon de livraison
  - Contrat

5️⃣ **Mise en service**
- Activation TPE
- Notification commerçant
- Clôture demande

**Urgences** :
- `BASSE` - Peut attendre
- `NORMALE` - Traitement standard
- `HAUTE` - Prioritaire
- `CRITIQUE` - Urgence immédiate

### 4. Maintenance & Pannes

**Workflow Panne** :
```
DECLAREE → DIAGNOSTIQUEE → EN_REPARATION → REPAREE → TESTEE → CLOTUREE
```

**Fonctionnalités** :
- ✅ Déclaration panne (Agence)
- ✅ Diagnostic technique
- ✅ Affectation technicien
- ✅ TPE de remplacement
- ✅ Suivi intervention
- ✅ Gestion pièces utilisées
- ✅ Test après réparation
- ✅ Temps de résolution (MTTR)
- ✅ Export rapport pannes

**Types d'urgence** :
- `FAIBLE` - Problème mineur
- `MOYENNE` - Problème standard
- `HAUTE` - Impact business
- `CRITIQUE` - Hors service complet

### 5. Gestion des Taux TPE

**Système à 4 yeux (Inputer/Authorizer)** :

#### Règle Métier
> ⚠️ **IMPORTANT** : Toute modification de taux (commission et commission inter) doit être réalisée en **deux étapes obligatoires** :
> 1. Saisie par un **Inputer**
> 2. Validation par un **Authorizer**
> 
> **Contrôle** : Inputer ≠ Authorizer (utilisateurs distincts)

**Workflow** :
```
BROUILLON → EN_ATTENTE_VALIDATION → VALIDE
                                  ↓
                              REJETE
```

**Processus** :

1️⃣ **Saisie (Inputer)**
- Sélection TPE
- Ancien taux (affiché)
- Nouveau taux commission
- Nouveau taux commission inter
- Commentaires
- Statut : `BROUILLON`

2️⃣ **Soumission pour validation**
- Passage en `EN_ATTENTE_VALIDATION`
- Notification Authorizer

3️⃣ **Validation (Authorizer)**
- Vérification : Authorizer ≠ Inputer
- Options :
  - **Valider** : Taux appliqué → `VALIDE`
  - **Rejeter** : Motif obligatoire → `REJETE`

**Traçabilité complète** :
- Ancien taux
- Nouveau taux
- Date saisie
- Inputer (nom + ID)
- Date validation
- Authorizer (nom + ID)
- Motif rejet (si applicable)

### 6. Dashboards

#### Dashboard Monétique
**Statistiques** :
- Répartition parc par statut
- Taux de disponibilité
- Demandes nouvelles/en cours
- Délai moyen de traitement
- Pannes en cours
- MTTR (Mean Time To Repair)
- Taux de panne
- Répartition par marque
- Top 10 commerçants
- Alertes stock bas
- Pannes dépassant SLA

**Graphiques** :
- Pie Chart : Statuts TPE
- Bar Chart : TPE par marque
- Line Chart : Évolution mensuelle
- Bar Chart : Demandes par statut
- Pie Chart : Pannes par type

#### Dashboard Agence
**Statistiques** :
- Mes demandes en cours
- Délai moyen de réponse
- Pannes déclarées
- TPE affectés (commerçants de l'agence)

## API Backend

**Base URL** : `http://localhost:8080/api`

### Endpoints Principaux

#### TPE
```
GET    /tpe                          # Liste tous les TPE
GET    /tpe/{id}                     # Détails TPE
GET    /tpe/numero-serie/{ns}        # TPE par numéro série
GET    /tpe/statut/{statut}          # TPE par statut
GET    /tpe/type/{type}              # TPE par type
GET    /tpe/disponibles              # TPE disponibles
GET    /tpe/commercant/{id}          # TPE d'un commerçant
GET    /tpe/search                   # Recherche multicritère
POST   /tpe                          # Créer TPE
POST   /tpe/physique                 # Créer TPE physique
POST   /tpe/ecommerce                # Créer TPE e-commerce
PUT    /tpe/{id}                     # Modifier TPE
PUT    /tpe/{id}/statut/{statut}    # Changer statut
POST   /tpe/{id}/affecter/{cid}     # Affecter à commerçant
POST   /tpe/{id}/liberer             # Libérer TPE
POST   /tpe/generer-tid              # Générer TID
GET    /tpe/{id}/historique          # Historique TPE
POST   /tpe/import                   # Import Excel
GET    /tpe/export                   # Export Excel
DELETE /tpe/{id}                     # Supprimer TPE
```

#### Commerçants
```
GET    /commercants                  # Liste commerçants
GET    /commercants/{id}             # Détails commerçant
GET    /commercants/{id}/historique-tpe # Historique TPE
POST   /commercants                  # Créer commerçant
PUT    /commercants/{id}             # Modifier commerçant
PUT    /commercants/{id}/statut/{s}  # Changer statut
POST   /commercants/{id}/upload-rne  # Upload RNE
GET    /commercants/top              # Top commerçants
DELETE /commercants/{id}             # Supprimer commerçant
```

#### Demandes
```
GET    /demandes                     # Liste demandes
GET    /demandes/{id}                # Détails demande
GET    /demandes/statut/{statut}    # Demandes par statut
GET    /demandes/en-attente          # Demandes en attente
POST   /demandes                     # Créer demande
PUT    /demandes/{id}                # Modifier demande
POST   /demandes/{id}/valider        # Valider demande
POST   /demandes/{id}/rejeter        # Rejeter demande
POST   /demandes/{id}/affecter/{tid} # Affecter TPE
POST   /demandes/{id}/cloturer       # Clôturer demande
POST   /demandes/{id}/piece-jointe   # Upload fichier
GET    /demandes/{id}/bon-livraison  # Générer bon livraison
GET    /demandes/{id}/contrat        # Générer contrat
```

#### Pannes
```
GET    /pannes                       # Liste pannes
GET    /pannes/{id}                  # Détails panne
GET    /pannes/statut/{statut}      # Pannes par statut
GET    /pannes/tpe/{id}             # Pannes d'un TPE
POST   /pannes                       # Déclarer panne
PUT    /pannes/{id}                  # Modifier panne
POST   /pannes/{id}/diagnostiquer    # Diagnostiquer
POST   /pannes/{id}/en-reparation    # Marquer en réparation
POST   /pannes/{id}/reparee          # Marquer réparée
POST   /pannes/{id}/tester           # Tester
POST   /pannes/{id}/cloturer         # Clôturer
POST   /pannes/{id}/tpe-remplacement/{tid} # Affecter remplacement
```

#### Taux
```
POST   /taux/saisir                  # Saisir taux (Inputer)
PUT    /taux/{id}/soumettre          # Soumettre validation
PUT    /taux/{id}/valider            # Valider (Authorizer)
PUT    /taux/{id}/rejeter            # Rejeter (Authorizer)
GET    /taux/en-attente              # Taux en attente validation
GET    /taux/historique/{tpeId}      # Historique taux TPE
```

#### Dashboard
```
GET    /dashboard/stats              # Statistiques globales
GET    /dashboard/repartition-statut # Répartition par statut
GET    /dashboard/pannes-periode     # Pannes par période
GET    /dashboard/performance-demandes # Performance demandes
GET    /dashboard/top-commercants    # Top commerçants
GET    /dashboard/alertes            # Alertes actives
```

## Installation et Démarrage

### Prérequis
- Node.js 14+
- npm 6+
- Angular CLI 14+

### Installation

```bash
# Cloner le projet
cd front\ end

# Installer les dépendances
npm install

# Lancer le serveur de développement
ng serve

# L'application sera disponible sur http://localhost:4200
```

### Build Production

```bash
# Build optimisé pour production
ng build --prod

# Les fichiers seront dans dist/
```

## Configuration

### Environment

**`src/environments/environment.ts`** (Développement)
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

**`src/environments/environment.prod.ts`** (Production)
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://api.production.com/api'
};
```

## Authentification & Sécurité

### JWT Token
- Token stocké dans `localStorage`
- Auto-injection via `AuthInterceptor`
- Expiration : vérifié à chaque requête
- Déconnexion auto si token invalide

### Roles & Permissions
```typescript
enum Role {
  ADMIN = 'ADMIN',
  MONETIQUE = 'MONETIQUE',
  AGENCE = 'AGENCE',
  INPUTER = 'INPUTER',
  AUTHORIZER = 'AUTHORIZER',
  TECHNICIEN = 'TECHNICIEN',
  COMMERCANT = 'COMMERCANT'
}
```

### Guards de Routes
```typescript
// Protection basée sur authentification
canActivate: [AuthGuard]

// Protection basée sur rôles
canActivate: [AuthGuard],
data: { roles: [Role.ADMIN, Role.MONETIQUE] }
```

## Tests

```bash
# Tests unitaires
npm run test

# Tests e2e
npm run e2e

# Coverage
npm run test:coverage
```

## Règles Métier Importantes

1. ✅ **Un TPE ne peut être affecté qu'à un seul commerçant à la fois**
2. ✅ **Un numéro de série est unique**
3. ✅ **Affectation possible seulement si statut = DISPONIBLE**
4. ✅ **Un commerçant peut avoir plusieurs TPE**
5. ✅ **Tous les changements de statut sont tracés**
6. ✅ **Agence initie la demande, Monétique valide**
7. ✅ **Modification taux : Inputer ≠ Authorizer (obligatoire)**
8. ✅ **URL site marchand obligatoire pour E-commerce**
9. ✅ **TID auto-généré selon règle Luhn**
10. ✅ **Traçabilité complète de toutes les actions**

## Support & Contact

- **Documentation Backend** : Voir `TPE/README.md`
- **API Documentation** : `TPE/API-ENDPOINTS.md`
- **Structure Projet** : `TPE/STRUCTURE.md`

## License

Propriétaire - Banque ABC © 2026
