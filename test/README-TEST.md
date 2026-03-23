# Fichier de Test - Analyse Bancaire

## Description

Ce fichier (`fichier-test-bancaire.txt`) est un exemple de fichier bancaire au format fixe pour tester la fonctionnalité d'analyse.

## Contenu

- **1 ligne d'en-tête** (type 01)
- **7 lignes de transactions** (type 10)

### Transactions incluses

1. SERGENT MAJOR - MILLENIUM - 44.538 TND
2. SERGENT MAJOR - TUNIS CITY - 47.070 TND
3. GIMEL - MILLENIUM - 136.375 TND
4. G I M E L - LAC 1 - 195.276 TND
5. GIOSEPPO - MANAR CITY - 53.470 TND
6. GIMEL - MANAR CITY - 223.497 TND

## Comment tester

### 1. Démarrer l'application

```bash
cd "front end"
npm start
```

L'application démarre sur `http://localhost:4200`

### 2. Accéder à la page d'analyse

- Connectez-vous avec vos identifiants
- Allez dans le menu "Upload Transactions"
- Ou accédez directement à `http://localhost:4200/file-upload`

### 3. Uploader le fichier de test

1. Cliquez sur la zone de dépôt
2. Sélectionnez `fichier-test-bancaire.txt` dans le dossier `test/`
3. Cliquez sur "Analyser le fichier"

### 4. Résultats attendus

#### Statistiques
- Total lignes: **8** (1 en-tête + 7 transactions)
- Lignes traitées: **7**
- Lignes en erreur: **0**
- Taux de réussite: **100%**

#### Transactions affichées
Vous devriez voir un tableau avec:
- 7 lignes de transactions
- Colonnes: Ligne, Terminal, Commerçant, Localisation, Date, etc.
- Montants formatés en dinars (avec 3 décimales)
- Dates formatées en DD/MM/YYYY

### 5. Générer les rapports

#### Rapport PDF
1. Cliquez sur "Rapport PDF"
2. Un fichier `Rapport_Fichier_Bancaire_[timestamp].pdf` sera téléchargé
3. Ouvrez-le pour vérifier:
   - Format paysage
   - Tableaux bien formatés
   - Totaux corrects
   - Pas d'erreurs listées

#### Rapport TXT
1. Cliquez sur "Rapport TXT"
2. Un fichier `Rapport_Fichier_Bancaire_[timestamp].txt` sera téléchargé
3. Ouvrez-le avec un éditeur de texte pour vérifier:
   - Tableaux ASCII art
   - Colonnes alignées
   - Totaux calculés
   - Format 180 caractères de large

## Validation des données

### Vérifier les montants

Les montants dans le fichier sont en millimes. Ils doivent être divisés par 1000:

| Fichier (millimes) | Affiché (dinars) |
|-------------------|------------------|
| 000000044538 | 44.538 |
| 000000047070 | 47.070 |
| 000000136375 | 136.375 |
| 000000195276 | 195.276 |
| 000000053470 | 53.470 |
| 000000223497 | 223.497 |

### Vérifier les dates

Les dates au format JJMMAAAA doivent être converties:

| Fichier | Affiché |
|---------|---------|
| 18022628 | 18/02/2628 |

## Tests supplémentaires

### Tester avec le fichier original complet

Si vous avez le fichier original `CPABC049` (147 lignes), vous pouvez aussi le tester:

1. Utilisez le fichier complet
2. Vérifiez que toutes les 147 lignes sont traitées
3. Vérifiez les totaux dans le rapport TXT
4. Vérifiez la pagination du PDF (plusieurs pages)

### Créer des fichiers de test avec erreurs

Pour tester la gestion des erreurs, créez un fichier avec:

```
01000001  18022622222200128                                                                X
1000000201180226280000016428000000501100000181    SERGENT MAJOR            MILLENIUM     X
LIGNE_INVALIDE
1000000301180226280000018028000000501100000181    SERGENT MAJOR            TUNIS CITY    X
```

Résultats attendus:
- Total lignes: 4
- Lignes traitées: 3
- Lignes en erreur: 1
- L'erreur sera listée dans les rapports

## Dépannage

### Le fichier ne se charge pas
- Vérifiez que vous êtes dans le bon dossier: `test/`
- Vérifiez l'extension: `.txt`
- Vérifiez que le fichier n'est pas vide

### Les montants sont incorrects
- Vérifiez que la division par 1000 est appliquée
- Les montants doivent avoir 3 décimales

### Les dates sont incorrectes
- Vérifiez le format: DD/MM/YYYY
- Exemple: 18/02/2628

### Le PDF ne se télécharge pas
- Vérifiez les paramètres du navigateur
- Autorisez les téléchargements depuis localhost
- Vérifiez la console du navigateur (F12)

## Structure du fichier

### Ligne d'en-tête (positions)
```
01000001  18022622222200128
^^        ^^^^^^^^ ^^^^^^^^^
||        |        |
||        |        +-- Séquence (19-28)
||        +----------- Date (11-18)
|+-------------------- Code banque (3-10)
+---------------------- Type (1-2)
```

### Ligne de transaction (positions principales)
```
1000000201180226280000016428000000501100000181    SERGENT MAJOR            MILLENIUM
^^        ^^^^^^^^ ^^^^^^^^^^ ^^^^^^^^^^ ^^^^^^^^ ^^^^^^^^^^^^^^^^^^^^^ ^^^^^^^^^^^^^^^^^
||        |        |          |          |        |                     |
||        |        |          |          |        |                     +-- Localisation (74-110)
||        |        |          |          |        +------------------------ Nom commerçant (49-73)
||        |        |          |          +--------------------------------- Numéro commerçant (29-48)
||        |        |          +-------------------------------------------- Numéro terminal (19-28)
||        |        +------------------------------------------------------- Date fichier (11-18)
||        +---------------------------------------------------------------- Séquence (3-10)
|+------------------------------------------------------------------------- Type (1-2)
```

## Prochaines étapes

Une fois que ce test fonctionne:

1. Testez avec des fichiers plus gros
2. Testez avec des fichiers contenant des erreurs
3. Vérifiez les performances avec 1000+ lignes
4. Testez sur différents navigateurs
5. Testez l'impression des rapports

---

**Note**: Ce fichier de test est fourni uniquement pour valider la fonctionnalité. Les données sont issues d'un exemple réel mais ne doivent pas être utilisées en production.
