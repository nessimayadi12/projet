# Format du Fichier Bancaire - Spécifications Techniques

## Vue d'ensemble

Ce document décrit le format exact du fichier bancaire utilisé pour le traitement des transactions TPE et Porteur, conforme à la logique métier implémentée dans le système existant.

## Types d'enregistrements

### Type 01 - En-tête de fichier
```
Position  Longueur  Description
1-2       2         Type d'enregistrement = "01"
3-10      8         Code banque
11-18     8         Date du fichier (JJMMAAAA)
19-28     10        Numéro de séquence
29-...    Variable  Remplissage
```

**Exemple:**
```
01000001  18022622222200128...
^^        ^^^^^^^^  ^^^^^^^^^^
Type      Banque    Date       Séquence
```

### Type 10 - Transaction TPE/Commerçant
```
Position  Longueur  Description
1-2       2         Type d'enregistrement = "10"
3-10      8         Numéro de séquence
11-18     8         Date du fichier (JJMMAAAA)
19-28     10        N° Affiliation (TPE)
29-48     20        Numéro commerçant
49-75     27        Nom du commerçant (Narrative)
76-...    Variable  Autres données
220-231   12        Commission (en millimes)
243-254   12        Montant brut (en millimes)
```

**Positions critiques pour Type 10:**
- **Position 16-25** (10 car): **N_AFFILIATION** - Clé pour recherche dans table TPE
- **Position 50-74** (25 car): **NARRATIVE** - Nom du commerçant
- **Position 219-230** (12 car): **COMMISSION** - Divisé par 10000
- **Position 242-253** (12 car): **MONTANT_BRUT** - Divisé par 1000

**Exemple:**
```
1000000201180226280000016428000000501100000181    SERGENT MAJOR            MILLENIUM...
^^        ^^^^^^^^  ^^^^^^^^^^  ^^^^^^^^^^^^^ ^^^^^^^^^^^^^^^^^^^^^^^^
Type      Seq       Date        N_Affiliation Nom commerçant
```

### Type 20 - Transaction Porteur/Carte
```
Position  Longueur  Description
1-2       2         Type d'enregistrement = "20"
3-10      8         Numéro de séquence
11-18     8         Date du fichier (JJMMAAAA)
19-28     10        N° Affiliation (TPE)
29-48     20        Numéro commerçant
49-75     27        Nom du commerçant (Narrative)
100       1         Indicateur Transaction (T ou I)
114-129   16        Numéro de carte
204-209   6         Date transaction (AAMMJJ)
210-215   6         Référence transaction
216-227   12        Montant (en millimes)
```

**Positions critiques pour Type 20:**
- **Position 16-25** (10 car): **N_AFFILIATION** - Clé pour recherche dans table TPE
- **Position 50-74** (25 car): **NARRATIVE** - Nom du commerçant
- **Position 99** (1 car): **INDICATEUR** - Doit être 'T' ou 'I' pour traiter la ligne
- **Position 113-128** (16 car): **NUMERO_CARTE** - Clé pour recherche dans table PORTEUR
- **Position 203-208** (6 car): **DATE_TRANS** - Format AAMMJJ
- **Position 209-214** (6 car): **REF** - Référence transaction
- **Position 215-226** (12 car): **MONTANT** - Divisé par 1000

**Exemple:**
```
2000003028000000501100000181    COMMERCE ABC             4111111111111111                          T     230226230226123456      00000012500
^^        ^^^^^^^^  ^^^^^^^^^^  ^^^^^^^^^^^^^ ^^^^^^^^^^^^^^^^^^^^^^^^ ^^^^^^^^^^^^^^^^          ^     ^^^^^^ ^^^^^^ ^^^^^^      ^^^^^^^^^^^^
Type      Seq       Date        N_Affiliation Commerçant               Carte                      Ind   DateT  DateF  Ref         Montant
```

## Règles de traitement

### Pour les lignes de Type 10 (TPE)

#### Condition de traitement
- La ligne commence par "10"
- Le N_AFFILIATION (position 16-25) doit exister dans la table **TPE**

#### Écritures générées (4 écritures)

