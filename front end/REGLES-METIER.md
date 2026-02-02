# ⚖️ RÈGLES MÉTIER - Système de Gestion TPE Bancaire

## 🎯 Règles Fondamentales

### R1 - Unicité TPE
**Règle** : Un numéro de série de TPE doit être unique dans tout le système.

**Implémentation** :
- Validation backend : contrainte `UNIQUE` sur `numero_serie`
- Validation frontend : vérification avant soumission
- Message d'erreur : "Ce numéro de série existe déjà"

---

### R2 - Affectation Exclusive
**Règle** : Un TPE ne peut être affecté qu'à un seul commerçant à la fois.

**Implémentation** :
- Champ `commercantActuelId` : nullable, unique si non null
- Avant affectation : vérifier que le TPE est `DISPONIBLE`
- Lors de l'affectation : 
  - Statut TPE → `AFFECTE`
  - `commercantActuelId` → ID du commerçant
  - `dateMiseEnService` → date du jour
- Lors de la libération :
  - Statut TPE → `DISPONIBLE`
  - `commercantActuelId` → null

**Contrôles** :
```typescript
if (tpe.statut !== StatutTPE.DISPONIBLE) {
  throw new Error("TPE non disponible pour affectation");
}
if (tpe.commercantActuelId) {
  throw new Error("TPE déjà affecté");
}
```

---

### R3 - Disponibilité pour Affectation
**Règle** : Seuls les TPE avec le statut `DISPONIBLE` peuvent être affectés à un commerçant.

**Statuts autorisant l'affectation** :
- ✅ `DISPONIBLE`

**Statuts interdisant l'affectation** :
- ❌ `RESERVE` - Réservé pour une autre affectation
- ❌ `AFFECTE` - Déjà affecté
- ❌ `EN_PANNE` - Nécessite réparation
- ❌ `EN_MAINTENANCE` - En cours de maintenance
- ❌ `HORS_SERVICE` - Inutilisable

**Implémentation** :
```typescript
const tpeDisponibles = tpes.filter(t => t.statut === StatutTPE.DISPONIBLE);
```

---

### R4 - Multi-TPE par Commerçant
**Règle** : Un commerçant peut avoir plusieurs TPE affectés simultanément.

**Cas d'usage** :
- Restaurant avec plusieurs caisses
- Boutique multi-sites
- E-commerce + boutique physique

**Implémentation** :
- Relation `1 Commerçant` → `N TPE`
- Pas de limite de nombre de TPE par commerçant
- Affichage du nombre de TPE : `commercant.nombreTpes`

---

### R5 - Traçabilité Obligatoire
**Règle** : Tous les changements de statut et modifications importantes doivent être tracés.

**Éléments tracés** :
- Changement de statut TPE
- Affectation/Libération
- Modification des taux
- Création/Validation/Rejet de demandes
- Déclaration/Résolution de pannes

**Informations enregistrées** :
```typescript
{
  action: string,           // Ex: "Changement statut"
  ancienneValeur: string,   // Valeur avant
  nouvelleValeur: string,   // Valeur après
  utilisateurId: number,    // Qui a fait l'action
  utilisateurNom: string,   // Nom de l'utilisateur
  date: Date,               // Quand
  commentaire?: string      // Pourquoi (optionnel)
}
```

**Consultation** :
- Historique accessible par TPE
- Historique accessible par Commerçant
- Export possible pour audit

---

### R6 - Workflow Demandes
**Règle** : Une demande TPE doit suivre un workflow strict avec validation Monétique.

**Workflow** :
```
NOUVELLE → EN_COURS → VALIDEE → AFFECTEE → CLOTUREE
                              ↓
                          REJETEE
```

**Transitions autorisées** :

| De | Vers | Acteur | Condition |
|---|---|---|---|
| NOUVELLE | EN_COURS | Monétique | Prise en charge |
| EN_COURS | VALIDEE | Monétique | Demande approuvée |
| EN_COURS | REJETEE | Monétique | Demande refusée (motif obligatoire) |
| VALIDEE | AFFECTEE | Monétique | TPE affecté |
| AFFECTEE | CLOTUREE | Monétique/Agence | Mise en service confirmée |

