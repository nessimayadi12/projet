# Guide de Test - Formulaire Demande Affectation TPE

## 🎯 Objectif
Vérifier que le formulaire de demande d'affectation TPE fonctionne correctement pour les deux types : TPE Physique et E-commerce.

## 📋 Pré-requis

### Base de données
- [ ] Migration `V2__add_demande_affectation_fields.sql` appliquée
- [ ] Utilisateurs de test créés (AGENCE, MONETIQUE)

### Serveurs
```bash
# Backend
cd TPE
./mvnw spring-boot:run

# Frontend
cd "front end"
npm start
```

## ✅ Tests à effectuer

### Test 1: Affichage du formulaire

#### 1.1 Connexion utilisateur AGENCE
- [ ] Se connecter avec un utilisateur ayant le rôle AGENCE
- [ ] Naviguer vers "Demandes" → "Nouvelle demande"
- [ ] Vérifier que le formulaire s'affiche correctement

#### 1.2 Vérification des champs par défaut
- [ ] Type de demande: TPE Physique (par défaut)
- [ ] Urgence: Normale (par défaut)
- [ ] Tous les champs communs sont visibles
- [ ] Les champs TPE Physique sont visibles
- [ ] Les champs E-commerce sont cachés

### Test 2: TPE Physique

#### 2.1 Sélection du type
- [ ] Type de demande = "TPE Physique"
- [ ] Champs visibles:
  - [ ] Type de TPE
  - [ ] Raison Sociale
  - [ ] Activité
  - [ ] Numéro de Compte
  - [ ] Adresse
  - [ ] Code Postal
  - [ ] Code Agence
  - [ ] Téléphone
  - [ ] Email Notification
  - [ ] Fichier RNE

- [ ] Champs cachés:
  - [ ] Localité
  - [ ] RIB
  - [ ] Webmaster
  - [ ] Contact Technique
  - [ ] URL site Marchand

#### 2.2 Validation des champs obligatoires
- [ ] Cliquer sur "Soumettre" sans remplir les champs
- [ ] Vérifier que les messages d'erreur apparaissent pour:
  - [ ] Type de TPE
  - [ ] Raison Sociale
  - [ ] Activité
  - [ ] Numéro de Compte
  - [ ] Adresse
  - [ ] Code Postal
  - [ ] Code Agence
  - [ ] Téléphone
  - [ ] Email Notification

#### 2.3 Remplissage du formulaire TPE Physique
Données de test:
```
Type de TPE: TPE mobile
Raison Sociale: Restaurant Le Bon Coin
Activité: Restauration
Numéro de Compte: 001234567890
Adresse: 15 Rue de la Paix
Code Postal: 75001
Code Agence: AG001
Téléphone: 0123456789
Email: contact@leboncoin.fr
Description: Demande urgente pour remplacer TPE défectueux
```

- [ ] Remplir tous les champs
- [ ] Joindre un fichier RNE (PDF, JPG ou PNG)
- [ ] Vérifier que le nom du fichier apparaît dans la liste
- [ ] Cliquer sur "Soumettre la demande"
- [ ] Vérifier le message de succès
- [ ] Vérifier la redirection vers la liste des demandes
- [ ] Vérifier que la demande apparaît avec le statut "NOUVELLE"

#### 2.4 Vérification dans la base de données
```sql
SELECT id, raison_sociale, activite, numero_compte, type_tpe_requis, statut, type_demande
FROM demandes
WHERE raison_sociale = 'Restaurant Le Bon Coin';
```

Vérifier:
- [ ] `type_demande` = TPE_PHYSIQUE
- [ ] `statut` = NOUVELLE
- [ ] Tous les champs sont remplis

### Test 3: E-commerce

#### 3.1 Création nouvelle demande E-commerce
- [ ] Créer une nouvelle demande
- [ ] Sélectionner "E-Commerce" dans le type de demande
- [ ] Vérifier que les champs changent instantanément

