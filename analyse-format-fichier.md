# Analyse du format de fichier CPABC049.txt

## Ligne Type 10 (Commissions)
Exemple: `1000000201180226280000016428000000501100000181    SERGENT MAJOR            MILLENIUM                            0000000000241180226000002     00000044538000000012548078830000000220820000000041960000004191030000004437800000000016000000000000000000003199006`

### Positions extraites:
- 0-2: Type ligne "10"
- 16-26: Numéro terminal (10 digits) = "2800000164"
- 26-50: Numéro compte = "28000000501100000181    "
- 50-75: Nom commerce = "SERGENT MAJOR            "
- 75-112: Localisation = "MILLENIUM                            "
- 168-180: Date session (6 digits) = "180226"
- 180-186: Nombre transactions = "000002"
- 219-231: Montant 1 (12 digits) = "000000044538" = 44.538 TND (divisé par 1000)
- 231-243: Montant 2 (12 digits) = "000000012548" = 12.548 TND (divisé par 1000)
- 243-248: MCC = "07883"
- 248-260: Commission 1 = "000000022082" = 2.2082 TND (divisé par 10000)
- 260-272: Commission 2 = "000000004196" = 0.4196 TND (divisé par 10000)

### Correspondance avec sortie TPE20260218..txt:
Ligne 2 dans CPABC049.txt génère:
1. DR 151.1105.0000 - 319.9 (montant principal écriture 1)
2. CR 707.9102.1000 - 2.559 (commission écriture 1)
3. DR 151.1105.0001 - 125.48 (montant principal écriture 2)
4. CR 707.9102.1000 - 1.882 (commission écriture 2)

NARRATIVE = "TPE-000-170226-20260218-2800000164"
Format: "TPE-{BRANCH}-{DATE_SESSION}-{DATE_TRAITEMENT}-{TERMINAL}"

## Ligne Type 20 (Paiements)
- 0-2: Type "20"
- Génère des écritures CADV (crédit) et CTPE (débit commission)
