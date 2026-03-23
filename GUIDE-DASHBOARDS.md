# 📊 DASHBOARDS TPE - GUIDE D'UTILISATION

## Vue d'ensemble

Le système de gestion TPE comprend maintenant **4 dashboards professionnels** offrant des analyses approfondies et interactives :

1. **Dashboard Principal** - Vue d'ensemble exécutive
2. **Dashboard Gestion TPE** - Analyse détaillée du parc
3. **Dashboard Demandes** - Suivi du workflow 
4. **Dashboard Pannes** - Analyse technique et maintenance

---

## 📍 Accès aux Dashboards

### Via le Dashboard Principal
Après connexion, accédez au dashboard principal puis cliquez sur l'une des cartes spécialisées :

```
/dashboard → Vue principale
/dashboard/tpe → Gestion TPE
/dashboard/demandes → Demandes d'affectation
/dashboard/pannes → Pannes & Maintenance
```

### Navigation Directe
Utilisez les URLs suivantes pour un accès direct :

- **Vue d'ensemble** : http://localhost:4200/dashboard
- **Gestion TPE** : http://localhost:4200/dashboard/tpe
- **Demandes** : http://localhost:4200/dashboard/demandes
- **Pannes** : http://localhost:4200/dashboard/pannes

---

## 🎯 Dashboard 1 : Vue d'Ensemble

### Fonctionnalités
- **8 KPIs principaux** : Total TPE, Disponibilité, Affectation, Pannes, etc.
- **3 graphiques** : Répartition par statut, par marque, et pannes
- **Navigation rapide** vers les dashboards spécialisés
- **Intégration Power BI** (si configuré)

### KPIs Affichés
```typescript
- Total TPE
- TPE Disponibles
- TPE Affectés
- TPE En Panne
- Total Commerçants
- Taux Disponibilité (%)
- MTTR (heures)
- Demandes en Attente
```

### Cas d'Usage
✅ Utilisé par la **Direction Générale** pour vision stratégique  
✅ Réunions de **reporting mensuel**  
✅ **Prise de décision** rapide

---

## 🖥️ Dashboard 2 : Gestion du Parc TPE

### Fonctionnalités
- **Filtres interactifs** : Par statut, marque, agence
- **4 KPIs** : Total TPE, Taux de disponibilité, Taux d'affectation, Taux de panne
- **5 visualisations** :
  - Graphique en anneau : Répartition par statut
  - Graphique à barres : TPE par marque
  - Graphique en courbes : Évolution sur 6 mois
  - Liste détaillée : Stats par statut
  - Graphique empilé : Distribution par agence (17 agences)

### Filtres Disponibles
```typescript
// Sélection multiple
- Statut : TOUS | DISPONIBLE | AFFECTE | EN_PANNE | MAINTENANCE | HORS_SERVICE
- Marque : TOUTES | Ingenico | Verifone | PAX | Autres
- Agence : TOUTES | Lac 2 | Centre Ville | Sousse | Sfax | ...
```

### Actions
- **Export Excel/CSV** : Exporter les données filtrées
- **Actualisation** : Rafraîchir les données en temps réel
- **Drill-down** : Cliquer sur un graphique pour plus de détails

### Cas d'Usage
✅ **Direction Moyens de Paiement** : Pilotage opérationnel  
✅ **Gestionnaires TPE** : Suivi quotidien du stock  
✅ **Responsables Agences** : Suivi de leur parc local

---

## 📋 Dashboard 3 : Demandes d'Affectation

### Fonctionnalités
- **8 KPIs** : Nouvelles, En cours, Délai moyen, En retard, etc.
- **6 visualisations** :
  - Entonnoir de conversion : Pipeline des demandes (NOUVELLE → CLOTUREE)
  - Graphique à barres : Demandes par statut
  - Graphique en courbes : Évolution mensuelle
  - Graphique horizontal : Délai par étape
  - Table d'alertes : Demandes en retard (>3 jours)
  - Benchmarking : Performance par agence