**Transitions interdites** :
- ❌ NOUVELLE → AFFECTEE (doit passer par VALIDEE)
- ❌ REJETEE → VALIDEE (créer nouvelle demande)
- ❌ CLOTUREE → * (statut final)

---

### R7 - Validation 4 Yeux (Taux)
**Règle** : ⚠️ **CRITIQUE** - Toute modification de taux TPE doit être validée par deux utilisateurs distincts : un Inputer et un Authorizer.

**Processus obligatoire** :

1️⃣ **Saisie (Inputer)**
```typescript
// Vérifier le rôle
if (user.role !== 'INPUTER') {
  throw new Error("Seul un Inputer peut saisir les taux");
}

// Créer la demande de modification
taux = {
  tpeId: tpeId,
  ancienTauxCommission: tpeActuel.tauxCommission,
  ancienTauxCommissionInter: tpeActuel.tauxCommissionInter,
  nouveauTauxCommission: nouveauTaux.commission,
  nouveauTauxCommissionInter: nouveauTaux.commissionInter,
  statut: StatutTaux.BROUILLON,
  inputerId: user.id,
  inputerNom: user.nom
}
```

2️⃣ **Soumission pour validation**
```typescript
// Passage en attente
taux.statut = StatutTaux.EN_ATTENTE_VALIDATION;
taux.dateSaisie = new Date();

// Notification Authorizer
notifierAuthorizers(taux);
```

3️⃣ **Validation (Authorizer)**
```typescript
// Contrôle 4 yeux - RÈGLE CRITIQUE
if (taux.inputerId === authorizer.id) {
  throw new Error(
    "ERREUR 4 YEUX : L'Authorizer ne peut pas valider ses propres saisies"
  );
}

if (authorizer.role !== 'AUTHORIZER') {
  throw new Error("Seul un Authorizer peut valider les taux");
}

// Validation
if (approuvé) {
  taux.statut = StatutTaux.VALIDE;
  taux.authorizerId = authorizer.id;
  taux.authorizerNom = authorizer.nom;
  taux.dateValidation = new Date();
  
  // Appliquer les taux au TPE
  tpe.tauxCommission = taux.nouveauTauxCommission;
  tpe.tauxCommissionInter = taux.nouveauTauxCommissionInter;
  
} else {
  taux.statut = StatutTaux.REJETE;
  taux.motifRejet = motif; // Obligatoire
  taux.authorizerId = authorizer.id;
  taux.dateValidation = new Date();
}
```

**Traçabilité** :
- Ancien taux conservé
- Nouveau taux enregistré
- Inputer identifié
- Authorizer identifié
- Dates enregistrées
- Motif de rejet si applicable

**Alertes** :
- Email Authorizer lors de soumission
- Email Inputer lors de validation/rejet

---

### R8 - Initiation Demande (Agence)
**Règle** : Seules les agences peuvent créer des demandes TPE. La Monétique valide ou rejette.

**Rôles et permissions** :

| Action | Agence | Monétique |
|---|:---:|:---:|
| Créer demande | ✅ | ❌ |
| Modifier demande (avant validation) | ✅ | ❌ |
| Valider demande | ❌ | ✅ |
| Rejeter demande | ❌ | ✅ |
| Affecter TPE | ❌ | ✅ |
| Clôturer demande | ✅ | ✅ |

**Justification** :
- Agence : proche du terrain, connaît les besoins commerçants
- Monétique : expertise technique, validation de faisabilité

---

### R9 - E-Commerce URL Obligatoire
**Règle** : Pour un TPE E-Commerce, l'URL du site marchand est obligatoire et doit être valide.

**Validation** :
```typescript
if (tpe.typeTpe === TypeTPE.E_COMMERCE) {
  if (!tpe.urlSiteMarchand) {
    throw new Error("URL du site marchand obligatoire pour E-Commerce");
  }
  
  // Validation format URL
  const urlPattern = /^https?:\/\/.+\..+/;
  if (!urlPattern.test(tpe.urlSiteMarchand)) {
    throw new Error("URL du site marchand invalide");
  }
  
  // Vérification accessibilité (optionnel)
  await verifierAccessibiliteSite(tpe.urlSiteMarchand);
}
```

