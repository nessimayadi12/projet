import { Component, Inject, OnInit, Optional } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { TPE, TypeTPE } from '../../models/tpe.model';
import { DemandeTPE, TypeDemande } from '../../models/demande-tpe.model';
import { TpeService } from '../../services/tpe.service';
import { DemandeService } from '../../services/demande.service';
import { PDFService } from '../../services/pdf.service';

@Component({
  selector: 'app-affectation-tpe',
  templateUrl: './affectation-tpe.component.html',
  styleUrls: ['./affectation-tpe.component.css']
})
export class AffectationTPEComponent implements OnInit {
  affectationForm: FormGroup;
  tpesDisponibles: TPE[] = [];
  loading = false;
  generatingPDF = false;
  demande: DemandeTPE | null = null;

  constructor(
    private fb: FormBuilder,
    private tpeService: TpeService,
    private demandeService: DemandeService,
    private pdfService: PDFService,
    private snackBar: MatSnackBar,
    private route: ActivatedRoute,
    private router: Router,
    @Optional() public dialogRef: MatDialogRef<AffectationTPEComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { demande: DemandeTPE } | null
  ) {
    this.demande = data?.demande || null;
    this.affectationForm = this.fb.group({
      tpeId: ['', Validators.required],
      dateAffectation: [new Date(), Validators.required],
      commentaire: [''],
      genererContrat: [false],
      genererBonLivraison: [false]
    });
  }

  ngOnInit(): void {
    if (this.demande) {
      this.loadTPEsDisponibles();
      return;
    }

    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.showNotification('Demande introuvable', 'error');
      this.navigateBack();
      return;
    }

    this.loading = true;
    this.demandeService.getDemandeById(id).subscribe({
      next: (demande) => {
        this.demande = demande;
        this.loadTPEsDisponibles();
      },
      error: (error) => {
        console.error('Erreur chargement demande', error);
        this.showNotification('Erreur lors du chargement de la demande', 'error');
        this.loading = false;
        this.navigateBack();
      }
    });
  }

  loadTPEsDisponibles(): void {
    if (!this.demande) {
      return;
    }

    this.loading = true;
    const requiredType = this.getRequiredTpeType();
    this.tpeService.getTPEDisponibles().subscribe({
      next: (tpes) => {
        this.tpesDisponibles = tpes.filter(tpe =>
          tpe.statut === 'DISPONIBLE' && tpe.typeTpe === requiredType
        );
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur chargement TPEs disponibles', error);
        this.showNotification('Erreur lors du chargement des TPEs', 'error');
        this.loading = false;
      }
    });
  }

  onTPESelectionChange(tpeId: number): void {
    const tpe = this.tpesDisponibles.find(item => item.id === tpeId);
    if (tpe) {
      this.affectationForm.patchValue({ tpeId: tpe.id });
    }
  }

  async affecterTPE(): Promise<void> {
    if (!this.demande?.id) {
      this.showNotification('Demande introuvable', 'error');
      return;
    }

    if (this.affectationForm.invalid) {
      this.showNotification('Veuillez selectionner un TPE', 'error');
      return;
    }

    const tpeId = this.affectationForm.value.tpeId;
    const genererContrat = this.affectationForm.value.genererContrat;
    const genererBonLivraison = this.affectationForm.value.genererBonLivraison;

    try {
      this.loading = true;

      await firstValueFrom(this.demandeService.affecterTPE(
        this.demande.id,
        tpeId,
        this.affectationForm.value.commentaire || 'Affectation depuis workflow Demande'
      ));

      const documentsGeneres: string[] = [];
      const documentsEchoues: string[] = [];

      if (genererContrat) {
        this.generatingPDF = true;
        try {
          if (!this.demande.commercantId) {
            throw new Error('Commercant non disponible pour la demande');
          }
          await firstValueFrom(this.pdfService.genererContrat(this.demande.commercantId, tpeId));
          documentsGeneres.push('Contrat');
        } catch (error) {
          console.error('Erreur generation contrat', error);
          documentsEchoues.push('Contrat');
        }
      }

      if (genererBonLivraison) {
        this.generatingPDF = true;
        try {
          await firstValueFrom(this.pdfService.genererBonLivraison(this.demande.id, tpeId));
          documentsGeneres.push('Bon de Livraison');
        } catch (error) {
          console.error('Erreur generation bon de livraison', error);
          documentsEchoues.push('Bon de Livraison');
        }
      }

      this.generatingPDF = false;
      this.loading = false;

      let message = 'TPE affecte avec succes';
      if (documentsGeneres.length > 0) {
        message += `. Documents generes: ${documentsGeneres.join(', ')}`;
      }
      if (documentsEchoues.length > 0) {
        message += `. Documents non generes: ${documentsEchoues.join(', ')}`;
      }

      this.showNotification(message, 'success');
      this.finish({ success: true });
    } catch (error) {
      console.error('Erreur affectation TPE', error);
      this.showNotification('Erreur lors de l affectation', 'error');
      this.loading = false;
      this.generatingPDF = false;
    }
  }

  annuler(): void {
    this.finish({ success: false });
  }

  getTPELabel(tpe: TPE): string {
    return `${tpe.numeroSerie} - ${tpe.modele} (${tpe.marque})`;
  }

  getTPEInfo(tpe: TPE): string {
    return `Statut: ${tpe.statut} | Type: ${tpe.typeTpe}`;
  }

  private getRequiredTpeType(): TypeTPE {
    return this.demande?.typeDemande === TypeDemande.ECOMMERCE ? TypeTPE.ECOMMERCE : TypeTPE.PHYSIQUE;
  }

  private finish(result: { success: boolean }): void {
    if (this.dialogRef) {
      this.dialogRef.close(result);
      return;
    }

    this.navigateBack();
  }

  private navigateBack(): void {
    this.router.navigate(['/demandes']);
  }

  private showNotification(message: string, type: 'success' | 'error' | 'info'): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: [`snackbar-${type}`]
    });
  }
}
