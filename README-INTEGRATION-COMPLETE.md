# 🎯 Système de Traitement CPABC049 - Intégration Complète

## 📋 Vue d'ensemble

Ce système permet de traiter les fichiers bancaires au format CPABC049, de vérifier les TPE et porteurs dans la base de données, de générer des écritures comptables, et d'exporter les résultats en PDF ou TXT.

**Architecture** : Backend Java Spring Boot + Frontend Angular

---

## ✨ Fonctionnalités

### 🔍 Parsing de fichiers CPABC049
- Format fixe (fixed-width format)
- Support des types : 01 (En-tête), 10 (Transaction), 20 (Retrait)
- Validation des positions exactes selon spécifications C#

### 🔐 Vérification Backend
- **TPE** : Validation du numéro d'affiliation dans la table `TPE`
- **Porteur** : Validation de la carte dans `PORTEUR` avec jointure sur `FM_CURRENCY` et `RATES`
- Récupération automatique des taux de change

### 💼 Génération d'écritures comptables
- **Type 10** : 4 écritures (Porteur → Commission → TPE)
- **Type 20 TND/TNC** : 2 écritures (Porteur → TPE)
- **Type 20 Étranger** : 4 écritures avec conversion (Porteur devise locale → Conversion → TPE TND)

### 📊 Export PDF/TXT
- **PDF** : Format paysage, 14 colonnes, auto-pagination
- **TXT** : 180 caractères par ligne, alignement fixe

### 💾 Sauvegarde base de données
- Insertion dans `TPE_POSTING_comp` (14 colonnes)
- Mode optionnel activable depuis l'interface

---

## 🏗️ Architecture

```
┌─────────────────────┐          HTTP REST API         ┌─────────────────────┐
│   Frontend Angular  │◄──────────────────────────────►│   Backend Spring    │
│                     │   /verify-tpe/{affiliation}    │       Boot          │
│ - file-upload.comp  │   /verify-porteur/{ncarte}     │                     │
│ - tpe-posting.svc   │   /insert-postings             │ - TPEPostingService │
│ - jsPDF export      │                                │ - PorteurRepository │
└─────────────────────┘                                └──────────┬──────────┘
                                                                  │
                                                                  │ JDBC
                                                                  ▼
                                                         ┌──────────────────┐
                                                         │  SQL Server DB   │
                                                         │                  │
                                                         │ - TPE            │
                                                         │ - PORTEUR        │
                                                         │ - FM_CURRENCY    │
                                                         │ - RATES          │
                                                         │ - TPE_POSTING_comp│
                                                         └──────────────────┘
```

---

## 🚀 Démarrage Rapide

### 1️⃣ Backend

```powershell
cd TPE
mvn clean install
mvn spring-boot:run
```

**Accessible sur** : http://localhost:8080

### 2️⃣ Frontend

```powershell
cd "front end"
npm install
ng serve
```

**Accessible sur** : http://localhost:4200

### 3️⃣ Test

1. Naviguez vers la page de chargement de fichiers
2. Choisissez vos options :
   - ✅ **Utiliser le backend** : Active les vérifications en base
   - ✅ **Sauvegarder en base** : Insère les écritures dans `TPE_POSTING_comp`
3. Uploadez un fichier CPABC049 (exemple fourni : `test_cpabc049_sample.txt`)
4. Téléchargez le PDF ou TXT généré

---

## 📂 Structure du projet

### Backend (TPE/)
```
TPE/
├── src/main/java/com/tpe/
│   ├── controller/
│   │   └── TPEPostingCompController.java      # 3 endpoints REST
│   ├── service/
│   │   └── TPEPostingService.java             # Logique métier
│   ├── repository/
│   │   ├── TPERepository.java
│   │   ├── PorteurRepository.java             # Requêtes JDBC avec JOIN
│   │   └── TPEPostingCompRepository.java
│   ├── dto/
│   │   ├── TPEInfoDTO.java
│   │   ├── PorteurInfoDTO.java
│   │   └── EcritureComptableDTO.java
│   └── entity/
│       └── TPEPostingComp.java                # 14 colonnes
└── src/main/resources/
    └── application.properties                 # Configuration DB
```

### Frontend (front end/)
```
front end/src/app/
├── components/
│   └── file-upload/
│       ├── file-upload.component.ts           # Logique principale (1025 lignes)
│       ├── file-upload.component.html         # Interface avec options
│       └── file-upload.component.css
├── services/
│   └── tpe-posting.service.ts                 # Service HTTP (73 lignes)
└── environments/
    └── environment.ts                         # URL backend
```

---

## 🔧 Configuration

### Backend : application.properties