**Autres champs spécifiques E-Commerce** :
- `urlSiteMarchand` : **Obligatoire**
- `webhookUrl` : Optionnel
- `cleApi` : Optionnel (généré si vide)
- `numeroAffiliation` : Optionnel
- `typeCommerce` : Recommandé
- `cartesAcceptees` : Recommandé
- `modeTest` : Par défaut `true` pour nouvelle création

---

### R10 - Génération TID Automatique
**Règle** : Le numéro de terminal (TID) doit être généré automatiquement selon un algorithme précis.

**Algorithme de génération** :
```
Structure : XX XXX XXX X (10 chiffres)

├─ Position 1-2  : 2 premiers chiffres du RIB (numéro de compte)
├─ Position 3-5  : Code agence (3 chiffres)
├─ Position 6-8  : Compteur séquentiel (3 chiffres)
└─ Position 9    : Clé de Luhn (1 chiffre de contrôle)

Exemple :
  RIB = 23041234567
  Code Agence = 041
  Compteur = 008
  
  Calcul TID :
  - RIB (2 premiers) : 23
  - Code agence : 041
  - Compteur : 008
  - Base : 23041008
  - Clé Luhn : 5
  
  TID Final : 230410085
```

**Implémentation clé de Luhn** :
```typescript
function calculerCleLuhn(base: string): number {
  let sum = 0;
  let double = false;
  
  // Parcourir de droite à gauche
  for (let i = base.length - 1; i >= 0; i--) {
    let digit = parseInt(base[i]);
    
    if (double) {
      digit *= 2;
      if (digit > 9) digit -= 9;
    }
    
    sum += digit;
    double = !double;
  }
  
  return (10 - (sum % 10)) % 10;
}

function genererTID(numeroCompte: string, codeAgence: string): string {
  // 2 premiers chiffres du RIB
  const rib2 = numeroCompte.substring(0, 2);
  
  // Code agence (3 chiffres)
  const agence = codeAgence.padStart(3, '0');
  
  // Compteur (récupérer le dernier + 1)
  const compteur = (getLastCompteur() + 1).toString().padStart(3, '0');
  
  // Base sans clé
  const base = rib2 + agence + compteur;
  
  // Calculer clé Luhn
  const cle = calculerCleLuhn(base);
  
  // TID final
  return base + cle;
}
```

**Contrôles** :
- RIB doit contenir au moins 2 chiffres
- Code agence doit être numérique (3 chiffres)
- TID généré doit être unique
- Impossible de modifier manuellement un TID

---

### R11 - Commerçants Multiples TPE
**Règle** : Un commerçant peut posséder plusieurs TPE, mais chaque TPE ne peut appartenir qu'à un seul commerçant.

**Cas d'usage** :
```
Commerçant "Restaurant Le Gourmet"
├─ TPE-001 : Caisse principale
├─ TPE-002 : Caisse terrasse
├─ TPE-003 : Caisse bar
└─ TPE-ECOM-001 : Site e-commerce
```

**Limite** : Aucune limite technique, mais alertes si > 10 TPE pour un même commerçant.

---

### R12 - Statuts TPE Cohérents
**Règle** : Les transitions de statut TPE doivent respecter une logique métier.

**Transitions autorisées** :

| De | Vers | Condition |
|---|---|---|
| DISPONIBLE | RESERVE | Demande en cours |
| DISPONIBLE | AFFECTE | Affectation commerçant |
| RESERVE | DISPONIBLE | Annulation réservation |
| RESERVE | AFFECTE | Validation affectation |
| AFFECTE | EN_PANNE | Panne déclarée |
| AFFECTE | DISPONIBLE | Libération |
| EN_PANNE | EN_MAINTENANCE | Prise en charge réparation |
| EN_MAINTENANCE | AFFECTE | Réparation terminée |
| EN_MAINTENANCE | HORS_SERVICE | Irréparable |
| * | HORS_SERVICE | Décision monétique |

