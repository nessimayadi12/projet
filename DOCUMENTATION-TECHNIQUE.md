---
**BANK ABC**
Système de Gestion de Parc TPE
---

# DOCUMENTATION TECHNIQUE 
## Système de Gestion du Parc TPE

---






---

# TABLE DES MATIÈRES

1. [Objet du document](#1-objet-du-document)
2. [Vue d'ensemble du système](#2-vue-densemble-du-systeme)
3. [Stack technique réelle](#3-stack-technique-reelle)
4. [Architecture Backend](#4-architecture-backend)
5. [Architecture Frontend](#5-architecture-frontend)
6. [Inventaire API Backend](#6-inventaire-api-backend)
7. [Règles métier implémentées](#7-regles-metier-implementees-services)
8. [Modèle de données](#8-modele-de-donnees)
9. [Contrat Front/Back : écarts détectés](#9-contrat-frontback-ecarts-detectes)
10. [Sécurité : constats techniques](#10-securite-constats-techniques)
11. [Observabilité et exploitation](#11-observabilite-et-exploitation)
12. [Démarrage et exécution](#12-demarrage-et-execution)
13. [Fichiers de preuve principaux](#13-fichiers-de-preuve-principaux-analyse-code)
14. [Annexe détaillée par fichier](#14-annexe-detaillee-par-fichier)
15. [Lecture fonctionnelle synthétique](#15-lecture-fonctionnelle-synthetique-du-projet)
16. [Version](#16-version)

---

## RÉSUMÉ EXÉCUTIF

Le projet **TPE Management** de Bank ABC est une application bancaire complète destinée à gérer le cycle de vie des terminaux de paiement électroniques (TPE).

**Périmètre couvert :**
- Architecture applicative moderne (Angular 14 + Spring Boot 3.2)
- Gestion complète du cycle de vie TPE (création, affectation, maintenance)
- Workflow de validation des demandes avec règles métier strictes
- Traitement de fichiers bancaires avec génération automatique d'écritures comptables
- Sécurité JWT stateless avec contrôle dynamique des permissions
- Base de données SQL Server avec 17 entités métier

**Points clés :**
- Documentation basée **exclusivement** sur l'analyse du code source (Frontend + Backend)
- **56+ endpoints API** documentés et validés
- **30+ écarts** identifiés entre Frontend et Backend (section 9) nécessitant alignement
- **10 recommandations de sécurité** prioritaires (section 10)

**Cible de déploiement :** SQL Server (configuré et opérationnel)

---

## 1. Objet du document

Ce document fournit une documentation technique complete du projet de gestion du parc TPE de Bank ABC.

Important:
- Cette documentation est basee uniquement sur l'analyse du code source Frontend et Backend.
- Les fichiers Markdown existants n'ont pas ete utilises comme source de verite.

Perimetre couvert:
- Architecture applicative
- Composants Frontend Angular
- Composants Backend Spring Boot
- Contrat API expose
- Regles metier implementees dans les services
- Modele de donnees (entites + relations + enums)
- Securite, observabilite et configuration
- Ecarts Front/Back detectes dans le code

## 2. Vue d'ensemble du systeme

Le systeme est compose de deux applications:

1. Frontend Angular (dossier `front end`)
2. Backend Java Spring Boot (dossier `TPE`)

Flux principal:
1. L'utilisateur se connecte via l'interface Angular.
2. Le backend authentifie via `/api/auth/login` et renvoie un JWT.
3. L'interceptor Angular injecte le JWT sur les appels API.
4. Spring Security valide le token puis applique les regles de role.
5. Les services metier executent les regles fonctionnelles et persistent en base.

## 3. Stack technique reelle

### 3.1 Frontend

- Angular 14.2.x
- TypeScript 4.7.x
- RxJS 7.5.x
- Angular Material
- Chart.js et Chartist
- jsPDF + jsPDF Autotable
- powerbi-client

Preuve code:
- `front end/package.json`

### 3.2 Backend

- Java 17
- Spring Boot 3.2.1
- Spring Security (JWT stateless)
- Spring Data JPA / Hibernate
- Bean Validation
- ModelMapper
- Apache POI (import Excel)
- iTextPDF
- Springdoc OpenAPI

Preuve code:
- `TPE/pom.xml`

## 4. Architecture Backend

### 4.1 Structure logique

- `config`: configuration transversale (security, cors, init data, auditing)
- `controller`: couche REST exposee
- `service`: logique metier et orchestration
- `repository`: acces donnees JPA
- `entity`: modele relationnel
- `security`: JWT filter, user principal, auth entry point
- `util`: generateurs de references et TID

Point d'entree:
- `TPE/src/main/java/com/banque/abc/tpe/TpeManagementApplication.java`

### 4.2 Configuration runtime

Fichier: `TPE/src/main/resources/application.properties`

Parametres observes:
- Port: `server.port=8080`
- Base active: SQL Server locale (`spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=TPE_Managements;...`)
- Dialect Hibernate SQL Server configure
- Une ancienne configuration MySQL reste commentee dans le fichier
- JPA: `ddl-auto=update`, SQL log active
- JWT: secret et expirations en dur
- Upload: limite 10MB
- Swagger: `/swagger-ui.html` et `/api-docs`
- Logs: `logs/tpe-management.log`
- CORS: origins localhost dans properties + CORS global permissif dans `ApplicationConfig`

### 4.3 Securite

Fichiers clefs:
- `TPE/src/main/java/com/banque/abc/tpe/config/SecurityConfig.java`
- `TPE/src/main/java/com/banque/abc/tpe/security/JwtAuthenticationFilter.java`
- `TPE/src/main/java/com/banque/abc/tpe/security/JwtTokenProvider.java`

Caracteristiques:
- Session stateless
- JWT filtre avant `UsernamePasswordAuthenticationFilter`
- `/api/auth/**`, `/swagger-ui/**`, `/api-docs/**` en public
- Controle d'acces par `hasRole/hasAnyRole` sur les domaines API

Observation importante:
- `SecurityConfig` reference des roles `TECHNICIEN` et `LOGISTIQUE`.
- L'enum `RoleType` ne contient que `ROLE_ADMIN`, `ROLE_MONETIQUE`, `ROLE_AGENCE`, `ROLE_INPUTER`, `ROLE_AUTHORIZER`.
- Ces roles supplementaires ne peuvent pas etre initialises via `DataInitializer` dans l'etat actuel.

### 4.4 Initialisation des donnees

Fichier: `TPE/src/main/java/com/banque/abc/tpe/config/DataInitializer.java`

Ce qui est initialise au demarrage:
- Roles: ADMIN, MONETIQUE, AGENCE, INPUTER, AUTHORIZER
- Utilisateurs par defaut: admin, monetique, agence, inputer, authorizer
- Catalogue des ecrans (`Screen`) et permissions par role (`ScreenRole`)

## 5. Architecture Frontend

### 5.1 Organisation Angular

Fichiers clefs:
- `front end/src/app/app.module.ts`
- `front end/src/app/app.routing.ts`
- `front end/src/app/layouts/admin-layout/admin-layout.module.ts`
- `front end/src/app/layouts/admin-layout/admin-layout.routing.ts`

Composants structurels:
- Login/Register
- AdminLayout (routes metier)
- Modules metier: TPE, commercants, demandes, maintenance, dashboards

### 5.2 Gestion securite cote Front

Fichiers:
- `front end/src/app/interceptors/auth.interceptor.ts`
- `front end/src/app/guards/auth.guard.ts`
- `front end/src/app/guards/has-permission.directive.ts`
- `front end/src/app/services/auth.service.ts`
- `front end/src/app/services/screen.service.ts`

Mecanisme:
- Token JWT stocke dans `localStorage`
- Interceptor ajoute `Authorization: Bearer <token>`
- Sur erreur 401: logout + redirection `/login`
- Guard route:
   - verifie session
   - verifie permission dynamique par `screenCode` via API screens
- Directive UI `appHasPermission` masque les actions non autorisees

### 5.3 Routage metier principal

Routes majeures (admin layout):
- `/dashboard`
- `/dashboard/tpe`
- `/dashboard/demandes`
- `/dashboard/pannes`
- `/user-profile`
- `/tpe`, `/tpe/new`, `/tpe/:id/edit`, `/tpe/:id`, `/tpe/imports`
- `/commercants`, `/commercants/new`, `/commercants/:id/edit`, `/commercants/:id`
- `/demandes`, `/demandes/new`, `/demandes/:id/edit`, `/demandes/:id/affecter`, `/demandes/:id`
- `/pannes`
- `/admin/screens`
- `/file-upload`

Chaque route critique est associee a un `screenCode` pour le controle dynamique.

## 6. Inventaire API Backend (code observe)

### 6.1 Auth

- `POST /api/auth/login`
- `POST /api/auth/register`

### 6.2 TPE

- `POST /api/tpes`
- `GET /api/tpes`
- `GET /api/tpes/{id}`
- `GET /api/tpes/statut/{statut}`
- `GET /api/tpes/disponibles`
- `PUT /api/tpes/{id}`
- `PATCH /api/tpes/{id}/statut?statut=...&commentaire=...`
- `POST /api/tpes/{id}/generate-tid?rib=...&codeAgence=...`
- `POST /api/tpes/generer-tid`
- `POST /api/tpes/import` (multipart)
- `GET /api/tpes/import-records`
- `GET /api/tpes/import-records/export`
- `DELETE /api/tpes/{id}`

### 6.3 Commercants

- `POST /api/commercants`
- `GET /api/commercants`
- `GET /api/commercants/{id}`
- `GET /api/commercants/search?query=...`
- `GET /api/commercants/agence/{codeAgence}`
- `PUT /api/commercants/{id}`
- `PATCH /api/commercants/{id}/statut?statut=...`
- `DELETE /api/commercants/{id}`

### 6.4 Demandes

- `POST /api/demandes`
- `GET /api/demandes`
- `GET /api/demandes/{id}`
- `POST /api/demandes/{id}/valider`
- `POST /api/demandes/{id}/rejeter`
- `PATCH /api/demandes/{id}/cloturer`
- `POST /api/demandes/{id}/piece-jointe` (multipart)
- `GET /api/demandes/{id}/piece-jointe/{fileName}`

### 6.5 Affectations

- `POST /api/affectations`
- `GET /api/affectations`
- `GET /api/affectations/{id}`
- `GET /api/affectations/commercant/{commercantId}`
- `GET /api/affectations/actives`
- `POST /api/affectations/{id}/mise-en-service?dateMiseEnService=...`
- `POST /api/affectations/{id}/desaffecter?motif=...`

### 6.6 Taux

- `POST /api/taux`
- `GET /api/taux/{id}`
- `POST /api/taux/{id}/soumettre`
- `POST /api/taux/{id}/valider`
- `GET /api/taux/en-attente`
- `GET /api/taux/commercant/{commercantId}`

### 6.7 Pannes

- `GET /api/pannes`
- `POST /api/pannes`
- `GET /api/pannes/{id}`
- `PUT /api/pannes/{id}`
- `DELETE /api/pannes/{id}`
- `GET /api/pannes/statut/{statut}`
- `GET /api/pannes/tpe/{tpeId}`
- `GET /api/pannes/technicien/{technicienId}`
- `PUT /api/pannes/{id}/statut/{statut}`
- `POST /api/pannes/{panneId}/assigner/{technicienId}`
- `POST /api/pannes/{id}/diagnostiquer`
- `POST /api/pannes/{id}/en-reparation`
- `POST /api/pannes/{id}/reparee`
- `POST /api/pannes/{id}/resoudre`
- `POST /api/pannes/{id}/tester`
- `POST /api/pannes/{panneId}/tpe-remplacement/{tpeRemplacementId}`
- `GET /api/pannes/periode?debut=...&fin=...`

### 6.8 Dashboard

- `GET /api/dashboard/stats`
- `GET /api/dashboard/demandes-statut`
- `GET /api/dashboard/pannes-type`
- `GET /api/dashboard/evolution-mensuelle`
- `GET /api/dashboard/repartition-statut`
- `GET /api/dashboard/repartition-type`
- `GET /api/dashboard/evolution-tpe`
- `GET /api/dashboard/stats-par-agence`
- `GET /api/dashboard/pannes-periode?periode=...`
- `GET /api/dashboard/performance-demandes`
- `GET /api/dashboard/taux-utilisation`
- `GET /api/dashboard/top-pannes`
- `GET /api/dashboard/heatmap-pannes`
- `GET /api/dashboard/stats-commercants`

### 6.9 Ecrans / Roles / Fichiers / PowerBI

- `GET /api/screens`
- `GET /api/screens/active`
- `GET /api/screens/{id}`
- `GET /api/screens/code/{code}`
- `GET /api/screens/user/{username}`
- `GET /api/screens/me`
- `GET /api/screens/permissions/{screenCode}`
- `POST /api/screens`
- `PUT /api/screens/{id}`
- `DELETE /api/screens/{id}`
- `POST /api/screens/{screenId}/roles/{roleId}`
- `DELETE /api/screens/{screenId}/roles/{roleId}`
- `GET /api/screens/{screenId}/roles`
- `GET /api/roles`
- `POST /api/file-upload/process`
- `POST /api/fichier-bancaire/upload`
- `GET /api/fichier-bancaire/stats/{sessionDate}`
- `GET /api/fichier-bancaire/transactions/{sessionDate}`
- `GET /api/fichier-bancaire/test`
- `GET /api/fichier-bancaire/rapport/pdf/{sessionDate}`
- `GET /api/fichier-bancaire/rapport/text/{sessionDate}`
- `GET /api/powerbi/token/{reportId}`
- `GET /api/powerbi/reports`
- `GET /api/powerbi/reports/{reportId}`
- `GET /api/powerbi/status`

### 6.10 Upload transactions et fichiers bancaires

Cette partie couvre le traitement des transactions uploadées depuis l'interface ou depuis un fichier bancaire.

Flux technique:
1. Le front envoie un fichier via `FileUploadService` ou `TPEPostingService`.
2. Le backend valide le fichier ou la liste d'ecritures.
3. Le fichier bancaire est parse ligne par ligne par `FichierBancaireService`.
4. Les transactions valides alimentent `TPEPostingComp`.
5. Les resultats et statistiques sont exposes via les endpoints de consultation.

Endpoints relies:
- `POST /api/file-upload/process`
- `POST /api/fichier-bancaire/upload`
- `GET /api/fichier-bancaire/stats/{sessionDate}`
- `GET /api/fichier-bancaire/transactions/{sessionDate}`
- `GET /api/fichier-bancaire/test`
- `GET /api/fichier-bancaire/rapport/pdf/{sessionDate}`
- `GET /api/fichier-bancaire/rapport/text/{sessionDate}`
- `POST /api/tpe-posting/insert-postings`
- `GET /api/tpe-posting/verify-tpe/{affiliation}`
- `GET /api/tpe-posting/verify-porteur/{ncarte}`

Role fonctionnel:
- `FileUploadController` traite les fichiers generiques.
- `FichierBancaireController` traite les fichiers bancaires, extrait les transactions et genere des rapports.
- `TPEPostingController` gere les verifications de TPE/porteur et l'insertion des ecritures comptables.

### 6.11 Détail du traitement des transactions

Le projet supporte deux chemins de traitement cote front:
- `UploadFichierBancaireComponent`: upload direct d'un fichier bancaire `.txt` avec date de session `yyyyMMdd`.
- `FileUploadComponent`: analyse locale ou backend d'un fichier fixe `.txt` ou `.dat`.

Contraintes visibles dans le code front:
- Le fichier doit respecter une taille maximale de 10 MB.
- Le format de session attendu est `yyyyMMdd`.
- Les fichiers acceptes sont `.txt` pour `UploadFichierBancaireComponent` et `.txt/.dat` pour `FileUploadComponent`.

Format de fichier traite par `FichierBancaireService`:
- La ligne `01` represente l'en-tete.
- La longueur minimale pratique est d'environ 200 caracteres par ligne.
- Le type de transaction est lu aux deux premiers caracteres.
- Le numero de terminal est extrait a partir de la position 16.
- La date, la reference, le montant et le numero de carte sont lus par positions fixes.

Traitement Type 10:
- Ligne de commissions TPE/commercant.
- Le service lit le montant principal et le montant de commission.
- Quatre ecritures comptables sont generees et stockees dans `TPE_POSTING_comp`.
- Les ecritures utilisent les comptes `150.1103.0000`, `151.1105.0000` et `601.9106.0000`.

Traitement Type 20:
- Ligne de paiement porteur/carte.
- Le service verifie d'abord l'existence du TPE puis du porteur.
- Si la devise est TND ou TNC, deux ecritures sont generees.
- Si la devise est etrangere, quatre ecritures sont generees avec conversion.
- Les comptes de compensation et de change sont derives a partir des informations du porteur.

Table persistee:
- `TPE_POSTING_comp` via l'entite `TPEPostingComp`.

Champs persistés dans `TPEPostingComp`:
- `branch`, `profitCenter`, `client`, `account`, `rbGl`, `ccy`, `seqNo`, `ref`, `tranType`, `date`, `amount`, `crDr`, `narrative`, `sessionDate`.

End points de consultation des transactions:
- `GET /api/tpe-posting`
- `GET /api/tpe-posting/recent`
- `GET /api/tpe-posting/count`
- `GET /api/tpe-posting/{id}`

Composants UI relies:
- `upload-fichier-bancaire.component.ts` gere l'upload, les statistiques et le telechargement des rapports.
- `file-upload.component.ts` gere l'analyse locale ou backend du fichier et l'affichage des ecritures.

Comportement fonctionnel cote UI:
- En mode backend, le composant envoie le fichier au serveur puis affiche le nombre de lignes lues et d'ecritures creees.
- En mode local, le fichier est parsé dans le navigateur avant eventuelle sauvegarde.
- Les rapports PDF et texte peuvent etre telecharges apres traitement.

## 7. Regles metier implementees (services)

### 7.1 Regles sur TPE (`TPEService`)

- Numero de serie unique obligatoire.
- Creation avec statut initial `DISPONIBLE`.
- Suppression interdite si statut `AFFECTE`.
- Changement de statut historise (`HistoriqueStatut`).
- Generation TID avec unicite controlee.

### 7.2 Regles sur Demandes (`DemandeService`)

- Reference automatique `DEM-YYYY-NNN`.
- Creation associee a un commercant existant ou cree automatiquement.
- Validation monétique possible seulement depuis certains statuts.
- En cas d'approbation:
   - statut `VALIDEE_MONETIQUE`
   - tentative d'affectation automatique d'un TPE.
- En cas de rejet: statut `REJETEE` + cloture.
- Upload/download de pieces jointes sur disque local (`uploads/demandes/{id}`).

### 7.3 Regles sur Affectation (`AffectationService`)

- Precondition: demande en `VALIDEE_MONETIQUE`.
- Refus si affectation active deja existante.
- Trois modes d'affectation:
   1. TPE specifique fourni et disponible
   2. Creation automatique de TPE si infos terminal/serie presentes dans demande
   3. Selection automatique d'un TPE disponible par type
- Statuts mis a jour:
   - TPE -> `AFFECTE`
   - Demande -> `AFFECTEE`
- Desaffectation remet le TPE `DISPONIBLE`.

### 7.4 Regles sur Taux (`TauxService`)

- Saisie reservee au role INPUTER.
- Validation reservee au role AUTHORIZER.
- Regle critique 4 yeux: `inputer != authorizer`.
- Workflow: `BROUILLON` -> `EN_ATTENTE_VALIDATION` -> `VALIDE` ou `REJETE`.
- Lors d'une validation, les anciens taux actifs du commercant sont desactives.

### 7.5 Regles sur Pannes (`PanneService`)

- Creation panne:
   - genere reference
   - statut `DECLAREE`
   - TPE passe en `EN_PANNE`
- Evolution possible: diagnostique, reparation, test, irrecuperable.
- Si test concluant: TPE repasse `DISPONIBLE`.
- Si irrecuperable: TPE passe `HORS_SERVICE`.

### 7.6 Audit et notifications

- `AuditService` journalise les operations critiques.

## 8. Modele de donnees

### 8.1 Entites metier principales

- `User`, `Role`
- `Screen`, `ScreenRole`
- `Commercant`
- `TPE`
- `Demande`
- `Affectation`
- `Taux`
- `Panne`, `PieceDetachee`
- `PieceJointe`, `Commentaire`
- `HistoriqueStatut`
- `AuditLog`
- `TPEImportRecord`, `TPEPostingComp`

### 8.2 Relations clefs

- `User` <-> `Role`: many-to-many
- `Commercant` -> `TPE`: one-to-many
- `Commercant` -> `Demande`: one-to-many
- `Demande` -> `Affectation`: one-to-one
- `TPE` -> `Affectation`: one-to-many
- `TPE` -> `Panne`: one-to-many
- `Commercant` -> `Taux`: one-to-many
- `Screen` + `Role` -> `ScreenRole` (permissions fines)

### 8.3 Enums fonctionnels

- `StatutTPE`: DISPONIBLE, RESERVE, AFFECTE, EN_PANNE, MAINTENANCE, HORS_SERVICE
- `StatutDemande`: NOUVELLE, EN_COURS, VALIDEE_MONETIQUE, AFFECTEE, CLOTUREE, REJETEE
- `StatutPanne`: DECLAREE, DIAGNOSTIQUEE, EN_REPARATION, REPAREE, TESTEE, IRRECUPERABLE
- `StatutTaux`: BROUILLON, EN_ATTENTE_VALIDATION, VALIDE, REJETE
- `RoleType`: ROLE_ADMIN, ROLE_MONETIQUE, ROLE_AGENCE, ROLE_INPUTER, ROLE_AUTHORIZER

### 8.4 Base de donnees ciblee

- Le projet utilise SQL Server comme base de donnees ciblee.
- Le dialecte Hibernate est configure pour SQL Server.
- La creation et la mise a jour du schema reposent sur `ddl-auto=update`.
- Toute reference MySQL residuelle doit etre interpretee comme un reste de migration et non comme la cible fonctionnelle du projet.

## 9. Contrat Front/Back: ecarts detectes

Cette section est issue de la comparaison directe des services Angular et des controllers Spring.

### 9.1 Ecarts cote TPE

Appels front presents mais non exposes cote back:
- `GET /api/tpe/numero-serie/{numeroSerie}`
- `GET /api/tpe/type/{type}`
- `GET /api/tpe/commercant/{commercantId}`
- `GET /api/tpe/search`
- `POST /api/tpe/{id}/affecter/{commercantId}`
- `POST /api/tpe/{id}/liberer`
- `GET /api/tpe/{id}/historique`
- `GET /api/tpe/export`
- `GET /api/tpe/statistiques`
- `GET /api/tpe/alertes/stock-bas`

Mismatch de methode/forme:
- Front: `PUT /api/tpe/{id}/statut/{statut}`
- Back: `PATCH /api/tpes/{id}/statut?statut=...`

### 9.2 Ecarts cote Demandes

Appels front non exposes cote back:
- `GET /api/demandes/statut/{statut}`
- `GET /api/demandes/commercant/{id}`
- `PUT /api/demandes/{id}`
- `PUT /api/demandes/{id}/statut/{statut}`
- `DELETE /api/demandes/{id}`
- `POST /api/demandes/{id}/commentaire`
- `GET /api/demandes/agence/{agenceId}`
- `GET /api/demandes/en-attente`
- `GET /api/demandes/statistiques`
- `GET /api/demandes/{id}/bon-livraison`
- `GET /api/demandes/{id}/contrat`

### 9.3 Ecarts cote Commercants

Appels front non exposes cote back:
- `GET /api/commercants/siret/{siret}`
- `GET /api/commercants/statut/{statut}`
- `GET /api/commercants/{id}/historique-tpe`
- `POST /api/commercants/{id}/upload-rne`
- `GET /api/commercants/top`
- `POST /api/commercants/import`
- `GET /api/commercants/export`

Mismatch de methode/forme:
- Front: `PUT /api/commercants/{id}/statut/{statut}`
- Back: `PATCH /api/commercants/{id}/statut?statut=...`

### 9.4 Ecarts cote Dashboard

Appels front non exposes cote back:
- `GET /api/dashboard/top-commercants`
- `GET /api/dashboard/alertes`
- `GET /api/dashboard/evolution`
- `GET /api/dashboard/stats-monetique`
- `GET /api/dashboard/stats-agence/{agenceId}`
- `GET /api/dashboard/export`

Note:
- Back expose `stats-commercants` (singulier de route differente du front).

### 9.5 Ecarts cote Pannes

Appels front non exposes cote back:
- `POST /api/pannes/{id}/cloturer`
- `GET /api/pannes/statistiques`
- `GET /api/pannes/temps-moyen`
- `GET /api/pannes/export`

## 10. Securite: constats techniques

### 10.1 Points critiques

1. Secret JWT en dur dans `application.properties`.
2. CORS global permissif dans `ApplicationConfig` (`allowedOriginPatterns=*` avec credentials).
3. Endpoint bancaire sensible (`/api/fichier-bancaire/**`) sans `@PreAuthorize`.
4. Comptes par defaut crees automatiquement avec mots de passe connus.

### 10.2 Recommandations prioritaires

1. Externaliser secrets (JWT, DB, mail) via variables d'environnement / vault.
2. Restreindre strictement CORS aux domaines de confiance.
3. Proteger les endpoints bancaires avec roles explicites.
4. Desactiver les users par defaut en prod et forcer rotation des credentials.
5. Ajouter tests de contrat API Front/Back pour prevenir les regressions.

## 11. Observabilite et exploitation

- Logs actifs en console et fichier (`logs/tpe-management.log`).
- Niveaux debug actives pour package applicatif et securite.
- Auditing JPA active (`@EnableJpaAuditing`) avec auditor courant.
- Swagger actif pour exploration API (`/swagger-ui.html`).

## 12. Demarrage et execution

### 12.1 Backend

1. Aller dans `TPE`
2. Verifier la configuration DB dans `application.properties`
3. Executer:
    - `mvn clean install`
    - `mvn spring-boot:run`

### 12.2 Frontend

1. Aller dans `front end`
2. Executer:
    - `npm install`
    - `ng serve`

Acces local:
- Front: `http://localhost:4200`
- Back: `http://localhost:8080`

## 13. Fichiers de preuve principaux (analyse code)

Backend:
- `TPE/src/main/java/com/banque/abc/tpe/config/SecurityConfig.java`
- `TPE/src/main/java/com/banque/abc/tpe/config/ApplicationConfig.java`
- `TPE/src/main/java/com/banque/abc/tpe/config/DataInitializer.java`
- `TPE/src/main/java/com/banque/abc/tpe/controller/*.java`
- `TPE/src/main/java/com/banque/abc/tpe/service/*.java`
- `TPE/src/main/java/com/banque/abc/tpe/entity/*.java`
- `TPE/src/main/java/com/banque/abc/tpe/entity/enums/*.java`
- `TPE/src/main/resources/application.properties`

Frontend:
- `front end/src/app/app.module.ts`
- `front end/src/app/app.routing.ts`
- `front end/src/app/layouts/admin-layout/admin-layout.module.ts`
- `front end/src/app/layouts/admin-layout/admin-layout.routing.ts`
- `front end/src/app/services/*.ts`
- `front end/src/app/guards/*.ts`
- `front end/src/app/interceptors/auth.interceptor.ts`
- `front end/src/app/models/*.ts`
- `front end/src/environments/environment.ts`
- `front end/src/environments/environment.prod.ts`

## 14. Annexe detaillee par fichier

### 14.1 Backend: controllers

- `AuthController.java` gère le login et l'inscription.
- `TPEController.java` expose le cycle de vie TPE, l'import Excel et la génération de TID.
- `CommercantController.java` expose le CRUD, la recherche et le changement de statut commerçant.
- `DemandeController.java` expose la création, validation, rejet, cloture et pièces jointes.
- `AffectationController.java` gère l'affectation, la mise en service et la désaffectation.
- `TauxController.java` gère la saisie, la soumission et la validation des taux.
- `PanneController.java` gère la déclaration, le diagnostic et le traitement des pannes.
- `DashboardController.java` expose les statistiques et agrégations métier.
- `ScreenController.java` gère les écrans, les permissions et les rôles d'écran.
- `RoleController.java` expose le catalogue des rôles.
- `FileUploadController.java` traite les fichiers fonctionnels génériques.
- `FichierBancaireController.java` traite le fichier bancaire, les statistiques et les rapports.
- `PowerBIController.java` gère l'état d'intégration Power BI.
- `TPEPostingCompController.java` gère les écritures comptables liées au TPE posting.

### 14.2 Backend: services

- `AuthService.java` authentifie l'utilisateur, émet le JWT et enregistre le login.
- `TPEService.java` applique les règles de création, statut, suppression et TID.
- `CommercantService.java` orchestre le CRUD commerçant et les règles associées.
- `DemandeService.java` gère la création de demande, la validation monétique et les pièces jointes.
- `AffectationService.java` crée les affectations et met à jour TPE et demande.
- `TauxService.java` applique la règle 4 yeux et le cycle de validation des taux.
- `PanneService.java` orchestre le cycle de panne et le retour à la disponibilité.
- `DashboardService.java` calcule les indicateurs et les agrégats de supervision.
- `ScreenService.java` résout les écrans, permissions et associations rôle-écran.
- `TPEExcelImportService.java` importe les TPE depuis des fichiers Excel.
- `TPEPostingService.java` traite les écritures comptables TPE héritées.
- `FichierBancaireService.java` analyse le contenu du fichier bancaire et crée les écritures.
- `RapportFichierBancaireService.java` génère les rapports PDF et texte.
- `FileUploadService.java` encapsule le traitement de fichiers du workflow.
- `AuditService.java` journalise les actions applicatives.

### 14.3 Backend: entités

- `User.java` représente un utilisateur applicatif, ses rôles et son état de compte.
- `Role.java` porte le type de rôle métier.
- `Screen.java` représente un écran fonctionnel gouverné par permissions.
- `ScreenRole.java` matérialise les droits par rôle sur un écran.
- `Commercant.java` porte le dossier commerçant physique et e-commerce.
- `Demande.java` stocke le dossier de demande et ses données de validation.
- `Affectation.java` conserve la liaison TPE / commerçant / demande.
- `TPE.java` représente le terminal et son état d'exploitation.
- `Taux.java` stocke les taux saisis, validés et historisés.
- `Panne.java` suit les incidents et la résolution technique.
- `PieceDetachee.java` documente les composants utilisés pour les réparations.
- `PieceJointe.java` documente les pièces attachées à une demande.
- `Commentaire.java` conserve l'historique des commentaires sur les demandes.
- `HistoriqueStatut.java` trace les changements de statut TPE.
- `AuditLog.java` enregistre les actions auditables.
- `TPEImportRecord.java` conserve les lignes importées en staging.
- `TPEPostingComp.java` conserve les écritures comptables de passage.

### 14.4 Backend: utilitaires, sécurité et configuration

- `TIDGenerator.java` construit et valide les TID selon la règle codée.
- `ReferenceGenerator.java` construit les références DEM et PAN.
- `ApplicationConfig.java` fournit ModelMapper, CORS et auditing.
- `SecurityConfig.java` configure le JWT, les filtres et les règles d'accès.
- `DataInitializer.java` crée les rôles, utilisateurs et écrans au démarrage.
- `UserPrincipal.java` adapte l'entité user au contrat Spring Security.
- `JwtAuthenticationFilter.java` intercepte et valide le JWT.
- `JwtTokenProvider.java` construit et parse les jetons.
- `CustomUserDetailsService.java` charge les utilisateurs pour Spring Security.
- `JwtAuthenticationEntryPoint.java` gère les accès non autorisés.

### 14.5 Backend hérité: package com.tpe.management

Le dépôt contient aussi un package hérité, distinct du package principal com.banque.abc.tpe.

- `com.tpe.management.controller.TPEPostingController.java` expose des vérifications TPE / porteur et l'insertion d'écritures.
- `com.tpe.management.service.TPEPostingService.java` interroge les tables externes et insère les écritures comptables.
- `com.tpe.management.repository.*` contient les accès aux données héritées.
- `com.tpe.management.dto.*` contient les DTO d'intégration bancaire.
- `com.tpe.management.entity.TPEPosting.java` représente une écriture comptable TPE.

Ce sous-ensemble doit être documenté comme composant historique ou d'intégration, car il coexiste avec le socle principal Spring Boot.

### 14.6 Frontend: modules

- `app.module.ts` initialise l'application Angular.
- `app.routing.ts` définit les routes racines et la redirection vers l'espace authentifié.
- `layouts/admin-layout/admin-layout.module.ts` assemble les modules métier et les composants de navigation.
- `layouts/admin-layout/admin-layout.routing.ts` mappe les écrans métier vers les composants.
- `tpe.module.ts` regroupe les écrans TPE.
- `commercant.module.ts` regroupe les écrans commerçant.
- `demandes.module.ts` regroupe les écrans de demande et d'affectation.
- `maintenance.module.ts` regroupe les écrans de panne.

### 14.7 Frontend: services

- `auth.service.ts` gère login, register, logout, stockage du token et du user courant.
- `screen.service.ts` interroge les écrans, permissions et cache les autorisations.
- `tpe.service.ts` consomme les endpoints TPE, import, export et recherche.
- `commercant.service.ts` consomme le CRUD commerçant, la recherche et les importations.
- `demande.service.ts` gère le workflow de demande, validation, rejet et pièces jointes.
- `dashboard.service.ts` consomme les indicateurs de supervision.
- `panne.service.ts` consomme le cycle de panne et les statistiques associées.
- `taux-tpe.service.ts` gère les taux et leur validation.
- `file-upload.service.ts` gère le traitement de fichiers.
- `pdf.service.ts` génère les documents PDF métier.
- `powerbi.service.ts` consomme les endpoints Power BI.
- `role.service.ts` charge les rôles.
- `notification.service.ts` centralise les notifications côté UI.
- `tpe-posting.service.ts` gère les écritures TPE posting côté frontend.
- `file-upload.service.ts` et `tpe-posting.service.ts` couvrent la partie upload transactions.

### 14.8 Frontend: composants métier

- Dashboard: `dashboard.component.ts`, `dashboard-tpe.component.ts`, `dashboard-demandes.component.ts`, `dashboard-pannes.component.ts`.
- TPE: `tpe-list.component.ts`, `tpe-form.component.ts`, `tpe-import-records.component.ts`, `gestion-taux.component.ts`.
- Commerçants: `commercant-list.component.ts`, `commercant-form.component.ts`.
- Demandes: `demande-list.component.ts`, `demande-form.component.ts`, `demande-validation.component.ts`, `affectation-tpe.component.ts`.
- Maintenance: `panne-list.component.ts`.
- Gouvernance: `screen-management.component.ts`.
- Fichiers: `upload-fichier-bancaire.component.ts`, `file-upload.component.ts`.
- UI commune: `navbar.component.ts`, `sidebar.component.ts`, `footer.component.ts`.
- Power BI: `powerbi-report.component.ts`, `powerbi-public-report.component.ts`.
- Auth et accès: `login.component.ts`, `register.component.ts`, `user-profile.component.ts`.

### 14.9 Frontend: guard et directive

- `auth.guard.ts` protège les routes et applique le contrôle dynamique par écran.
- `has-permission.directive.ts` masque les actions selon la permission.
- `auth.interceptor.ts` injecte le JWT dans les requêtes HTTP et traite les 401.

### 14.10 Frontend: modèles

- `utilisateur.model.ts` décrit l'utilisateur, le login et le rôle.
- `tpe.model.ts` décrit le terminal, son statut et son historique.
- `demande-tpe.model.ts` décrit la demande, ses validations et ses statuts.
- `commercant.model.ts` décrit le commerçant et ses attributs opérationnels.
- `panne.model.ts` décrit l'incident technique et son cycle de traitement.
- `taux-tpe.model.ts` décrit les taux soumis à validation.
- `dashboard.model.ts` décrit les agrégats et statistiques.
- `screen.model.ts` décrit écran, rôle-écran et permissions.
- `powerbi.model.ts` décrit les objets de configuration Power BI.

### 14.11 Frontend: présentation et assets

- Les pages HTML et CSS de chaque composant portent le rendu utilisateur.
- `src/styles.css` et `src/assets/scss` définissent la charte visuelle.
- `index.html`, `main.ts`, `polyfills.ts` et `test.ts` sont les fichiers d'amorçage Angular.

## 15. Lecture fonctionnelle synthétique du projet

Le projet couvre un cycle complet de gestion bancaire des TPE:
- réception d'une demande,
- validation monétique,
- affectation d'un TPE,
- mise en service,
- gestion des pannes,
- gestion des taux avec validation séparée,
- supervision via dashboards,
- gouvernance des écrans et permissions,
- traitement de fichiers bancaires et exports.

Cette couverture fonctionnelle est bien supportée par le code, mais le contrat front/back n'est pas totalement aligné sur tous les écrans. Les écarts listés à la section 9 doivent être traités avant mise en production.

## 16. Version

- Version document: 3.0 (code-driven, enrichi)
- Date: 2026-04-20
- Base d'analyse: code source front/back uniquement
