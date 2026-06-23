# Documentation des Endpoints API

## Base URL
```
http://localhost:8080/api
```

## Authentication

### POST /auth/login
Authentification utilisateur

**Request Body:**
```json
{
    "username": "admin",
    "password": "Admin@123"
}
```

**Response:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "username": "admin",
    "email": "admin@banque.com",
    "roles": ["ROLE_ADMIN"]
}
```

### POST /auth/register
Enregistrement d'un nouvel utilisateur

**Permissions:** ADMIN

---

## Assistant IA Metier

Endpoints disponibles aussi via `/assistant-ia`.

### POST /assistant/interroger
Poser une question metier libre en langage naturel. L'assistant appelle Groq en deux etapes :
- lecture dynamique du schema SQL reel via `information_schema`
- generation d'un SELECT MySQL securise a partir de ce schema
- execution via JdbcTemplate apres validation SELECT uniquement
- correction automatique par Groq si la premiere requete echoue
- reformulation en francais professionnel a partir des donnees retournees

**Permissions:** MONETIQUE, AGENCE, ADMIN

**Request Body:**
```json
{
    "question": "Quels sont les TPE en panne depuis plus de 7 jours ?"
}
```

**Response:**
```json
{
    "question": "Quels sont les TPE en panne depuis plus de 7 jours ?",
    "reponseIA": "Voici les TPE actuellement en panne depuis plus de 7 jours...",
    "sqlGenere": "SELECT ...",
    "explication": "Recherche des TPE en panne depuis plus de 7 jours",
    "donnees": [],
    "nombreResultats": 0,
    "erreur": false,
    "messageErreur": null
}
```

Alias compatibles :
- `POST /assistant-metier/questions`
- `POST /assistant-ia/questions`

---

## TPE Endpoints

### POST /tpes
Créer un nouveau TPE

**Permissions:** MONETIQUE, ADMIN

**Request Body:**
```json
{
    "typeTPE": "PHYSIQUE",
    "numeroSerie": "TPE-2026-001",
    "marque": "Ingenico",
    "modele": "Move 5000",
    "mcc": "5814"
}
```

### GET /tpes
Lister tous les TPE (avec pagination)

**Permissions:** MONETIQUE, AGENCE, ADMIN

**Query Params:**
- `page` (optional): Numéro de page (défaut: 0)
- `size` (optional): Taille de page (défaut: 20)

### GET /tpes/{id}
Obtenir un TPE par ID

**Permissions:** MONETIQUE, AGENCE, ADMIN

### GET /tpes/disponibles
Lister les TPE disponibles

**Permissions:** MONETIQUE, ADMIN

### GET /tpes/statut/{statut}
Lister les TPE par statut

**Permissions:** MONETIQUE, AGENCE, ADMIN

**Valeurs possibles:** DISPONIBLE, RESERVE, AFFECTE, EN_PANNE, MAINTENANCE, HORS_SERVICE

### PUT /tpes/{id}
Mettre à jour un TPE

**Permissions:** MONETIQUE, ADMIN

### PATCH /tpes/{id}/statut
Changer le statut d'un TPE

**Permissions:** MONETIQUE, ADMIN

**Query Params:**
- `statut`: Nouveau statut
- `commentaire` (optional): Commentaire

### POST /tpes/{id}/generate-tid
Générer un TID pour un TPE

**Permissions:** MONETIQUE, ADMIN

**Query Params:**
- `rib`: RIB du commerçant
- `codeAgence`: Code agence

**Response:** String (TID généré)

### DELETE /tpes/{id}
Supprimer un TPE

**Permissions:** ADMIN

---

## Commerçants Endpoints

### POST /commercants
Créer un nouveau commerçant

**Permissions:** MONETIQUE, AGENCE, ADMIN

**Request Body:**
```json
{
    "raisonSociale": "Café Central",
    "activite": "Restauration",
    "numeroCompte": "12345678901234567890",
    "codeAgence": "041",
    "adresse": "123 Rue Principale",
    "codePostal": "1000",
    "telephone": "0600000000",
    "email": "cafe.central@example.com",
    "loyer": 50.00
}
```

**E-commerce spécifique:**
```json
{
    "raisonSociale": "Boutique en ligne",
    "activite": "E-commerce",
    "numeroCompte": "98765432109876543210",
    "codeAgence": "041",
    "email": "contact@boutique.com",
    "typeCommerce": "ECOMMERCE",
    "urlSiteMarchand": "https://www.boutique.com",
    "webhookUrl": "https://www.boutique.com/webhook",
    "webmaster": "webmaster@boutique.com",
    "typeCartesAcceptees": "Visa,Mastercard,DCI",
    "modeTest": true
}
```

### GET /commercants
Lister tous les commerçants (avec pagination)

**Permissions:** MONETIQUE, AGENCE, ADMIN

### GET /commercants/{id}
Obtenir un commerçant par ID

**Permissions:** MONETIQUE, AGENCE, ADMIN

### GET /commercants/search?query={query}
Rechercher des commerçants

**Permissions:** MONETIQUE, AGENCE, ADMIN

### GET /commercants/agence/{codeAgence}
Lister les commerçants par agence

**Permissions:** MONETIQUE, AGENCE, ADMIN

### PUT /commercants/{id}
Mettre à jour un commerçant

**Permissions:** MONETIQUE, AGENCE, ADMIN

### PATCH /commercants/{id}/statut
Changer le statut d'un commerçant

**Permissions:** MONETIQUE, ADMIN

**Query Params:**
- `statut`: ACTIF, INACTIF, SUSPENDU

### DELETE /commercants/{id}
Supprimer un commerçant

**Permissions:** ADMIN

---

## Demandes Endpoints

### POST /demandes
Créer une nouvelle demande

**Permissions:** AGENCE, ADMIN

**Request Body:**
```json
{
    "typeDemande": "PHYSIQUE",
    "commercantId": 1,
    "description": "Demande TPE pour nouveau commerçant",
    "urgence": false
}
```

### GET /demandes
Lister toutes les demandes (avec pagination)

**Permissions:** MONETIQUE, AGENCE, ADMIN

### GET /demandes/{id}
Obtenir une demande par ID

**Permissions:** MONETIQUE, AGENCE, ADMIN

### POST /demandes/{id}/valider
Valider ou rejeter une demande

**Permissions:** MONETIQUE, ADMIN

**Request Body:**
```json
{
    "approuver": true,
    "commentaire": "Demande validée"
}
```

### PATCH /demandes/{id}/cloturer
Clôturer une demande

**Permissions:** MONETIQUE, ADMIN

---

## Pannes / Maintenance Endpoints

### Workflow obligatoire

```text
Agence/Admin:
DECLAREE

