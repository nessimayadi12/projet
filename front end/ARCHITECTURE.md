# 🏗️ ARCHITECTURE SYSTÈME - Gestion TPE Bancaire

## Vue d'Ensemble

```
┌──────────────────────────────────────────────────────────────────┐
│                        UTILISATEURS                               │
│  ┌─────────┐  ┌─────────┐  ┌──────────┐  ┌──────────────┐      │
│  │ Agence  │  │Monétique│  │ Inputer  │  │ Authorizer   │      │
│  └────┬────┘  └────┬────┘  └────┬─────┘  └──────┬───────┘      │
└───────┼────────────┼────────────┼────────────────┼──────────────┘
        │            │            │                │
        │            │            │                │
┌───────┴────────────┴────────────┴────────────────┴──────────────┐
│                     FRONTEND ANGULAR                             │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    AUTH GUARD (JWT)                       │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │   TPE    │  │Commerçant│  │ Demandes │  │  Pannes  │       │
│  │  Module  │  │  Module  │  │  Module  │  │  Module  │       │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘       │
│       │             │              │             │              │
│  ┌────┴─────────────┴──────────────┴─────────────┴──────────┐  │
│  │               SERVICES LAYER (RxJS)                        │  │
│  │  • TpeService  • CommercantService  • DemandeService      │  │
│  │  • PanneService • TauxService • DashboardService          │  │
│  └────────────────────────┬───────────────────────────────────┘ │
│                           │                                      │
│  ┌────────────────────────┴───────────────────────────────────┐ │
│  │            HTTP INTERCEPTOR (Auto-inject JWT)              │ │
│  └────────────────────────┬───────────────────────────────────┘ │
└───────────────────────────┼──────────────────────────────────────┘
                            │ HTTP/REST
                            │
┌───────────────────────────┼──────────────────────────────────────┐
│                           ▼                                       │
│                    BACKEND JAVA                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              SPRING SECURITY (JWT Filter)                 │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │   TPE    │  │Commerçant│  │ Demandes │  │  Pannes  │       │
│  │Controller│  │Controller│  │Controller│  │Controller│       │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘       │
│       │             │              │             │              │
│  ┌────┴─────────────┴──────────────┴─────────────┴──────────┐  │
│  │                   SERVICE LAYER                            │  │
│  │  • Business Logic  • Validation  • Workflow               │  │
│  └────────────────────────┬───────────────────────────────────┘ │
│                           │                                      │
│  ┌────────────────────────┴───────────────────────────────────┐ │
│  │                   REPOSITORY LAYER (JPA)                   │ │
│  └────────────────────────┬───────────────────────────────────┘ │
└───────────────────────────┼──────────────────────────────────────┘
                            │ JDBC
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│                    BASE DE DONNÉES (MySQL/PostgreSQL)             │
│  ┌─────┐  ┌──────────┐  ┌────────┐  ┌──────┐  ┌──────────┐     │
│  │ tpe │  │commercant│  │demandes│  │pannes│  │historique│     │
│  └─────┘  └──────────┘  └────────┘  └──────┘  └──────────┘     │
└───────────────────────────────────────────────────────────────────┘
```

---

## Architecture Frontend Détaillée

