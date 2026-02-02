import { Component, OnInit, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TPE } from '../../models/tpe.model';
import { DemandeTPE } from '../../models/demande-tpe.model';
import { TpeService } from '../../services/tpe.service';
import { DemandeService } from '../../services/demande.service';
import { PDFService } from '../../services/pdf.service';
import { TypeDemande } from '../../models/demande-tpe.model';

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
  demande: DemandeTPE;

  constructor(
    private fb: FormBuilder,
    private tpeService: TpeService,
    private demandeService: DemandeService,
    private pdfService: PDFService,
    private snackBar: MatSnackBar,
    public dialogRef: MatDialogRef<AffectationTPEComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { demande: DemandeTPE }
  ) {
    this.demande = data.demande;
    this.affectationForm = this.fb.group({
      tpeId: ['', Validators.required],
      dateAffectation: [new Date(), Validators.required],
      commentaire: [''],
      genererContrat: [true],
      genererBonLivraison: [true]
    });
  }

  ngOnInit(): void {
    this.loadTPEsDisponibles();
  }

  loadTPEsDisponibles(): void {
    this.loading = true;
    this.tpeService.getTPEDisponibles().subscribe({
      next: (tpes) => {
        // Filtrer les TPEs selon le type de demande
        if (this.demande.typeDemande === TypeDemande.PHYSIQUE) {
          this.tpesDisponibles = tpes.filter(tpe => tpe.statut === 'DISPONIBLE');
        } else if (this.demande.typeDemande === TypeDemande.ECOMMERCE) {
          this.tpesDisponibles = tpes.filter(tpe => 
            tpe.statut === 'DISPONIBLE' && tpe.typeTpe === 'PHYSIQUE'
          );
        }
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
    const tpe = this.tpesDisponibles.find(t => t.id === tpeId);
    if (tpe) {
      console.log('TPE sélectionné:', tpe);
    }
  }

  async affecterTPE(): Promise<void> {
    if (this.affectationForm.invalid) {
      this.showNotification('Veuillez sélectionner un TPE', 'error');
      return;
    }

    const tpeId = this.affectationForm.value.tpeId;
    const genererContrat = this.affectationForm.value.genererContrat;
    const genererBonLivraison = this.affectationForm.value.genererBonLivraison;

    try {
      this.loading = true;

      // 1. Affecter le TPE à la demande
      await this.demandeService.affecterTPE(this.demande.id!, tpeId).toPromise();

      // 2. Générer les documents PDF si demandés
      const documentsGeneres: string[] = [];

      if (genererContrat) {
        this.generatingPDF = true;
        await this.pdfService.genererContrat(this.demande.commercantId, tpeId).toPromise();
        documentsGeneres.push('Contrat');
      }

      if (genererBonLivraison) {
        this.generatingPDF = true;
        await this.pdfService.genererBonLivraison(this.demande.id!, tpeId).toPromise();
        documentsGeneres.push('Bon de Livraison');
      }

      this.generatingPDF = false;
      this.loading = false;

      let message = 'TPE affecté avec succès';
      if (documentsGeneres.length > 0) {
        message += `. Documents générés: ${documentsGeneres.join(', ')}`;
      }

      this.showNotification(message, 'success');
      this.dialogRef.close({ success: true });

    } catch (error) {
      console.error('Erreur affectation TPE', error);
      this.showNotification('Erreur lors de l\'affectation', 'error');
      this.loading = false;
      this.generatingPDF = false;
    }
  }

  annuler(): void {
    this.dialogRef.close({ success: false });
  }

  private showNotification(message: string, type: 'success' | 'error' | 'info'): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: [`snackbar-${type}`]
    });
  }

  getTPELabel(tpe: TPE): string {
    return `${tpe.numeroSerie} - ${tpe.modele} (${tpe.marque})`;
  }

  getTPEInfo(tpe: TPE): string {
    return `Statut: ${tpe.statut} | Type: ${tpe.typeTpe}`;
  }
}