#### 3.2 Champs visibles pour E-commerce
- [ ] Raison Sociale
- [ ] Activité
- [ ] Adresse
- [ ] Code Postal
- [ ] Code Agence
- [ ] Téléphone
- [ ] Email Notification
- [ ] **Localité** (E-commerce uniquement)
- [ ] **RIB** (E-commerce uniquement)
- [ ] **Webmaster / Contact Technique** (E-commerce uniquement)
- [ ] **Contact Technique** (E-commerce uniquement)
- [ ] **URL site Marchand** (E-commerce uniquement)
- [ ] Fichier RNE

#### 3.3 Champs cachés pour E-commerce
- [ ] Type de TPE (caché)
- [ ] Numéro de Compte (caché)

#### 3.4 Validation E-commerce
- [ ] Cliquer sur "Soumettre" sans remplir
- [ ] Vérifier les messages d'erreur pour tous les champs E-commerce obligatoires

#### 3.5 Remplissage du formulaire E-commerce
Données de test:
```
Raison Sociale: Boutique Mode Online
Activité: Vente en ligne de vêtements
Adresse: 25 Avenue des Champs
Localité: Paris 8ème
RIB: FR7612345678901234567890123
Code Postal: 75008
Code Agence: AG002
Téléphone: 0987654321
Email: admin@modenomine.fr
Webmaster: Jean Dupont
Contact Technique: Marie Martin
URL site: https://www.modeonline.fr
Description: Nouveau site e-commerce, besoin TPE virtuel
```