```
src/
│
├── app/
│   │
│   ├── core/                          # Module Core (Singleton)
│   │   ├── auth/
│   │   │   ├── auth.service.ts        ← JWT, Login/Logout
│   │   │   ├── auth.guard.ts          ← Protection routes
│   │   │   └── auth.interceptor.ts    ← Injection token
│   │   ├── services/
│   │   │   └── notification.service.ts
│   │   └── core.module.ts
│   │
│   ├── shared/                        # Composants réutilisables
│   │   ├── components/
│   │   │   ├── navbar/
│   │   │   ├── sidebar/
│   │   │   └── footer/
│   │   ├── pipes/
│   │   ├── directives/
│   │   └── shared.module.ts
│   │
│   ├── features/                      # Modules métier
│   │   │
│   │   ├── tpe/                       # 📱 Module TPE
│   │   │   ├── components/
│   │   │   │   ├── tpe-list/
│   │   │   │   ├── tpe-form/          ← Physique + E-Commerce
│   │   │   │   └── gestion-taux/      ← 4 yeux
│   │   │   ├── services/
│   │   │   │   └── tpe.service.ts
│   │   │   ├── models/
│   │   │   │   └── tpe.model.ts
│   │   │   └── tpe.module.ts
│   │   │
│   │   ├── commercants/               # 👥 Module Commerçants
│   │   │   ├── components/
│   │   │   │   ├── commercant-list/
│   │   │   │   └── commercant-form/
│   │   │   ├── services/
│   │   │   │   └── commercant.service.ts
│   │   │   └── commercant.module.ts
│   │   │
│   │   ├── demandes/                  # 📋 Module Demandes
│   │   │   ├── components/
│   │   │   │   ├── demande-list/
│   │   │   │   ├── demande-form/
│   │   │   │   └── affectation-tpe/
│   │   │   ├── services/
│   │   │   │   └── demande.service.ts
│   │   │   └── demandes.module.ts
│   │   │
│   │   ├── maintenance/               # 🔧 Module Pannes
│   │   │   ├── components/
│   │   │   │   └── panne-list/
│   │   │   ├── services/
│   │   │   │   └── panne.service.ts
│   │   │   └── maintenance.module.ts
│   │   │
│   │   └── dashboard/                 # 📊 Dashboard
│   │       ├── dashboard.component.ts
│   │       └── dashboard.service.ts
│   │
│   ├── layouts/                       # Layouts
│   │   └── admin-layout/
│   │       ├── admin-layout.component.ts
│   │       ├── admin-layout.module.ts
│   │       └── admin-layout.routing.ts
│   │
│   ├── app.component.ts
│   ├── app.module.ts
│   └── app.routing.ts
│
└── environments/
    ├── environment.ts                 # Dev
    └── environment.prod.ts            # Prod
```

---

## Flux de Données

### 1. Création Demande TPE

```
┌─────────┐
│ Agence  │
└────┬────┘
     │ 1. Remplit formulaire
     ▼
┌────────────────┐
│ DemandeForm    │
│  Component     │
└────┬───────────┘
     │ 2. onSubmit()
     ▼
┌────────────────┐
│ DemandeService │
└────┬───────────┘
     │ 3. POST /api/demandes
     ▼
┌────────────────┐
│AuthInterceptor │ ← Injecte JWT
└────┬───────────┘
     │ 4. HTTP Request
     ▼
┌─────────────────┐
│ Backend         │
│ DemandeController│
└────┬────────────┘
     │ 5. Validation
     ▼
┌─────────────────┐
│ DemandeService  │
│ (Backend)       │
└────┬────────────┘
     │ 6. Save + Workflow
     ▼
┌─────────────────┐
│ Database        │
└────┬────────────┘
     │ 7. Response
     ▼
┌─────────────────┐
│ Frontend        │
│ Notification    │ ← "Demande créée !"
└─────────────────┘
     │
     ▼
┌─────────────────┐
│ Email Service   │ → Notifie Monétique
└─────────────────┘
```

### 2. Validation Taux (4 yeux)

```
┌─────────┐                              ┌────────────┐
│ Inputer │                              │ Authorizer │
└────┬────┘                              └─────┬──────┘
     │ 1. Saisit taux                          │
     ▼                                         │
┌──────────────┐                               │
│ GestionTaux  │                               │
│  Component   │                               │
└────┬─────────┘                               │
     │ 2. saisirTaux()                         │
     ▼                                         │
┌──────────────┐                               │
│ TauxService  │                               │
└────┬─────────┘                               │
     │ 3. POST /api/taux/saisir                │
     ▼                                         │
┌──────────────┐                               │
│   Backend    │                               │
│   Contrôle:  │                               │
│  inputerId   │                               │
└────┬─────────┘                               │
     │ 4. Statut = BROUILLON                   │
     ▼                                         │
┌──────────────┐                               │
│   Database   │                               │
└────┬─────────┘                               │
     │ 5. Notification                         │
     │───────────────────────────────────────> │
     │                                         │
     │                              6. Consulte│
     │                                 ▼       │
     │                          ┌──────────────┤
     │                          │ GestionTaux  │
     │                          │  Component   │
     │                          └──────┬───────┘
     │                                 │ 7. validerTaux()
     │                                 ▼
     │                          ┌──────────────┐
     │                          │ TauxService  │
     │                          └──────┬───────┘
     │                                 │ 8. PUT /api/taux/{id}/valider
     │                                 ▼
     │                          ┌──────────────┐
     │                          │   Backend    │
     │                          │   Contrôle:  │
     │ ⚠️ CRITIQUE              │ authorizerId │
     │ Vérification:            │      ≠       │
     │ authorizerId ≠ inputerId │  inputerId   │
     │                          └──────┬───────┘
     │                                 │ 9. Si OK: Applique taux
     │                                 ▼
     │                          ┌──────────────┐
     │                          │   Database   │
     │                          │  UPDATE tpe  │
     │                          │  SET taux=.. │
     │                          └──────┬───────┘
     │                                 │ 10. Notification
     │  <──────────────────────────────┘
     │ "Taux validé"
     ▼
```

