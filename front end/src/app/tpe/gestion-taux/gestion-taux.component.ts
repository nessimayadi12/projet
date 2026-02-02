import { Component, OnInit } from '@angular/core';
import { TauxTPE, StatutTaux } from '../../models/taux-tpe.model';
import { TauxTpeService } from '../../services/taux-tpe.service';
import { AuthService } from '../../services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-gestion-taux',
  templateUrl: './gestion-taux.component.html',
  styleUrls: ['./gestion-taux.component.css']
})
export class GestionTauxComponent implements OnInit {
  tauxEnAttente: TauxTPE[] = [];
  loading = false;
  currentUserId: number;
  currentUserRole: string;

  displayedColumns: string[] = [
    'numeroTerminal',
    'ancienTaux',
    'nouveauTaux',
    'inputer',
    'dateSaisie',
    'statut',
    'actions'
  ];

  constructor(
    private tauxTpeService: TauxTpeService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    public dialog: MatDialog
  ) {
    const currentUser = this.authService.getCurrentUser();
    this.currentUserId = currentUser?.id || 0;
    this.currentUserRole = currentUser?.role || '';
  }

  ngOnInit(): void {
    this.loadTauxEnAttente();
  }

  loadTauxEnAttente(): void {
    this.loading = true;
    this.tauxTpeService.getTauxEnAttenteValidation().subscribe({
      next: (data) => {
        this.tauxEnAttente = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des taux', error);
        this.showNotification('Erreur lors du chargement des taux', 'error');
        this.loading = false;
      }
    });
  }

  validerTaux(taux: TauxTPE): void {
    if (confirm(`Confirmer la validation des taux pour le TPE ${taux.numeroTerminal} ?`)) {
      this.tauxTpeService.validerTaux(taux.id!, this.currentUserId).subscribe({
        next: () => {
          this.showNotification('Taux validés avec succès', 'success');
          this.loadTauxEnAttente();
        },
        error: (error) => {
          console.error('Erreur lors de la validation', error);
          this.showNotification(error.error?.message || 'Erreur lors de la validation', 'error');
        }
      });
    }
  }

  rejeterTaux(taux: TauxTPE): void {
    const motif = prompt('Motif du rejet:');
    if (motif) {
      this.tauxTpeService.rejeterTaux(taux.id!, this.currentUserId, motif).subscribe({
        next: () => {
          this.showNotification('Taux rejeté avec succès', 'success');
          this.loadTauxEnAttente();
        },
        error: (error) => {
          console.error('Erreur lors du rejet', error);
          this.showNotification(error.error?.message || 'Erreur lors du rejet', 'error');
        }
      });
    }
  }

  canValidate(): boolean {
    return this.currentUserRole === 'AUTHORIZER' || 
           this.currentUserRole === 'MONETIQUE' || 
           this.currentUserRole === 'ADMIN';
  }

  isInputerDifferent(taux: TauxTPE): boolean {
    return taux.inputerId !== this.currentUserId;
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
