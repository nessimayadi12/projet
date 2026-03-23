# Documentation - Implémentation de l'Upload de Fichier de Transactions

## Vue d'ensemble

J'ai implémenté une fonctionnalité complète d'upload et de traitement de fichiers de transactions pour votre application Spring Boot/Angular. Cette implémentation est basée sur la méthode C# que vous avez fournie.

## Architecture Implémentée

### Backend (Spring Boot)

#### 1. Entité TPEPostingComp
**Fichier**: `TPE/src/main/java/com/banque/abc/tpe/entity/TPEPostingComp.java`

Entité JPA qui représente une écriture comptable avec les champs suivants:
- Branch, Client, Account, Ref
- Date, Amount, CR_DR (Crédit/Débit)
- Narrative, Tran Type, RB_GL
- Session Date, Currency (ccy)

#### 2. Repository
**Fichier**: `TPE/src/main/java/com/banque/abc/tpe/repository/TPEPostingCompRepository.java`

Interface JPA standard pour les opérations CRUD.

#### 3. Service FileUploadService
**Fichier**: `TPE/src/main/java/com/banque/abc/tpe/service/FileUploadService.java`

Le service principal qui traite le fichier ligne par ligne:

**Fonctionnalités:**
- Parse chaque ligne du fichier
- Traite deux types de lignes:
  - **Type "10"**: Transactions commerciales avec calcul de commissions
  - **Type "20"**: Paiements par carte avec gestion multi-devises
- Valide l'existence des TPE et des cartes dans la base de données
- Gère les conversions de devises étrangères
- Insère les écritures comptables en batch

**Logique de traitement:**

**Lignes Type "10"**:
- Vérifie l'existence du TPE via N_AFFILIATION
- Crée 4 écritures comptables:
  1. Débit 150.1103.0000 (montant principal)
  2. Crédit 151.1105.0000 (montant principal)
  3. Débit 601.9106.0000 (commission)
  4. Crédit 150.1103.0000 (commission)

**Lignes Type "20"**:
- Vérifie l'existence du TPE et de la carte
- Si devise = TND/TNC: 2 écritures simples
- Si devise étrangère: 4 écritures avec conversion de devise

#### 4. Controller FileUploadController
**Fichier**: `TPE/src/main/java/com/banque/abc/tpe/controller/FileUploadController.java`

Endpoint REST: `POST /api/file-upload/process`
- Accepte un fichier MultipartFile
- Retourne un JSON avec statut de succès/échec
- Sécurisé avec @PreAuthorize (rôles MONETIQUE ou ADMIN)

### Frontend (Angular)

#### 1. Service FileUploadService
**Fichier**: `front end/src/app/services/file-upload.service.ts`

Service Angular qui communique avec l'API:
- Méthode `uploadFile()` qui envoie le fichier via FormData

#### 2. Composant FileUploadComponent
**Fichiers**: 
- `front end/src/app/components/file-upload/file-upload.component.ts`
- `front end/src/app/components/file-upload/file-upload.component.html`
- `front end/src/app/components/file-upload/file-upload.component.css`

**Fonctionnalités UI:**
- Zone de sélection de fichier avec drag & drop visuel
- Affichage du nom et de la taille du fichier sélectionné
- Indicateur de progression pendant le traitement
- Messages de succès/erreur
- Section d'instructions pour l'utilisateur
- Boutons Traiter et Annuler

#### 3. Configuration des Routes
**Fichiers modifiés**:
- `front end/src/app/layouts/admin-layout/admin-layout.routing.ts`
- `front end/src/app/layouts/admin-layout/admin-layout.module.ts`
- `front end/src/app/components/components.module.ts`
- `front end/src/app/components/sidebar/sidebar.component.ts`

Route ajoutée: `/file-upload` avec accès restreint aux rôles ADMIN et MONETIQUE.

Entrée de menu ajoutée dans la sidebar avec l'icône "cloud_upload".

## Format du Fichier Attendu

Le fichier doit être au format texte (.txt ou .dat) avec des lignes à positions fixes:

**Ligne Type "10"** (≥100 caractères):
- Positions 0-1: "10"
- Positions 16-25: N_AFFILIATION (10 caractères)
- Positions 50-74: Narrative (25 caractères)
- Positions 219-230: Montant commission (12 chiffres)
- Positions 242-253: Montant principal (12 chiffres)

**Ligne Type "20"** (≥227 caractères):
- Positions 0-1: "20"
- Position 99: Indicateur "T" ou "I"
- Positions 15-24: N_AFFILIATION (10 caractères)
- Positions 50-74: Narrative (25 caractères)
- Positions 113-128: N_CARTE (16 chiffres)
- Positions 203-208: Date transaction (YYMMDD)
- Positions 209-214: Référence (6 caractères)
- Positions 215-226: Montant (12 chiffres)

## Tables de Base de Données Requises

L'implémentation suppose l'existence des tables suivantes:

1. **TPE**: Avec colonnes N_AFFILIATION, N_compte
2. **PORTEUR**: Informations des cartes avec ncarte, compte, devise, typecarte
3. **FM_CURRENCY**: Devises avec ccy, ccy_id
4. **RATES**: Taux de change avec ccy, ccy_rate, deci_places, effective_date
5. **TPE_POSTING_comp**: Table créée pour stocker les écritures (sera créée automatiquement par JPA)

## Utilisation

1. **Accéder à la page**: Cliquez sur "Upload Transactions" dans le menu latéral
2. **Sélectionner le fichier**: Cliquez dans la zone ou glissez-déposez le fichier
3. **Traiter**: Cliquez sur "Traiter le fichier"
4. **Vérifier**: Un message de succès/erreur s'affichera

## Sécurité

- Endpoint protégé par Spring Security
- Accès restreint aux rôles MONETIQUE et ADMIN
- Gestion des erreurs avec logs détaillés
- Validation de l'existence des données avant insertion

## Points d'attention

1. **Performance**: Les insertions sont effectuées en batch pour optimiser les performances
2. **Transactions**: Le traitement est transactionnel (@Transactional)
3. **Logging**: Logs détaillés pour le débogage (via SLF4J)
4. **Gestion d'erreurs**: Les erreurs de parsing sont loguées mais n'interrompent pas le traitement des autres lignes

## Prochaines étapes recommandées

1. Créer un script SQL pour initialiser la table TPE_POSTING_comp si nécessaire
2. Tester avec un fichier réel
3. Ajouter des validations supplémentaires si nécessaire
4. Créer des rapports de traitement pour visualiser les transactions importées
5. Ajouter un historique des uploads de fichiers
