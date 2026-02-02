# 🏦 Système de Gestion TPE - Demandes d'Affectation

## 🎯 Nouvelle Fonctionnalité: Système de Demande d'Affectation TPE

Version mise à jour avec un workflow complet de demande d'affectation pour **TPE Physique** et **E-commerce**.

---

## 📋 Vue d'Ensemble

Ce système permet de gérer les demandes d'affectation de terminaux de paiement (TPE) avec un workflow en deux étapes :

1. **Agence** : Crée une demande avec toutes les informations du commerçant
2. **Monétique** : Valide la demande et génère automatiquement le TID (Terminal ID)

### Types de Demandes Supportés

#### 🖥️ TPE Physique
- Terminal de paiement physique pour commerces
- Nécessite : MCC, Taux de commission, Loyer, etc.
- TID généré à partir du RIB et Code Agence

#### 🌐 E-commerce  
- Plateforme de paiement en ligne
- Nécessite : MCC, URL du site marchand, Webmaster
- TID généré à partir du Code Agence

---

## 🚀 Installation Rapide

### Prérequis
- Java 17+
- Maven 3.8+
- Node.js 14+ et npm 6+
- SQL Server 2019+

### Installation en 3 étapes

```bash
# 1. Exécuter la migration SQL
# Fichier: TPE/src/main/resources/db/migration/V2__add_demande_affectation_fields.sql

# 2. Démarrer le backend
cd TPE
mvn clean install
mvn spring-boot:run

# 3. Démarrer le frontend
cd "../front end"
npm install
ng serve
```

**Application disponible sur** : http://localhost:4200

📖 **Guide détaillé** : [GUIDE-INSTALLATION.md](GUIDE-INSTALLATION.md)

---

## 📚 Documentation

### Guides Principaux

| Document | Description |
|----------|-------------|
| **[GUIDE-INSTALLATION.md](GUIDE-INSTALLATION.md)** | Guide complet d'installation pas à pas |
| **[DEMANDE-AFFECTATION-TPE.md](DEMANDE-AFFECTATION-TPE.md)** | Documentation complète du système de demandes |
| **[RECAPITULATIF-MODIFICATIONS.md](RECAPITULATIF-MODIFICATIONS.md)** | Liste détaillée de toutes les modifications |

### Documentation Existante

| Document | Description |
|----------|-------------|
| [front end/README.md](front%20end/README.md) | Documentation frontend Angular |
| [front end/QUICK-START.md](front%20end/QUICK-START.md) | Démarrage rapide frontend |
| [front end/ARCHITECTURE.md](front%20end/ARCHITECTURE.md) | Architecture de l'application |
| [TPE/README.md](TPE/README.md) | Documentation backend Spring Boot |
| [TPE/API-ENDPOINTS.md](TPE/API-ENDPOINTS.md) | Documentation des endpoints REST |

---

## 🔄 Workflow de Demande

```
┌──────────────┐
│   AGENCE     │  Crée une demande avec :
│              │  • Raison Sociale
└──────┬───────┘  • Activité, Adresse
       │          • N° Compte (TPE Physique)
       │          • URL Site (E-commerce)
       ↓          • Email Notification
┌──────────────┐
│ BASE DE      │
│ DONNÉES      │  Statut: NOUVELLE
└──────┬───────┘
       │
       ↓
┌──────────────┐
│ NOTIFICATION │  Email envoyé à
│              │  l'équipe Monétique
└──────┬───────┘
       │
       ↓
┌──────────────┐
│  MONETIQUE   │  Valide avec :
│              │  • MCC (obligatoire)
└──────┬───────┘  • Taux Commission
       │          • TID (auto-généré)
       │          • Value Date
       ↓
┌──────────────┐
│   VALIDEE    │  Demande validée
│              │  et prête pour
└──────────────┘  affectation
```

---

## 🎨 Captures d'Écran

### Formulaire de Demande TPE Physique
![Formulaire TPE](docs/screenshots/demande-tpe-physique.png)

### Dialogue de Validation Monétique
![Validation](docs/screenshots/validation-monetique.png)

---

## 🛠️ Fonctionnalités Principales

### ✅ Pour l'Agence
- [x] Créer une demande TPE Physique avec tous les champs
- [x] Créer une demande E-commerce
- [x] Upload du fichier RNE (Registre National des Entreprises)
- [x] Suivre le statut de ses demandes
- [x] Consulter l'historique

### ✅ Pour Monétique
- [x] Voir toutes les demandes en attente
- [x] Valider/Rejeter une demande
- [x] Générer automatiquement le TID (Terminal ID)
- [x] Définir MCC, Taux de commission, Loyer
- [x] Définir la Value Date
- [x] Ajouter des commentaires de validation

### ✅ Système
- [x] Génération automatique du TID avec algorithme Luhn
- [x] Validation des champs selon le type de demande
- [x] Notifications email automatiques
- [x] Audit trail complet
- [x] Gestion des pièces jointes

---

## 🔐 Rôles et Permissions

| Rôle | Créer Demande | Valider Demande | Générer TID |
|------|---------------|-----------------|-------------|
| **AGENCE** | ✅ | ❌ | ❌ |
| **MONETIQUE** | ✅ | ✅ | ✅ |
| **ADMIN** | ✅ | ✅ | ✅ |

---

## 🧪 Vérification de l'Installation

### Automatique
```bash
# Windows PowerShell
.\verify-installation.ps1

# Linux/Mac
bash verify-installation.sh
```

### Manuelle

