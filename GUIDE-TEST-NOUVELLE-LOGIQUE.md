# Guide de test - Nouvelle logique fichier bancaire

## ✅ Changements effectués

### 1. **Refonte complète de FichierBancaireService.java**

#### Type 10 (Commissions) - Logique mise à jour:
- **Multiples montants**: Gère jusqu'à 3 paires (montant, commission) par ligne
- **Positions extraites**:
  * Session date: position 168, 6 chars (ex: "180226")
  * Montant 1: position 219, 12 chars  (÷ 1000)
  * Montant 2: position 231, 12 chars  (÷ 1000)
  * Montant 3: position 272, 12 chars  (÷ 1000)
  * Commission 1: position 248, 12 chars (÷ 10000)
  * Commission 2: position 260, 12 chars (÷ 10000)
  * Commission 3: position 284, 12 chars (÷ 10000)

- **Écritures générées** (pour chaque montant non nul):
  1. **DR** sur `151.1105.xxxx` (xxxx = 0000, 0001, 0002)
  2. **CR** sur `707.9102.1000` (commission)

- **Champs remplis**:
  * BRANCH: Extrait du compte client (position 2-5) → "000"
  * CLIENT: Extrait du compte client (position 5-11) → "000501"
  * PROFIT_CENTER: "TR"
  * RB_GL: "G" (GL account)
  * NARRATIVE: Format `TPE-{BRANCH}-{SESSION_DATE}-{PROCESSING_DATE}-{TERMINAL}`
    - Exemple: "TPE-000-180226-20260303-2800000164"

#### Type 20 (Paiements) - Logique mise à jour:
- **Positions extraites**:
  * Montant: position 215, 12 chars (÷ 1000)
  * Référence: position 209, 6 chars
  * Date transaction: position 203, 6 chars (format AAMMJJ)

- **Écritures générées** (par ligne type 20):
  1. **CADV** (CR) - Crédit avance sur compte client, date = date transaction
  2. **CTPE** (DR) - Débit commission sur compte client, date = date session

- **Champs remplis**:
  * BRANCH: Extrait du compte → "000"
  * CLIENT: Extrait du compte → "000501"
  * ACCOUNT: Compte client complet (ex: "11001280000050")
  * RB_GL: "C" (Client account)
  * TRAN_TYPE: "CADV" ou "CTPE"
  * NARRATIVE: Même format que type 10

### 2. **Corrections base de données**
- ✅ PROFIT_CENTER: Augmenté de `varchar(10)` à `varchar(50)`
- ✅ Valeurs PROFIT_CENTER: Changé de chaînes longues vers "TR" (2 caractères)

### 3. **TPE de test créés**
- 2800000164 → Commercant: COMMERCE TEST, Compte: 11001280000050
- 2800000180 → Idem
- 2800000206 → Idem  
- 2800001121 → Idem

---

## 🧪 Test avec CPABC049.txt

### Étape 1: Redémarrer le backend
```powershell
cd TPE
mvn spring-boot:run
```

### Étape 2: Uploader CPABC049.txt
1. Ouvrir http://localhost:4200/#/file-upload
2. Session date: **20260218** (ou autre date correspondant aux transactions)
3. Sélectionner: **CPABC049.txt**
4. Cliquer: **TRAITER LE FICHIER**

### Attentes pour ligne 2 de CPABC049.txt:
```
1000000201180226280000016428000000501100000181    SERGENT MAJOR...
```

**4 écritures type 10 créées**:
1. DR 151.1105.0000 = 44.538 TND (BRANCH=000, CLIENT=000501)
2. CR 707.9102.1000 = 2.2082 TND (commission)
3. DR 151.1105.0001 = 12.548 TND
4. CR 707.9102.1000 = 0.4196 TND (commission)

Puis **lignes type 20** correspondantes créeront:
- CADV (crédit) sur compte client
- CTPE (débit) sur compte client

### Étape 3: Vérifier les résultats
1. Cliquer **STATISTIQUES** pour voir le tableau
2. Colonnes affichées: BRANCH | PROFIT CENTER | CLIENT | ACCOUNT | RB_GL | CCY | SEQ NO | REF | TRAN TYPE | date | Montant | CR/DR | NARRATIVE

3. Vérifier les données:
   - BRANCH = "000"
   - PROFIT_CENTER = "TR"
   - CLIENT = "000501"
   - ACCOUNT = "151.1105.0000", "151.1105.0001", "707.9102.1000", ou compte client complet
   - RB_GL = "G" pour GL accounts, "C" pour compte client
   - TRAN_TYPE = "" pour type 10, "CADV" ou "CTPE" pour type 20
   - NARRATIVE = "TPE-000-180226-20260218-2800000164" (format long)

### Étape 4: Exporter
- **PDF**: Devrait afficher toutes les écritures avec le nouveau format
- **TXT**: Export tabulaire avec 12 colonnes

---

## 📊 Résultat attendu

Pour un fichier avec:
- 10 lignes type 10  
- 20 lignes type 20

**Total d'écritures**: Variable selon les montants non nuls
- Type 10: 2 à 6 écritures par ligne (selon nombre de montants)
- Type 20: 2 écritures par ligne

Exemple fichier CPABC049.txt (4446 lignes):
- Si ~2200 lignes type 10 avec moyenne 2 montants → ~8800 écritures
- Si ~2200 lignes type 20 → ~4400 écritures
- **Total estimé: ~13,200 écritures**

---

## 🐛 Debugging

### Si aucune écriture créée:
1. Vérifier logs backend pour erreurs
2. Vérifier positions dans le fichier correspondent au format
3. Tester avec `fichier-test-banking-simple.txt` d'abord

### Si format incorrect:
```sql
SELECT TOP 10 
    BRANCH, PROFIT_CENTER, CLIENT, ACCOUNT, RB_GL, 
    TRAN_TYPE, DATE, AMOUNT, CR_DR, NARRATIVE
FROM TPE_POSTING_comp
WHERE sessiondate = '20260218'
ORDER BY id DESC
```

### Nettoyer données test:
```sql
DELETE FROM TPE_POSTING_comp WHERE sessiondate = '20260218'
```

---

## 📝 Différences avec l'ancienne logique

| Aspect | Ancienne logique | Nouvelle logique |
|--------|------------------|------------------|
| Type 10 | 4 écritures fixes | 2-6 écritures selon montants |
| Positions | Fixes simples | Multiples positions montants |
| NARRATIVE | Texte simple | Format TPE-XXX-XXXXXX-... |
| BRANCH | Hardcodé "000" | Extrait du compte |
| CLIENT | Hardcodé | Extrait du compte |
| PROFIT_CENTER | Chaînes longues | Code court "TR" |
| Type 20 | 150.1103.0000 | Compte client complet |
| Dates | Une seule | Session + Transaction |

---

## ✨ Prochaines étapes

1. **Tester avec CPABC049.txt** (fichier réel)
2. **Vérifier correspondance** avec TPE20260218..txt
3. **Ajuster positions** si nécessaire
4. **Implémenter gestion devises** (EUR, USD) si requis
5. **Optimiser performances** pour gros fichiers (4000+ lignes)
