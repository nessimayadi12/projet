import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { DemandeService } from '../../services/demande.service';
import { DemandeTPE, TypeDemande, Urgence, StatutDemande } from '../../models/demande-tpe.model';
import { AuthService } from '../../services/auth.service';
import { Role } from '../../models/utilisateur.model';
import { CommercantService } from '../../services/commercant.service';
import { Commercant } from '../../models/commercant.model';

@Component({
  selector: 'app-demande-form',
  templateUrl: './demande-form.component.html',
  styleUrls: ['./demande-form.component.css']
})
export class DemandeFormComponent implements OnInit {
  demandeForm: FormGroup;
  isEditMode = false;
  demandeId: number | null = null;
  loading = false;
  currentDemande: DemandeTPE | null = null;
  showReworkflowNotice = false;
  typesDemande = Object.values(TypeDemande);
  urgences = Object.values(Urgence);
  isAgence = false;
  isMonetique = false;
  selectedFiles: File[] = [];
  TypeDemande = TypeDemande; // Pour utilisation dans le template
  
  // Recherche commerçant
  searchCommercant: string = '';
  commercantsFound: Commercant[] = [];
  selectedCommercant: Commercant | null = null;
  showCommercantSearch = false;

  constructor(
    private fb: FormBuilder,
    private demandeService: DemandeService,
    private authService: AuthService,
    private commercantService: CommercantService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.demandeForm = this.fb.group({
      // Champs communs
      typeDemande: [TypeDemande.TPE, [Validators.required]],
      urgence: [Urgence.NORMALE, [Validators.required]],
      description: [''],
      
      // Champs communs aux deux types
      raisonSociale: ['', [Validators.required]],
      activite: ['', [Validators.required]],
      adresse: ['', [Validators.required]],
      codePostal: ['', [Validators.required]],
      codeAgence: ['', [Validators.required]],
      telephone: ['', [Validators.required]],
      
      // TPE uniquement
      typeTpeRequis: [''],
      numeroCompte: [''],

      // Donnees monetiques, modifiables en update par Monetique/Admin
      mcc: [''],
      tauxCommission: [null, [Validators.min(0), Validators.max(100)]],
      tauxCommissionInter: [null, [Validators.min(0), Validators.max(100)]],
      loyer: [null, [Validators.min(0)]],
      serieTpe: [''],
      numeroTerminal: [''],
      valueDate: [1, [Validators.min(1), Validators.max(2), Validators.pattern(/^[12]$/)]],
      
      // Mobile uniquement
      localite: [''],
      rib: [''],
      webmaster: [''],
      contactTechnique: [''],
      urlSiteMarchand: ['']
    });
  }