**Écriture 1 - Débit compte commerçant**
```sql
BRANCH   = 999
CLIENT   = N_compte[5-10] (de TPE)
ACCOUNT  = 150.1103.0000
REF      = N_AFFILIATION
DATE     = SESSIONDATE (YYYYMMDD)
AMOUNT   = Montant brut (pos 242) / 1000
CR_DR    = DR
NARRATIVE = Narrative (pos 50-74)
```

**Écriture 2 - Crédit compte banque**
```sql
BRANCH   = 999
CLIENT   = N_compte[5-10] (de TPE)
ACCOUNT  = 151.1105.0000
REF      = N_AFFILIATION
DATE     = SESSIONDATE
AMOUNT   = Montant brut (pos 242) / 1000
CR_DR    = CR
NARRATIVE = Narrative (pos 50-74)
```

**Écriture 3 - Débit commission**
```sql
BRANCH   = 999
CLIENT   = N_compte[5-10] (de TPE)
ACCOUNT  = 601.9106.0000
REF      = N_AFFILIATION
DATE     = SESSIONDATE
AMOUNT   = Commission (pos 219) / 10000
CR_DR    = DR
NARRATIVE = Narrative (pos 50-74)
```

**Écriture 4 - Crédit commission**
```sql
BRANCH   = 999
CLIENT   = N_compte[5-10] (de TPE)
ACCOUNT  = 150.1103.0000
REF      = N_AFFILIATION
DATE     = SESSIONDATE
AMOUNT   = Commission (pos 219) / 10000
CR_DR    = CR
NARRATIVE = Narrative (pos 50-74)
```

### Pour les lignes de Type 20 (Porteur)

#### Conditions de traitement
1. La ligne commence par "20"
2. **ET** Position 99 = 'T' **OU** Position 99 = 'I'
3. **ET** Le N_AFFILIATION doit exister dans la table **TPE**
4. **ET** Le numéro de carte doit exister dans la table **PORTEUR**

#### Écritures générées - Cas DEVISE = TND ou TNC (2 écritures)

**Écriture 1 - Débit compte porteur**
```sql
BRANCH    = compte[2-4] (de PORTEUR)
CLIENT    = compte[5-10] (de PORTEUR)
ACCOUNT   = compte complet (de PORTEUR)
REF       = Ref transaction (pos 209)
DATE      = Date transaction formatée (YYYYMMDD)
AMOUNT    = Montant (pos 215) / 1000
CR_DR     = DR
NARRATIVE = "PAYMENT -" + Narrative (pos 50-74)
TRAN TYPE = CMS2
RB_GL     = C
```

**Écriture 2 - Crédit compte banque**
```sql
BRANCH    = compte[2-4] (de PORTEUR)
CLIENT    = compte[5-10] (de PORTEUR)
ACCOUNT   = 150.1103.0000
REF       = Ref transaction (pos 209)
DATE      = SESSIONDATE
AMOUNT    = Montant (pos 215) / 1000
CR_DR     = CR
NARRATIVE = "PAYMENT -" + Narrative (pos 50-74)
```

#### Écritures générées - Cas DEVISE ≠ TND/TNC (4 écritures)

**Écriture 1 - Crédit compte attente devise**
```sql
BRANCH    = 999
CLIENT    = 000234
ACCOUNT   = 151.1103.0000
REF       = Ref transaction (pos 209)
DATE      = SESSIONDATE
AMOUNT    = Montant (pos 215) / 1000
CR_DR     = CR
NARRATIVE = "PAYMENT -" + Narrative
```

**Écriture 2 - Débit position change**
```sql
BRANCH    = 999
CLIENT    = 000234
ACCOUNT   = 342.1101.0 + ccy_id (de FM_CURRENCY)
REF       = Ref transaction (pos 209)
DATE      = SESSIONDATE
AMOUNT    = Montant (pos 215) / 1000
CR_DR     = DR
NARRATIVE = "PAYMENT -" + Narrative
```

**Écriture 3 - Débit compte porteur (devise)**
```sql
BRANCH    = compte[2-4] (de PORTEUR)
CLIENT    = compte[5-10] (de PORTEUR)
ACCOUNT   = compte complet (de PORTEUR)
REF       = Ref transaction (pos 209)
DATE      = Date transaction formatée
AMOUNT    = (Montant / 1000) / ccy_rate (arrondi selon deci_places)
CR_DR     = DR
NARRATIVE = "PAYMENT -" + Narrative
TRAN TYPE = CMS2
RB_GL     = C
CCY       = devise (de PORTEUR)
```