Monetique/Admin:
DECLAREE -> DIAGNOSTIQUEE -> EN_REPARATION

Depuis EN_REPARATION:
EN_REPARATION -> REPAREE
EN_REPARATION -> IRRECUPERABLE + nouveauNumeroSerie
```

**Regles metier:**
- La declaration est autorisee uniquement si le TPE est deja `AFFECTE`, `EN_PANNE` ou `MAINTENANCE`.
- Une panne creee passe toujours au statut `DECLAREE`.
- Les transitions directes hors workflow sont refusees.
- Le passage a `IRRECUPERABLE` exige la saisie d'un nouveau numero de serie pour creer le TPE de remplacement.
- Une panne `REPAREE` remet le TPE en service: `AFFECTE` si une affectation active existe, sinon `DISPONIBLE`.

### POST /pannes
Declarer une panne depuis Maintenance.

**Permissions:** AGENCE, ADMIN

**Request Body:**
```json
{
    "tpeId": 1,
    "typePanne": "HARDWARE",
    "description": "Ecran noir au demarrage"
}
```

**Response:** statut `DECLAREE`.

### POST /pannes/{id}/diagnostiquer
Faire le diagnostic d'une panne.

**Permissions:** MONETIQUE, ADMIN

**Transition:** `DECLAREE -> DIAGNOSTIQUEE`

**Request Body:**
```json
{
    "diagnostic": "Carte mere defectueuse"
}
```

### POST /pannes/{id}/en-reparation
Demarrer la reparation.

**Permissions:** MONETIQUE, ADMIN

**Transition:** `DIAGNOSTIQUEE -> EN_REPARATION`

### POST /pannes/{id}/reparee
Marquer la panne comme resolue.

**Permissions:** MONETIQUE, ADMIN

**Transition:** `EN_REPARATION -> REPAREE`

**Request Body:**
```json
{
    "solution": "Carte mere remplacee et tests OK"
}
```

### POST /pannes/{id}/irrecuperable
Marquer le TPE comme irrecuperable et creer un TPE de remplacement.

**Permissions:** MONETIQUE, ADMIN

**Transition:** `EN_REPARATION -> IRRECUPERABLE`

**Request Body:**
```json
{
    "nouveauNumeroSerie": "TPE-2026-REM-001",
    "commentaire": "Remplacement suite a panne irreparable"
}
```

### GET /pannes/export/excel
Exporter le suivi des pannes au format Excel.

**Permissions:** AGENCE, MONETIQUE, ADMIN

**Query Params optionnels:**
- `debut`: date ISO, ex: `2026-06-01T00:00:00`
- `fin`: date ISO, ex: `2026-06-30T23:59:59`

### GET /pannes/export/pdf
Exporter le suivi des pannes au format PDF.

**Permissions:** AGENCE, MONETIQUE, ADMIN

**Query Params optionnels:** identiques a `/pannes/export/excel`.

---

## Taux Endpoints (4 Yeux)

### POST /taux
Créer/Saisir un nouveau taux

**Permissions:** INPUTER, ADMIN

**Request Body:**
```json
{
    "commercantId": 1,
    "nouveauTauxCommission": 1.5,
    "nouveauTauxCommissionInter": 0.8,
    "commentaire": "Nouveau taux commercial"
}
```

### GET /taux/{id}
Obtenir un taux par ID

**Permissions:** MONETIQUE, INPUTER, AUTHORIZER, ADMIN

### POST /taux/{id}/soumettre
Soumettre un taux à validation

**Permissions:** INPUTER, ADMIN

### POST /taux/{id}/valider
Valider ou rejeter un taux

**Permissions:** AUTHORIZER, ADMIN

**Request Body:**
```json
{
    "approuver": true
}
```
ou
```json
{
    "approuver": false,
    "motifRejet": "Taux trop élevé"
}
```

### GET /taux/en-attente
Lister les taux en attente de validation

**Permissions:** AUTHORIZER, ADMIN

### GET /taux/commercant/{commercantId}
Lister tous les taux d'un commerçant

**Permissions:** MONETIQUE, INPUTER, AUTHORIZER, ADMIN

---

## Codes de statut HTTP

- **200 OK**: Succès
- **201 Created**: Ressource créée
- **400 Bad Request**: Erreur de validation
- **401 Unauthorized**: Non authentifié
- **403 Forbidden**: Accès refusé
- **404 Not Found**: Ressource non trouvée
- **409 Conflict**: Conflit (ex: doublon)
- **500 Internal Server Error**: Erreur serveur

---

## Format des erreurs

```json
{
    "timestamp": "2026-01-28T10:30:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Le numéro de série est obligatoire",
    "path": "/api/tpes",
    "validationErrors": {
        "numeroSerie": "Le numéro de série est obligatoire"
    }
}
```

---

## Headers requis

Tous les endpoints protégés nécessitent :

```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

