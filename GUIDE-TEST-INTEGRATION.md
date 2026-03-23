# Guide de Test - Intégration Backend-Frontend

## Vue d'ensemble
Ce guide vous aidera à tester l'intégration complète entre le backend Java Spring Boot et le frontend Angular pour le traitement des fichiers CPABC049.

---

## Prérequis

### Base de données SQL Server
Assurez-vous que les tables suivantes existent :
- `TPE` : Informations des terminaux
- `PORTEUR` : Informations des porteurs de cartes
- `FM_CURRENCY` : Devises
- `RATES` : Taux de change
- `TPE_POSTING_comp` : Table des écritures comptables (14 colonnes)

### Configuration Backend
**Fichier** : `TPE/src/main/resources/application.properties`

```properties
# Base de données
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=VotreBDD;encrypt=true;trustServerCertificate=true
spring.datasource.username=votre_utilisateur
spring.datasource.password=votre_mot_de_passe
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# JPA
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect

# Server
server.port=8080
```

### Configuration Frontend
**Fichier** : `front end/src/environments/environment.ts`

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'  // URL du backend
};
```

---

## Étape 1 : Démarrage du Backend

### Option A : Avec Maven (recommandé)
```powershell
cd TPE
mvn clean install
mvn spring-boot:run
```

### Option B : Avec JAR compilé
```powershell
cd TPE
mvn clean package -DskipTests
java -jar target/tpe-management-1.0.0.jar
```

### Vérification
Le backend devrait démarrer sur **http://localhost:8080**

Vérifiez dans les logs :
```
Started TpeManagementApplication in X seconds
```

### Test des endpoints
```powershell
# Test TPE verification
curl http://localhost:8080/api/tpe-posting/verify-tpe/123456789

# Test Porteur verification
curl http://localhost:8080/api/tpe-posting/verify-porteur/4000000000000001
```

---

## Étape 2 : Démarrage du Frontend

```powershell
cd "front end"
npm install  # Si première fois
ng serve --open
```

Le frontend s'ouvrira sur **http://localhost:4200**

---

## Étape 3 : Test de l'interface

### 3.1 Accès au composant
Naviguez vers la page de chargement de fichiers (selon votre routing)

### 3.2 Options de configuration

Vous verrez deux options :

#### ✅ **Utiliser le backend**
- **Coché** : Appelle les APIs du backend pour vérifier TPE et Porteur
- **Décoché** : Mode simulation (accepte tous les TPE/Porteurs)

#### 💾 **Sauvegarder en base**
- **Coché** : Insère les écritures dans `TPE_POSTING_comp` (nécessite backend activé)
- **Décoché** : Génère seulement les rapports PDF/TXT sans sauvegarder

---

## Étape 4 : Scénarios de test

### Scénario 1 : Mode Simulation (Sans Backend)
**Configuration** :
- [ ] Utiliser le backend
- [ ] Sauvegarder en base

**Résultat attendu** :
- Tous les TPE sont acceptés
- Toutes les cartes sont en TND par défaut
- Cartes test `5000000000000001` → EUR, `5000000000000002` → USD
- Type 10 : 4 écritures comptables
- Type 20 TND : 2 écritures
- Type 20 EUR/USD : 4 écritures (avec conversion)
- Génération PDF et TXT avec 14 colonnes

---

### Scénario 2 : Backend sans sauvegarde
**Configuration** :
- [x] Utiliser le backend
- [ ] Sauvegarder en base

**Résultat attendu** :
- Si TPE n'existe pas → Transaction rejetée
- Si Porteur n'existe pas → Transaction rejetée
- Utilise le taux de change réel de la base
- Génère rapports mais ne sauvegarde pas

**Vérification** :
1. Ouvrez DevTools (F12) → Onglet Network
2. Uploadez le fichier
3. Vérifiez les appels :
   - `GET /api/tpe-posting/verify-tpe/{affiliation}`
   - `GET /api/tpe-posting/verify-porteur/{ncarte}`
   - **PAS** d'appel `POST /insert-postings`

---

### Scénario 3 : Mode Production (Backend + Sauvegarde)
**Configuration** :
- [x] Utiliser le backend
- [x] Sauvegarder en base

**Résultat attendu** :
- Vérifie TPE et Porteur dans la base
- Génère les écritures comptables
- **Insère** toutes les écritures dans `TPE_POSTING_comp`
- Affiche un message de confirmation : "X écritures insérées dans la base de données"

**Vérification** :
1. DevTools → Network : Vérifiez l'appel `POST /api/tpe-posting/insert-postings`
2. Vérifiez la base de données :
```sql
SELECT TOP 100 * FROM TPE_POSTING_comp
ORDER BY date_v DESC
```

---

## Étape 5 : Vérification des résultats

### Format PDF (Landscape, 14 colonnes)
| N° | DATE_V | TIME_T | BRANCH | PARTIE | PROFIT_CENTRE | N_COMPT | DEAL_NO | AMOUNT | DEVISE | DT_CT | Client_Id | N_CARTE | N_AFFILIATION |
|----|--------|--------|--------|--------|---------------|---------|---------|--------|--------|-------|-----------|---------|---------------|

### Format TXT (180 caractères par ligne)
```
N°    DATE_V     TIME_T   BRANCH PARTIE PROFIT_CENTRE N_COMPT                DEAL_NO         AMOUNT       DEVISE DT_CT Client_Id  N_CARTE          N_AFFILIATION
===== ========== ======== ====== ====== ============= ====================== =============== ============ ====== ===== ========== ================ =============
```

### Écritures Type 10 (4 écritures)
1. Débit compte Porteur (TND)
2. Crédit compte commission (TND)
3. Débit compte commission (TND)
4. Crédit compte TPE (TND)

### Écritures Type 20 TND (2 écritures)
1. Débit compte Porteur (TND)
2. Crédit compte TPE (TND)

### Écritures Type 20 Étranger (4 écritures)
1. Débit compte Porteur (devise locale)
2. Crédit compte conversion (devise locale)
3. Débit compte conversion (TND)
4. Crédit compte TPE (TND)

---

## Étape 6 : Debugging

### Backend : logs Spring Boot
Surveillez la console backend pour :
```
Recherche TPE avec affiliation: 123456789
TPE trouvé: ...
Vérification porteur: 4000000000000001
Porteur trouvé avec devise: TND
Insertion de 500 écritures dans TPE_POSTING_comp
```

### Frontend : DevTools Console
Ouvrez la console (F12) pour voir :
```
useBackend: true
saveToDatabase: true
Validation TPE backend: {nAffiliation: "123456789", exists: true, ...}
Validation Porteur backend: {ncarte: "4000000000000001", devise: "TND", ...}
Génération PDF: 500 écritures
Sauvegarde backend: 500 écritures insérées
```

### Erreurs communes

#### ❌ "CORS Error"
**Solution** : Vérifiez que le backend a `@CrossOrigin(origins = "*")` sur le controller

#### ❌ "Connection refused localhost:8080"
**Solution** : Le backend n'est pas démarré. Exécutez `mvn spring-boot:run`

#### ❌ "Unknown column in table TPE_POSTING_comp"
**Solution** : Vérifiez que la table a bien 14 colonnes :
```sql
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'TPE_POSTING_comp'
ORDER BY ORDINAL_POSITION
```

#### ❌ "Cannot find module '@angular/common/http'"
**Solution** : Vérifiez que `HttpClientModule` est importé dans `app.module.ts`

---

## Étape 7 : Données de test

### Créer un TPE de test
```sql
INSERT INTO TPE (numeroAffiliation, compte, statut)
VALUES ('123456789', '001-100-CLIENT001-12345678', 'ACTIF');
```

### Créer un Porteur de test
```sql
INSERT INTO PORTEUR (ncarte, compte, devise)
VALUES ('4000000000000001', '001-200-CLIENT002-87654321', 'TND');