```properties
# Base de données
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=VotreBDD
spring.datasource.username=sa
spring.datasource.password=VotreMotDePasse
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# JPA
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true

# Server
server.port=8080
```

### Frontend : environment.ts

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

---

## 📡 API Endpoints

### GET /api/tpe-posting/verify-tpe/{affiliation}
**Description** : Vérifie l'existence d'un TPE et récupère ses informations

**Réponse** :
```json
{
  "nAffiliation": "123456789",
  "nCompte": "001-100-CLIENT001-12345678",
  "exists": true,
  "branch": "001",
  "profitCentre": "100",
  "clientId": "CLIENT001"
}
```

### GET /api/tpe-posting/verify-porteur/{ncarte}
**Description** : Vérifie un porteur et récupère devise + taux de change

**Réponse** :
```json
{
  "ncarte": "4000000000000001",
  "compte": "001-200-CLIENT002-87654321",
  "devise": "EUR",
  "ccyId": "EUR",
  "ccyRate": 3.2500,
  "deciPlaces": 2,
  "exists": true,
  "branch": "001",
  "profitCentre": "200",
  "clientId": "CLIENT002"
}
```

### POST /api/tpe-posting/insert-postings
**Description** : Insère les écritures comptables dans `TPE_POSTING_comp`

**Body** :
```json
[
  {
    "date_v": "2025-01-15",
    "time_t": "09:00:00",
    "branch": "001",
    "partie": "PORTEUR",
    "profit_centre": "200",
    "n_compt": "001-200-CLIENT002-87654321",
    "deal_no": "DEAL-001",
    "amount": 100.000,
    "devise": "TND",
    "dt_ct": "D",
    "client_id": "CLIENT002",
    "n_carte": "4000000000000001",
    "n_affiliation": "123456789"
  }
]
```

**Réponse** :
```json
{
  "status": "success",
  "insertedCount": 500
}
```

---

## 🎮 Modes de fonctionnement

### Mode 1 : Simulation (Offline)
- ❌ Backend décoché
- Accepte tous les TPE
- Toutes les cartes en TND par défaut (sauf test cards EUR/USD)
- Génère rapports uniquement

### Mode 2 : Backend sans sauvegarde
- ✅ Backend coché
- ❌ Sauvegarde décoché
- Vérifie TPE et Porteur en base
- Utilise taux de change réels
- Génère rapports uniquement

### Mode 3 : Production (Backend + DB)
- ✅ Backend coché
- ✅ Sauvegarde coché
- Vérifie TPE et Porteur
- **Insère dans TPE_POSTING_comp**
- Génère rapports

---

## 📝 Format des écritures comptables

### Type 10 : Transaction avec commission (4 écritures)

| N° | PARTIE | COMPTE | MONTANT | DEVISE | DT_CT |
|----|--------|--------|---------|--------|-------|
| 1 | PORTEUR | Compte porteur | Montant original | Devise carte | D |
| 2 | COMMISSION | 999-999-COMM-99999999 | Commission (1%) | TND | C |
| 3 | COMMISSION | 999-999-COMM-99999999 | Commission (1%) | TND | D |
| 4 | TPE | Compte TPE | Montant - Commission | TND | C |

### Type 20 : Retrait TND (2 écritures)

| N° | PARTIE | COMPTE | MONTANT | DEVISE | DT_CT |
|----|--------|--------|---------|--------|-------|
| 1 | PORTEUR | Compte porteur | Montant | TND | D |
| 2 | TPE | Compte TPE | Montant | TND | C |

### Type 20 : Retrait devise étrangère (4 écritures)

| N° | PARTIE | COMPTE | MONTANT | DEVISE | DT_CT |
|----|--------|--------|---------|--------|-------|
| 1 | PORTEUR | Compte porteur | Montant | EUR/USD | D |
| 2 | CONVERSION | 999-999-CONV-99999999 | Montant | EUR/USD | C |
| 3 | CONVERSION | 999-999-CONV-99999999 | Montant × Taux | TND | D |
| 4 | TPE | Compte TPE | Montant × Taux | TND | C |

---

## 🧪 Données de test

### Créer des TPE de test

```sql
INSERT INTO TPE (numeroAffiliation, compte, statut)
VALUES 
  ('123456789', '001-100-CLIENT001-12345678', 'ACTIF'),
  ('987654321', '001-100-CLIENT002-87654321', 'ACTIF'),
  ('555555555', '001-100-CLIENT003-11111111', 'ACTIF');
```

### Créer des Porteurs de test

