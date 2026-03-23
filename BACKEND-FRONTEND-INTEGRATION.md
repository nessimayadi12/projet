# Architecture Backend + Frontend - TPE Posting

## ✅ Ce qui a été créé

### Backend (Java Spring Boot)

#### 1. DTOs (`com.banque.abc.tpe.dto`)
- ✅ `TPEInfoDTO.java` - Informations TPE (affiliation, compte, branch, etc.)
- ✅ `PorteurInfoDTO.java` - Informations porteur (carte, devise, taux, etc.)
- ✅ `EcritureComptableDTO.java` - Structure d'une écriture comptable

#### 2. Repository (`com.banque.abc.tpe.repository`)
- ✅ `PorteurRepository.java` - Requêtes JDBC pour PORTEUR + FM_CURRENCY + RATES
- ✅ `TPERepository.java` - Déjà existant
- ✅ `TPEPostingCompRepository.java` - Déjà existant

#### 3. Service (`com.banque.abc.tpe.service`)
- ✅ `TPEPostingService.java` - Logique métier complète
  - `verifyTPE()` - Vérifie si TPE existe
  - `verifyPorteur()` - Vérifie si porteur existe + récupère devise
  - `insertPostings()` - Insère écritures dans TPE_POSTING_comp

#### 4. Controller (`com.banque.abc.tpe.controller`)
- ✅ `TPEPostingCompController.java` - **Endpoints ajoutés** :
  - `GET /api/tpe-posting/verify-tpe/{affiliation}` - Vérifier TPE
  - `GET /api/tpe-posting/verify-porteur/{ncarte}` - Vérifier porteur
  - `POST /api/tpe-posting/insert-postings` - Insérer écritures

### Frontend (Angular)

#### 1. Service Angular
- ✅ `tpe-posting.service.ts` - Appels API REST vers backend
  - `verifyTPE()` - Observable HTTP GET
  - `verifyPorteur()` - Observable HTTP GET
  - `insertPostings()` - Observable HTTP POST

#### 2. Composant (Partiellement modifié)
- ⚠️ `file-upload.component.ts` - **NÉCESSITE CORRECTIONS**
  - Import du service TPEPostingService ✅
  - Méthodes async pour appels backend ❌ (besoin de finalisation)
  - Mode simulation/backend ❌ (besoin de finalisation)

## 🔧 Configuration requise

### Backend - application.properties

```properties
# Base de données SQL Server
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=TPE_DB
spring.datasource.username=votre_user
spring.datasource.password=votre_password
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# JPA
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect

# CORS (si fronté et backend sur ports différents)
cors.allowed-origins=http://localhost:4200
```

### Frontend - environment.ts

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/tpe-posting'
};
```

## 🚀 Démarrage

### 1. Backend

```bash
cd TPE
mvn clean install
mvn spring-boot:run
```

Le backend démarre sur `http://localhost:8080`

### 2. Frontend

```bash
cd "front end"
npm install
ng serve
```

Le frontend démarre sur `http://localhost:4200`

## 📊 Flux de données

1. **Utilisateur télécharge fichier CPABC049**
2. **Frontend parse le fichier** ligne par ligne
3. Pour chaque transaction :
   - **Type 10** : Frontend appelle `verifyTPE()`
     - Backend vérifie dans table TPE
     - Génère 4 écritures comptables
   - **Type 20** : Frontend appelle `verifyTPE()` + `verifyPorteur()`
     - Backend vérifie TPE + PORTEUR + devise
     - Génère 2 ou 4 écritures selon devise (TND=2, EUR/USD=4)
4. **Frontend génère PDF/TXT** avec résumé
5. **Option** : Frontend peut appeler `insertPostings()` pour sauvegarder en base

## 🔄 Modes de fonctionnement

### Mode 1 : Simulation totale (Frontend seul)
```typescript
useBackend = false; // Dans file-upload.component.ts
```
- Pas d'appel API
- Simulations locales uniquement
- Toutes cartes acceptées en TND par défaut

### Mode 2 : Backend + Frontend (Production)
```typescript
useBackend = true; // Par défaut
```
- Appels API pour vérifications
- Données réelles depuis base SQL Server
- Fallback en simulation si erreur

### Mode 3 : Avec sauvegarde en base  
```typescript
useBackend = true;
saveToDatabase = true;
```
- Même que Mode 2
- + Insertion automatique dans TPE_POSTING_comp après parsing

## ⚠️ État actuel

### ✅ Complété :
- Backend complet et fonctionnel
- Service Angular créé
- Logique métier conforme au C#

### ⚠️ À finaliser :
- Corriger file-upload.component.ts (conflits d'interfaces)
- Tester l'intégration end-to-end
- Ajouter gestion d'erreurs API

### 🔜 Améliorations possibles :
- Créer entité Porteur JPA complète
- Ajouter authentification JWT pour insertPostings
- Implémenter pagination pour gros fichiers
- Ajouter logs détaillés côté backend
- Dashboard statistiques temps réel

## 📝 Notes importantes

1. **Tables SQL requises** :
   - `TPE` (avec N_affiliation, N_compte)
   - `PORTEUR` (avec ncarte, compte, devise)
   - `FM_CURRENCY` (ccy_id, deci_places)
   - `RATES` (ccy_id, ccy_rate)
   - `TPE_POSTING_comp` (table cible)

2. **Dépendances backend** :
   - Spring Boot Web
   - Spring Data JPA
   - SQL Server JDBC Driver
   - Lombok (annotations)

3. **Dépendances frontend** :
   - HttpClientModule (pour calls API)
   - RxJS (Observables)
   - jsPDF + jspdf-autotable (génération PDF)
   - file-saver (téléchargement fichiers)

## 🐛 Debugging

### Backend ne répond pas ?
```bash
# Vérifier logs
tail -f TPE/logs/spring.log

# Tester endpoints manuellement
curl http://localhost:8080/api/tpe-posting/verify-tpe/0000000001
```

### Frontend erreurs CORS ?
Ajouter dans backend :
```java
@CrossOrigin(origins = "http://localhost:4200")
```

### Base de données inaccessible ?
Vérifier :
- SQL Server démarré
- Credentials corrects dans application.properties
- Firewall autorise port 1433