INSERT INTO PORTEUR (ncarte, compte, devise)
VALUES ('5000000000000001', '001-200-CLIENT003-11111111', 'EUR');
```

### Créer des devises
```sql
INSERT INTO FM_CURRENCY (ccy_id, deci_places)
VALUES ('TND', 3), ('EUR', 2), ('USD', 2);
```

### Créer des taux de change
```sql
INSERT INTO RATES (ccy_id, ccy_rate)
VALUES ('EUR', 3.2500), ('USD', 3.1000);
```

---

## Étape 8 : Fichier CPABC049 de test

Créez un fichier test `test_cpabc049.txt` :

```
0120240115CPABC049     Test Bank           
1020240115123000123456789                    4000000000000001000000100000TND001
2020240115133000123456789                    4000000000000001000000050000TND001
1020240115143000987654321                    5000000000000001000001000000EUR002
2020240115153000987654321                    5000000000000002000000500000USD003
```

**Explication** :
- Ligne 1 (Type 01) : En-tête
- Ligne 2 (Type 10) : Transaction 100 TND, carte TND → 4 écritures
- Ligne 3 (Type 20) : Retrait 50 TND, carte TND → 2 écritures
- Ligne 4 (Type 10) : Transaction 1000 EUR, carte EUR → 4 écritures + conversion
- Ligne 5 (Type 20) : Retrait 500 USD, carte USD → 4 écritures + conversion

**Total attendu** : 
- Type 10 TND : 4 écritures
- Type 20 TND : 2 écritures
- Type 10 EUR : 4 écritures
- Type 20 USD : 4 écritures
- **Total : 14 écritures comptables**

---

## Checklist finale

- [ ] Backend démarré avec succès
- [ ] Frontend accessible sur localhost:4200
- [ ] Options "Utiliser backend" et "Sauvegarder en base" visibles
- [ ] Mode simulation fonctionne (backend décoché)
- [ ] Mode backend fonctionne (verify-tpe/verify-porteur appellés)
- [ ] Sauvegarde en base fonctionne (insert-postings appelé)
- [ ] PDF généré avec 14 colonnes en mode paysage
- [ ] TXT généré avec 180 caractères par ligne
- [ ] Écritures Type 10 : 4 écritures
- [ ] Écritures Type 20 TND : 2 écritures
- [ ] Écritures Type 20 EUR/USD : 4 écritures
- [ ] Messages d'erreur clairs en cas de problème
- [ ] Base de données mise à jour correctement

---

## Support

En cas de problème :

1. **Vérifiez les logs backend** dans la console Maven
2. **Ouvrez DevTools** (F12) → Console + Network
3. **Testez les endpoints** manuellement avec curl ou Postman
4. **Vérifiez la base de données** avec SQL Server Management Studio
5. **Consultez** `BACKEND-FRONTEND-INTEGRATION.md` pour plus de détails

---

**Dernière mise à jour** : {{ date }}  
**Version** : 1.0.0