  ngOnInit(): void {
    // Vérifier le rôle
    const currentUser = this.authService.currentUserValue;
    if (currentUser) {
      this.isAgence = this.authService.hasAnyRole([Role.AGENCE]);
      this.isMonetique = this.authService.hasAnyRole([Role.MONETIQUE, Role.ADMIN]);
    }

    // Écouter les changements de type de demande pour adapter les validateurs
    this.demandeForm.get('typeDemande')?.valueChanges.subscribe(type => {
      this.updateFormValidators(type);
    });

    // Initialiser les validateurs pour le type par défaut
    this.updateFormValidators(TypeDemande.TPE);

    // Mode édition
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'new') {
      this.isEditMode = true;
      this.demandeId = +id;
      this.loadDemande(this.demandeId);
    }
  }

  onSearchCommercant(): void {
    if (!this.searchCommercant || this.searchCommercant.length < 2) {
      this.commercantsFound = [];
      return;
    }

    this.commercantService.searchCommercants(this.searchCommercant)
      .subscribe(
        (commercants) => {
          this.commercantsFound = commercants;
        },
        (error) => {
          console.error('Erreur lors de la recherche:', error);
          this.commercantsFound = [];
        }
      );
  }

  selectCommercant(commercant: Commercant): void {
    this.selectedCommercant = commercant;
    this.showCommercantSearch = false;
    const numeroCompte = commercant.numeroCompte || commercant.rib || '';
    
    // Pré-remplir le formulaire avec les données du commerçant
    this.demandeForm.patchValue({
      raisonSociale: commercant.raisonSociale,
      activite: commercant.activite || '',
      numeroCompte,
      rib: numeroCompte,
      adresse: commercant.adresse,
      localite: commercant.localite || commercant.ville || '',
      codePostal: commercant.codePostal,
      codeAgence: commercant.codeAgence || '',
      telephone: commercant.telephone
    });
    
    this.searchCommercant = commercant.raisonSociale;
    this.commercantsFound = [];
  }

  clearCommercantSelection(): void {
    this.selectedCommercant = null;
    this.searchCommercant = '';
    this.commercantsFound = [];
    
    // Réinitialiser les champs commerçant
    this.demandeForm.patchValue({
      raisonSociale: '',
      activite: '',
      numeroCompte: '',
      rib: '',
      adresse: '',
      localite: '',
      codePostal: '',
      codeAgence: '',
      telephone: ''
    });
  }

  toggleCommercantSearch(): void {
    this.showCommercantSearch = !this.showCommercantSearch;
    if (this.showCommercantSearch) {
      this.clearCommercantSelection();
    }
  }

  updateFormValidators(typeDemande: TypeDemande): void {
    const requireTypeSpecificFields = !this.isEditMode;

    if (typeDemande === TypeDemande.TPE) {
      // TPE: numeroCompte et typeTpeRequis requis
      this.demandeForm.get('typeTpeRequis')?.setValidators(requireTypeSpecificFields ? [Validators.required] : []);
      this.demandeForm.get('numeroCompte')?.setValidators(requireTypeSpecificFields ? [Validators.required] : []);
      
      // Mobile: champs optionnels
      this.demandeForm.get('localite')?.clearValidators();
      this.demandeForm.get('rib')?.clearValidators();
      this.demandeForm.get('webmaster')?.clearValidators();
      this.demandeForm.get('contactTechnique')?.clearValidators();
      this.demandeForm.get('urlSiteMarchand')?.clearValidators();
    } else {
      // Mobile: champs requis
      this.demandeForm.get('localite')?.setValidators(requireTypeSpecificFields ? [Validators.required] : []);
      this.demandeForm.get('rib')?.setValidators(requireTypeSpecificFields ? [Validators.required] : []);
      this.demandeForm.get('webmaster')?.setValidators(requireTypeSpecificFields ? [Validators.required] : []);
      this.demandeForm.get('contactTechnique')?.setValidators(requireTypeSpecificFields ? [Validators.required] : []);
      this.demandeForm.get('urlSiteMarchand')?.setValidators(
        requireTypeSpecificFields
          ? [Validators.required, Validators.pattern('https?://.+')]
          : [Validators.pattern('https?://.+')]
      );
      
      // TPE: champs optionnels
      this.demandeForm.get('typeTpeRequis')?.clearValidators();
      this.demandeForm.get('numeroCompte')?.clearValidators();
    }
    
    // Mettre à jour la validité de tous les champs modifiés
    ['typeTpeRequis', 'numeroCompte', 'localite', 'rib', 'webmaster', 'contactTechnique', 'urlSiteMarchand'].forEach(field => {
      this.demandeForm.get(field)?.updateValueAndValidity();
    });
  }

  loadDemande(id: number): void {
    this.loading = true;
    this.demandeService.getDemandeById(id).subscribe({
      next: (demande) => {
        this.currentDemande = demande;
        this.showReworkflowNotice = this.isEditMode && demande.statut === StatutDemande.AFFECTEE;
        this.demandeForm.patchValue({
          ...demande,
          valueDate: this.normalizeValueDate(demande.valueDate)
        });
        this.updateFormValidators(demande.typeDemande);
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement demande:', err);
        this.showNotification('Impossible de charger la demande', 'danger');
        this.router.navigate(['/demandes']);
      }
    });
  }

  onFileSelected(event: any): void {
    const files: FileList = event.target.files;
    if (!files || files.length === 0) {
      return;
    }

    const maxSize = 5 * 1024 * 1024; // 5 MB
    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'];
    const validFiles: File[] = [];
    const errors: string[] = [];

    Array.from(files).forEach(file => {
      // Vérifier le type
      if (!allowedTypes.includes(file.type)) {
        errors.push(`${file.name}: Type de fichier non autorisé. Utilisez PDF, JPG ou PNG.`);
        return;
      }

      // Vérifier la taille
      if (file.size > maxSize) {
        errors.push(`${file.name}: Fichier trop volumineux (max 5 MB). Taille: ${(file.size / 1024 / 1024).toFixed(2)} MB`);
        return;
      }

      validFiles.push(file);
    });

    if (errors.length > 0) {
      this.showNotification(
        'Erreurs de validation:\n' + errors.join('\n'),
        'warning'
      );
    }

    if (validFiles.length > 0) {
      this.selectedFiles = [...this.selectedFiles, ...validFiles];
      this.showNotification(
        `${validFiles.length} fichier(s) ajouté(s) avec succès`,
        'success'
      );
    }

    // Réinitialiser l'input pour permettre la sélection du même fichier
    event.target.value = '';
  }

  removeFile(index: number): void {
    this.selectedFiles.splice(index, 1);
  }

  onSubmit(): void {
    if (this.demandeForm.invalid) {
      Object.keys(this.demandeForm.controls).forEach(key => {
        this.demandeForm.get(key)?.markAsTouched();
      });
      this.showNotification('Veuillez remplir tous les champs obligatoires', 'warning');
      return;
    }

    this.loading = true;
    const formValue = this.demandeForm.getRawValue();
    const demandeData: DemandeTPE = {
      ...formValue,
      tauxCommission: this.toNullableNumber(formValue.tauxCommission),
      tauxCommissionInter: this.toNullableNumber(formValue.tauxCommissionInter),
      loyer: this.toNullableNumber(formValue.loyer),
      valueDate: this.normalizeValueDate(formValue.valueDate),
      statut: this.currentDemande?.statut || StatutDemande.NOUVELLE
    };

    const request$ = this.isEditMode && this.demandeId
      ? this.demandeService.updateDemande(this.demandeId, demandeData)
      : this.demandeService.createDemande(demandeData);

    request$.subscribe({
      next: (demande) => {
        // Upload des pièces jointes si présentes
        if (this.selectedFiles.length > 0 && demande.id) {
          this.uploadPiecesJointes(demande.id);
        } else {
          this.showNotification(
            `Demande ${this.isEditMode ? 'mise à jour' : 'créée'} avec succès`,
            'success'
          );
          this.router.navigate(['/demandes']);
        }
      },
      error: (err) => {
        console.error('Erreur:', err);
        const message = err.error?.message || 'Erreur lors de l\'enregistrement';
        this.showNotification(message, 'danger');
        this.loading = false;
      }
    });
  }

  uploadPiecesJointes(demandeId: number): void {
    if (this.selectedFiles.length === 0) {
      this.showNotification(
        `Demande ${this.isEditMode ? 'mise à jour' : 'créée'} avec succès`,
        'success'
      );
      this.router.navigate(['/demandes']);
      return;
    }

    let uploadedCount = 0;
    let errorCount = 0;
    const totalFiles = this.selectedFiles.length;

    this.selectedFiles.forEach((file, index) => {
      this.demandeService.uploadPieceJointe(demandeId, file).subscribe({
        next: () => {
          uploadedCount++;
          console.log(`Fichier ${index + 1}/${totalFiles} uploadé: ${file.name}`);
          
          if (uploadedCount + errorCount === totalFiles) {
            this.handleUploadComplete(uploadedCount, errorCount, totalFiles);
          }
        },
        error: (err) => {
          errorCount++;
          console.error(`Erreur upload fichier ${file.name}:`, err);
          console.error('Détails erreur:', {
            status: err.status,
            statusText: err.statusText,
            message: err.error?.message || err.message,
            url: err.url
          });
          
          if (uploadedCount + errorCount === totalFiles) {
            this.handleUploadComplete(uploadedCount, errorCount, totalFiles);
          }
        }
      });
    });
  }

  private handleUploadComplete(uploaded: number, errors: number, total: number): void {
    this.loading = false;
    
    if (errors === 0) {
      this.showNotification(
        `Demande créée avec succès. ${uploaded} fichier(s) uploadé(s).`,
        'success'
      );
    } else if (uploaded > 0) {
      this.showNotification(
        `Demande créée avec succès. ${uploaded}/${total} fichier(s) uploadé(s) avec succès. ${errors} fichier(s) en erreur.`,
        'warning'
      );
    } else {
      this.showNotification(
        `Demande créée avec succès mais échec de l'upload des fichiers (${errors}/${total}). Vous pourrez les ajouter via la modification de la demande.`,
        'warning'
      );
    }
    
    this.router.navigate(['/demandes']);
  }

  cancel(): void {
    this.router.navigate(['/demandes']);
  }

  getUrgenceLabel(urgence: Urgence): string {
    const labels: any = {
      'BASSE': 'Basse',
      'NORMALE': 'Normale',
      'HAUTE': 'Haute',
      'CRITIQUE': 'Critique'
    };
    return labels[urgence] || urgence;
  }

  private toNullableNumber(value: unknown): number | undefined {
    if (value === null || value === undefined || value === '') {
      return undefined;
    }

    const numberValue = Number(value);
    return Number.isNaN(numberValue) ? undefined : numberValue;
  }

  private normalizeValueDate(value: unknown): number {
    return Number(value) === 2 ? 2 : 1;
  }

  showNotification(message: string, type: string): void {
    alert(message); // À remplacer par un vrai système de notifications
  }
}
