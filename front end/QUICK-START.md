# 🚀 GUIDE DE DÉMARRAGE RAPIDE - Frontend TPE

## Installation Express

### 1. Prérequis
Vérifiez que vous avez :
```bash
node --version  # v14+ requis
npm --version   # v6+ requis
```

### 2. Installation
```bash
cd "front end"
npm install
```

### 3. Configuration API
Vérifiez que le backend est lancé sur `http://localhost:8080`

Si différent, modifiez :
```typescript
// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'  // ← Votre URL backend
};
```

### 4. Démarrage
```bash
ng serve
```

✅ Application disponible sur **http://localhost:4200**

---

## 🔑 Connexion

### Comptes de test (à créer dans le backend)

**Monétique**
```
Username: monetique
Password: [votre mot de passe]
Rôle: MONETIQUE
```

**Agence**
```
Username: agence
Password: [votre mot de passe]
Rôle: AGENCE
```

**Inputer**
```
Username: inputer
Password: [votre mot de passe]
Rôle: INPUTER
```

**Authorizer**
```
Username: authorizer
Password: [votre mot de passe]
Rôle: AUTHORIZER
```

---

## 📋 Workflows à Tester

### 1️⃣ Créer un TPE Physique (Monétique)
1. Se connecter en tant que **Monétique**
2. Menu **TPE** → **Nouveau TPE**
3. Sélectionner **Type : TPE Physique**
4. Remplir :
   - Numéro de série
   - Marque, Modèle
   - Date d'acquisition
   - **Données Monétiques** :
     - Raison sociale
     - MCC
     - Taux commission
     - Numéro compte (RIB)
     - Code agence
5. Cliquer **Générer TID**
6. Sauvegarder

### 2️⃣ Créer un TPE E-Commerce (Monétique)
1. Menu **TPE** → **Nouveau TPE**
2. Sélectionner **Type : E-Commerce**
3. Remplir :
   - Numéro de série
   - Date d'acquisition
   - **Données E-Commerce** :
     - URL Site Marchand (obligatoire)
     - Webhook URL
     - Type de commerce
     - Cartes acceptées
     - Mode Test : coché
5. Générer TID
6. Sauvegarder

### 3️⃣ Créer un Commerçant (Agence)
1. Se connecter en tant que **Agence**
2. Menu **Commerçants** → **Nouveau**
3. Remplir :
   - Raison sociale
   - Email, Téléphone
   - Adresse complète
   - Activité
   - Numéro de compte
   - Code agence
4. Upload fichier RNE (facultatif)
5. Sauvegarder

### 4️⃣ Créer une Demande TPE (Agence)
1. Menu **Demandes** → **Nouvelle demande**
2. Sélectionner un commerçant
3. Type de demande : **TPE Physique**
4. Type requis : ex. "TPE Mobile"
5. Urgence : **Normale**
6. Description détaillée
7. Ajouter pièces jointes (facultatif)
8. **Soumettre la demande**

### 5️⃣ Valider une Demande (Monétique)
1. Se connecter en tant que **Monétique**
2. Menu **Demandes**
3. Voir les demandes **En attente**
4. Cliquer sur une demande
5. Actions possibles :
   - **Valider** : passe en "Validée"
   - **Rejeter** : motif obligatoire

### 6️⃣ Affecter un TPE (Monétique)
1. Demande validée
2. Cliquer **Affecter TPE**
3. Sélectionner un TPE **Disponible**
4. Confirmer l'affectation
5. Statut demande → **Affectée**
6. Statut TPE → **Affecté**
7. Générer **Bon de livraison** et **Contrat**

### 7️⃣ Déclarer une Panne (Agence)
1. Menu **Maintenance** → **Nouvelle panne**
2. Sélectionner le TPE
3. Type de panne
4. Urgence
5. Description détaillée
6. Soumettre

### 8️⃣ Traiter une Panne (Technicien/Monétique)
1. Menu **Maintenance** → Liste pannes
2. Sélectionner panne **Déclarée**
3. Actions :
   - **Diagnostiquer** : ajouter diagnostic
   - **En réparation** : marquer en cours
   - **Réparée** : solution appliquée
   - **Tester** : test après réparation
   - **Clôturer** : fermer la panne

