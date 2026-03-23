# 🚀 Installation des Nouveaux Dashboards TPE

## ✅ Fichiers Créés

### Composants Dashboard TPE
- ✅ `src/app/dashboard-tpe/dashboard-tpe.component.ts` (388 lignes)
- ✅ `src/app/dashboard-tpe/dashboard-tpe.component.html` (272 lignes)
- ✅ `src/app/dashboard-tpe/dashboard-tpe.component.css` (98 lignes)

### Composants Dashboard Demandes
- ✅ `src/app/dashboard-demandes/dashboard-demandes.component.ts` (273 lignes)
- ✅ `src/app/dashboard-demandes/dashboard-demandes.component.html` (361 lignes)
- ✅ `src/app/dashboard-demandes/dashboard-demandes.component.css` (61 lignes)

### Composants Dashboard Pannes
- ✅ `src/app/dashboard-pannes/dashboard-pannes.component.ts` (310 lignes)
- ✅ `src/app/dashboard-pannes/dashboard-pannes.component.html` (399 lignes)
- ✅ `src/app/dashboard-pannes/dashboard-pannes.component.css` (56 lignes)

### Fichiers Modifiés
- ✅ `src/app/layouts/admin-layout/admin-layout.module.ts` (Ajout de 3 composants + modules Material)
- ✅ `src/app/layouts/admin-layout/admin-layout.routing.ts` (Ajout de 3 routes)
- ✅ `src/app/dashboard/dashboard.component.html` (Ajout de navigation vers dashboards spécialisés)
- ✅ `src/app/dashboard/dashboard.component.css` (Ajout de styles pour cartes de navigation)
- ✅ `package.json` (Ajout de Chart.js 4.4.0)

---

## 📦 Étape 1 : Installation des Dépendances

Ouvrez un terminal PowerShell dans le répertoire `front end` et exécutez :

```powershell
cd "c:\Users\nessim.ayadi\Desktop\mangement-tpe\front end"
npm install
```

Cela installera Chart.js et toutes les autres dépendances nécessaires.

---

## 🔧 Étape 2 : Vérification du Backend

Assurez-vous que votre backend Spring Boot est en cours d'exécution :

```powershell
cd "c:\Users\nessim.ayadi\Desktop\mangement-tpe\TPE"
.\start.bat
```

Le backend doit être accessible sur **http://localhost:8080**

---

## 🎨 Étape 3 : Démarrage de l'Application Angular

Retournez dans le répertoire `front end` et démarrez le serveur de développement :

```powershell
cd "c:\Users\nessim.ayadi\Desktop\mangement-tpe\front end"
npm start
```

Ou :

```powershell
ng serve
```

L'application sera disponible sur **http://localhost:4200**

---

## 🌐 Étape 4 : Accès aux Nouveaux Dashboards

Une fois connecté à l'application, accédez aux dashboards via :

### Option A : Navigation depuis le Dashboard Principal
1. Accédez à **http://localhost:4200/dashboard**
2. Cliquez sur l'une des 3 cartes colorées :
   - 🖥️ **Gestion TPE** → Analyse du parc de TPE
   - 📋 **Demandes d'Affectation** → Suivi du workflow
   - 🔧 **Pannes & Maintenance** → Analyse technique

### Option B : Accès Direct
- **Dashboard TPE** : http://localhost:4200/dashboard/tpe
- **Dashboard Demandes** : http://localhost:4200/dashboard/demandes
- **Dashboard Pannes** : http://localhost:4200/dashboard/pannes

---

## 📊 Fonctionnalités par Dashboard

### 1️⃣ Dashboard Gestion TPE
**KPIs :**
- Total TPE dans le parc
- Taux de disponibilité (%)
- Taux d'affectation (%)
- Taux de panne (%)

**Graphiques :**
- 📊 Donut Chart : Répartition par statut (Disponible, Affecté, En Panne, etc.)
- 📊 Bar Chart : TPE par marque (Ingenico, Verifone, PAX, etc.)
- 📊 Line Chart : Évolution sur 6 mois
- 📊 Stacked Bar : Distribution par agence (17 agences Bank ABC)

**Filtres :**
- Par statut (DISPONIBLE, AFFECTÉ, EN_PANNE, MAINTENANCE, HORS_SERVICE)
- Par marque (Ingenico, Verifone, PAX, Autres)
- Par agence (Lac 2, Centre Ville, Sousse, Sfax, etc.)

