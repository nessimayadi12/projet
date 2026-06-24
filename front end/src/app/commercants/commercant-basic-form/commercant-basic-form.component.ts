import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Commercant } from '../../models/commercant.model';
import { CommercantService } from '../../services/commercant.service';

@Component({
  selector: 'app-commercant-basic-form',
  templateUrl: './commercant-basic-form.component.html',
  styleUrls: ['./commercant-basic-form.component.css']
})
export class CommercantBasicFormComponent implements OnInit {
  commercantForm: FormGroup;
  isEditMode = false;
  commercantId: number | null = null;
  loading = false;
  loadingData = false;

  constructor(
    private fb: FormBuilder,
    private commercantService: CommercantService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.commercantForm = this.fb.group({
      raisonSociale: ['', [Validators.required]],
      activite: ['', [Validators.required]],
      numeroCompte: ['', [Validators.required]],
      codeAgence: ['', [Validators.required, Validators.pattern(/^\d{3}$/)]],
      adresse: ['', [Validators.required]],
      localite: [''],
      codePostal: ['', [Validators.required]],
      telephone: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'new') {
      this.isEditMode = true;
      this.commercantId = Number(id);
      this.loadCommercant(this.commercantId);
    }
  }

  loadCommercant(id: number): void {
    this.loadingData = true;
    this.commercantService.getCommercantById(id).subscribe({
      next: commercant => {
        this.commercantForm.patchValue({
          raisonSociale: commercant.raisonSociale,
          activite: commercant.activite,
          numeroCompte: commercant.numeroCompte || commercant.rib,
          codeAgence: commercant.codeAgence,
          adresse: commercant.adresse,
          localite: commercant.localite || commercant.ville,
          codePostal: commercant.codePostal,
          telephone: commercant.telephone,
          email: commercant.email
        });
        this.loadingData = false;
      },
      error: err => {
        console.error('Erreur lors du chargement du commer\u00e7ant:', err);
        alert('Impossible de charger les donn\u00e9es du commer\u00e7ant');
        this.loadingData = false;
        this.router.navigate(['/commercants']);
      }
    });
  }

  onSubmit(): void {
    if (this.commercantForm.invalid) {
      Object.values(this.commercantForm.controls).forEach(control => control.markAsTouched());
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    this.loading = true;
    const commercantData = this.commercantForm.getRawValue() as Commercant;
    const request$ = this.isEditMode && this.commercantId
      ? this.commercantService.updateCommercant(this.commercantId, commercantData)
      : this.commercantService.createCommercant(commercantData);

    request$.subscribe({
      next: () => {
        alert(this.isEditMode
          ? 'Commer\u00e7ant mis \u00e0 jour avec succ\u00e8s'
          : 'Commer\u00e7ant cr\u00e9\u00e9 avec succ\u00e8s');
        this.router.navigate(['/commercants']);
      },
      error: err => {
        console.error('Erreur lors de l\'enregistrement:', err);
        const message = err.error?.message || 'Erreur lors de l\'enregistrement du commer\u00e7ant';
        alert('Une erreur inattendue s\'est produite : ' + message);
        this.loading = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/commercants']);
  }
}
