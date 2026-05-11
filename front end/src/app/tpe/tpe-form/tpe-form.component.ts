import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TpeService } from '../../services/tpe.service';
import { CommercantService } from '../../services/commercant.service';
import { TPE, StatutTPE, TypeTPE } from '../../models/tpe.model';
import { Commercant } from '../../models/commercant.model';
import { AuthService } from '../../services/auth.service';
import { Role } from '../../models/utilisateur.model';

@Component({
  selector: 'app-tpe-form',
  templateUrl: './tpe-form.component.html',
  styleUrls: ['./tpe-form.component.css']
})
export class TpeFormComponent implements OnInit {
  tpeForm: FormGroup;
  isEditMode = false;
  tpeId: number | null = null;
  loading = false;
  statuts = Object.values(StatutTPE);
  typesTPE = Object.values(TypeTPE);
  commercants: Commercant[] = [];
  isMonetique = false;
  isAgence = false;
  selectedType: TypeTPE = TypeTPE.PHYSIQUE;

  constructor(
    private fb: FormBuilder,
    private tpeService: TpeService,
    private commercantService: CommercantService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.tpeForm = this.createForm();
  }

  createForm(): FormGroup {
    return this.fb.group({
      // Champs de base
      numeroSerie: ['', [Validators.required]],
      marque: [''],
      modele: [''],
      statut: [StatutTPE.DISPONIBLE, [Validators.required]],
      typeTpe: [TypeTPE.PHYSIQUE, [Validators.required]],
      dateAcquisition: ['', [Validators.required]],
      dateMiseEnService: [''],
      commercantActuelId: [''],
      
      // Champs Mon\u00e9tiques (pour les 2 types)
      raisonSociale: [''],
      activite: [''],
      mcc: [''],
      tauxCommission: [0],
      tauxCommissionInter: [0],
      numeroCompte: [''],
      codeAgence: [''],
      serieTpe: [''],
      valueDate: [''],
      numeroTerminal: [{ value: '', disabled: true }], // TID auto-g\u00e9n\u00e9r\u00e9
      
      // Champs E-commerce sp\u00e9cifiques
      urlSiteMarchand: [''],
      webhookUrl: [''],
      cleApi: [''],
      numeroAffiliation: [''],
      typeCommerce: [''],
      cartesAcceptees: [''],
      modeTest: [false],
      
      // Champs administratifs
      loyer: [0],
      commentaire: ['']
    });
  }

  ngOnInit(): void {
    // V\u00e9rifier le r\u00f4le de l'utilisateur
    const currentUser = this.authService.currentUserValue;
    if (currentUser) {
      this.isMonetique = this.authService.hasAnyRole([Role.MONETIQUE, Role.ADMIN]);
      this.isAgence = this.authService.hasAnyRole([Role.AGENCE]);
    }

    // Charger la liste des commer\u00e7ants
    this.loadCommercants();

    // Mode \u00e9dition
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'new') {
      this.isEditMode = true;
      this.tpeId = +id;
      this.loadTPE(this.tpeId);
    }

