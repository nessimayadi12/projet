# 🧪 Test Processus 4 Yeux - Workflow Complet

## 📋 Résumé
Test du workflow d'approbation des Taux avec règle 4 yeux (INPUTER ≠ AUTHORIZER)

**Backend:** http://localhost:8080  
**Frontend:** http://localhost:4200  
**Base de données:** MySQL - TPE_Managements

---

## 🔐 Étape 1: Authentication & Tokens JWT

### 1.1 - Login INPUTER (Role: INPUTER)
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "inputer_user",
  "password": "Password123!"
}
```

**Réponse attendue:**
```json
{
  "accessToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "refreshToken": "...",
  "expiresIn": 86400000
}
```

**Action:** Copier le `accessToken` → `{{INPUTER_TOKEN}}`

---

### 1.2 - Login AUTHORIZER (Role: AUTHORIZER)
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "authorizer_user",
  "password": "Password123!"
}
```

**Action:** Copier le `accessToken` → `{{AUTHORIZER_TOKEN}}`

---

## 📝 Étape 2: INPUTER Crée un Taux

### 2.1 - Créer nouveau Taux (Status: BROUILLON)
```
POST http://localhost:8080/api/taux
Authorization: Bearer {{INPUTER_TOKEN}}
Content-Type: application/json

{
  "commercantId": 1,
  "nouveauTauxCommission": 2.5,
  "nouveauTauxCommissionInter": 1.75,
  "commentaire": "Réduction pour fidélisation client"
}
```

**Réponse attendue:**
```json
{
  "id": 100,
  "commercantId": 1,
  "commercantNom": "Carrefour Manouba",
  "nouveauTauxCommission": 2.5,
  "nouveauTauxCommissionInter": 1.75,
  "statut": "BROUILLON",
  "inputerId": 1,
  "inputerNom": "Nessim Ayadi",
  "dateSaisie": "2026-05-06T14:30:00Z",
  "actif": false,
  "commentaire": "Réduction pour fidélisation client"
}
```

**Action:** Copier `tauxId = 100` → `{{TAUX_ID}}`

---

### 2.2 - INPUTER Soumet le Taux (Status: EN_ATTENTE_VALIDATION)
```
POST http://localhost:8080/api/taux/{{TAUX_ID}}/soumettre
Authorization: Bearer {{INPUTER_TOKEN}}
Content-Type: application/json

{}
```

**Réponse attendue:**
```json
{
  "id": 100,
  "statut": "EN_ATTENTE_VALIDATION",
  "inputerId": 1,
  "dateSaisie": "2026-05-06T14:30:00Z",
  ...
}
```

---

## ✅ Étape 3: AUTHORIZER Approuve/Rejette le Taux

### 3.1 - AUTHORIZER Approuve le Taux
```
POST http://localhost:8080/api/taux/{{TAUX_ID}}/valider
Authorization: Bearer {{AUTHORIZER_TOKEN}}
Content-Type: application/json

{
  "approuver": true,
  "motifRejet": null
}
```

**Réponse attendue:**
```json
{
  "id": 100,
  "statut": "VALIDE",
  "authorizerId": 2,
  "authorizerNom": "Aymen Raoudha",
  "dateValidation": "2026-05-06T14:35:00Z",
  "actif": true,
  ...
}
```

---

### 3.2 - AUTHORIZER Rejette le Taux (Alternative)
```
POST http://localhost:8080/api/taux/{{TAUX_ID}}/valider
Authorization: Bearer {{AUTHORIZER_TOKEN}}
Content-Type: application/json

{
  "approuver": false,
  "motifRejet": "Taux trop élevé - dépasse limite autorisée"
}
```

**Réponse attendue:**
```json
{
  "id": 100,
  "statut": "REJETE",
  "authorizerId": 2,
  "authorizerNom": "Aymen Raoudha",
  "motifRejet": "Taux trop élevé - dépasse limite autorisée",
  "dateValidation": "2026-05-06T14:35:00Z",
  ...
}
```

---

## 🚫 Étape 4: Tests de Violation de la Règle 4 Yeux

### 4.1 - INPUTER Tente d'Approuver Son Propre Taux (DEVRAIT ÉCHOUER)
```
POST http://localhost:8080/api/taux/{{TAUX_ID}}/valider
Authorization: Bearer {{INPUTER_TOKEN}}
Content-Type: application/json

{
  "approuver": true
}
```

**Réponse attendue (400):**
```json
{
  "success": false,
  "message": "Violation règle 4 yeux: L'INPUTER ne peut pas valider son propre taux",
  "errorCode": "FOUR_EYES_VIOLATION"
}
```

---

### 4.2 - Non-AUTHORIZER Tente de Valider (DEVRAIT ÉCHOUER)
```
POST http://localhost:8080/api/taux/{{TAUX_ID}}/valider
Authorization: Bearer {{AGENCE_TOKEN}}
Content-Type: application/json

{
  "approuver": true
}
```

**Réponse attendue (403):**
```
Forbidden: Only users with role AUTHORIZER can validate rates
```

---

## 📊 Étape 5: Vérifications

### 5.1 - Récupérer les Taux en Attente (AUTHORIZER uniquement)
```
GET http://localhost:8080/api/taux/en-attente
Authorization: Bearer {{AUTHORIZER_TOKEN}}
```

**Réponse attendue:**
```json
[
  {
    "id": 100,
    "commercantNom": "Carrefour Manouba",
    "nouveauTauxCommission": 2.5,
    "statut": "EN_ATTENTE_VALIDATION",
    "inputerNom": "Nessim Ayadi"
  }
]
```

---

### 5.2 - Vérifier l'Audit Log (Backend logs)
Vérifier que `logs/tpe-management.log` contient:
```
[INFO] AUDIT: Taux#100 created by INPUTER userId=1
[INFO] AUDIT: Taux#100 submitted by INPUTER userId=1, status: BROUILLON → EN_ATTENTE_VALIDATION
[INFO] AUDIT: Taux#100 validated by AUTHORIZER userId=2, status: EN_ATTENTE_VALIDATION → VALIDE
```

---

## 📋 Checklist Validation

- [ ] INPUTER peut créer un taux (status: BROUILLON)
- [ ] INPUTER peut soumettre le taux (status: EN_ATTENTE_VALIDATION)
- [ ] AUTHORIZER peut voir les taux en attente (/en-attente)
- [ ] AUTHORIZER peut approuver un taux (status: VALIDE)
- [ ] AUTHORIZER peut rejeter un taux (status: REJETE)
- [ ] INPUTER NE PEUT PAS approuver son propre taux ❌
- [ ] Non-AUTHORIZER NE PEUT PAS valider ❌
- [ ] Audit logs enregistrent toutes les actions ✅
- [ ] Les timestamps (dateSaisie, dateValidation) sont corrects ✅

---

## 🎯 Résultat Attendu

✅ **4 Eyes Process Working Correctly**
- Backend enforce les règles d'approbation
- Frontend utilise les bonnes signatures API (POST+body)
- Audit trail complet
- Erreurs appropriées pour les violations