### KPIs Clés
```typescript
- Demandes Nouvelles : 24
- Demandes En Cours : 18
- Délai Moyen : 2.3 jours
- En Retard (>48h) : 3
- Taux de Conversion : 37.6%
- Clôturées ce Mois : 156
- SLA Respect : 89%
- Productivité : 7.2 demandes/jour/agent
```

### Alertes Automatiques
🔴 **Demandes en retard** : Affichage des demandes dépassant le SLA de 48h  
🟡 **Pic anormal** : Notification si volume > moyenne +50%  
🟢 **Performance** : Suivi du taux de respect des SLA par agence

### Cas d'Usage
✅ **Gestionnaires de Demandes** : Suivi opérationnel quotidien  
✅ **Chefs d'Agence** : Validation et approbation des demandes  
✅ **Direction Opérationnelle** : Optimisation des processus

---

## 🔧 Dashboard 4 : Pannes & Maintenance

### Fonctionnalités
- **8 KPIs techniques** : Pannes en cours, MTTR, Taux de panne, etc.
- **5 visualisations** :
  - Graphique en courbes : Évolution par type (Matériel, Logiciel, Réseau)
  - Graphique circulaire : Répartition par type de panne
  - Analyse de Pareto : Top 10 TPE problématiques (80/20)
  - Heatmap : Pannes par jour et heure
  - Liste détaillée : Durée de résolution par type

### KPIs Techniques
```typescript
- Pannes En Cours : 12
- Résolues ce Mois : 45
- MTTR (Mean Time To Repair) : 18.5 heures
- Taux de Panne : 1.4%
- TPE en Maintenance : 18
- Taux de Résolution : 79%
- Maintenance Préventive : 12 TPE à planifier
- En Attente Pièces : 3
```

### Analyse de Pareto (80/20)
Le graphique de Pareto identifie les **20% de TPE** qui causent **80% des pannes** :
```
TPE-123 : 15 pannes (12%)
TPE-456 : 12 pannes (10%)
TPE-789 : 11 pannes (9%)
...
→ Recommandation : Remplacer ces 10 TPE en priorité
```

### Maintenance Préventive
Table interactive affichant les TPE nécessitant une maintenance préventive :
- 🔴 **Urgent** : Dernière maintenance > 90 jours
- 🟡 **Moyenne** : Dernière maintenance > 60 jours
- 🟢 **Normale** : Dernière maintenance > 45 jours

### Cas d'Usage
✅ **Équipe Technique** : Planification des interventions  
✅ **Responsable Maintenance** : Suivi du MTTR et optimisation  
✅ **Direction IT** : Décisions d'investissement (remplacement TPE)

---

## 🚀 Installation et Configuration

### Prérequis
```bash
Node.js >= 14.x
Angular CLI >= 13.x
```

### Installation des dépendances
```bash
cd "front end"
npm install chart.js --save
npm install @angular/material --save
```

### Configuration des routes
Les routes sont automatiquement configurées dans :
```
front end/src/app/layouts/admin-layout/admin-layout.routing.ts
```

### Variables d'environnement
Aucune configuration spéciale requise. Les dashboards utilisent l'API existante.

---

## 📊 Intégration des Graphiques

### Chart.js
Les dashboards utilisent **Chart.js 4.x** pour des visualisations modernes :

```typescript
// Exemple : Graphique en anneau
this.statutChart = new Chart(ctx, {
  type: 'doughnut',
  data: {
    labels: ['Disponibles', 'Affectés', 'En Panne'],
    datasets: [{
      data: [185, 652, 12],
      backgroundColor: ['#28a745', '#007bff', '#dc3545']
    }]
  }
});
```

### Types de graphiques disponibles
- ✅ Doughnut (Anneau)
- ✅ Bar (Barres verticales/horizontales)
- ✅ Line (Courbes)
- ✅ Pie (Camembert)
- ✅ Radar
- ✅ Scatter (Nuage de points)