    // \u00c9couter les changements de type
    this.tpeForm.get('typeTpe')?.valueChanges.subscribe(type => {
      this.selectedType = type;
      this.updateValidators();
    });
  }

  loadCommercants(): void {
    this.commercantService.getAllCommercants().subscribe({
      next: (data) => {
        this.commercants = data;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des commer\u00e7ants:', err);
      }
    });
  }

  loadTPE(id: number): void {
    this.loading = true;
    this.tpeService.getTPEById(id).subscribe({
      next: (tpe) => {
        this.selectedType = tpe.typeTpe;
        this.tpeForm.patchValue({
          numeroSerie: tpe.numeroSerie,
          marque: tpe.marque,
          modele: tpe.modele,
          statut: tpe.statut,
          typeTpe: tpe.typeTpe,
          dateAcquisition: this.formatDateForInput(tpe.dateAcquisition),
          dateMiseEnService: this.formatDateForInput(tpe.dateMiseEnService),
          commercantActuelId: tpe.commercantActuelId,
          raisonSociale: tpe.raisonSociale,
          activite: tpe.activite,
          mcc: tpe.mcc,
          tauxCommission: tpe.tauxCommission,
          tauxCommissionInter: tpe.tauxCommissionInter,
          numeroCompte: tpe.numeroCompte,
          codeAgence: tpe.codeAgence,
          serieTpe: tpe.serieTpe,
          valueDate: this.formatDateForInput(tpe.valueDate),
          numeroTerminal: tpe.numeroTerminal,
          urlSiteMarchand: tpe.urlSiteMarchand,
          webhookUrl: tpe.webhookUrl,
          cleApi: tpe.cleApi,
          numeroAffiliation: tpe.numeroAffiliation,
          typeCommerce: tpe.typeCommerce,
          cartesAcceptees: tpe.cartesAcceptees,
          modeTest: tpe.modeTest,
          loyer: tpe.loyer,
          commentaire: tpe.commentaire
        });
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement du TPE:', err);
        this.showNotification('Impossible de charger les donn\u00e9es du TPE', 'danger');
        this.router.navigate(['/tpe']);
      }
    });
  }

  updateValidators(): void {
    // R\u00e9initialiser les validateurs
    const urlField = this.tpeForm.get('urlSiteMarchand');
    
    if (this.selectedType === TypeTPE.ECOMMERCE) {
      // Champs obligatoires pour E-commerce
      urlField?.setValidators([Validators.required]);
    } else {
      // Retirer les validateurs pour TPE physique
      urlField?.clearValidators();
    }
    
    urlField?.updateValueAndValidity();
  }

  genererTID(): void {
    if (!this.isMonetique) {
      this.showNotification('Seul le service Mon\u00e9tique peut g\u00e9n\u00e9rer un TID', 'warning');
      return;
    }

    const numeroCompte = this.tpeForm.get('numeroCompte')?.value; const codeAgence = this.tpeForm.get('codeAgence')?.value; if (!numeroCompte || !codeAgence) {
      this.showNotification('Veuillez renseigner le num\u00e9ro de compte et le code agence', 'warning');
      return;
    }

    const requestData = { rib: numeroCompte, codeAgence: codeAgence, typeTPE: this.tpeForm.get('typeTpe')?.value || 'PHYSIQUE', numeroSerie: this.tpeForm.get('numeroSerie')?.value || 'TEMP-' + Date.now() }; this.tpeService.genererNumeroTerminal(requestData).subscribe({
      next: (tid) => {
        this.tpeForm.patchValue({ numeroTerminal: tid });
        this.showNotification('TID g\u00e9n\u00e9r\u00e9 avec succ\u00e8s : ' + tid, 'success');
      },
      error: (err) => {
        console.error('Erreur g\u00e9n\u00e9ration TID:', err);
        this.showNotification('Erreur lors de la g\u00e9n\u00e9ration du TID', 'danger');
      }
    });
  }

  formatDateForInput(date: Date | string | undefined): string {
    if (!date) return '';
    const d = new Date(date);
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const year = d.getFullYear();
    return `${year}-${month}-${day}`;
  }

  onSubmit(): void {
    if (this.tpeForm.invalid) {
      Object.keys(this.tpeForm.controls).forEach(key => {
        this.tpeForm.get(key)?.markAsTouched();
      });
      this.showNotification('Veuillez remplir tous les champs obligatoires', 'warning');
      return;
    }

    this.loading = true;
    const formValue = this.tpeForm.getRawValue();
    
    // Mapper les données vers le format backend
    const tpeData = {
      typeTPE: formValue.typeTpe || 'PHYSIQUE',
      numeroSerie: formValue.numeroSerie || 'SERIE-' + Date.now(),
      marque: formValue.marque,
      modele: formValue.modele,
      dateAcquisition: formValue.dateAcquisition,
      mcc: formValue.mcc,
      numeroAffiliation: formValue.numeroAffiliation,
      rib: formValue.numeroCompte,
      codeAgence: formValue.codeAgence,
      commentaire: formValue.commentaire
    };

    const request$ = this.isEditMode && this.tpeId
      ? this.tpeService.updateTPE(this.tpeId, tpeData as any)
      : this.tpeService.createTPE(tpeData as any);

    request$.subscribe({
      next: () => {
        this.showNotification(
          `TPE ${this.isEditMode ? 'mis \u00e0 jour' : 'cr\u00e9\u00e9'} avec succ\u00e8s`,
          'success'
        );
        this.router.navigate(['/tpe']);
      },
      error: (err) => {
        console.error('Erreur:', err);
        const message = err.error?.message || `Erreur lors de ${this.isEditMode ? 'la mise \u00e0 jour' : 'la cr\u00e9ation'} du TPE`;
        this.showNotification(message, 'danger');
        this.loading = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/tpe']);
  }

  getStatutLabel(statut: StatutTPE): string {
    return statut.replace(/_/g, ' ');
  }

  showNotification(message: string, type: string): void {
    // Utiliser les notifications Angular Material ou une library
    alert(message); // Temporaire - \u00e0 remplacer par une vraie notification
  }
}

