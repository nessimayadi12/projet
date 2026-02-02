# Système de Demande d'Affectation TPE

## Vue d'ensemble

Ce système gère les demandes d'affectation TPE avec deux types principaux :
1. **TPE Physique** - Demandes pour terminaux de paiement physiques
2. **E-commerce** - Demandes pour plateformes de paiement en ligne

## Workflow de Demande

### 1. Création de Demande (Agence)

L'utilisateur agence crée une demande en remplissant :

#### TPE Physique
- **Données Agence** :
  - Type TPE
  - Raison Sociale
  - Activité
  - Numéro de Compte
  - Adresse
  - Code Postal
  - Code Agence (qui fait la demande)
  - Téléphone
  - Identifiant Unique RNE (pièce jointe)
  - Email Notification (à Monétique)

#### E-commerce
- **Données Agence** :
  - Raison Sociale
  - Adresse
  - Localité
  - RIB
  - Code Postal
  - Code Agence (qui fait la demande)
  - Activité
  - Téléphone
  - Identifiant Unique RNE (pièce jointe)
  - Webmaster / Contact Technique
  - URL Site Marchand
  - Email Notification (à Monétique)

### 2. Validation Monétique

L'utilisateur Monétique reçoit toutes les demandes et doit les valider avec :

#### TPE Physique
- **MCC** (Merchant Category Code) - Obligatoire
- **Taux Commission** - Obligatoire
- **Taux Commission Inter** - Optionnel
- **Loyer** - Optionnel
- **Série TPE** - Optionnel
- **N° Terminal (TID)** - Généré automatiquement
- **Value Date** - Obligatoire

#### E-commerce
- **MCC** (Merchant Category Code) - Obligatoire
- **N° Terminal (TID)** - Généré automatiquement

## Génération Automatique du N° Terminal (TID)

Le TID est généré automatiquement selon l'algorithme Luhn :

### TPE Physique
```
Composants:
- RIB (Numéro de compte)
- Code Agence
- Type TPE
- Numéro de série
```

### E-commerce
```
Composants:
- Code Agence
- Type TPE (E_COMMERCE)
- Compteur séquentiel
```

## Statuts de Demande

1. **NOUVELLE** - Demande créée par l'agence
2. **EN_COURS** - Demande en cours de traitement
3. **VALIDEE** - Demande validée par Monétique
4. **AFFECTEE** - TPE affecté au commerçant
5. **CLOTUREE** - Demande clôturée
6. **REJETEE** - Demande rejetée

## Points d'API REST

### Créer une demande
```http
POST /api/demandes
Content-Type: application/json

{
  "typeDemande": "TPE_PHYSIQUE",
  "commercantId": 1,
  "raisonSociale": "Commerçant ABC",
  "activite": "Commerce de détail",
  "numeroCompte": "1234567890",
  "adresse": "123 Rue Example",
  "codePostal": "20000",
  "codeAgence": "AG001",
  "telephone": "+212612345678",
  "emailNotification": "monetique@banque.ma"
}
```

### Valider une demande (Monétique)
```http
POST /api/demandes/{id}/valider
Content-Type: application/json

{
  "approuver": true,
  "mcc": "5411",
  "tauxCommission": 2.5,
  "tauxCommissionInter": 1.5,
  "loyer": 150.00,
  "serieTpe": "SN123456",
  "valueDate": "2026-01-29T10:00:00",
  "commentaire": "Demande validée"
}
```

### Lister toutes les demandes
```http
GET /api/demandes
```

### Récupérer une demande
```http
GET /api/demandes/{id}
```

### Récupérer les demandes en attente
```http
GET /api/demandes/en-attente
```

## Composants Frontend

### 1. DemandeFormComponent
Formulaire de création de demande avec champs dynamiques selon le type (TPE Physique / E-commerce).

**Localisation** : `src/app/demandes/demande-form/`

### 2. DemandeValidationComponent
Dialogue de validation pour l'utilisateur Monétique avec génération automatique du TID.

**Localisation** : `src/app/demandes/demande-validation/`

### 3. DemandeListComponent
Liste de toutes les demandes avec filtres et actions.

**Localisation** : `src/app/demandes/demande-list/`

## Modèles de Données

### Frontend (TypeScript)
```typescript
export interface DemandeTPE {
  id?: number;
  reference?: string;
  typeDemande: TypeDemande;
  statut: StatutDemande;
  
  // Champs agence (TPE Physique)
  raisonSociale?: string;
  activite?: string;
  numeroCompte?: string;
  adresse?: string;
  codePostal?: string;
  codeAgence?: string;
  telephone?: string;
  emailNotification?: string;
  
  // Champs validation Monétique
  mcc?: string;
  tauxCommission?: number;
  numeroTerminal?: string;
  
  // Champs E-commerce
  localite?: string;
  rib?: string;
  urlSiteMarchand?: string;
  webmaster?: string;
}
```

### Backend (Java)
```java
@Entity
@Table(name = "demandes")
public class Demande extends BaseEntity {
    private String reference;
    private TypeTPE typeDemande;
    private StatutDemande statut;
    
    // Champs agence
    private String raisonSociale;
    private String activite;
    private String numeroCompte;
    
    // Champs validation Monétique
    private String mcc;
    private Double tauxCommission;
    private String numeroTerminal;
    
    // Relations
    @ManyToOne
    private Commercant commercant;
    
    @ManyToOne
    private User demandeur;
}
```

## Sécurité et Autorisations

### Rôles
- **AGENCE** : Peut créer et consulter ses demandes
- **MONETIQUE** : Peut valider/rejeter toutes les demandes et générer les TID
- **ADMIN** : Accès complet

### Guards Angular
- `AuthGuard` : Vérifie l'authentification
- `RoleGuard` : Vérifie les rôles requis

## Notifications

Le système envoie des notifications email automatiques :
1. **Nouvelle demande** → Envoyée à Monétique
2. **Demande validée** → Envoyée à l'Agence demandeuse
3. **Demande rejetée** → Envoyée à l'Agence avec motif

## Gestion des Pièces Jointes

Les fichiers RNE sont uploadés via :
```http
POST /api/demandes/{id}/piece-jointe
Content-Type: multipart/form-data
```

Formats acceptés : PDF, JPG, JPEG, PNG

## Tests

### Tests Backend
```bash
cd TPE
mvn test
```

### Tests Frontend
```bash
cd "front end"
ng test
```

## Déploiement

### Backend
```bash
cd TPE
mvn clean package
java -jar target/tpe-management-1.0.0.jar
```

### Frontend
```bash
cd "front end"
ng build --prod
```

## Troubleshooting

### Problème : TID non généré
**Solution** : Vérifier que le RIB et le code agence sont correctement renseignés

### Problème : Validation échoue
**Solution** : Vérifier que tous les champs obligatoires sont remplis selon le type de demande

### Problème : Pièce jointe non uploadée
**Solution** : Vérifier le format et la taille du fichier (max 5MB)

## Support

Pour toute question ou problème, contactez l'équipe technique.
