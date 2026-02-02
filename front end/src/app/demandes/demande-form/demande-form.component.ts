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
      typeDemande: [TypeDemande.PHYSIQUE, [Validators.required]],
      urgence: [Urgence.NORMALE, [Validators.required]],
      description: [''],
      
      // Champs communs aux deux types
      raisonSociale: ['', [Validators.required]],
      activite: ['', [Validators.required]],
      adresse: ['', [Validators.required]],
      codePostal: ['', [Validators.required]],
      codeAgence: ['', [Validators.required]],
      telephone: ['', [Validators.required]],
      emailNotification: ['', [Validators.required, Validators.email]],
      
      // TPE Physique uniquement
      typeTpeRequis: [''],
      numeroCompte: [''],
      
      // E-commerce uniquement
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
      this.isAgence = currentUser.role === Role.AGENCE;
      this.isMonetique = currentUser.role === Role.MONETIQUE || currentUser.role === Role.ADMIN;
    }

    // Écouter les changements de type de demande pour adapter les validateurs
    this.demandeForm.get('typeDemande')?.valueChanges.subscribe(type => {
      this.updateFormValidators(type);
    });

    // Initialiser les validateurs pour le type par défaut
    this.updateFormValidators(TypeDemande.PHYSIQUE);

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
    
    // Pré-remplir le formulaire avec les données du commerçant
    this.demandeForm.patchValue({
      raisonSociale: commercant.raisonSociale,
      activite: commercant.activite || '',
      adresse: commercant.adresse,
      codePostal: commercant.codePostal,
      telephone: commercant.telephone,
      emailNotification: commercant.email
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
      adresse: '',
      codePostal: '',
      telephone: '',
      emailNotification: ''
    });
  }

  toggleCommercantSearch(): void {
    this.showCommercantSearch = !this.showCommercantSearch;
    if (this.showCommercantSearch) {
      this.clearCommercantSelection();
    }
  }

  updateFormValidators(typeDemande: TypeDemande): void {
    if (typeDemande === TypeDemande.PHYSIQUE) {
      // TPE Physique: numeroCompte et typeTpeRequis requis
      this.demandeForm.get('typeTpeRequis')?.setValidators([Validators.required]);
      this.demandeForm.get('numeroCompte')?.setValidators([Validators.required]);
      
      // E-commerce: champs optionnels
      this.demandeForm.get('localite')?.clearValidators();
      this.demandeForm.get('rib')?.clearValidators();
      this.demandeForm.get('webmaster')?.clearValidators();
      this.demandeForm.get('contactTechnique')?.clearValidators();
      this.demandeForm.get('urlSiteMarchand')?.clearValidators();
    } else {
      // E-commerce: champs E-commerce requis
      this.demandeForm.get('localite')?.setValidators([Validators.required]);
      this.demandeForm.get('rib')?.setValidators([Validators.required]);
      this.demandeForm.get('webmaster')?.setValidators([Validators.required]);
      this.demandeForm.get('contactTechnique')?.setValidators([Validators.required]);
      this.demandeForm.get('urlSiteMarchand')?.setValidators([Validators.required, Validators.pattern('https?://.+')]);
      
      // TPE Physique: champs optionnels
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
        this.demandeForm.patchValue(demande);
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
    const files = event.target.files;
    if (files) {
      this.selectedFiles = Array.from(files);
    }
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
    const demandeData: DemandeTPE = {
      ...this.demandeForm.value,
      statut: StatutDemande.NOUVELLE
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
    let uploadedCount = 0;
    const totalFiles = this.selectedFiles.length;

    this.selectedFiles.forEach(file => {
      this.demandeService.uploadPieceJointe(demandeId, file).subscribe({
        next: () => {
          uploadedCount++;
          if (uploadedCount === totalFiles) {
            this.showNotification('Demande créée avec succès', 'success');
            this.router.navigate(['/demandes']);
          }
        },
        error: (err) => {
          console.error('Erreur upload fichier:', err);
          uploadedCount++;
          if (uploadedCount === totalFiles) {
            this.showNotification('Demande créée mais erreur upload fichiers', 'warning');
            this.router.navigate(['/demandes']);
          }
        }
      });
    });
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

  showNotification(message: string, type: string): void {
    alert(message); // À remplacer par un vrai système de notifications
  }
}