```sql
INSERT INTO PORTEUR (ncarte, compte, devise)
VALUES 
  ('4000000000000001', '001-200-CLIENT001-12345678', 'TND'),
  ('5000000000000001', '001-200-CLIENT002-87654321', 'EUR'),
  ('5000000000000002', '001-200-CLIENT003-11111111', 'USD');
```

### Configurer les devises et taux

```sql
INSERT INTO FM_CURRENCY (ccy_id, deci_places)
VALUES ('TND', 3), ('EUR', 2), ('USD', 2);

INSERT INTO RATES (ccy_id, ccy_rate)
VALUES ('EUR', 3.2500), ('USD', 3.1000), ('TND', 1.0000);
```

---

## 📖 Documentation complète

- **[GUIDE-TEST-INTEGRATION.md](GUIDE-TEST-INTEGRATION.md)** : Guide de test étape par étape
- **[BACKEND-FRONTEND-INTEGRATION.md](BACKEND-FRONTEND-INTEGRATION.md)** : Architecture technique détaillée
- **[test_cpabc049_sample.txt](test_cpabc049_sample.txt)** : Fichier de test CPABC049

---

## 🐛 Debugging

### Vérifier le backend

```powershell
# Test endpoint verify-tpe
curl http://localhost:8080/api/tpe-posting/verify-tpe/123456789

# Test endpoint verify-porteur
curl http://localhost:8080/api/tpe-posting/verify-porteur/4000000000000001
```

### Vérifier le frontend

Ouvrez DevTools (F12) → Network → Uploadez un fichier

Vous devriez voir :
- `GET verify-tpe/...` → Status 200
- `GET verify-porteur/...` → Status 200
- `POST insert-postings` → Status 200 (si sauvegarde activée)

### Logs backend

```
Recherche TPE avec affiliation: 123456789
TPE trouvé: nAffiliation=123456789, compte=001-100-CLIENT001-12345678
Vérification porteur: 4000000000000001
Porteur trouvé avec devise: EUR (taux: 3.2500)
Insertion de 500 écritures dans TPE_POSTING_comp
```

---

## ⚠️ Notes importantes

1. **CORS** : Le backend autorise toutes les origines (`@CrossOrigin(origins = "*")`)
2. **Mode simulation** : Le backend accepte tous les TPE/Porteurs si non trouvés en base
3. **Taux de change** : Par défaut 1.0 si non trouvé dans la table `RATES`
4. **Commission** : Fixée à 1% du montant (modifiable dans le code)
5. **Format date** : YYYYMMDD dans le fichier, converti en LocalDate dans le backend
6. **Format heure** : HHMMSS dans le fichier, converti en LocalTime dans le backend

---

## 📊 Statistiques

- **Frontend** : 1025 lignes TypeScript + 73 lignes service
- **Backend** : 161 lignes service + 36 lignes repository + 3 endpoints
- **DTOs** : 3 classes (TPEInfo, PorteurInfo, EcritureComptable)
- **Format PDF** : 14 colonnes, landscape mode
- **Format TXT** : 180 caractères par ligne

---

## 🎓 Exemple de workflow

1. L'utilisateur upload `test_cpabc049_sample.txt` (11 transactions)
2. Le frontend parse le fichier et extrait :
   - 1 ligne Type 01 (en-tête)
   - 7 lignes Type 10 (transactions)
   - 3 lignes Type 20 (retraits)
3. Pour chaque transaction :
   - Appel `verify-tpe/{affiliation}` → Récupère compte TPE
   - Appel `verify-porteur/{ncarte}` → Récupère compte + devise + taux
4. Génération des écritures comptables :
   - Type 10 × 7 = 28 écritures (4 par transaction)
   - Type 20 TND × 1 = 2 écritures
   - Type 20 EUR × 1 = 4 écritures
   - Type 20 USD × 1 = 4 écritures
   - **Total : 38 écritures**
5. Si sauvegarde activée :
   - Appel `POST /insert-postings` avec les 38 écritures
   - Backend insère dans `TPE_POSTING_comp`
   - Message : "38 écritures insérées avec succès"
6. Génération rapports :
   - PDF landscape, 14 colonnes, 2 pages
   - TXT 180 caractères, alignement fixe

---

## 👨‍💻 Auteur

Développé pour le système de gestion TPE - Traitement des fichiers bancaires CPABC049

**Version** : 1.0.0  
**Date** : Janvier 2025

---

## 📞 Support

Pour toute question ou problème :
1. Consultez les guides dans `/documentation`
2. Vérifiez les logs backend et frontend DevTools
3. Testez les endpoints manuellement avec curl/Postman
4. Vérifiez la configuration de la base de données

---

**🎉 Le système est prêt à l'emploi !**