---

### 2️⃣ Dashboard Demandes d'Affectation
**KPIs :**
- Demandes nouvelles
- Demandes en cours
- Délai moyen (jours)
- Demandes en retard (>3 jours)
- Taux de conversion (%)
- Demandes clôturées ce mois
- SLA Respect (%)
- Productivité (demandes/jour/agent)

**Graphiques :**
- 📊 Funnel Chart : Pipeline de conversion (NOUVELLE → CLÔTURÉE)
- 📊 Bar Chart : Demandes par statut
- 📊 Line Chart : Évolution mensuelle
- 📊 Horizontal Bar : Délai par étape

**Alertes :**
- 🔴 Demandes dépassant le SLA de 48h
- 🔴 Agences avec faible performance

---

### 3️⃣ Dashboard Pannes & Maintenance
**KPIs :**
- Pannes en cours
- Pannes résolues ce mois
- MTTR (Mean Time To Repair) en heures
- Taux de panne (%)
- TPE en maintenance
- Taux de résolution (%)
- Maintenance préventive à planifier
- En attente de pièces

**Graphiques :**
- 📊 Multi-Line Chart : Évolution par type (Matériel, Logiciel, Réseau)
- 📊 Donut Chart : Répartition par type de panne
- 📊 Pareto Chart : Top 10 TPE problématiques (80/20)
- 📊 Heatmap : Pannes par jour et heure

**Fonctionnalités Avancées :**
- 🎯 Analyse de Pareto : Identifier les 20% de TPE causant 80% des pannes
- 🗓️ Heatmap temporelle : Identifier les périodes à risque (ex: Vendredi 12h-18h)
- 🛠️ Maintenance préventive : Liste des TPE nécessitant une intervention
- ⏱️ MTTR tracking : Suivi du temps moyen de réparation

---

## 🎨 Personnalisation

### Modifier les Couleurs
Éditez les fichiers CSS des composants :

```css
/* dashboard-tpe.component.css */
.status-disponible { background: #28a745; } /* Vert */
.status-affecte { background: #007bff; }    /* Bleu */
.status-panne { background: #dc3545; }      /* Rouge */
```

### Modifier les KPIs
Éditez les fichiers TypeScript :

```typescript
// dashboard-tpe.component.ts - Ligne 60
get tauxDisponibilite(): number {
  return this.stats ? (this.stats.tpeDisponibles / this.stats.totalTPE * 100) : 0;
}
```

### Ajouter des Graphiques
Utilisez les méthodes Chart.js :

```typescript
createMonGraphique(): void {
  const ctx = document.getElementById('monCanvasId') as HTMLCanvasElement;
  new Chart(ctx, {
    type: 'bar', // ou 'line', 'doughnut', 'pie', etc.
    data: { /* vos données */ }
  });
}
```

---

## 🔒 Permissions et Sécurité

### Configuration des Permissions
Les dashboards nécessitent des permissions spécifiques. Ajoutez-les dans votre base de données :

```sql
-- Permissions pour les nouveaux dashboards
INSERT INTO permissions (code, libelle) VALUES 
  ('DASHBOARD_TPE', 'Accès Dashboard Gestion TPE'),
  ('DASHBOARD_DEMANDES', 'Accès Dashboard Demandes'),
  ('DASHBOARD_PANNES', 'Accès Dashboard Pannes');

-- Attribution au rôle MONETIQUE
INSERT INTO role_permissions (role_id, permission_code) 
SELECT id, 'DASHBOARD_TPE' FROM roles WHERE code = 'MONETIQUE';
-- Répétez pour DASHBOARD_DEMANDES et DASHBOARD_PANNES
```

### Rôles par Défaut
- **ADMIN** : Accès à tous les dashboards
- **MONETIQUE** : Accès TPE + Demandes + Pannes
- **AGENCE** : Accès TPE + Demandes (lecture seule)
- **COMMERCANT** : Pas d'accès aux dashboards analytiques

---

## 🐛 Dépannage Courant

### Erreur : "Cannot find module 'chart.js'"
**Solution :**
```powershell
npm install chart.js --save
```

