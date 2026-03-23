# 🧪 Guide de Test - Upload Fichier Bancaire

## 📁 Fichier de Test Créé

**Emplacement**: `fichier-test-banking-simple.txt`

Ce fichier contient 6 transactions de test:
- ✅ 3 transactions Type 10 (Commissions)
- ✅ 3 transactions Type 20 (Paiements)
- ✅ Date: 25/02/2026 (format: 250225)
- ✅ Terminaux de test: 2800000164, 2800000180, 2800000206

---

## 🚀 DÉMARRAGE RAPIDE

### Étape 1: Démarrer le Backend
```powershell
cd TPE
mvn spring-boot:run
```
**Attendre** que le message apparaisse: `Started TpeManagementApplication in X seconds`

### Étape 2: Démarrer le Frontend (nouveau terminal)
```powershell
cd "front end"
npm start
```
**Attendre** l'ouverture automatique du navigateur sur `http://localhost:4200`

---

## 📤 TEST D'UPLOAD

### 1. Accéder à l'interface d'upload

Ouvrez votre navigateur: **http://localhost:4200/#/file-upload**

Ou via le menu:
- Cliquez sur **"File Upload"** dans le menu latéral

### 2. Uploader le fichier

1. **Date de Session**: Entrez `20260225` (format yyyyMMdd)
2. **Sélectionner fichier**: Cliquez sur "Choose File"
3. **Parcourir**: Allez à la racine du projet
4. **Sélectionner**: `fichier-test-banking-simple.txt`
5. **Cliquer**: Bouton **"Traiter le Fichier"** (bleu)

### 3. Résultat attendu

✅ **Message de succès**:
```
Traitement réussi !
Fichier: fichier-test-banking-simple.txt
Lignes lues: 6
Écritures créées: 18
Date de session: 20260225
```

**Explication**: 
- 3 transactions Type 10 → 3 × 4 = **12 écritures**
- 3 transactions Type 20 → 3 × 2 = **6 écritures**
- **Total**: 18 écritures comptables

### 4. Générer le rapport PDF

Après le traitement réussi:
1. Cliquez sur le bouton **"Export PDF"** (rouge)
2. Le PDF `rapport_fichier_bancaire_20260225.pdf` se télécharge
3. Ouvrez le PDF pour voir:
   - Statistiques
   - Tableau des écritures
   - Totaux débits/crédits

---

## 🔍 VÉRIFICATIONS

### Vérifier via l'API directement

```powershell
# Test 1: Vérifier que le backend est accessible
Invoke-RestMethod -Uri "http://localhost:8080/api/fichier-bancaire/test"

# Test 2: Statistiques de la session
Invoke-RestMethod -Uri "http://localhost:8080/api/fichier-bancaire/stats/20260225"

# Test 3: Télécharger le PDF
Invoke-WebRequest -Uri "http://localhost:8080/api/fichier-bancaire/rapport/pdf/20260225" `
    -OutFile "test-rapport.pdf"
```

### Vérifier dans la base de données

```sql
-- Compter les écritures créées
SELECT COUNT(*) as NombreEcritures 
FROM TPE_POSTING_comp 
WHERE session_date = '20260225';

-- Afficher le détail
SELECT 
    id,
    branch,
    account,
    amount,
    cr_dr,
    narrative,
    session_date
FROM TPE_POSTING_comp 
WHERE session_date = '20260225'
ORDER BY id DESC;

-- Vérifier les totaux
SELECT 
    cr_dr,
    COUNT(*) as nombre,
    SUM(amount) as total
FROM TPE_POSTING_comp 
WHERE session_date = '20260225'
GROUP BY cr_dr;
```

---

## ⚠️ PRÉREQUIS

### 1. Vérifier que les TPE existent

```sql
SELECT numero_terminal, statut 
FROM tpes 
WHERE numero_terminal IN ('2800000164', '2800000180', '2800000206');
```

**Si les TPE n'existent pas**, créez-les:

```sql
-- Créer les TPE de test (ajustez selon votre structure)
INSERT INTO tpes (numero_terminal, statut, modele, date_acquisition)
VALUES 
    ('2800000164', 'AFFECTE', 'INGENICO iWL250', GETDATE()),
    ('2800000180', 'AFFECTE', 'INGENICO iWL250', GETDATE()),
    ('2800000206', 'AFFECTE', 'INGENICO iWL250', GETDATE());
```

### 2. Vérifier que les commerçants existent

```sql
-- Vérifier les affectations TPE → Commerçant
SELECT t.numero_terminal, c.nom, c.numero_compte
FROM tpes t
LEFT JOIN commercants c ON t.commercant_id = c.id
WHERE t.numero_terminal IN ('2800000164', '2800000180', '2800000206');
```

**Si pas de commerçant associé**:

```sql
-- Créer un commerçant de test
INSERT INTO commercants (nom, numero_compte, email, telephone)
VALUES ('COMMERCE TEST', '11001280000050', 'test@banque.com', '0123456789');

-- Affecter les TPE au commerçant
UPDATE tpes 
SET commercant_id = (SELECT id FROM commercants WHERE nom = 'COMMERCE TEST')
WHERE numero_terminal IN ('2800000164', '2800000180', '2800000206');
```

---

## 🐛 DÉPANNAGE

### Erreur: "Backend non accessible"
```powershell
# Vérifier que le backend tourne
netstat -ano | findstr :8080

# Si pas de résultat, redémarrer le backend
cd TPE
mvn spring-boot:run
```

### Erreur: "TPE non trouvé"
- Vérifiez que les numéros de terminal existent dans la table `tpes`
- Vérifiez les logs backend: `TPE\logs\application.log`

### Erreur: "Commerçant sans numéro de compte"
```sql
-- Mettre à jour les numéros de compte manquants
UPDATE commercants 
SET numero_compte = '11001280000050'
WHERE id IN (SELECT commercant_id FROM tpes WHERE numero_terminal IN (...));
```

### Erreur CORS
Dans `FichierBancaireController.java`, vérifier:
```java
@CrossOrigin(origins = "*")  // ou "http://localhost:4200"
```

---

## 📊 AUTRES FICHIERS DE TEST

Vous pouvez aussi tester avec:

1. **test_cpabc049_sample.txt** (12 lignes - plus complet)
2. **test/fichier-test-bancaire.txt** (format original)

---

## ✅ CHECKLIST DE TEST

- [ ] Backend démarré (port 8080)
- [ ] Frontend démarré (port 4200)
- [ ] TPE existent dans la base
- [ ] Commerçants affectés aux TPE
- [ ] Numéros de compte renseignés
- [ ] Fichier uploadé avec succès
- [ ] 18 écritures créées
- [ ] PDF généré et téléchargé
- [ ] Données visibles dans la base

---

## 🎯 RÉSULTAT FINAL

Après un test réussi, vous devriez avoir:

✅ **18 nouvelles écritures** dans `TPE_POSTING_comp`  
✅ **1 rapport PDF** téléchargé  
✅ **Statistiques** visibles via l'API  
✅ **Données** consultables dans la base

---

**Date de création**: 25/02/2026  
**Fichier de test**: `fichier-test-banking-simple.txt`  
**Session de test**: 20260225
