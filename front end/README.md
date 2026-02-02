# 🏦 Système de Gestion du Parc TPE Bancaire - Frontend

[![Angular](https://img.shields.io/badge/Angular-14+-red.svg)](https://angular.io/)
[![TypeScript](https://img.shields.io/badge/TypeScript-4+-blue.svg)](https://www.typescriptlang.org/)
[![Material Dashboard](https://img.shields.io/badge/Material%20Dashboard-Free-green.svg)](https://www.creative-tim.com/product/material-dashboard-angular2)
[![License](https://img.shields.io/badge/License-Proprietary-yellow.svg)]()

## 📖 À Propos

Application web Angular pour la gestion complète du parc **TPE (Terminaux de Paiement Électronique)** et **E-commerce** de la Banque ABC.

### 🎯 Objectifs
- ✅ Centraliser stock, affectations, maintenance et historique
- ✅ Digitaliser les workflows (demande → validation → affectation)
- ✅ Automatiser la génération TID et la traçabilité
- ✅ Améliorer la qualité de service commerçants

---

## 📚 DOCUMENTATION COMPLÈTE

> **🚀 Pour démarrer rapidement** : Consultez [QUICK-START.md](QUICK-START.md)

### 📑 Index de la Documentation

| Document | Description | Pour qui ? |
|----------|-------------|------------|
| **[INDEX.md](INDEX.md)** | 📚 Index complet de toute la documentation | Tous |
| **[QUICK-START.md](QUICK-START.md)** | 🚀 Guide de démarrage rapide (10 min) | Débutants |
| **[README-PROJET.md](README-PROJET.md)** | 📖 Documentation complète du projet | Tous |
| **[ARCHITECTURE.md](ARCHITECTURE.md)** | 🏗️ Architecture technique détaillée | Architectes, Devs |
| **[REGLES-METIER.md](REGLES-METIER.md)** | ⚖️ 15 règles métier à respecter | Devs, QA |
| **[MODIFICATIONS-DETAILLEES.md](MODIFICATIONS-DETAILLEES.md)** | 📝 Liste des modifications apportées | Équipe projet |

### 🗂️ Documentation Backend
- [../TPE/README.md](../TPE/README.md) - Backend Java/Spring Boot
- [../TPE/API-ENDPOINTS.md](../TPE/API-ENDPOINTS.md) - API REST
- [../TPE/STRUCTURE.md](../TPE/STRUCTURE.md) - Structure backend

---

## ⚡ Installation Rapide

### Prérequis
```bash
node --version  # v14+ requis
npm --version   # v6+ requis
```

### Installation
```bash
cd "front end"
npm install
ng serve
```

✅ Application disponible sur **http://localhost:4200**

> **Pour plus de détails** : Voir [QUICK-START.md](QUICK-START.md)

---

## 🎯 Fonctionnalités Principales

### 📱 Gestion TPE
- TPE Physique et E-Commerce
- Génération automatique TID (algorithme Luhn)
- 6 statuts (Disponible, Affecté, En panne, etc.)
- Recherche multicritère
- Import/Export massif
- Historique complet

### 👥 Gestion Commerçants
- CRUD complet
- Upload fichier RNE
- Historique TPE par commerçant
- Top commerçants

### 📋 Workflow Demandes
- Création par Agence
- Validation Monétique
- Affectation TPE
- Génération documents (bon livraison, contrat)
- Notifications automatiques

### 🔧 Maintenance & Pannes
- Déclaration et suivi
- TPE de remplacement
- Gestion interventions
- Temps de résolution (MTTR)

### 💰 Gestion Taux (4 yeux)
- Saisie par Inputer
- Validation par Authorizer
- Contrôle : Inputer ≠ Authorizer ⚠️
- Traçabilité complète

### 📊 Dashboards
- Monétique : Vue complète du parc
- Agence : Mes demandes et pannes
- Graphiques interactifs (Chartist.js)

---

## 👥 Acteurs du Système

| Rôle | Permissions |
|------|-------------|
| **Monétique** | Gestion stock, Validation demandes, Affectation TPE |
| **Agence** | Création demandes, Signalement pannes |
| **Inputer** | Saisie des taux TPE |
| **Authorizer** | Validation des taux (≠ Inputer) |
| **Technicien** | Gestion pannes et réparations |

---

## 🏗️ Architecture

```
Frontend Angular (Port 4200)
     │
     ├── Auth (JWT)
     ├── Guards & Interceptors
     ├── Modules (TPE, Commerçants, Demandes, Maintenance)
     ├── Services (HTTP + RxJS)
     └── Components (Material Dashboard)
     
Backend Java (Port 8080)
     │
     ├── Spring Security (JWT)
     ├── Controllers REST
     ├── Services (Business Logic)
     ├── Repositories (JPA)
     └── Database (MySQL/PostgreSQL)
```

> **Détails complets** : Voir [ARCHITECTURE.md](ARCHITECTURE.md)

---

## 🔐 Sécurité

- **Authentification** : JWT Token
- **Autorisation** : RBAC (Role-Based Access Control)
- **Guards** : Protection routes selon rôles
- **Interceptors** : Auto-injection token
- **Validation** : Frontend + Backend

---

## ⚖️ Règles Métier Critiques

1. ✅ Un TPE = 1 commerçant à la fois
2. ✅ Numéro série unique
3. ✅ TID auto-généré (algorithme Luhn)
4. ✅ Modification taux : **Inputer ≠ Authorizer** 🔴
5. ✅ URL obligatoire pour E-Commerce
6. ✅ Traçabilité complète

> **Liste complète** : Voir [REGLES-METIER.md](REGLES-METIER.md)

---

## 🧪 Tests

```bash
# Tests unitaires
npm run test

# Tests E2E
npm run e2e

# Coverage
npm run test:coverage
```

---

## 📦 Build Production

```bash
ng build --prod
# Fichiers générés dans dist/
```

---

## 🔄 Workflow Exemple

### Créer une Demande TPE (Agence → Monétique)

```
1. Agence crée demande (formulaire)
2. Statut : NOUVELLE
3. Notification → Monétique
4. Monétique valide
5. Statut : VALIDEE
6. Affectation TPE disponible
7. Statut : AFFECTEE
8. Génération bon livraison + contrat
9. Mise en service
10. Statut : CLOTUREE
```

> **Tous les workflows détaillés** : Voir [QUICK-START.md](QUICK-START.md)

---

## 📞 Support

### Documentation
- **Guide démarrage** : [QUICK-START.md](QUICK-START.md)
- **Documentation complète** : [README-PROJET.md](README-PROJET.md)
- **Index** : [INDEX.md](INDEX.md)

### Contacts
- Frontend Lead : [À définir]
- Backend Lead : [À définir]
- Product Owner : [À définir]

---

## 📋 Checklist Premier Lancement

- [ ] Node.js 14+ installé
- [ ] npm install terminé
- [ ] Backend lancé (port 8080)
- [ ] ng serve lancé (port 4200)
- [ ] Connexion réussie
- [ ] Test création TPE ✅
- [ ] Test création demande ✅
- [ ] Test workflow taux (4 yeux) ✅

---

## 🚀 Prochaines Étapes

### Pour démarrer :
1. ✅ Lire [QUICK-START.md](QUICK-START.md)
2. ✅ Installer le projet
3. ✅ Tester les workflows

### Pour contribuer :
1. ✅ Lire [README-PROJET.md](README-PROJET.md)
2. ✅ Comprendre [ARCHITECTURE.md](ARCHITECTURE.md)
3. ✅ Respecter [REGLES-METIER.md](REGLES-METIER.md)

---

## 📄 License

Propriétaire - Banque ABC © 2026

---

## 🎯 Version

**1.0.0** - Janvier 2026

✅ Frontend complètement adapté au système de gestion TPE bancaire  
✅ Documentation complète  
✅ Prêt pour tests d'intégration  

---

## 📚 Material Dashboard (Base Template)

Ce projet utilise **Material Dashboard Angular** de Creative Tim comme base template.

- [Material Dashboard Original](https://www.creative-tim.com/product/material-dashboard-angular2)
- [GitHub](https://github.com/creativetimofficial/material-dashboard-angular2)

**Adaptations majeures** :
- ✅ Tous les modules métier créés (TPE, Commerçants, Demandes, Maintenance)
- ✅ Services complets (8 services)
- ✅ Authentification JWT
- ✅ Guards et Interceptors
- ✅ Workflows métier
- ✅ Dashboards personnalisés

---

**Pour toute question, consultez [INDEX.md](INDEX.md) pour naviguer dans la documentation** 📚


| Dashboard | User Profile | Tables | Icons | Notifications |
| --- | --- | --- | --- | --- |
| [![Start page](https://raw.githubusercontent.com/creativetimofficial/public-assets/master/material-dashboard-angular/dashboard.png?raw=true)](https://demos.creative-tim.com/material-dashboard-angular2/#/dashboard) | [![User profile page](https://raw.githubusercontent.com/creativetimofficial/public-assets/master/material-dashboard-angular/user-profile.png?raw=true)](https://demos.creative-tim.com/material-dashboard-angular2/#/user-profile) | [![Tables page ](https://raw.githubusercontent.com/creativetimofficial/public-assets/master/material-dashboard-angular/tables.png?raw=true)](https://demos.creative-tim.com/material-dashboard-angular2/#/table-list) | [![Icons Page](https://raw.githubusercontent.com/creativetimofficial/public-assets/master/material-dashboard-angular/icons.png?raw=true)](https://demos.creative-tim.com/material-dashboard-angular2/#/maps) | [![Notifications page](https://raw.githubusercontent.com/creativetimofficial/public-assets/master/material-dashboard-angular/notifications.png?raw=true)](https://demos.creative-tim.com/material-dashboard-angular2/#/notifications)

[View More](https://demos.creative-tim.com/material-dashboard-angular2/#/dashboard).

## Quick start

Quick start options:

- [Download from Github](https://github.com/tiniestory/material-dashboard-angular2/archive/master.zip).
- [Download from Creative Tim](http://www.creative-tim.com/product/material-dashboard-angular2).

## Deploy

:rocket: You can deploy your own version of the template to Genezio with one click:

[![Deploy to Genezio](https://raw.githubusercontent.com/Genez-io/graphics/main/svg/deploy-button.svg)](https://app.genez.io/start/deploy?repository=https://github.com/creativetimofficial/material-dashboard-angular2&utm_source=github&utm_medium=referral&utm_campaign=github-creativetim&utm_term=deploy-project&utm_content=button-head)

## Terminal Commands

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 1.0.0 and angular 4.x.

1. Install NodeJs from [NodeJs Official Page](https://nodejs.org/en).
2. Open Terminal
3. Go to your file project
4. Make sure you have installed [Angular CLI](https://github.com/angular/angular-cli) already. If not, please install.
5. Run in terminal: ```npm install```
6. Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).

### What's included

Within the download you'll find the following directories and files:

```
material-dashboard-angular
├── CHANGELOG.md
├── LICENSE.md
├── README.md
├── angular-cli.json
├── documentation
├── e2e
├── karma.conf.js
├── package-lock.json
├── package.json
├── protractor.conf.js
├── src
│   ├── app
│   │   ├── app.component.css
│   │   ├── app.component.html
│   │   ├── app.component.spec.ts
│   │   ├── app.component.ts
│   │   ├── app.module.ts
│   │   ├── app.routing.ts
│   │   ├── components
│   │   │   ├── components.module.ts
│   │   │   ├── footer
│   │   │   │   ├── footer.component.css
│   │   │   │   ├── footer.component.html
│   │   │   │   ├── footer.component.spec.ts
│   │   │   │   └── footer.component.ts
│   │   │   ├── navbar
│   │   │   │   ├── navbar.component.css
│   │   │   │   ├── navbar.component.html
│   │   │   │   ├── navbar.component.spec.ts
│   │   │   │   └── navbar.component.ts
│   │   │   └── sidebar
│   │   │       ├── sidebar.component.css
│   │   │       ├── sidebar.component.html
│   │   │       ├── sidebar.component.spec.ts
│   │   │       └── sidebar.component.ts
│   │   ├── dashboard
│   │   │   ├── dashboard.component.css
│   │   │   ├── dashboard.component.html
│   │   │   ├── dashboard.component.spec.ts
│   │   │   └── dashboard.component.ts
│   │   ├── icons
│   │   │   ├── icons.component.css
│   │   │   ├── icons.component.html
│   │   │   ├── icons.component.spec.ts
│   │   │   └── icons.component.ts
│   │   ├── layouts
│   │   │   └── admin-layout
│   │   │       ├── admin-layout.component.html
│   │   │       ├── admin-layout.component.scss
│   │   │       ├── admin-layout.component.spec.ts
│   │   │       ├── admin-layout.component.ts
│   │   │       ├── admin-layout.module.ts
│   │   │       └── admin-layout.routing.ts
│   │   ├── maps
│   │   │   ├── maps.component.css
│   │   │   ├── maps.component.html
│   │   │   ├── maps.component.spec.ts
│   │   │   └── maps.component.ts
│   │   ├── notifications
│   │   │   ├── notifications.component.css
│   │   │   ├── notifications.component.html
│   │   │   ├── notifications.component.spec.ts
│   │   │   └── notifications.component.ts
│   │   ├── table-list
│   │   │   ├── table-list.component.css
│   │   │   ├── table-list.component.html
│   │   │   ├── table-list.component.spec.ts
│   │   │   └── table-list.component.ts
│   │   ├── typography
│   │   │   ├── typography.component.css
│   │   │   ├── typography.component.html
│   │   │   ├── typography.component.spec.ts
│   │   │   └── typography.component.ts
│   │   ├── upgrade
│   │   │   ├── upgrade.component.css
│   │   │   ├── upgrade.component.html
│   │   │   ├── upgrade.component.spec.ts
│   │   │   └── upgrade.component.ts
│   │   └── user-profile
│   │       ├── user-profile.component.css
│   │       ├── user-profile.component.html
│   │       ├── user-profile.component.spec.ts
│   │       └── user-profile.component.ts
│   ├── assets
│   │   ├── css
│   │   │   └── demo.css
│   │   ├── img
│   │   └── scss
│   │       ├── core
│   │       └── material-dashboard.scss
│   ├── environments
│   ├── favicon.ico
│   ├── index.html
│   ├── main.ts
│   ├── polyfills.ts
│   ├── styles.css
│   ├── test.ts
│   ├── tsconfig.app.json
│   ├── tsconfig.spec.json
│   └── typings.d.ts
├── tsconfig.json
├── tslint.json
└── typings

```

## Browser Support

At present, we officially aim to support the last two versions of the following browsers:

<img src="https://s3.amazonaws.com/creativetim_bucket/github/browser/chrome.png" width="64" height="64"> <img src="https://s3.amazonaws.com/creativetim_bucket/github/browser/firefox.png" width="64" height="64"> <img src="https://s3.amazonaws.com/creativetim_bucket/github/browser/edge.png" width="64" height="64"> <img src="https://s3.amazonaws.com/creativetim_bucket/github/browser/safari.png" width="64" height="64"> <img src="https://s3.amazonaws.com/creativetim_bucket/github/browser/opera.png" width="64" height="64">



## Resources
- Demo: <https://demos.creative-tim.com/material-dashboard-angular2/#/dashboard>
- Download Page: <https://www.creative-tim.com/product/material-dashboard-angular2>
- Documentation: <https://demos.creative-tim.com/material-dashboard-angular2/#/documentation/tutorial>
- License Agreement: <https://www.creative-tim.com/license>
- Support: <https://www.creative-tim.com/contact-us>
- Issues: [Github Issues Page](https://github.com/creativetimofficial/material-dashboard-angular2/issues)
- [Material Kit](https://www.creative-tim.com/product/material-kit?ref=github-mda-free) - For Front End Development

## Reporting Issues

We use GitHub Issues as the official bug tracker for the Material Dashboard. Here are some advices for our users that want to report an issue:

1. Make sure that you are using the latest version of the Material Dashboard. Check the CHANGELOG from your dashboard on our [website](https://www.creative-tim.com/).
2. Providing us reproducible steps for the issue will shorten the time it takes for it to be fixed.
3. Some issues may be browser specific, so specifying in what browser you encountered the issue might help.


## Technical Support or Questions

If you have questions or need help integrating the product please [contact us](https://www.creative-tim.com/contact-us) instead of opening an issue.



## Licensing

- Copyright 2018 Creative Tim (https://www.creative-tim.com/)

- Licensed under MIT (https://github.com/creativetimofficial/material-dashboard-angular2/blob/master/LICENSE.md)


## Useful Links

- [More products](https://www.creative-tim.com/bootstrap-themes) from Creative Tim
- [Tutorials](https://www.youtube.com/channel/UCVyTG4sCw-rOvB9oHkzZD1w)
- [Freebies](https://www.creative-tim.com/bootstrap-themes/free) from Creative Tim
- [Affiliate Program](https://www.creative-tim.com/affiliates/new) (earn money)

##### Social Media

Twitter: <https://twitter.com/CreativeTim>

Facebook: <https://www.facebook.com/CreativeTim>

Dribbble: <https://dribbble.com/creativetim>

Google+: <https://plus.google.com/+CreativetimPage>

Instagram: <https://www.instagram.com/CreativeTimOfficial>

[CHANGELOG]: ./CHANGELOG.md

[version-badge]: https://img.shields.io/badge/version-2.8.0-blue.svg