### 9️⃣ Modifier Taux TPE - 4 yeux (Inputer → Authorizer)

**Étape 1 - Inputer**
1. Se connecter en tant que **Inputer**
2. Menu **TPE** → **Gestion Taux**
3. Cliquer **Nouveau taux**
4. Sélectionner TPE
5. Saisir nouveaux taux :
   - Taux commission
   - Taux commission inter
6. **Soumettre pour validation**
7. Statut → **En attente validation**

**Étape 2 - Authorizer**
1. Se connecter en tant que **Authorizer** (utilisateur différent !)
2. Menu **TPE** → **Gestion Taux**
3. Voir les taux **En attente**
4. Vérifier :
   - Ancien taux
   - Nouveau taux
   - Inputer (doit être différent)
5. Actions :
   - **Valider** : taux appliqué → **Validé**
   - **Rejeter** : motif obligatoire → **Rejeté**

### 🔟 Consulter Dashboard
1. Menu **Dashboard**
2. Voir :
   - **Monétique** :
     - Répartition TPE par statut
     - Demandes en cours
     - Pannes actives
     - Top commerçants
     - Alertes
   - **Agence** :
     - Mes demandes
     - Délais moyens
     - Mes pannes

---

## 🎨 Navigation Rapide

### Menu Monétique
```
📊 Dashboard
📱 TPE
   ├─ Liste des TPE
   ├─ Nouveau TPE
   └─ Gestion Taux
👥 Commerçants
📋 Demandes
   ├─ Liste demandes
   ├─ Nouvelle demande
   └─ En attente validation
🔧 Maintenance
   └─ Pannes
```

### Menu Agence
```
📊 Dashboard
📋 Demandes
   ├─ Mes demandes
   └─ Nouvelle demande
👥 Commerçants
   └─ Mes commerçants
🔧 Maintenance
   └─ Déclarer panne
```

---

## 🐛 Dépannage Rapide

### Problème : Erreur CORS
**Solution** :
```bash
# Backend : Vérifier configuration CORS
# Autoriser http://localhost:4200
```

### Problème : Token expiré
**Solution** :
```bash
# Se déconnecter/reconnecter
# Vider localStorage si nécessaire
localStorage.clear()
```

### Problème : Module non trouvé
**Solution** :
```bash
npm install
ng serve
```

### Problème : Port 4200 déjà utilisé
**Solution** :
```bash
ng serve --port 4300
# ou
ng serve --port 4400
```

---

## 📊 Données de Test

### Script SQL (Backend)
Créer des données de test dans le backend :

```sql
-- TPE
INSERT INTO tpe (numero_serie, type_tpe, statut, marque, modele) 
VALUES ('TPE001', 'PHYSIQUE', 'DISPONIBLE', 'Ingenico', 'iWL250');

-- Commerçant
INSERT INTO commercant (raison_sociale, email, telephone, adresse, statut) 
VALUES ('Café Central', 'cafe@central.com', '0612345678', '1 Rue de la Paix', 'ACTIF');

-- Utilisateurs
INSERT INTO utilisateur (username, password, role) 
VALUES 
  ('monetique', '$2a$...', 'MONETIQUE'),
  ('agence', '$2a$...', 'AGENCE'),
  ('inputer', '$2a$...', 'INPUTER'),
  ('authorizer', '$2a$...', 'AUTHORIZER');
```

---

## 📞 Support

- **Documentation complète** : `README-PROJET.md`
- **Modifications** : `MODIFICATIONS-DETAILLEES.md`
- **Backend** : `../TPE/README.md`

---

## ✅ Checklist Premier Lancement

- [ ] Node.js installé (v14+)
- [ ] npm install terminé
- [ ] Backend lancé (port 8080)
- [ ] URL API configurée
- [ ] ng serve lancé (port 4200)
- [ ] Comptes utilisateurs créés
- [ ] Connexion réussie
- [ ] Au moins 1 TPE créé
- [ ] Au moins 1 Commerçant créé
- [ ] Workflow demande testé
- [ ] Workflow taux testé (4 yeux)

---

**Prêt à démarrer ! 🚀**

Pour toute question, consultez la documentation complète dans `README-PROJET.md`