---

## 🎨 Personnalisation

### Couleurs Bank ABC
Les dashboards utilisent les couleurs de la charte graphique :

```css
--primary: #003366 (Bleu marine)
--secondary: #00b4d8 (Bleu ciel)
--accent: #ffc107 (Or)
--success: #28a745 (Vert)
--warning: #ffc107 (Orange)
--danger: #dc3545 (Rouge)
```

### Modifier un graphique
Pour personnaliser un graphique, éditez le fichier TypeScript correspondant :

```typescript
// dashboard-tpe.component.ts - Ligne 60
createStatutChart(): void {
  // Modifier les couleurs, labels, ou données ici
  backgroundColor: ['#VOTRE_COULEUR', ...]
}
```

---

## 🔒 Permissions et Sécurité

### Contrôle d'accès
Chaque dashboard nécessite une permission spécifique :

```typescript
// admin-layout.routing.ts
{ 
  path: 'dashboard/tpe', 
  component: DashboardTpeComponent, 
  canActivate: [AuthGuard], 
  data: { screenCode: 'DASHBOARD_TPE' } 
}
```

### Rôles par défaut
- **ADMIN** : Accès à tous les dashboards
- **MONETIQUE** : Accès TPE + Demandes + Pannes
- **AGENCE** : Accès TPE + Demandes (lecture seule)
- **COMMERCANT** : Pas d'accès (uniquement ses propres TPE)

---

## 📱 Responsive Design

Tous les dashboards sont **100% responsive** :
- **Desktop** (>1200px) : 4 colonnes
- **Tablette** (768-1199px) : 2 colonnes
- **Mobile** (<768px) : 1 colonne

Les graphiques s'adaptent automatiquement à la taille de l'écran.

---

## 🐛 Dépannage

### Erreur : "Cannot find module 'chart.js'"
```bash
npm install chart.js --save
ng serve
```

### Les graphiques ne s'affichent pas
Vérifiez que les éléments canvas existent :
```typescript
setTimeout(() => {
  this.createStatutChart();
}, 100);
```

### Erreur de permissions
Vérifiez que votre utilisateur possède le screenCode requis dans la base de données.

### Données vides
Assurez-vous que le backend tourne :
```bash
cd TPE
./start.bat
```

---

## 📈 Évolutions Futures

### Version 2.0 (Prévue)
- ⏳ Dashboard Commerçants (Top clients, Segmentation)
- ⏳ Dashboard Performance (KPIs opérationnels, Productivité)
- ⏳ Dashboard Financier (ROI, TCO, Coûts)
- ⏳ Intégration IA (Prédiction des pannes)
- ⏳ Alertes en temps réel (WebSocket)
- ⏳ Export PDF automatique
- ⏳ Application mobile

---

## 🆘 Support

### Documentation
- Guide Power BI : `GUIDE-POWER-BI.md`
- API Endpoints : `TPE/API-ENDPOINTS.md`
- Architecture : `front end/ARCHITECTURE.md`

### Contact
- Email : support@bankabc.tn
- Équipe DSI : (+216) 70 292 000

---

## ✅ Checklist de Déploiement

Avant de déployer en production :

- [ ] Tests sur environnement de développement réussis
- [ ] Permissions configurées pour tous les rôles
- [ ] Backend API opérationnel
- [ ] Données de test chargées
- [ ] Graphiques s'affichent correctement
- [ ] Responsive testé (Mobile, Tablette, Desktop)
- [ ] Sécurité validée (AuthGuard actif)
- [ ] Performance vérifiée (Temps de chargement < 3s)
- [ ] Documentation utilisateur fournie
- [ ] Formation des utilisateurs finaux effectuée

---

**Date de création** : Février 2026  
**Version** : 1.0  
**Auteur** : Équipe DSI Bank ABC Tunisie

---

🎉 **Vos dashboards sont maintenant opérationnels !** 🎉
