# Structure du Projet - Backend TPE Management

## Vue d'ensemble du projet

```
TPE/
├── src/
│   ├── main/
│   │   ├── java/com/banque/abc/tpe/
│   │   │   ├── config/                          # Configuration
│   │   │   │   ├── ApplicationConfig.java       # Config générale (CORS, ModelMapper, Audit)
│   │   │   │   ├── DataInitializer.java         # Initialisation données (roles, users)
│   │   │   │   └── SecurityConfig.java          # Spring Security & JWT
│   │   │   │
│   │   │   ├── controller/                      # API REST Controllers
│   │   │   │   ├── AuthController.java          # Login, Register
│   │   │   │   ├── TPEController.java           # CRUD TPE, génération TID
│   │   │   │   ├── CommercantController.java    # CRUD Commerçants
│   │   │   │   ├── DemandeController.java       # CRUD Demandes, validation
│   │   │   │   └── TauxController.java          # CRUD Taux, validation 4 yeux
│   │   │   │
│   │   │   ├── dto/                             # Data Transfer Objects
│   │   │   │   ├── auth/
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── LoginResponse.java
│   │   │   │   │   └── RegisterRequest.java
│   │   │   │   ├── tpe/
│   │   │   │   │   ├── TPERequest.java
│   │   │   │   │   └── TPEResponse.java
│   │   │   │   ├── commercant/
│   │   │   │   │   ├── CommercantRequest.java
│   │   │   │   │   └── CommercantResponse.java
│   │   │   │   ├── demande/
│   │   │   │   │   ├── DemandeRequest.java
│   │   │   │   │   ├── DemandeResponse.java
│   │   │   │   │   └── ValiderDemandeRequest.java
│   │   │   │   ├── affectation/
│   │   │   │   │   ├── AffectationRequest.java
│   │   │   │   │   └── AffectationResponse.java
│   │   │   │   ├── panne/
│   │   │   │   │   ├── PanneRequest.java
│   │   │   │   │   └── PanneResponse.java
│   │   │   │   └── taux/
│   │   │   │       ├── TauxRequest.java
│   │   │   │       ├── TauxResponse.java
│   │   │   │       └── ValiderTauxRequest.java
│   │   │   │
│   │   │   ├── entity/                          # JPA Entities (Modèle de données)
│   │   │   │   ├── enums/
│   │   │   │   │   ├── StatutTPE.java           # DISPONIBLE, AFFECTE, EN_PANNE...
│   │   │   │   │   ├── TypeTPE.java             # PHYSIQUE, ECOMMERCE
│   │   │   │   │   ├── StatutCommercant.java    # ACTIF, INACTIF, SUSPENDU
│   │   │   │   │   ├── StatutDemande.java       # NOUVELLE, VALIDEE, CLOTUREE...
│   │   │   │   │   ├── StatutPanne.java         # DECLAREE, REPAREE...
│   │   │   │   │   ├── StatutTaux.java          # BROUILLON, EN_ATTENTE, VALIDE...
│   │   │   │   │   └── RoleType.java            # ROLE_ADMIN, ROLE_MONETIQUE...
│   │   │   │   ├── BaseEntity.java              # Entité de base (id, dates, version)
│   │   │   │   ├── User.java                    # Utilisateurs
│   │   │   │   ├── Role.java                    # Rôles
│   │   │   │   ├── TPE.java                     # Terminaux de paiement
│   │   │   │   ├── Commercant.java              # Commerçants
│   │   │   │   ├── Demande.java                 # Demandes TPE
│   │   │   │   ├── Affectation.java             # Affectations TPE-Commerçant
│   │   │   │   ├── Panne.java                   # Pannes et réparations
│   │   │   │   ├── Taux.java                    # Taux de commission
│   │   │   │   ├── HistoriqueStatut.java        # Historique changements statut
│   │   │   │   ├── Commentaire.java             # Commentaires sur demandes
│   │   │   │   ├── PieceJointe.java             # Fichiers attachés
│   │   │   │   ├── PieceDetachee.java           # Pièces de réparation
│   │   │   │   └── AuditLog.java                # Logs d'audit
│   │   │   │
│   │   │   ├── exception/                       # Gestion des exceptions
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── UnauthorizedException.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   └── GlobalExceptionHandler.java  # Gestionnaire global @RestControllerAdvice
│   │   │   │
│   │   │   ├── repository/                      # JPA Repositories
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── RoleRepository.java
│   │   │   │   ├── TPERepository.java
│   │   │   │   ├── CommercantRepository.java
│   │   │   │   ├── DemandeRepository.java
│   │   │   │   ├── AffectationRepository.java
│   │   │   │   ├── PanneRepository.java
│   │   │   │   ├── TauxRepository.java
│   │   │   │   ├── HistoriqueStatutRepository.java
│   │   │   │   ├── CommentaireRepository.java
│   │   │   │   ├── PieceJointeRepository.java
│   │   │   │   ├── PieceDetacheeRepository.java
│   │   │   │   └── AuditLogRepository.java
│   │   │   │
│   │   │   ├── security/                        # Sécurité JWT
│   │   │   │   ├── UserPrincipal.java           # UserDetails custom
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── JwtTokenProvider.java        # Génération/Validation JWT
│   │   │   │   ├── JwtAuthenticationFilter.java # Filtre d'authentification
│   │   │   │   └── JwtAuthenticationEntryPoint.java
│   │   │   │
│   │   │   ├── service/                         # Services métier
│   │   │   │   ├── AuthService.java             # Login, Register
│   │   │   │   ├── AuditService.java            # Logging audit
│   │   │   │   ├── TPEService.java              # Logique métier TPE
│   │   │   │   ├── CommercantService.java       # Logique métier Commerçants
│   │   │   │   ├── DemandeService.java          # Logique métier Demandes
│   │   │   │   └── TauxService.java             # Logique métier Taux (4 yeux)
│   │   │   │
│   │   │   ├── util/                            # Utilitaires
│   │   │   │   ├── TIDGenerator.java            # Génération TID + Luhn
│   │   │   │   └── ReferenceGenerator.java      # Génération références (DEM-2026-001)
│   │   │   │
│   │   │   └── TpeManagementApplication.java    # Classe principale
│   │   │
│   │   └── resources/
│   │       ├── application.properties           # Configuration Spring Boot
│   │       └── schema.sql                       # Script SQL initial
│   │
│   └── test/                                    # Tests unitaires/intégration
│
├── pom.xml                                      # Dépendances Maven
├── .gitignore                                   # Fichiers à ignorer
├── README.md                                    # Documentation principale
├── QUICKSTART.md                                # Guide démarrage rapide
├── API-ENDPOINTS.md                             # Documentation API
└── TPE-Management-API.postman_collection.json   # Collection Postman
```

