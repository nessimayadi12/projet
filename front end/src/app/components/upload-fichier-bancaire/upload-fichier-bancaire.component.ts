import { Component, OnInit } from '@angular/core';
import { TPEPostingService, FichierBancaireResult } from '../../services/tpe-posting.service';

@Component({
  selector: 'app-upload-fichier-bancaire',
  templateUrl: './upload-fichier-bancaire.component.html',
  styleUrls: ['./upload-fichier-bancaire.component.css']
})
export class UploadFichierBancaireComponent implements OnInit {

  // États du composant
  selectedFile: File | null = null;
  sessionDate: string = '';
  uploading: boolean = false;
  result: FichierBancaireResult | null = null;
  errorMessage: string = '';
  transactions: any[] = [];
  showTransactions: boolean = false;

  constructor(private tpePostingService: TPEPostingService) { }

  ngOnInit(): void {
    // Initialiser avec la date du jour au format yyyyMMdd
    const today = new Date();
    this.sessionDate = this.formatDate(today);
  }

  /**
   * Gère la sélection d'un fichier
   */
  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    
    if (file) {
      // Vérifier la taille (max 10 MB)
      if (file.size > 10 * 1024 * 1024) {
        this.errorMessage = 'Le fichier est trop volumineux (max 10 MB)';
        this.selectedFile = null;
        return;
      }

      this.selectedFile = file;
      this.errorMessage = '';
      this.result = null;
    }
  }

  /**
   * Upload et traite le fichier bancaire
   */
  uploadFile(): void {
    if (!this.selectedFile) {
      this.errorMessage = 'Veuillez sélectionner un fichier';
      return;
    }

    if (!this.sessionDate || this.sessionDate.length !== 8) {
      this.errorMessage = 'Date de session invalide (format: yyyyMMdd)';
      return;
    }

    this.uploading = true;
    this.errorMessage = '';
    this.result = null;

    this.tpePostingService.uploadFichierBancaire(this.selectedFile, this.sessionDate)
      .subscribe({
        next: (result) => {
          this.result = result;
          this.uploading = false;
          
          if (result.success) {
            console.log('✅ Traitement réussi:', result);
          } else {
            this.errorMessage = result.error || 'Erreur inconnue';
          }
        },
        error: (error) => {
          console.error('❌ Erreur upload:', error);
          this.uploading = false;
          
          if (error.error && error.error.error) {
            this.errorMessage = error.error.error;
          } else {
            this.errorMessage = 'Erreur lors de l\'upload du fichier';
          }
        }
      });
  }

  /**
   * Réinitialise le formulaire
   */
  reset(): void {
    this.selectedFile = null;
    this.result = null;
    this.errorMessage = '';
    this.sessionDate = this.formatDate(new Date());
  }

  /**
   * Consulte les statistiques pour une date
   */
  viewStats(): void {
    if (!this.sessionDate || this.sessionDate.length !== 8) {
      this.errorMessage = 'Date de session invalide';
      return;
    }

    this.tpePostingService.getTransactions(this.sessionDate)
      .subscribe({
        next: (response) => {
          console.log('📊 Transactions:', response);
          if (response.success) {
            this.transactions = response.transactions;
            this.showTransactions = true;
            this.errorMessage = '';
          } else {
            this.errorMessage = response.error || 'Erreur lors de la récupération des transactions';
            this.transactions = [];
            this.showTransactions = false;
          }
        },
        error: (error) => {
          console.error('Erreur transactions:', error);
          this.errorMessage = 'Erreur lors de la récupération des transactions';
          this.transactions = [];
          this.showTransactions = false;
        }
      });
  }

  /**
   * Formate une date en yyyyMMdd
   */
  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}${month}${day}`;
  }

  /**
   * Obtient le nom du fichier sélectionné
   */
  get fileName(): string {
    return this.selectedFile ? this.selectedFile.name : 'Aucun fichier sélectionné';
  }

  /**
   * Obtient la taille du fichier en KB
   */
  get fileSize(): string {
    if (!this.selectedFile) return '';
    const sizeKB = (this.selectedFile.size / 1024).toFixed(2);
    return `${sizeKB} KB`;
  }

  /**
   * Télécharge le rapport PDF
   */
  telechargerPDF(): void {
    if (!this.sessionDate || this.sessionDate.length !== 8) {
      this.errorMessage = 'Date de session invalide';
      return;
    }

    this.tpePostingService.telechargerRapportPDF(this.sessionDate)
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = `rapport_fichier_bancaire_${this.sessionDate}.pdf`;
          link.click();
          window.URL.revokeObjectURL(url);
          console.log('✅ PDF téléchargé');
        },
        error: (error) => {
          console.error('❌ Erreur téléchargement PDF:', error);
          this.errorMessage = 'Erreur lors du téléchargement du rapport PDF';
        }
      });
  }

  /**
   * Télécharge le rapport texte
   */
  telechargerTexte(): void {
    if (!this.sessionDate || this.sessionDate.length !== 8) {
      this.errorMessage = 'Date de session invalide';
      return;
    }

    this.tpePostingService.telechargerRapportTexte(this.sessionDate)
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = `rapport_fichier_bancaire_${this.sessionDate}.txt`;
          link.click();
          window.URL.revokeObjectURL(url);
          console.log('✅ Rapport texte téléchargé');
        },
        error: (error) => {
          console.error('❌ Erreur téléchargement texte:', error);
          this.errorMessage = 'Erreur lors du téléchargement du rapport texte';
        }
      });
  }
}
