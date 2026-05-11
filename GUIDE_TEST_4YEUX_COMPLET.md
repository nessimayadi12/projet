# 🧪 GUIDE COMPLET - Test Processus 4 Yeux TPE

## 📋 Pré-requis

✅ Backend Spring Boot: **http://localhost:8080**  
✅ Frontend Angular: **http://localhost:4200**  
✅ MySQL: **TPE_Managements** (prêt)  

### Identifiants de Test
```
👤 INPUTER:
   Username: inputer_user
   Password: Password123!
   Rôle: INPUTER (saisit les taux)
   
👤 AUTHORIZER:
   Username: authorizer_user
   Password: Password123!
   Rôle: AUTHORIZER (valide les taux)
```

---

## 🚀 Étape 1: Login INPUTER

1. Ouvrir http://localhost:4200/#/login
2. Entrer:
   - **Nom d'utilisateur:** `inputer_user`
   - **Mot de passe:** `Password123!`
3. Cliquer **"SE CONNECTER"**
4. ✅ Devrait voir le **Dashboard INPUTER**

---

## ✏️ Étape 2: INPUTER Crée un Taux

1. Naviguer vers **"Gestion Taux"** ou **"Taux TPE"** (dans le menu)
2. Cliquer **"Nouveau Taux"** ou **"+"**
3. Remplir le formulaire:
   ```
   Commerçant: Carrefour Manouba (ou autre)
   Nouveau Taux Commission: 2.5
   Nouveau Taux Commission Inter: 1.75
   Commentaire: Test 4 Eyes
   ```
4. Cliquer **"Enregistrer"** → Status = **BROUILLON** ✅

---

## 📤 Étape 3: INPUTER Soumet le Taux

1. Sur le taux créé, cliquer **"Soumettre"** ou **"Soumettre pour Validation"**
2. ✅ Status change → **EN_ATTENTE_VALIDATION**
3. **Logout** (coin haut droit → Déconnexion)

---

## 👤 Étape 4: Login AUTHORIZER

1. Login avec:
   - **Username:** `authorizer_user`
   - **Password:** `Password123!`
2. ✅ Devrait voir le **Dashboard AUTHORIZER**

---

## ✅ Étape 5: AUTHORIZER Approuve/Rejette

### Scénario A: APPROUVER le Taux

1. Naviguer vers **"Gestion Taux - En Attente"** ou **"Taux à Valider"**
2. Voir le taux soumis par INPUTER
3. Cliquer **"Approuver"** ou **"Valider"**
4. Confirmer
5. ✅ Status → **VALIDE** + **actif = true**
6. Vérifier les timestamps:
   - `dateValidation` = maintenant
   - `authorizerId` = ID de l'AUTHORIZER
   - `authorizerNom` = Raoudha Aymen

### Scénario B: REJETER le Taux

1. Sur le même taux, cliquer **"Rejeter"** ou **"Refuser"**
2. Entrer un motif:
   ```
   Motif de rejet: Taux trop élevé - dépasse la limite autorisée
   ```
3. Confirmer
4. ✅ Status → **REJETE** + `motifRejet` rempli

---

## 🚫 Étape 6: Tester la Violation 4 Yeux (INPUTER ne peut pas approuver son propre taux)

### Test 1: Créer un 2e Taux

1. **Logout** AUTHORIZER
2. **Login** INPUTER
3. Créer + Soumettre **un 2e taux**
4. **Logout**

### Test 2: INPUTER tente d'approuver son propre taux

1. **Login** INPUTER (même utilisateur qui a créé le taux)
2. Naviguer vers le 2e taux
3. Essayer de cliquer **"Approuver"** ou **"Valider"**
4. 🚫 Devrait voir une **ERREUR**:
   ```
   ❌ "Violation règle 4 yeux: Vous ne pouvez pas valider votre propre taux"
   ```
5. ✅ Cela confirme que la règle 4 yeux fonctionne!

---

## 📊 Étape 7: Vérifier l'Audit Trail

### Dans les Logs du Backend

Chercher dans `logs/tpe-management.log`:
```
[INFO] AUDIT: Taux#XXX created by INPUTER userId=6
[INFO] AUDIT: Taux#XXX submitted by INPUTER userId=6
[INFO] AUDIT: Taux#XXX validated by AUTHORIZER userId=7
```

### Dans l'Historique Frontend

1. Cliquer sur un taux approuvé
2. Voir la timeline ou l'historique:
   - ✏️ Créé par: Nessim Ayadi (inputer_user)
   - 📤 Soumis par: Nessim Ayadi
   - ✅ Validé par: Aymen Raoudha (authorizer_user)
   - ⏱️ Dates: saisie → validation

---

## ✨ Résultat Attendu

| Étape | Résultat | Status |
|-------|----------|--------|
| Login INPUTER | ✅ Dashboard INPUTER | ✅ OK |
| Créer taux | ✅ Status = BROUILLON | ✅ OK |
| Soumettre | ✅ Status = EN_ATTENTE | ✅ OK |
| Login AUTHORIZER | ✅ Dashboard AUTHORIZER | ✅ OK |
| Approuver | ✅ Status = VALIDE | ✅ OK |
| Vérifier audit | ✅ Timeline complète | ✅ OK |
| Test violation 4Y | ❌ Message d'erreur | ✅ OK |

---

## 🐛 Troubleshooting

### Erreur: "Bad credentials" au login
→ Redémarrez le backend et l'frontend
→ Vérifiez que les utilisateurs existent en BD (script `verify_users.py`)

### Erreur: "Cannot load resource" (401, 403)
→ Redémarrez le backend
→ Vérifiez que le JWT token n'a pas expiré

### Taux ne s'affiche pas en "En Attente"
→ Vérifiez que le taux a bien le status EN_ATTENTE_VALIDATION en BD
→ Redémarrez le frontend

### Impossible d'approuver
→ Vérifiez que vous êtes connecté avec le compte AUTHORIZER
→ Vérifiez que vous n'êtes pas l'INPUTER du taux

---

## 📞 Notes

- **Session JWT expire dans 24h** (vérifier `jwt.expiration` dans application.properties)
- **Règle 4 yeux enforced au BACKEND** - frontend ne peut pas contourner
- **Audit logging** automatique pour chaque action
- **Timestamps en ISO 8601** dans la BD (UTC)