## Diagramme des relations entre entités

```
User ──────┬─ many-to-many ─→ Role
           │
           ├─ one-to-many ──→ Demande (demandeur)
           ├─ one-to-many ──→ Demande (valideur)
           ├─ one-to-many ──→ Affectation (affectePar)
           ├─ one-to-many ──→ Panne (declarant/technicien)
           ├─ one-to-many ──→ Taux (inputer)
           ├─ one-to-many ──→ Taux (authorizer)
           └─ one-to-many ──→ Commentaire

Commercant ┬─ one-to-many ──→ TPE
           ├─ one-to-many ──→ Demande
           └─ one-to-many ──→ Taux

TPE ───────┬─ one-to-many ──→ Affectation
           ├─ one-to-many ──→ Panne
           └─ one-to-many ──→ HistoriqueStatut

Demande ───┬─ one-to-many ──→ Commentaire
           ├─ one-to-many ──→ PieceJointe
           └─ one-to-one ───→ Affectation

Panne ─────┴─ one-to-many ──→ PieceDetachee

AuditLog (table indépendante pour l'audit)
```

## Flux des données

### 1. Authentification
```
User input → AuthController → AuthService 
  → AuthenticationManager → CustomUserDetailsService 
  → JwtTokenProvider → JWT Token
```

### 2. Requête API protégée
```
HTTP Request with JWT → JwtAuthenticationFilter 
  → JwtTokenProvider (validation) 
  → CustomUserDetailsService → SecurityContext 
  → Controller → Service → Repository → Database
```