---

## Sécurité - Flux JWT

```
┌──────────────┐
│ Utilisateur  │
└──────┬───────┘
       │ 1. Login (username, password)
       ▼
┌──────────────┐
│ AuthService  │
└──────┬───────┘
       │ 2. POST /api/auth/login
       ▼
┌──────────────┐
│   Backend    │
│ Vérification │
│ credentials  │
└──────┬───────┘
       │ 3. Si OK: Génère JWT
       │
       │ Response: { token: "eyJ...", user: {...} }
       ▼
┌──────────────┐
│ AuthService  │
│ Stocke:      │
│ - localStorage.setItem('token')
│ - currentUser (BehaviorSubject)
└──────┬───────┘
       │ 4. Redirect to /dashboard
       ▼
┌──────────────┐
│  AuthGuard   │ ← Vérifie token avant chaque route
│ canActivate()│
└──────┬───────┘
       │ 5. Token valide ?
       ├─ OUI → Accès autorisé
       └─ NON → Redirect /login
       
       
Requêtes suivantes:
       
┌──────────────┐
│ TpeService   │
│ getAllTPE()  │
└──────┬───────┘
       │ GET /api/tpe
       ▼
┌──────────────────┐
│ AuthInterceptor  │
│ Intercepte       │
│ Ajoute header:   │
│ Authorization:   │
│ Bearer eyJ...    │
└──────┬───────────┘
       │ Request avec token
       ▼
┌──────────────┐
│   Backend    │
│ JwtFilter    │
│ Vérifie      │
│ signature    │
└──────┬───────┘
       │ Token valide ?
       ├─ OUI → Controller
       └─ NON → 401 Unauthorized
                  ↓
           ┌──────────────┐
           │ AuthService  │
           │ Auto-logout  │
           └──────────────┘
```

---

## Base de Données - Schéma Relationnel

```
┌─────────────────────┐
│   utilisateur       │
├─────────────────────┤
│ id (PK)             │
│ username (UNIQUE)   │
│ password (hash)     │
│ email               │
│ nom                 │
│ prenom              │
│ role                │ ← ENUM(MONETIQUE, AGENCE, INPUTER...)
│ actif               │
│ created_at          │
└──────┬──────────────┘
       │ 1
       │
       │ N
┌──────┴──────────────┐
│   tpe               │
├─────────────────────┤
│ id (PK)             │
│ numero_serie (UQ)   │───┐
│ type_tpe            │   │
│ statut              │   │
│ marque              │   │
│ modele              │   │
│ commercant_id (FK)  │───┼───┐
│ numero_terminal     │   │   │
│ taux_commission     │   │   │
│ mcc                 │   │   │
│ url_site_marchand   │   │   │
│ created_at          │   │   │
└──────┬──────────────┘   │   │
       │ 1                │   │
       │                  │   │
       │ N                │   │
┌──────┴──────────────┐   │   │
│   historique_tpe    │   │   │
├─────────────────────┤   │   │
│ id (PK)             │   │   │
│ tpe_id (FK)         │───┘   │
│ action              │       │
│ ancienne_valeur     │       │
│ nouvelle_valeur     │       │
│ utilisateur_id (FK) │       │
│ date                │       │
└─────────────────────┘       │
                              │
                              │ N
┌─────────────────────────────┴┐
│   commercant                 │
├──────────────────────────────┤
│ id (PK)                      │
│ raison_sociale               │
│ email                        │
│ telephone                    │
│ adresse                      │
│ statut                       │
│ nombre_tpes                  │ ← Calculé
│ created_at                   │
└──────┬───────────────────────┘
       │ 1
       │
       │ N
┌──────┴──────────────┐
│   demandes          │
├─────────────────────┤
│ id (PK)             │
│ reference (UQ)      │───┐
│ commercant_id (FK)  │   │
│ type_demande        │   │
│ statut              │   │
│ urgence             │   │
│ agence_id (FK)      │   │
│ monetique_id (FK)   │   │
│ tpe_affecte_id (FK) │───┘
│ date_validation     │
│ created_at          │
└─────────────────────┘


┌─────────────────────┐
│   pannes            │
├─────────────────────┤
│ id (PK)             │
│ tpe_id (FK)         │───┐
│ type_panne          │   │
│ statut              │   │
│ urgence             │   │
│ technicien_id (FK)  │   │
│ tpe_remplacement_id │───┘
│ diagnostic          │
│ solution            │
│ temps_resolution    │
│ created_at          │
└─────────────────────┘


┌─────────────────────┐
│   taux_tpe          │
├─────────────────────┤
│ id (PK)             │
│ tpe_id (FK)         │
│ ancien_taux_com     │
│ nouveau_taux_com    │
│ statut              │
│ inputer_id (FK)     │ ← Utilisateur saisie
│ authorizer_id (FK)  │ ← Utilisateur validation
│ date_saisie         │
│ date_validation     │
│ motif_rejet         │
└─────────────────────┘

Contraintes:
• authorizer_id ≠ inputer_id (CHECK constraint)
• numero_serie UNIQUE
• numero_terminal UNIQUE
```

