# 📋 GUIDE - Traitement des Fichiers Bancaires TPE

## 🎯 Objectif

Ce système permet de traiter des fichiers bancaires contenant des transactions TPE et de générer automatiquement des écritures comptables dans la table `TPE_POSTING_comp`.

## 🔧 Adaptations Réalisées

### ❌ Problème Initial
Le code C# original utilisait des tables inexistantes dans votre base de données :
- `[PORTEUR]` - Informations des porteurs de cartes
- `[FM_CURRENCY]` - Données de devises
- `[RATES]` - Taux de change

### ✅ Solution Implémentée
Le système a été adapté pour utiliser **uniquement** vos tables existantes :
- `[TPE_Managements].[dbo].[commercants]` - avec `numero_compte`
- `[TPE_Managements].[dbo].[tpes]` - avec `numero_terminal`

### 🔄 Modifications de Logique

#### 1. **Récupération des Informations Commerçant**
**Ancien Code (C#)** :
```csharp
SqlCommand cmd5 = new SqlCommand("select distinct numero_terminal, numero_compte from [TABLE_INEXISTANTE] WHERE ...");
```

**Nouveau Code (Java)** :
```java
// Jointure automatique via JPA : TPE → Commercant
Optional<TPE> tpeOpt = tpeRepository.findByNumeroTerminal(numeroTerminal);
TPE tpe = tpeOpt.get();
Commercant commercant = tpe.getCommercant(); // Relation JPA
String numeroCompte = commercant.getNumeroCompte();
```

#### 2. **Gestion des Devises**
**Ancien Code** : Requête complexe avec 3 tables pour les taux de change
```csharp
SqlCommand cmd6 = new SqlCommand("SELECT ncarte,compte,devise,ccy_id,ccy_rate,deci_places 
    from [PORTEUR] a,[FM_CURRENCY] b,[RATES] c where ...");
```

**Nouveau Code** : Traitement simplifié en TND (devise locale uniquement)
```java
// On suppose que toutes les transactions sont en TND
// Pas de conversion de devise nécessaire
```

#### 3. **Types de Transactions**

##### Type 10 : Commissions TPE
Crée 4 écritures comptables :
1. **Débit 150.1103.0000** - Montant principal (position 242)
2. **Crédit 151.1105.0000** - Montant principal
3. **Débit 601.9106.0000** - Commission (position 219)
4. **Crédit 150.1103.0000** - Commission

##### Type 20 : Paiements Porteurs
Crée 2 écritures comptables :
1. **Débit Compte Client** - Montant transaction (position 215)
2. **Crédit 150.1103.0000** - Compensation

## 🏗️ Architecture Créée

```
Backend (Java Spring Boot)
├── Entity
│   └── TPEPostingComp.java              ✅ Déjà existant
├── Repository
│   └── TPEPostingCompRepository.java    ✅ Déjà existant
├── Service
│   ├── TPEPostingService.java           ✅ Déjà existant
│   └── FichierBancaireService.java      🆕 NOUVEAU
└── Controller
    └── FichierBancaireController.java   🆕 NOUVEAU

Frontend (Angular)
└── services
    └── tpe-posting.service.ts           ✅ Mis à jour
```

## 📊 Structure du Fichier Bancaire

Le fichier .txt contient des lignes de longueur fixe (≥ 250 caractères) :

| Position | Longueur | Description |
|----------|----------|-------------|
| 0-2      | 2        | Type transaction ("10" ou "20") |
| 16-26    | 10       | Numéro terminal (TPE) |
| 50-75    | 25       | Narrative (description) |
| 113-129  | 16       | Numéro carte (Type 20) |
| 203-209  | 6        | Date transaction AAMMJJ (Type 20) |
| 209-215  | 6        | Référence (Type 20) |
| 215-227  | 12       | Montant Type 20 (/1000) |
| 219-231  | 12       | Commission Type 10 (/10000) |
| 242-254  | 12       | Montant principal Type 10 (/1000) |

### Exemple de Ligne Type 10
```
10ABCD1234567890...autres données...
└─┘              └─────────┘
Type              NumTerminal
```

### Exemple de Ligne Type 20
```
20XXXX1234567890...YYYYMMDD123456...montant...
└─┘              └────────┘└────┘
Type             DateTrans   Ref
```

## 🚀 Utilisation

### Backend API

#### 1. **Upload d'un Fichier**
```bash
POST http://localhost:8080/api/fichier-bancaire/upload
Content-Type: multipart/form-data

file: [fichier.txt]
sessionDate: 20260224  # Optionnel (format yyyyMMdd)
```

**Réponse** :
```json
{
  "success": true,
  "filename": "fichier_bancaire.txt",
  "lignesLues": 150,
  "ecrituresCreees": 420,
  "sessionDate": "20260224",
  "message": "Fichier traité avec succès: 420 écritures créées"
}
```

#### 2. **Statistiques de Traitement**
```bash
GET http://localhost:8080/api/fichier-bancaire/stats/20260224
```

**Réponse** :
```json
{
  "success": true,
  "sessionDate": "20260224",
  "transactionCount": 420
}
```

#### 3. **Test de l'API**
```bash
GET http://localhost:8080/api/fichier-bancaire/test
```

### Frontend (Angular)

```typescript
import { TPEPostingService } from './services/tpe-posting.service';

constructor(private tpePostingService: TPEPostingService) {}

// Upload d'un fichier
uploadFile(file: File) {
  const sessionDate = '20260224'; // Optionnel
  
  this.tpePostingService.uploadFichierBancaire(file, sessionDate)
    .subscribe({
      next: (result) => {
        console.log('Succès:', result.message);
        console.log('Écritures créées:', result.ecrituresCreees);
      },
      error: (err) => {
        console.error('Erreur:', err);
      }
    });
}

// Récupérer les statistiques
getStats(sessionDate: string) {
  this.tpePostingService.getStatistiquesFichierBancaire(sessionDate)
    .subscribe({
      next: (stats) => {
        console.log('Transactions:', stats.transactionCount);
      },
      error: (err) => {
        console.error('Erreur:', err);
      }
    });
}
```

## 🗂️ Base de Données

### Table `TPE_POSTING_comp`

```sql
CREATE TABLE TPE_POSTING_comp (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    BRANCH VARCHAR(10),
    CLIENT VARCHAR(20),
    ACCOUNT VARCHAR(50),
    REF VARCHAR(50),
    DATE DATE,
    AMOUNT DECIMAL(18,3),
    CR_DR VARCHAR(2),            -- 'CR' ou 'DR'
    NARRATIVE VARCHAR(255),
    TRAN_TYPE VARCHAR(10),
    RB_GL VARCHAR(10),
    sessiondate VARCHAR(8),      -- Format yyyyMMdd
    ccy VARCHAR(10),             -- Devise
    created_date DATETIME DEFAULT GETDATE(),
    last_modified_date DATETIME
);
```

### Requête SQL Adaptée

**Ancien Code C#** :
```csharp
SqlCommand cmd5 = new SqlCommand(
    "select distinct numero_terminal, numero_compte from [TABLE] WHERE numero_terminal='" + ... + "'", cn);
```

**Nouveau (Java avec JPA)** :
```java
// Utilise la relation JPA automatique
TPE tpe = tpeRepository.findByNumeroTerminal(numeroTerminal).orElse(null);
if (tpe != null && tpe.getCommercant() != null) {
    String numeroCompte = tpe.getCommercant().getNumeroCompte();
}
```

## 📝 Logs et Traçabilité

Le service génère des logs détaillés :
```
INFO  - Fichier reçu: fichier_bancaire.txt (150 lignes)
WARN  - TPE non trouvé pour numeroTerminal: 1234567890
ERROR - Erreur traitement Type 10: java.lang.NumberFormatException
```

## ⚠️ Points d'Attention

### 1. **Validation des TPE**
Le système vérifie que chaque `numero_terminal` existe dans la table `tpes` :
```java
Optional<TPE> tpeOpt = tpeRepository.findByNumeroTerminal(numeroTerminal);
if (!tpeOpt.isPresent()) {
    log.warn("TPE non trouvé");
    continue; // Ligne ignorée
}
```

### 2. **Commerçant Affecté**
Un TPE doit être affecté à un commerçant :
```java
Commercant commercant = tpe.getCommercant();
if (commercant == null) {
    log.warn("Aucun commerçant affecté");
    continue; // Ligne ignorée
}
```

### 3. **Longueur de Ligne**
Les lignes trop courtes sont ignorées :
```java
if (line == null || line.length() < 250) {
    log.warn("Ligne ignorée (trop courte)");
    continue;
}
```

### 4. **Devise**
Sans les tables `FM_CURRENCY` et `RATES` :
- Toutes les transactions sont traitées en **TND** (Dinar Tunisien)
- Pas de conversion de devise
- Pour supporter d'autres devises, il faudrait créer ces tables

## 🧪 Tests

### Test avec PowerShell
```powershell
# Test de l'API
curl http://localhost:8080/api/fichier-bancaire/test

# Upload d'un fichier
curl -X POST http://localhost:8080/api/fichier-bancaire/upload `
  -F "file=@fichier_bancaire.txt" `
  -F "sessionDate=20260224"

# Statistiques
curl http://localhost:8080/api/fichier-bancaire/stats/20260224
```

### Fichier de Test
Créez un fichier `test_fichier_bancaire.txt` :
```
10              1234567890                        Test Transaction Type 10                                                                                                                                                                          000000001000                  000000000500
20              1234567890                    Test Payment                                                        1234567890123456                                    240224123456              000000002000
```

## 🔐 Sécurité

### Points à Ajouter en Production

1. **Authentification** :
```java
@PreAuthorize("hasRole('MONETIQUE')")
@PostMapping("/upload")
public ResponseEntity<Map<String, Object>> uploadFichierBancaire(...) {
    // ...
}
```

2. **Validation Taille Fichier** :
```java
if (file.getSize() > 10_000_000) { // 10 MB max
    throw new IllegalArgumentException("Fichier trop volumineux");
}
```

3. **Audit Trail** :
```java
auditService.log("FICHIER_BANCAIRE_UPLOAD", filename, username, LocalDateTime.now());
```

## 📈 Améliorations Futures

1. ✨ **Support Multi-Devises**
   - Créer table `devises` avec taux de change
   - Adapter la logique de calcul

2. ✨ **Validation Avancée**
   - Vérifier le format des numéros de carte
   - Valider les montants selon des règles métier

3. ✨ **Rapports**
   - Génération de rapports PDF/Excel
   - Graphiques de transactions

4. ✨ **Traitement Asynchrone**
   - Pour les gros fichiers (>10000 lignes)
   - Notification par email à la fin

---

**Date de Création** : 24/02/2026  
**Version** : 1.0  
**Auteur** : Système de Gestion TPE