---

## Exemples d'utilisation

### 1. Workflow complet - Création TPE pour commerçant

#### Étape 1: Login (Agence)
```bash
POST /api/auth/login
{
    "username": "agence",
    "password": "Agence@123"
}
```

#### Étape 2: Créer le commerçant
```bash
POST /api/commercants
{
    "raisonSociale": "Café Central",
    "activite": "Restauration",
    "numeroCompte": "12345678901234567890",
    "codeAgence": "041",
    "email": "cafe@example.com"
}
```

#### Étape 3: Créer une demande TPE
```bash
POST /api/demandes
{
    "typeDemande": "PHYSIQUE",
    "commercantId": 1,
    "description": "Premier TPE"
}
```

#### Étape 4: Login (Monétique) et valider
```bash
POST /api/auth/login
{
    "username": "monetique",
    "password": "Monetique@123"
}

POST /api/demandes/1/valider
{
    "approuver": true,
    "commentaire": "Approuvé"
}
```

#### Étape 5: Créer le TPE
```bash
POST /api/tpes
{
    "typeTPE": "PHYSIQUE",
    "numeroSerie": "TPE-001",
    "marque": "Ingenico"
}
```

#### Étape 6: Générer le TID
```bash
POST /api/tpes/1/generate-tid?rib=2304512345&codeAgence=041
```

### 2. Workflow Taux (4 yeux)

#### Étape 1: Login (Inputer)
```bash
POST /api/auth/login
{
    "username": "inputer",
    "password": "Inputer@123"
}
```

#### Étape 2: Saisir le taux
```bash
POST /api/taux
{
    "commercantId": 1,
    "nouveauTauxCommission": 1.5,
    "nouveauTauxCommissionInter": 0.8
}
```

#### Étape 3: Soumettre à validation
```bash
POST /api/taux/1/soumettre
```

#### Étape 4: Login (Authorizer)
```bash
POST /api/auth/login
{
    "username": "authorizer",
    "password": "Authorizer@123"
}
```

#### Étape 5: Valider le taux
```bash
POST /api/taux/1/valider
{
    "approuver": true
}
```

---

## Pagination

Les endpoints avec pagination acceptent ces paramètres :

- `page`: Numéro de page (commence à 0)
- `size`: Nombre d'éléments par page
- `sort`: Critère de tri (ex: `createdDate,desc`)

**Exemple:**
```
GET /api/tpes?page=0&size=10&sort=createdDate,desc
```

**Réponse:**
```json
{
    "content": [...],
    "pageable": {...},
    "totalElements": 50,
    "totalPages": 5,
    "last": false,
    "size": 10,
    "number": 0
}
```

---

## Notes importantes

1. **JWT Token** : Expire après 24h (configurable)
2. **Règle 4 yeux** : Un Inputer ne peut JAMAIS valider ses propres taux
3. **TID** : Généré automatiquement avec algorithme Luhn
4. **Audit** : Toutes les actions sont loggées
5. **Notifications** : Emails automatiques pour les événements importants