### Les Graphiques ne S'affichent pas
**Solution :** Vérifiez la console du navigateur (F12). Assurez-vous que :
1. Chart.js est bien installé
2. Les éléments `<canvas>` existent dans le DOM
3. Le backend retourne des données

**Correctif :**
Ajoutez un délai dans le `ngAfterViewInit()` :
```typescript
ngAfterViewInit(): void {
  setTimeout(() => {
    this.createStatutChart();
    this.createMarqueChart();
  }, 100);
}
```

### Erreur 401 Unauthorized
**Solution :** Vérifiez que :
1. Vous êtes connecté
2. Votre token JWT est valide
3. Votre rôle possède les permissions nécessaires

### Backend ne Répond pas
**Solution :**
```powershell
# Vérifier que le backend tourne
cd TPE
.\start.bat

# Vérifier l'URL de l'API
# Doit être http://localhost:8080
```

### Erreur de Compilation Angular
**Solution :**
```powershell
# Nettoyer et réinstaller
rm -rf node_modules
rm package-lock.json
npm install
ng serve
```

---

## 📱 Test sur Mobile

Les dashboards sont **100% responsive**. Testez sur différentes tailles d'écran :

### Chrome DevTools
1. Ouvrez la console (F12)
2. Cliquez sur l'icône de device toolbar (Ctrl+Shift+M)
3. Sélectionnez un appareil :
   - iPhone 12 Pro
   - iPad
   - Galaxy S21

### Breakpoints
- **Desktop** : >1200px → 4 colonnes
- **Tablette** : 768-1199px → 2 colonnes
- **Mobile** : <768px → 1 colonne

---

## 📈 Prochaines Étapes

Une fois les dashboards testés et validés, vous pouvez :

### 1. Ajouter des Exports
Implémentez l'export PDF/Excel :
```typescript
exportPDF(): void {
  // Utiliser jsPDF ou similaire
}
```

### 2. Intégrer Power BI
Ajoutez des rapports Power BI embarqués :
```typescript
import { PowerBIReportEmbedComponent } from 'powerbi-client-angular';
```

### 3. Créer de Nouveaux Dashboards
Suivez la même structure pour :
- Dashboard Commerçants
- Dashboard Performance
- Dashboard Financier

### 4. Ajouter des Alertes Temps Réel
Implémentez WebSocket pour notifications en temps réel :
```typescript
import { WebSocketService } from './services/websocket.service';
```

---

## ✅ Checklist de Validation

Avant de déployer en production, vérifiez :

- [ ] `npm install` exécuté sans erreur
- [ ] Backend Spring Boot en cours d'exécution (port 8080)
- [ ] Frontend Angular démarre sans erreur (port 4200)
- [ ] Connexion à l'application réussie
- [ ] Navigation vers `/dashboard` fonctionne
- [ ] Navigation vers `/dashboard/tpe` fonctionne
- [ ] Navigation vers `/dashboard/demandes` fonctionne
- [ ] Navigation vers `/dashboard/pannes` fonctionne
- [ ] Les 3 cartes de navigation sont affichées
- [ ] Les graphiques Chart.js s'affichent correctement
- [ ] Les filtres fonctionnent (statut, marque, agence)
- [ ] Les KPIs affichent les bonnes valeurs
- [ ] Les alertes sont visibles (demandes en retard, etc.)
- [ ] Responsive testé sur mobile/tablette
- [ ] Pas d'erreurs dans la console (F12)
- [ ] Performance acceptable (chargement < 3s)

---

## 📚 Documentation Complète

Pour plus de détails, consultez :

- **Guide d'utilisation** : `GUIDE-DASHBOARDS.md`
- **API Backend** : `TPE/API-ENDPOINTS.md`
- **Architecture** : `front end/ARCHITECTURE.md`
- **Power BI** : `GUIDE-POWER-BI.md`

---

## 🆘 Support

En cas de problème :

1. Vérifiez les logs du backend : `TPE/logs/`
2. Vérifiez la console du navigateur (F12)
3. Consultez les fichiers README
4. Contactez l'équipe DSI Bank ABC

---

## 🎉 C'est Prêt !

Vos 3 nouveaux dashboards professionnels sont maintenant installés et configurés. 

**Lancez `npm install` puis `npm start` pour commencer !**

---

**Date** : Février 2026  
**Version** : 1.0  
**Bank ABC Tunisie** - Direction des Systèmes d'Information
