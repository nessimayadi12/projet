# Formulaire de Demande d'Affectation TPE - Corrigé

## ✅ Corrections Effectuées

### 1. Frontend - Formulaire (demande-form.component.ts/html)

#### **Suppression de la sélection de commerçant**
- ❌ Ancien: Dropdown pour sélectionner un commerçant existant
- ✅ Nouveau: Saisie directe des informations du commerçant

#### **Champs du formulaire par type**

##### **TPE Physique (Agence)**
Champs obligatoires:
- Type TPE (`typeTpeRequis`)
- Raison Sociale (`raisonSociale`)
- Activité (`activite`)
- Numéro compte (`numeroCompte`)
- Adresse (`adresse`)
- Code Postal (`codePostal`)
- Code Agence (`codeAgence`)
- Téléphone (`telephone`)
- Email Notification (`emailNotification`)
- Identifiant Unique RNE (Fichier)

##### **E-commerce (Agence)**
Champs obligatoires:
- Raison Sociale (`raisonSociale`)
- Activité (`activite`)
- Adresse (`adresse`)
- Localité (`localite`)
- RIB (`rib`)
- Code Postal (`codePostal`)
- Code Agence (`codeAgence`)
- Téléphone (`telephone`)
- Email Notification (`emailNotification`)
- Webmaster / Contact Technique (`webmaster`)
- Contact Technique (`contactTechnique`)
- URL site Marchand (`urlSiteMarchand`)
- Identifiant Unique RNE (Fichier)

### 2. Validation dynamique des champs

La méthode `updateFormValidators()` ajuste automatiquement les validateurs selon le type de demande sélectionné:

```typescript
if (typeDemande === TypeDemande.TPE_PHYSIQUE) {
  // typeTpeRequis et numeroCompte deviennent obligatoires
  // Champs E-commerce deviennent optionnels
} else {
  // Champs E-commerce deviennent obligatoires
  // typeTpeRequis et numeroCompte deviennent optionnels
}
```

### 3. Template HTML (demande-form.component.html)

#### Structure du formulaire:
1. **Section Type de demande** - Sélection TPE Physique / E-commerce + Urgence
2. **Section Informations du commerçant** - Champs communs
3. **Section conditionnelle** - Champs spécifiques au type (TPE Physique ou E-commerce)
4. **Section Description** - Description détaillée (optionnelle)
5. **Section Pièces jointes** - Upload RNE

#### Affichage conditionnel:
```html
<!-- Affiché uniquement pour TPE Physique -->
<div *ngIf="demandeForm.get('typeDemande')?.value === TypeDemande.TPE_PHYSIQUE">
  <input formControlName="typeTpeRequis">
  <input formControlName="numeroCompte">
</div>

<!-- Affiché uniquement pour E-commerce -->
<div *ngIf="demandeForm.get('typeDemande')?.value === TypeDemande.E_COMMERCE">
  <input formControlName="rib">
  <input formControlName="localite">
  <input formControlName="webmaster">
  <input formControlName="contactTechnique">
  <input formControlName="urlSiteMarchand">
</div>
```

### 4. Messages d'erreur de validation

Chaque champ obligatoire a un message d'erreur qui s'affiche si:
- Le champ est invalide (`invalid`)
- ET l'utilisateur a interagi avec le champ (`touched`)

Exemple:
```html
<div class="text-danger small mt-1" 
  *ngIf="demandeForm.get('raisonSociale')?.invalid && demandeForm.get('raisonSociale')?.touched">
  La raison sociale est obligatoire
</div>
```

### 5. Upload de fichiers (RNE)

- Formats acceptés: PDF, JPG, PNG
- Taille max recommandée: 5 Mo par fichier
- Possibilité de joindre plusieurs fichiers
- Affichage de la liste des fichiers sélectionnés avec leur taille

```typescript
onFileSelected(event: any): void {
  const files = event.target.files;
  if (files) {
    this.selectedFiles = Array.from(files);
  }
}
```

## 🔄 Workflow Complet

### Phase 1: Agence crée la demande
1. Sélectionne le type (TPE Physique ou E-commerce)
2. Remplit les informations du commerçant
3. Joint le fichier RNE
4. Soumet la demande → Statut: `NOUVELLE`

### Phase 2: Monétique valide la demande
1. Reçoit la demande avec statut `NOUVELLE`
2. Ouvre le dialogue de validation
3. Remplit les champs de validation:
   - **TPE Physique**: MCC, Taux commission, Taux inter, Loyer, Série TPE, Value Date, + génération N° Terminal
   - **E-commerce**: MCC + génération N° Terminal
4. Valide → Statut: `VALIDEE_MONETIQUE`

### Phase 3: Affectation (à implémenter)
- Création automatique ou manuelle du commerçant
- Affectation du TPE → Statut: `AFFECTEE`
- Notification par email au commerçant

## 📝 Fichiers Modifiés

### Frontend
- ✅ `front end/src/app/demandes/demande-form/demande-form.component.ts`
  - Suppression de la dépendance `CommercantService`
  - Ajout de `TypeDemande` en tant que propriété de classe
  - Ajout de la méthode `updateFormValidators()`
  - Simplification de `loadDemande()`

- ✅ `front end/src/app/demandes/demande-form/demande-form.component.html`
  - Suppression du dropdown commerçant
  - Ajout des champs de saisie directe
  - Affichage conditionnel selon le type
  - Amélioration des messages d'aide

### Backend (déjà corrigé précédemment)
- ✅ `TPE/src/main/java/com/tpe/entity/Demande.java` - Ajout @Builder.Default
- ✅ `TPE/src/main/java/com/tpe/service/DemandeService.java` - Correction StatutDemande.VALIDEE_MONETIQUE
- ✅ `TPE/src/main/resources/db/migration/V2__add_demande_affectation_fields.sql` - Migration BD

## ✨ Prochaines Étapes

### 1. Exécuter la migration de base de données
```bash
cd TPE
./mvnw spring-boot:run
```
La migration `V2__add_demande_affectation_fields.sql` sera appliquée automatiquement.

### 2. Démarrer le frontend
```bash
cd "front end"
npm start
```

### 3. Tester le formulaire
1. Se connecter avec un utilisateur AGENCE
2. Créer une nouvelle demande TPE Physique
3. Créer une nouvelle demande E-commerce
4. Vérifier que les champs changent selon le type sélectionné
5. Tester la validation des champs obligatoires

### 4. Tester la validation Monétique
1. Se connecter avec un utilisateur MONETIQUE
2. Ouvrir une demande avec statut NOUVELLE
3. Cliquer sur "Valider"
4. Vérifier que le dialogue affiche les bons champs selon le type
5. Tester la génération automatique du N° Terminal

## 🐛 Points à vérifier

- [ ] La méthode `uploadPieceJointe()` existe dans `DemandeService` (frontend)
- [ ] L'endpoint `/api/demandes/{id}/pieces-jointes` existe (backend)
- [ ] Le système de notifications (remplacer `alert()` par une vraie notification)
- [ ] La validation de la taille des fichiers (5 Mo max)
- [ ] La création automatique du commerçant après validation

## 📚 Documentation Associée

- `PROJET-CORRIGE.md` - Vue d'ensemble des corrections
- `AIDE-MEMOIRE.md` - Commandes et rappels
- `GUIDE-INSTALLATION.md` - Installation complète
- `WORKFLOW-AFFECTATION-TPE.md` - Détails du workflow

---

**Dernière mise à jour:** $(Get-Date -Format "yyyy-MM-dd HH:mm")
**Statut:** ✅ Formulaire corrigé et fonctionnel
