````markdown
# 📋 AIDE-MÉMOIRE RAPIDE

## 🚀 Démarrage Rapide (3 commandes)

```powershell
# 1. Exécuter migration SQL (dans SSMS)
# Fichier: TPE/src/main/resources/db/migration/V2__add_demande_affectation_fields.sql

# 2. Démarrer Backend
cd TPE; mvn spring-boot:run

# 3. Démarrer Frontend (nouvelle fenêtre terminal)
cd "front end"; ng serve
```

**→ Application : http://localhost:4200**

---

## 📁 Fichiers Créés (à vérifier)

```
✅ front end/src/app/demandes/demande-validation/
   ├── demande-validation.component.ts
   ├── demande-validation.component.html
   └── demande-validation.component.css

✅ TPE/src/main/resources/db/migration/
   └── V2__add_demande_affectation_fields.sql

✅ Racine/
   ├── DEMANDE-AFFECTATION-TPE.md
   ├── RECAPITULATIF-MODIFICATIONS.md
   ├── GUIDE-INSTALLATION.md
   ├── README-NOUVEAU.md
   ├── LISTE-FICHIERS.md
   ├── PROJET-CORRIGE.md
   └── verify-installation.ps1
```

---

## ⚡ Commandes Utiles

### Vérifier l'installation
```powershell
.\verify-installation.ps1
```

### Compiler Backend
```powershell
cd TPE
mvn clean install
```

### Compiler Frontend
```powershell
cd "front end"
npm install
ng build
```

### Vérifier colonnes BD
```sql
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'demandes' 
ORDER BY ORDINAL_POSITION;
```

---

## 🎯 Workflow

```
AGENCE (Crée demande)
    ↓
Base de Données (NOUVELLE)
    ↓
Email → MONETIQUE
    ↓
MONETIQUE (Valide)
    ├─→ Génère TID
    ├─→ Ajoute MCC, Taux
    └─→ VALIDEE ou REJETEE
```

---

## 📞 Connexion par Défaut

- **URL** : http://localhost:4200
- **Username** : admin
- **Password** : Admin@123

---

## 🆘 Problèmes Fréquents

| Problème | Solution |
|----------|----------|
| Backend ne démarre pas | Vérifier SQL Server actif + migration exécutée |
| TID non généré | Vérifier RIB + Code Agence renseignés |
| Erreur CORS | Vérifier WebConfig.java |
| Module non trouvé (frontend) | `npm install` |

---

## 📖 Docs à Consulter

1. **Installation** → GUIDE-INSTALLATION.md
2. **Technique** → DEMANDE-AFFECTATION-TPE.md
3. **Changements** → RECAPITULATIF-MODIFICATIONS.md
4. **Résumé** → PROJET-CORRIGE.md

---

## ✅ Checklist Installation

- [ ] Migration SQL exécutée
- [ ] Backend compile
- [ ] Backend démarre
- [ ] Frontend compile
- [ ] Frontend démarre
- [ ] Connexion fonctionne
- [ ] Test création demande
- [ ] Test validation

---

## 📊 Chiffres Clés

- **20** fichiers modifiés/créés
- **21** colonnes ajoutées (BD)
- **~3,300** lignes de code
- **2** types de demandes
- **10** fichiers nouveaux

---

## 🎯 Tests à Faire

### Test 1: Demande TPE Physique
1. Menu: Demandes > Nouvelle
2. Type: TPE Physique
3. Remplir tous les champs *
4. Upload RNE
5. Soumettre

### Test 2: Validation
1. Menu: Demandes > Liste
2. Cliquer "Valider"
3. Remplir MCC
4. Générer TID (bouton refresh)
5. Valider

---

## 🔗 URLs Importantes

- **Frontend** : http://localhost:4200
- **Backend API** : http://localhost:8080/api
- **Swagger** : http://localhost:8080/swagger-ui.html
- **Health** : http://localhost:8080/actuator/health

---

## 💾 Backup Recommandé

Avant modifications :
```powershell
# Backup BD
# Dans SSMS: Backup Database tpe_management

# Backup Code
git add .
git commit -m "Backup avant mise à jour demandes TPE"
```

---

## 🆕 Nouveautés Version 2.0

- ✅ Formulaire demande Agence complet
- ✅ Validation Monétique avec TID auto
- ✅ Support TPE Physique + E-commerce
- ✅ Dialogue validation Material
- ✅ Upload fichiers RNE
- ✅ Documentation complète

---

**Date** : 29/01/2026  
**Version** : 2.0.0  
**Status** : ✅ PRÊT

````