### 3. Génération TID
```
TPEController.generateTID() → TPEService 
  → TIDGenerator (calcul Luhn) → Save to DB 
  → AuditService (log action)
```

### 4. Workflow Taux (4 yeux)
```
Inputer: POST /taux → TauxService.createTaux()
  → Statut: BROUILLON

Inputer: POST /taux/{id}/soumettre → TauxService.soumettreValidation()
  → Statut: EN_ATTENTE_VALIDATION

Authorizer: POST /taux/{id}/valider → TauxService.validerTaux()
  → Vérification: Inputer ≠ Authorizer (règle métier)
  → Si approuvé: Statut: VALIDE, actif: true
  → Si rejeté: Statut: REJETE
```

## Couches de l'application

```
┌─────────────────────────────────────┐
│         Controllers (API)           │  ← Endpoints REST
├─────────────────────────────────────┤
│     DTOs (Request/Response)         │  ← Validation
├─────────────────────────────────────┤
│       Services (Métier)             │  ← Logique métier
├─────────────────────────────────────┤
│      Repositories (Data)            │  ← Accès BDD
├─────────────────────────────────────┤
│      Entities (Modèle)              │  ← JPA/Hibernate
├─────────────────────────────────────┤
│      SQL Server Database            │  ← Persistance
└─────────────────────────────────────┘

         ↕ (Transversal)
┌─────────────────────────────────────┐
│  Security (JWT + Spring Security)   │
│  Exception Handling (Global)        │
│  Audit Logging (AOP)                │
└─────────────────────────────────────┘
```

## Sécurité - Matrice des permissions

| Endpoint          | ADMIN | MONETIQUE | AGENCE | INPUTER | AUTHORIZER |
|-------------------|-------|-----------|--------|---------|------------|
| POST /tpes        | ✅    | ✅        | ❌     | ❌      | ❌         |
| GET /tpes         | ✅    | ✅        | ✅     | ❌      | ❌         |
| POST /commercants | ✅    | ✅        | ✅     | ❌      | ❌         |
| POST /demandes    | ✅    | ❌        | ✅     | ❌      | ❌         |
| POST /demandes/validate | ✅ | ✅     | ❌     | ❌      | ❌         |
| POST /taux        | ✅    | ❌        | ❌     | ✅      | ❌         |
| POST /taux/valider| ✅    | ❌        | ❌     | ❌      | ✅         |

## Règles métier clés (implémentées)

1. **Unicité du TID** : Chaque numéro terminal est unique
2. **Règle 4 yeux** : Inputer ≠ Authorizer pour les taux
3. **Affectation** : TPE doit être DISPONIBLE pour être affecté
4. **Validation demande** : Seule la Monétique peut valider
5. **Traçabilité** : Toutes les actions sont auditées
6. **Historique statuts** : Changements de statut TPE sont tracés
7. **Algorithme Luhn** : Validation du TID

## Technologies & Dépendances principales

| Technologie       | Version | Usage                          |
|-------------------|---------|--------------------------------|
| Java              | 17      | Langage                        |
| Spring Boot       | 3.2.1   | Framework                      |
| Spring Security   | 3.2.1   | Authentification/Autorisation  |
| JWT (jjwt)        | 0.12.3  | Tokens                         |
| JPA/Hibernate     | 6.x     | ORM                            |
| SQL Server JDBC   | Latest  | Driver BDD                     |
| Lombok            | Latest  | Réduction boilerplate          |
| ModelMapper       | 3.2.0   | DTO/Entity mapping             |
| SpringDoc OpenAPI | 2.3.0   | Documentation Swagger          |
| Apache POI        | 5.2.5   | Import/Export Excel            |

## Points d'extension possibles

- [ ] Dashboard avec statistiques temps réel
- [ ] Export Excel/PDF des rapports
- [ ] Notifications SMS via Twilio
- [ ] Intégration API bancaires externes
- [ ] Module de facturation automatique
- [ ] Gestion avancée des contrats
- [ ] Module de reporting BI
- [ ] Websockets pour notifications temps réel
- [ ] API publique pour les commerçants
- [ ] Mobile app (React Native/Flutter)

---

**Dernière mise à jour** : Janvier 2026
