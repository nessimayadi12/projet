# Quick Start - Génération PDF avec Angular & Java

## 🚀 Démarrage Rapide (3 minutes)

### Option 1: Script Automatique
```powershell
.\start-pdf-integration.ps1
```

### Option 2: Démarrage Manuel

**Terminal 1 - Backend:**
```powershell
cd TPE
mvn clean compile
mvn spring-boot:run
```

**Terminal 2 - Frontend:**
```powershell
cd "front end"
npm install
npm start
```

## 📱 Utilisation

1. Ouvrir: http://localhost:4200
2. Menu → **Upload Fichier Bancaire**
3. Sélectionner fichier `.txt`
4. Cliquer **"Traiter le Fichier"**
5. Cliquer **"Export PDF"** ✅

## 🧪 Test Direct

```powershell
# Tester l'API PDF
.\test-pdf-generation.ps1

# Ou manuellement:
curl http://localhost:8080/api/fichier-bancaire/rapport/pdf/20260224 --output rapport.pdf
```

## 📋 Endpoints Clés

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/api/fichier-bancaire/upload` | POST | Upload fichier bancaire |
| `/api/fichier-bancaire/rapport/pdf/{date}` | GET | Générer PDF |
| `/api/fichier-bancaire/rapport/text/{date}` | GET | Générer TXT |
| `/api/fichier-bancaire/stats/{date}` | GET | Statistiques |

## ⚙️ Configuration

**Backend:** `TPE/src/main/resources/application.properties`
- Port: 8080
- Base de données SQL Server

**Frontend:** `front end/src/environments/environment.ts`
```typescript
apiUrl: 'http://localhost:8080/api'
```

## 🐛 Dépannage Rapide

### Backend ne démarre pas
```powershell
cd TPE
mvn clean install -U
```

### Frontend ne démarre pas
```powershell
cd "front end"
rm -r node_modules
npm install
```

### PDF vide
- Vérifier que des données existent pour la sessionDate
- URL test: http://localhost:8080/api/fichier-bancaire/stats/20260224

### CORS error
Vérifier `@CrossOrigin(origins = "*")` dans le contrôleur

## 📚 Documentation Complète

- **Guide complet**: [GUIDE-PDF-GENERATION.md](GUIDE-PDF-GENERATION.md)
- **Architecture**: [BACKEND-FRONTEND-INTEGRATION.md](BACKEND-FRONTEND-INTEGRATION.md)
- **API Tests**: [test-pdf-generation.ps1](test-pdf-generation.ps1)

## ✅ Checklist

- [ ] Java 17+ installé
- [ ] Maven installé
- [ ] Node.js installé
- [ ] SQL Server accessible
- [ ] Backend démarre sur port 8080
- [ ] Frontend démarre sur port 4200
- [ ] Fichier test uploadé
- [ ] PDF téléchargé avec succès

## 💡 Exemples de Fichiers Test

- `test_cpabc049_sample.txt` - Fichier bancaire exemple
- `test/fichier-test-bancaire.txt` - Autre exemple

Format attendu:
```
Type = "10" ou "20"
Longueur ≥ 250 caractères
Encodage UTF-8
```

## 🎯 Résultat Attendu

Le PDF généré contient:
- ✅ En-tête avec date et titre
- ✅ Statistiques (nombre d'écritures, débits, crédits)
- ✅ Tableau détaillé des transactions
- ✅ Totaux colorés (rouge=débits, vert=crédits)
- ✅ Pied de page

Format: **A4 Paysage** avec mise en forme professionnelle

---

**Version**: 1.0  
**Date**: 25/02/2026  
**Support**: Voir GUIDE-PDF-GENERATION.md pour détails complets
