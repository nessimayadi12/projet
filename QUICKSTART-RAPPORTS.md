# 🚀 GUIDE RAPIDE - Génération de Rapports TPE

## ✅ Installation Terminée

Tous les fichiers ont été créés et configurés :
- ✅ jsPDF et jspdf-autotable installés
- ✅ Composant file-upload mis à jour
- ✅ Génération de rapports PDF/TXT ajoutée
- ✅ Statistiques visuelles ajoutées

---

## 📝 Comment Utiliser

### 1. Redémarrer le Serveur Angular

Si le serveur est déjà en cours d'exécution, **arrêtez-le** (Ctrl+C) puis relancez :

```powershell
cd "c:\Users\nessim.ayadi\Desktop\mangement-tpe\front end"
npm start
```

### 2. Accéder à la Page d'Upload

Une fois le serveur démarré, accédez à :
```
http://localhost:4200/file-upload
```

Ou via le menu : **Upload Transactions** (dans le menu latéral)

### 3. Téléverser un Fichier

1. Cliquez sur la zone de dépôt ou glissez un fichier `.txt` ou `.dat`
2. Cliquez sur **"Traiter le fichier"**
3. Attendez le traitement (barre de progression)

### 4. Consulter les Résultats

Après le traitement, vous verrez :

```
┌─────────────────────────────────────────────────────────────┐
│  ✅ Fichier traité avec succès                              │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ Total    │  │ Traitées │  │ Erreurs  │  │ Réussite │   │
│  │  1250    │  │  1198    │  │    52    │  │  95.8%   │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│                                                              │
│  📥 Télécharger le rapport de traitement :                  │
│  [📄 Rapport PDF]  [📝 Rapport TXT]                        │
└─────────────────────────────────────────────────────────────┘
```

### 5. Télécharger le Rapport

Cliquez sur :
- **📄 Rapport PDF** : Document professionnel avec tableaux et mise en page
- **📝 Rapport TXT** : Fichier texte structuré pour traitement batch

---

## 📊 Contenu des Rapports

### Rapport PDF Inclut :
- ✅ En-tête Bank ABC avec logo et date
- ✅ Résumé du traitement (tableau)
- ✅ Liste des transactions (jusqu'à 50)
- ✅ Erreurs rencontrées (jusqu'à 30)
- ✅ Numérotation des pages
- ✅ Pied de page confidentiel

### Rapport TXT Inclut :
- ✅ Format tabulaire ASCII
- ✅ Colonnes alignées
- ✅ Bordures décoratives
- ✅ Compatible impression
- ✅ Prêt pour archivage

---

## 🎨 Exemple de Fichier de Test

Si vous n'avez pas de fichier réel, créez `test_transactions.txt` :

```txt
1020240220TPE-001-123456COM-2024-00145000000012550TND140523PARIS
1020240220TPE-002-789012COM-2024-00278000000045000TND140523TUNIS
2020240220TPE-001-123456COM-2024-00145000000007525TND140523PARIS
1020240220TPE-003-456789COM-2024-00389000000098765TND140523SFAX
```

**Format :**
- Type (2) : `10` = Achat, `20` = Remboursement
- Date (8) : `20240220` = 20/02/2024
- Terminal (15) : `TPE-001-123456`
- Commerçant (15) : `COM-2024-00145`
- Montant (12) : `000000012550` = 125.50
- Devise (3) : `TND`
- Code autorisation (6) : `140523`
- Ville (20) : `PARIS`

---

## 🔧 Personnalisation Rapide

### Changer les Couleurs PDF
Éditez `file-upload.component.ts` ligne ~180 :

```typescript
headStyles: { 
  fillColor: [0, 51, 102],    // [R, G, B] - Bleu Bank ABC
  textColor: 255              // Blanc
}
```

### Changer la Largeur des Colonnes TXT
Éditez `file-upload.component.ts` ligne ~280 :

```typescript
this.padRight('Terminal', 20);   // Augmentez 20 pour plus large
this.padLeft('Montant', 15);      // Aligné à droite
```

---

## 📁 Emplacement des Fichiers

Les rapports sont téléchargés dans :
```
C:\Users\nessim.ayadi\Downloads\
```

**Noms de fichiers :**
- `Rapport_Transactions_1708437821234.pdf`
- `Rapport_Transactions_1708437821234.txt`

Le nombre à la fin est un timestamp (millisecondes).

---

## 🐛 Problèmes Courants

### Les boutons ne s'affichent pas
**Cause :** Le backend ne retourne pas les bonnes données

**Solution temporaire :** Modifiez `extractProcessingResult()` pour générer des données de test :

```typescript
extractProcessingResult(response: any): ProcessingResult {
  // Générer des données de test
  return {
    totalLines: 100,
    processedLines: 95,
    errorLines: 5,
    transactions: this.generateMockTransactions(95),
    errors: this.generateMockErrors(5)
  };
}
```

### Le PDF ne se télécharge pas
**Cause :** Bloqueur de pop-ups

**Solution :**
1. Autorisez les pop-ups pour `localhost:4200`
2. Vérifiez la console (F12) pour les erreurs

### Erreur "Cannot find module 'jspdf'"
**Solution :**
```powershell
npm install jspdf jspdf-autotable --save
```
Puis redémarrez le serveur.

---

## 📚 Documentation Complète

Pour plus de détails, consultez :
- **GUIDE-RAPPORTS-TRAITEMENT.md** : Documentation complète
- **GUIDE-DASHBOARDS.md** : Autres dashboards
- **TPE/API-ENDPOINTS.md** : API backend

---

## 🎉 C'est Prêt !

Votre système de génération de rapports est maintenant **opérationnel** !

**Prochaines étapes :**
1. Redémarrez le serveur Angular (`npm start`)
2. Accédez à `/file-upload`
3. Testez avec un fichier réel
4. Téléchargez votre premier rapport PDF 🎯

---

**Date** : 20 Février 2026  
**Version** : 1.0  
**Bank ABC Tunisie** - Direction des Systèmes d'Information