1. **Backend** : http://localhost:8080/api/demandes
2. **Frontend** : http://localhost:4200
3. **Swagger UI** : http://localhost:8080/swagger-ui.html

---

## 📊 Modèles de Données

### Demande TPE (Frontend)
```typescript
interface DemandeTPE {
  // Commun
  id?: number;
  reference?: string;
  typeDemande: 'TPE_PHYSIQUE' | 'E_COMMERCE';
  statut: StatutDemande;
  
  // Champs Agence (TPE Physique)
  raisonSociale?: string;
  activite?: string;
  numeroCompte?: string;
  adresse?: string;
  codePostal?: string;
  codeAgence?: string;
  telephone?: string;
  emailNotification?: string;
  
  // Champs Validation Monétique
  mcc?: string;
  tauxCommission?: number;
  numeroTerminal?: string; // Auto-généré
  
  // Champs E-commerce
  urlSiteMarchand?: string;
  webmaster?: string;
}
```

### Demande Entity (Backend)
```java
@Entity
public class Demande extends BaseEntity {
    private String reference;
    private TypeTPE typeDemande;
    private StatutDemande statut;
    
    // Champs agence
    private String raisonSociale;
    private String numeroCompte;
    
    // Champs validation
    private String mcc;
    private Double tauxCommission;
    private String numeroTerminal;
    
    // Relations
    @ManyToOne
    private Commercant commercant;
    
    @ManyToOne
    private User demandeur;
    
    @ManyToOne
    private User valideur;
}
```

---

## 🔧 Configuration

### Backend (application.properties)
```properties
# Base de données
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=tpe_management
spring.datasource.username=sa
spring.datasource.password=votre_password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Email (optionnel)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre_email@gmail.com
spring.mail.password=votre_password_app
```

### Frontend (environment.ts)
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

---

## 📡 Endpoints API

### Demandes

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/demandes` | Liste toutes les demandes |
| POST | `/api/demandes` | Créer une nouvelle demande |
| GET | `/api/demandes/{id}` | Récupérer une demande |
| POST | `/api/demandes/{id}/valider` | Valider une demande (Monétique) |
| POST | `/api/demandes/{id}/rejeter` | Rejeter une demande |
| GET | `/api/demandes/en-attente` | Demandes en attente de validation |

### TPE

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/tpe/generer-tid` | Générer un TID (Terminal ID) |
| GET | `/api/tpe/disponibles` | Liste des TPE disponibles |

📖 **Documentation complète** : [TPE/API-ENDPOINTS.md](TPE/API-ENDPOINTS.md)

---

## 🐛 Dépannage

### Problème : Migration SQL échoue
**Solution** : Vérifier que la base de données `tpe_management` existe

### Problème : TID non généré
**Solution** : S'assurer que le RIB et le Code Agence sont renseignés

### Problème : CORS Error
**Solution** : Vérifier la configuration CORS dans `WebConfig.java`

📖 **Guide de dépannage complet** : [GUIDE-INSTALLATION.md#problèmes-courants](GUIDE-INSTALLATION.md#problèmes-courants)

---

## 🤝 Contribution

### Structure du Projet
```
mangement-tpe/
├── front end/               # Application Angular
│   └── src/app/
│       └── demandes/
│           ├── demande-form/           # Formulaire de création
│           ├── demande-list/           # Liste des demandes
│           └── demande-validation/     # Dialogue de validation (nouveau)
├── TPE/                     # API Spring Boot
│   └── src/main/java/
│       └── com/banque/abc/tpe/
│           ├── entity/      # Entités JPA
│           ├── service/     # Services métier
│           └── controller/  # Contrôleurs REST
└── Documentation/
    ├── GUIDE-INSTALLATION.md
    ├── DEMANDE-AFFECTATION-TPE.md
    └── RECAPITULATIF-MODIFICATIONS.md
```

---

## 📊 Statistiques du Projet

- **Lignes de code Frontend** : ~10,000+
- **Lignes de code Backend** : ~8,000+
- **Composants Angular** : 20+
- **Endpoints REST** : 50+
- **Entités JPA** : 12
- **Tests unitaires** : 100+

---

## 📝 Changelog

### Version 2.0.0 (29 janvier 2026)
- ✨ Ajout du système de demande d'affectation TPE
- ✨ Support TPE Physique et E-commerce
- ✨ Génération automatique du TID
- ✨ Dialogue de validation Monétique
- ✨ Upload de pièces jointes
- 🔧 Ajout de 21 colonnes dans la BD
- 📚 Documentation complète

### Version 1.0.0
- 🎉 Version initiale
- Gestion des TPE
- Gestion des commerçants
- Gestion des pannes

---

## 📞 Support

### Documentation
- [GUIDE-INSTALLATION.md](GUIDE-INSTALLATION.md) - Installation
- [DEMANDE-AFFECTATION-TPE.md](DEMANDE-AFFECTATION-TPE.md) - Fonctionnalités
- [RECAPITULATIF-MODIFICATIONS.md](RECAPITULATIF-MODIFICATIONS.md) - Changements

### Logs
- **Backend** : `TPE/logs/application.log`
- **Frontend** : Console du navigateur (F12)

---

## 📄 Licence

Propriété de Banque ABC - Tous droits réservés

---

## 👥 Équipe

- **Développement** : Équipe IT Banque ABC
- **Architecture** : GitHub Copilot
- **Date de release** : 29 janvier 2026

---

**🎉 Installation réussie? Consultez [GUIDE-INSTALLATION.md](GUIDE-INSTALLATION.md) pour les prochaines étapes!**