**Écriture 4 - Crédit contrepartie change**
```sql
BRANCH    = 999
CLIENT    = 000234
ACCOUNT   = 341.1101.0000
REF       = Ref transaction (pos 209)
DATE      = SESSIONDATE
AMOUNT    = (Montant / 1000) / ccy_rate (arrondi selon deci_places)
CR_DR     = CR
NARRATIVE = Narrative
```

## Conversions et calculs

### Montants

**Montant brut (Type 10)**
```
Valeur fichier: 000000044538 (position 242-253)
Calcul: 44538 / 1000 = 44.538 TND
```

**Commission (Type 10)**
```
Valeur fichier: 000000012548 (position 219-230)
Calcul: 12548 / 10000 = 1.2548 TND
```

**Montant transaction (Type 20)**
```
Valeur fichier: 000000012500 (position 215-226)
Calcul: 12500 / 1000 = 12.500 TND
```

### Dates

**Date fichier (JJMMAAAA → DD/MM/YYYY)**
```
Valeur fichier: 18022628
Résultat: 18/02/2628
```

**Date transaction Type 20 (AAMMJJ → YYYYMMDD)**
```
Valeur fichier: 230226 (position 203-208)
Format AA = 23 → 2023
Format MM = 02 → 02
Format JJ = 26 → 26
Résultat: 20230226
```

**SessionDate (Date du jour)**
```
Date du jour: 23/02/2026
Format: 20260223 (YYYYMMDD)
```

## Tables de référence

### TPE
```sql
Colonnes utilisées:
- N_AFFILIATION (clé)
- N_compte (pour extraire Branch et Client)
```

### PORTEUR
```sql
Colonnes utilisées:
- ncarte (clé)
- compte (pour extraire Branch, Client, Account)
- devise
- typecarte (exclusions: AANP, ADNC, ADNG, etc.)
```

### FM_CURRENCY
```sql
Colonnes utilisées:
- ccy (devise)
- ccy_id
```

### RATES  
```sql
Colonnes utilisées:
- ccy (devise)
- ccy_rate (taux de change)
- deci_places (nombre de décimales)
- effective_date (prendre le max)
```

## Exemples complets

### Exemple Type 10

**Ligne:**
```
1000000201180226280000016428000000501100000181    SERGENT MAJOR            ...219:000000012548...242:000000044538
```

**Parsing:**
- Type: 10
- N_Affiliation: 0000001642
- Narrative: SERGENT MAJOR
- Commission (pos 219): 12548 → 1.2548 TND
- Montant brut (pos 242): 44538 → 44.538 TND

**Écritures générées:** 4 écritures comptables (voir règles ci-dessus)

### Exemple Type 20 avec TND

**Ligne:**
```
2000003028000000501100000181    COMMERCE ABC   ...99:T...113:4111111111111111...209:123456...215:000000012500
```

**Parsing:**
- Type: 20
- Indicateur (pos 99): T ✓
- N_Affiliation: 0000000502
- Carte: 4111111111111111
- Ref: 123456
- Montant: 12500 → 12.500 TND

**Écritures générées:** 2 écritures (compte porteur en TND)

## Validation

### Lignes à ignorer
- Type ≠ 10 et ≠ 20
- Type 20 avec indicateur (pos 99) ≠ 'T' et ≠ 'I'
- N_AFFILIATION inexistant dans TPE
- Type 20 avec carte inexistante dans PORTEUR
- Type carte exclus (AANP, ADNC, ADNG, ADNP, IANG, etc.)

### Erreurs communes
1. **Longueur de ligne insuffisante**
2. **Type d'enregistrement invalide**
3. **Positions dépassant la longueur de ligne**
4. **Montants non numériques**
5. **Dates invalides**

## Notes importantes

1. **SessionDate** = date du jour au format YYYYMMDD
2. **SessionUser** = nom d'utilisateur système (non utilisé dans Angular)
3. Les montants sont toujours en **millimes** (1/1000 de dinar)
4. La commission est divisée par **10000** (pas 1000)
5. Les écritures sont balancées (total DR = total CR)
6. En production, vérifier l'existence dans les tables de référence

---

**Version**: 2.0  
**Date**: 23 février 2026  
**Basé sur**: Code C# production