- [ ] Remplir tous les champs E-commerce
- [ ] Vérifier la validation du format URL (doit commencer par http:// ou https://)
- [ ] Joindre un fichier RNE
- [ ] Soumettre la demande
- [ ] Vérifier le succès et la redirection

#### 3.6 Vérification dans la base de données
```sql
SELECT id, raison_sociale, activite, localite, rib, url_site_marchand, statut, type_demande
FROM demandes
WHERE raison_sociale = 'Boutique Mode Online';
```

Vérifier:
- [ ] `type_demande` = E_COMMERCE
- [ ] `statut` = NOUVELLE
- [ ] `localite`, `rib`, `url_site_marchand` sont remplis
- [ ] `numero_compte`, `type_tpe_requis` sont NULL

### Test 4: Changement de type dynamique

#### 4.1 Changement TPE Physique → E-commerce
- [ ] Créer une nouvelle demande (TPE Physique par défaut)
- [ ] Remplir quelques champs communs
- [ ] Changer le type pour "E-Commerce"
- [ ] Vérifier que:
  - [ ] Les champs communs conservent leurs valeurs
  - [ ] Les champs TPE Physique disparaissent
  - [ ] Les champs E-commerce apparaissent
  - [ ] Les validateurs sont mis à jour

#### 4.2 Changement E-commerce → TPE Physique
- [ ] Partir d'une demande E-commerce
- [ ] Remplir quelques champs E-commerce
- [ ] Changer pour "TPE Physique"
- [ ] Vérifier le changement des champs

### Test 5: Upload de fichiers

#### 5.1 Fichiers valides
- [ ] Joindre un PDF → Vérifier l'acceptation
- [ ] Joindre un JPG → Vérifier l'acceptation
- [ ] Joindre un PNG → Vérifier l'acceptation
- [ ] Joindre plusieurs fichiers → Vérifier la liste

#### 5.2 Vérification affichage
- [ ] Nom du fichier affiché
- [ ] Taille du fichier affichée (en KB)
- [ ] Plusieurs fichiers listés

#### 5.3 Fichiers invalides (optionnel - si validation implémentée)
- [ ] Tenter de joindre un fichier > 5 Mo → Vérifier le rejet
- [ ] Tenter de joindre un type non accepté (.exe, .zip) → Vérifier le rejet

### Test 6: Validation Monétique

#### 6.1 Connexion utilisateur MONETIQUE
- [ ] Se déconnecter
- [ ] Se connecter avec un utilisateur MONETIQUE ou ADMIN
- [ ] Naviguer vers la liste des demandes

#### 6.2 Validation demande TPE Physique
- [ ] Ouvrir la demande TPE Physique créée précédemment
- [ ] Cliquer sur "Valider"
- [ ] Vérifier que le dialogue affiche:
  - [ ] MCC
  - [ ] Taux de commission
  - [ ] Taux de commission inter
  - [ ] Loyer
  - [ ] Série TPE
  - [ ] N° Terminal (avec bouton "Générer")
  - [ ] Value Date

#### 6.3 Génération N° Terminal (TPE Physique)
- [ ] Cliquer sur "Générer N° Terminal"
- [ ] Vérifier qu'un numéro est généré (format: TPEXXXXXXXX)
- [ ] Remplir les autres champs:
```
MCC: 5812
Taux commission: 1.5
Taux inter: 0.3
Loyer: 50
Série TPE: INGENICO
Value Date: [Date du jour]
```
- [ ] Valider
- [ ] Vérifier le statut passe à "VALIDEE_MONETIQUE"

#### 6.4 Validation demande E-commerce
- [ ] Ouvrir la demande E-commerce
- [ ] Cliquer sur "Valider"
- [ ] Vérifier que le dialogue affiche UNIQUEMENT:
  - [ ] MCC
  - [ ] N° Terminal (avec bouton "Générer")
- [ ] Générer le N° Terminal
- [ ] Remplir MCC: 5311
- [ ] Valider
- [ ] Vérifier le statut

### Test 7: Mode Édition (si implémenté)

#### 7.1 Édition demande existante
- [ ] Ouvrir une demande existante en mode édition
- [ ] Vérifier que tous les champs sont pré-remplis
- [ ] Modifier quelques valeurs
- [ ] Sauvegarder
- [ ] Vérifier la mise à jour

## 🐛 Bugs connus / À surveiller

### Frontend
- [ ] Système de notification (actuellement `alert()`)
- [ ] Validation taille fichier côté client
- [ ] Gestion des erreurs réseau

### Backend
- [ ] Endpoint upload pièces jointes
- [ ] Validation des données côté serveur
- [ ] Génération unique des N° Terminal

## 📊 Résultats des Tests

### Session de test: ___________
Testeur: ___________

| Test | Statut | Commentaire |
|------|--------|-------------|
| 1.1 Affichage formulaire | ⬜ PASS ⬜ FAIL | |
| 2.2 Validation TPE Physique | ⬜ PASS ⬜ FAIL | |
| 2.3 Soumission TPE Physique | ⬜ PASS ⬜ FAIL | |
| 3.4 Validation E-commerce | ⬜ PASS ⬜ FAIL | |
| 3.5 Soumission E-commerce | ⬜ PASS ⬜ FAIL | |
| 4.1 Changement type dynamique | ⬜ PASS ⬜ FAIL | |
| 5.1 Upload fichiers | ⬜ PASS ⬜ FAIL | |
| 6.3 Validation Monétique TPE | ⬜ PASS ⬜ FAIL | |
| 6.4 Validation Monétique E-commerce | ⬜ PASS ⬜ FAIL | |

## 🔍 Vérifications Post-Test

### Base de données
```sql
-- Vérifier toutes les demandes créées
SELECT id, raison_sociale, type_demande, statut, 
       created_at, numero_terminal, mcc
FROM demandes
ORDER BY created_at DESC
LIMIT 10;

-- Vérifier les pièces jointes
SELECT d.raison_sociale, pj.nom_fichier, pj.type_fichier, pj.taille
FROM demandes d
JOIN pieces_jointes pj ON pj.demande_id = d.id
ORDER BY pj.uploaded_at DESC;
```

### Console Backend
- [ ] Aucune erreur 500
- [ ] Logs de création de demande
- [ ] Logs d'upload de fichiers

### Console Frontend
- [ ] Aucune erreur JavaScript
- [ ] Appels API réussis (200 OK)

---

**Notes:**
- Les tests doivent être effectués sur un environnement de développement
- Documenter tous les bugs découverts
- Prendre des captures d'écran si nécessaire