---

## Workflow États (State Machine)

### TPE
```
        ┌──────────────┐
    ┌──▶│ DISPONIBLE   │◀──┐
    │   └──────┬───────┘   │
    │          │            │
    │          │ affecter   │ libérer
    │          ▼            │
    │   ┌──────────────┐   │
    │   │  RESERVE     │───┘
    │   └──────┬───────┘
    │          │
    │          │ confirmer
    │          ▼
    │   ┌──────────────┐
    │   │  AFFECTE     │
    │   └──────┬───────┘
    │          │
    │          │ panne
    │          ▼
    │   ┌──────────────┐
    │   │  EN_PANNE    │
    │   └──────┬───────┘
    │          │
    │          │ réparer
    │          ▼
    │   ┌──────────────┐
    │   │EN_MAINTENANCE│
    │   └──────┬───┬───┘
    │          │   │
    │  réparé  │   │ irréparable
    └──────────┘   │
                   ▼
            ┌──────────────┐
            │ HORS_SERVICE │ (Terminal)
            └──────────────┘
```

### Demande
```
┌─────────┐
│NOUVELLE │ (Agence crée)
└────┬────┘
     │ prendre en charge
     ▼
┌─────────┐
│EN_COURS │ (Monétique traite)
└────┬────┘
     │
  ┌──┴──┐
  │     │
  │     │ valider
  ▼     ▼
┌─────┐ ┌────────┐
│REJETE││VALIDEE │
└─────┘ └───┬────┘
            │ affecter TPE
            ▼
       ┌─────────┐
       │AFFECTEE │
       └────┬────┘
            │ mise en service
            ▼
       ┌─────────┐
       │CLOTUREE │ (Terminal)
       └─────────┘
```

### Taux
```
┌──────────┐
│BROUILLON │ (Inputer saisit)
└────┬─────┘
     │ soumettre
     ▼
┌───────────────────┐
│EN_ATTENTE_VALIDATION│
└────┬──────────────┘
     │
  ┌──┴──┐
  │     │
  │     │ valider (Authorizer)
  ▼     ▼
┌─────┐ ┌──────┐
│REJETE││VALIDE│ (Taux appliqué)
└─────┘ └──────┘

Contrainte: Authorizer ≠ Inputer ⚠️
```

---

## Deployment Architecture

```
┌─────────────────────────────────────────────────┐
│              INTERNET / INTRANET                │
└──────────────────┬──────────────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────────────┐
│           LOAD BALANCER / NGINX                  │
└──────────────┬──────────────┬────────────────────┘
               │              │
       ┌───────┴──────┐   ┌──┴──────────┐
       │   Frontend   │   │   Backend   │
       │   Angular    │   │   Java      │
       │   (Static)   │   │   (API)     │
       │   Port 80    │   │   Port 8080 │
       └──────────────┘   └──────┬──────┘
                                 │
                                 ▼
                    ┌────────────────────┐
                    │   Database         │
                    │   MySQL/PostgreSQL │
                    │   Port 3306/5432   │
                    └────────────────────┘
```

---

**Architecture Validation** : ✅ Prête pour déploiement

**Dernière mise à jour** : 28 Janvier 2026
