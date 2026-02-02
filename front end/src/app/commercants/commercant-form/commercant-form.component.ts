import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CommercantService } from '../../services/commercant.service';
import { AuthService } from '../../services/auth.service';
import { Commercant, StatutCommercant, TypeTPE } from '../../models/commercant.model';
import { Role } from '../../models/utilisateur.model';

@Component({
  selector: 'app-commercant-form',
  templateUrl: './commercant-form.component.html',
  styleUrls: ['./commercant-form.component.css']
})
export class CommercantFormComponent implements OnInit {
  commercantForm: FormGroup;
  isEditMode = false;
  commercantId: number | null = null;
  loading = false;
  statuts = Object.values(StatutCommercant);
  typesTPE = Object.values(TypeTPE);
  currentUserRole: string = '';
  isMonetique = false;
  isAgence = false;

  constructor(
    private fb: FormBuilder,
    private commercantService: CommercantService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    const currentUser = this.authService.getCurrentUser();
    this.currentUserRole = currentUser?.role || '';
    this.isMonetique = currentUser?.role === Role.MONETIQUE || currentUser?.role === Role.ADMIN;
    this.isAgence = currentUser?.role === Role.AGENCE;

    this.commercantForm = this.fb.group({
      // Type de TPE (obligatoire)
      typeCommerce: [TypeTPE.PHYSIQUE, [Validators.required]],
      
      // Données Administratives (AGENCE)
      raisonSociale: ['', [Validators.required]],
      activite: ['', [Validators.required]],
      numeroCompte: ['', [Validators.required]],
      adresse: ['', [Validators.required]],
      localite: [''],
      codePostal: ['', [Validators.required]],
      codeAgence: ['', [Validators.required]],
      telephone: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      emailNotification: ['', [Validators.email]],
      
      // TPE Physique uniquement
      loyer: [null],
      rneFilePath: [''],
      
      // E-commerce uniquement
      urlSiteMarchand: [''],
      webhookUrl: [''],
      webmaster: [''],
      contactTechnique: [''],
      typeCartesAcceptees: [''],
      modeTest: [false],
      
      // Données Monétiques (MONETIQUE seulement)
      mcc: [''],
      
      // Gestion
      statut: [StatutCommercant.ACTIF, [Validators.required]],
      nomContact: [''],
      prenomContact: ['']
    });
  }

  ngOnInit(): void {
    // Écouter les changements de type pour ajuster les validations
    this.commercantForm.get('typeCommerce')?.valueChanges.subscribe(type => {
      this.updateFormValidators(type);
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'new') {
      this.isEditMode = true;
      this.commercantId = +id;
      this.loadCommercant(this.commercantId);
    }
  }

  updateFormValidators(type: TypeTPE): void {
    // Réinitialiser les validateurs
    const loyerControl = this.commercantForm.get('loyer');
    const urlControl = this.commercantForm.get('urlSiteMarchand');
    const webmasterControl = this.commercantForm.get('webmaster');

    if (type === TypeTPE.PHYSIQUE) {
      // Pour TPE Physique, loyer peut être requis
      loyerControl?.setValidators([Validators.min(0)]);
      urlControl?.clearValidators();
      webmasterControl?.clearValidators();
    } else if (type === TypeTPE.ECOMMERCE) {
      // Pour E-commerce, URL et webmaster sont requis
      urlControl?.setValidators([Validators.required]);
      webmasterControl?.setValidators([Validators.required]);
      loyerControl?.clearValidators();
    }

    loyerControl?.updateValueAndValidity();
    urlControl?.updateValueAndValidity();
    webmasterControl?.updateValueAndValidity();
  }

  loadCommercant(id: number): void {
    this.loading = true;
    this.commercantService.getCommercantById(id).subscribe({
      next: (commercant) => {
        this.commercantForm.patchValue(commercant);
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement du commerçant:', err);
        alert('Impossible de charger les données du commerçant');
        this.router.navigate(['/commercants']);
      }
    });
  }

  onSubmit(): void {
    if (this.commercantForm.invalid) {
      Object.keys(this.commercantForm.controls).forEach(key => {
        this.commercantForm.get(key)?.markAsTouched();
      });
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    this.loading = true;
    const commercantData: Commercant = this.commercantForm.value;

    if (this.isEditMode && this.commercantId) {
      this.commercantService.updateCommercant(this.commercantId, commercantData).subscribe({
        next: () => {
          alert('Commerçant mis à jour avec succès');
          this.router.navigate(['/commercants']);
        },
        error: (err) => {
          console.error('Erreur lors de la mise à jour:', err);
          const message = err.error?.message || 'Erreur lors de la mise à jour du commerçant';
          alert('Une erreur inattendue s\'est produite: ' + message);
          this.loading = false;
        }
      });
    } else {
      this.commercantService.createCommercant(commercantData).subscribe({
        next: () => {
          alert('Commerçant créé avec succès');
          this.router.navigate(['/commercants']);
        },
        error: (err) => {
          console.error('Erreur lors de la création:', err);
          const message = err.error?.message || 'Erreur lors de la création du commerçant';
          alert('Une erreur inattendue s\'est produite: ' + message);
          this.loading = false;
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/commercants']);
  }

  get isTypePhysique(): boolean {
    return this.commercantForm.get('typeCommerce')?.value === TypeTPE.PHYSIQUE;
  }

  get isTypeEcommerce(): boolean {
    return this.commercantForm.get('typeCommerce')?.value === TypeTPE.ECOMMERCE;
  }
}