**Transitions interdites** :
- ❌ HORS_SERVICE → * (statut terminal)
- ❌ EN_PANNE → AFFECTE (doit passer par réparation)

---

### R13 - Pannes et Remplacement
**Règle** : Lors d'une panne critique, un TPE de remplacement peut être affecté temporairement.

**Processus** :
1. Panne déclarée → TPE passe en `EN_PANNE`
2. Si urgence `CRITIQUE` :
   - Recherche TPE disponible similaire
   - Affectation TPE remplacement
   - Lien entre panne et TPE remplacement
3. Réparation TPE original
4. Retour TPE remplacement en `DISPONIBLE`
5. Réaffectation TPE original

**Traçabilité** :
```typescript
panne = {
  tpeId: tpeEnPanne.id,
  tpeRemplacementId: tpeRemplacement.id,
  dateRemplacement: new Date(),
  dateRetourOriginal: null
}
```

---

### R14 - Notifications Automatiques
**Règle** : Certaines actions doivent déclencher des notifications automatiques.

**Événements notifiables** :

| Événement | Destinataire | Moyen |
|---|---|---|
| Nouvelle demande créée | Monétique | Email + Dashboard |
| Demande validée | Agence créatrice | Email |
| Demande rejetée | Agence créatrice | Email + motif |
| TPE affecté | Commerçant | Email |
| Panne déclarée | Monétique + Technicien | Email + SMS |
| Taux soumis validation | Authorizers | Email |
| Taux validé | Inputer | Email |
| Taux rejeté | Inputer | Email + motif |
| Stock bas | Monétique | Email + Alerte |

**Format email** :
```
Subject: [TPE] Nouvelle demande #${reference}

Bonjour,

Une nouvelle demande TPE a été créée :
- Commerçant : ${commercant.raisonSociale}
- Type : ${demande.typeDemande}
- Urgence : ${demande.urgence}

Accéder à la demande : ${url}

Cordialement,
Système de Gestion TPE
```

---

### R15 - Validation Métier Frontend
**Règle** : Le frontend doit effectuer des validations métier avant soumission au backend.

**Validations frontend** :
- Formats (email, téléphone, URL)
- Champs obligatoires
- Cohérence des dates
- Unicité numéro de série (vérification préalable)
- Règle 4 yeux (Inputer ≠ Authorizer)

**Principe** : Ne jamais faire confiance au client, toujours re-valider côté backend.

---

## 📊 Résumé des Règles Critiques

| # | Règle | Criticité | Impact si non respectée |
|---|---|:---:|---|
| R1 | Unicité numéro série | 🔴 HAUTE | Doublon, confusion |
| R2 | Affectation exclusive | 🔴 HAUTE | Conflits commerçants |
| R7 | Validation 4 yeux taux | 🔴 HAUTE | Fraude, erreur financière |
| R10 | Génération TID | 🟡 MOYENNE | Non conformité bancaire |
| R6 | Workflow demandes | 🟡 MOYENNE | Processus non maîtrisé |
| R9 | URL E-Commerce | 🟡 MOYENNE | TPE E-Com non fonctionnel |
| R5 | Traçabilité | 🟢 FAIBLE | Perte d'audit |

---

## ✅ Checklist Respect Règles Métier

Avant mise en production :

- [ ] R1 : Contrainte UNIQUE sur `numero_serie`
- [ ] R2 : Vérification `commercantActuelId` avant affectation
- [ ] R3 : Filtre `DISPONIBLE` pour affectation
- [ ] R5 : Table `historique` remplie
- [ ] R6 : Workflow demandes respecté
- [ ] R7 : Contrôle Inputer ≠ Authorizer
- [ ] R9 : Validation URL E-Commerce
- [ ] R10 : Algorithme TID implémenté
- [ ] R14 : Notifications configurées
- [ ] R15 : Validations frontend en place

---

**Document de référence** : À consulter lors des développements et des tests.

**Dernière mise à jour** : 28 Janvier 2026
