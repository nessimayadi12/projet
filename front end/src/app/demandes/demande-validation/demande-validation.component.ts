import { Component, OnInit, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { DemandeTPE, TypeDemande, StatutDemande } from '../../models/demande-tpe.model';
import { DemandeService } from '../../services/demande.service';
import { TpeService } from '../../services/tpe.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-demande-validation',
  templateUrl: './demande-validation.component.html',
  styleUrls: ['./demande-validation.component.css']
})
export class DemandeValidationComponent implements OnInit {
  validationForm: FormGroup;
  demande: DemandeTPE;
  loading = false;
  numeroTerminalGenere = '';

  constructor(
    private fb: FormBuilder,
    private demandeService: DemandeService,
    private tpeService: TpeService,
    private http: HttpClient,
    public dialogRef: MatDialogRef<DemandeValidationComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { demande: DemandeTPE }
  ) {
    this.demande = data.demande;
    
    // Formulaire de validation Monétique
    this.validationForm = this.fb.group({
      // Champs communs pour validation
      approuver: [true, [Validators.required]],
      commentaire: [''],
      
      // Champs spécifiques TPE Physique
      mcc: ['', [Validators.required]],
      tauxCommission: ['', [Validators.required, Validators.min(0), Validators.max(100)]],
      tauxCommissionInter: ['', [Validators.min(0), Validators.max(100)]],
      loyer: ['', [Validators.min(0)]],
      serieTpe: [''],
      valueDate: [new Date(), [Validators.required]],
      numeroTerminal: [{ value: '', disabled: true }] // Auto-généré
    });
  }

  ngOnInit(): void {
    // Ajuster les validateurs selon le type de demande
    if (this.demande.typeDemande === TypeDemande.ECOMMERCE) {
      // Pour E-commerce, seuls MCC et N° Terminal sont nécessaires
      this.validationForm.get('tauxCommission')?.clearValidators();
      this.validationForm.get('tauxCommissionInter')?.clearValidators();
      this.validationForm.get('loyer')?.clearValidators();
      this.validationForm.get('valueDate')?.clearValidators();
      
      this.validationForm.get('tauxCommission')?.updateValueAndValidity();
      this.validationForm.get('tauxCommissionInter')?.updateValueAndValidity();
      this.validationForm.get('loyer')?.updateValueAndValidity();
      this.validationForm.get('valueDate')?.updateValueAndValidity();
    }
  }

  genererNumeroTerminal(): void {
    if (this.demande.typeDemande === TypeDemande.PHYSIQUE) {
      if (!this.demande.numeroCompte || !this.demande.codeAgence) {
        this.showNotification('Numéro de compte et code agence requis', 'warning');
        return;
      }

      const requestData = {
        rib: this.demande.numeroCompte,
        codeAgence: this.demande.codeAgence,
        typeTPE: 'PHYSIQUE',
        numeroSerie: this.validationForm.get('serieTpe')?.value || 'TEMP-' + Date.now()
      };

      this.tpeService.genererNumeroTerminal(requestData).subscribe({
        next: (tid) => {
          this.numeroTerminalGenere = tid;
          this.validationForm.patchValue({ numeroTerminal: tid });
          this.showNotification('N° Terminal généré: ' + tid, 'success');
        },
        error: (err) => {
          console.error('Erreur génération TID:', err);
          this.showNotification('Erreur lors de la génération du TID', 'danger');
        }
      });
    } else {
      // E-commerce: génération simplifiée
      if (!this.demande.rib || !this.demande.codeAgence) {
        this.showNotification('RIB et code agence requis', 'warning');
        return;
      }

      const requestData = {
        rib: this.demande.rib,
        codeAgence: this.demande.codeAgence,
        typeTPE: 'ECOMMERCE',
        numeroSerie: 'ECOM-' + Date.now()
      };

      this.tpeService.genererNumeroTerminal(requestData).subscribe({
        next: (tid) => {
          this.numeroTerminalGenere = tid;
          this.validationForm.patchValue({ numeroTerminal: tid });
          this.showNotification('N° Terminal généré: ' + tid, 'success');
        },
        error: (err) => {
          console.error('Erreur génération TID:', err);
          this.showNotification('Erreur lors de la génération du TID', 'danger');
        }
      });
    }
  }

  onSubmit(): void {
    if (this.validationForm.invalid) {
      Object.keys(this.validationForm.controls).forEach(key => {
        this.validationForm.get(key)?.markAsTouched();
      });
      this.showNotification('Veuillez remplir tous les champs obligatoires', 'warning');
      return;
    }

    // Vérifier que le TID a été généré
    if (!this.numeroTerminalGenere) {
      this.showNotification('Veuillez générer le numéro de terminal', 'warning');
      return;
    }

    this.loading = true;
    const formData = this.validationForm.getRawValue();
    
    // Convertir la date au format ISO
    const validationData = {
      ...formData,
      numeroTerminal: this.numeroTerminalGenere,
      valueDate: formData.valueDate ? new Date(formData.valueDate).toISOString() : null
    };

    this.demandeService.validerDemande(this.demande.id!, validationData).subscribe({
      next: () => {
        this.showNotification('Demande validée avec succès', 'success');
        this.dialogRef.close(true);
      },
      error: (err) => {
        console.error('Erreur validation:', err);
        this.showNotification('Erreur lors de la validation', 'danger');
        this.loading = false;
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  isTPEPhysique(): boolean {
    return this.demande.typeDemande === TypeDemande.PHYSIQUE;
  }

  isECommerce(): boolean {
    return this.demande.typeDemande === TypeDemande.ECOMMERCE;
  }

  hasPiecesJointes(): boolean {
    return this.demande.piecesJointes && this.demande.piecesJointes.length > 0;
  }

  downloadPieceJointe(fileName: string): void {
    const url = `${environment.apiUrl}/demandes/${this.demande.id}/piece-jointe/${fileName}`;
    
    this.http.get(url, { 
      responseType: 'blob',
      observe: 'response'
    }).subscribe({
      next: (response) => {
        // Créer un blob et le télécharger
        const blob = response.body;
        if (blob) {
          const downloadUrl = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = downloadUrl;
          link.download = fileName;
          link.click();
          window.URL.revokeObjectURL(downloadUrl);
        }
      },
      error: (err) => {
        console.error('Erreur téléchargement:', err);
        this.showNotification('Erreur lors du téléchargement du fichier', 'danger');
      }
    });
  }

  getFileName(path: string): string {
    if (!path) return '';
    // Extraire le nom du fichier du chemin complet
    const parts = path.split(/[\\\/]/);
    return parts[parts.length - 1];
  }

  getFileIcon(fileName: string): string {
    const extension = fileName.split('.').pop()?.toLowerCase();
    if (extension === 'pdf') return 'picture_as_pdf';
    if (['jpg', 'jpeg', 'png'].includes(extension || '')) return 'image';
    return 'attachment';
  }

  showNotification(message: string, type: string): void {
    // À remplacer par un vrai système de notifications (Material Snackbar)
    alert(message);
  }
}
