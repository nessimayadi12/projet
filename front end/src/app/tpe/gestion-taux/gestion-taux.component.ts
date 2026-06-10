import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TauxTPE } from '../../models/taux-tpe.model';
import { TauxTpeService } from '../../services/taux-tpe.service';
import { AuthService } from '../../services/auth.service';
import { Role } from '../../models/utilisateur.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-gestion-taux',
  templateUrl: './gestion-taux.component.html',
  styleUrls: ['./gestion-taux.component.css']
})
export class GestionTauxComponent implements OnInit, OnDestroy {
  tauxEnAttente: TauxTPE[] = [];
  loading = false;
  currentUserId: number | null = null;
  currentUserRole: string = '';

  createLoading = false;
  submitLoading = false;
  listLoading = false;

  createModel = {
    commercantId: null as number | null,
    nouveauTauxCommission: null as number | null,
    nouveauTauxCommissionInter: null as number | null,
    commentaire: ''
  };

  createdTaux: TauxTPE | null = null;
  searchCommercantId: number | null = null;
  tauxByCommercant: TauxTPE[] = [];

  private destroy$ = new Subject<void>();

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
    this.currentUserId = currentUser?.id || null;
    this.currentUserRole = currentUser?.role || '';
  }

  ngOnInit(): void {
    if (this.canValidate()) {
      this.loadTauxEnAttente();
    } else {
      this.loading = false;
      this.tauxEnAttente = [];
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadTauxEnAttente(): void {
    this.loading = true;
    this.tauxTpeService.getTauxEnAttenteValidation()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
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
      this.tauxTpeService.approveTaux(taux.id!)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
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
      this.tauxTpeService.rejectTaux(taux.id!, motif)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
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

  createTaux(): void {
    if (!this.canCreate()) {
      this.showNotification('Acces refuse - role insuffisant', 'error');
      return;
    }

    if (!this.isCreateFormValid()) {
      this.showNotification('Veuillez remplir tous les champs obligatoires', 'error');
      return;
    }

    this.createLoading = true;
    const request = {
      commercantId: this.createModel.commercantId as number,
      nouveauTauxCommission: this.createModel.nouveauTauxCommission as number,
      nouveauTauxCommissionInter: this.createModel.nouveauTauxCommissionInter as number,
      commentaire: this.createModel.commentaire || undefined
    };

    this.tauxTpeService.createTaux(request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (taux) => {
          this.createdTaux = taux;
          this.createLoading = false;
          this.showNotification('Taux cree avec succes (BROUILLON)', 'success');
          this.loadTauxByCommercantFromResult(taux);
        },
        error: (error) => {
          console.error('Erreur lors de la creation du taux', error);
          this.showNotification(error.message || 'Erreur lors de la creation du taux', 'error');
          this.createLoading = false;
        }
      });
  }

  soumettreTaux(taux: TauxTPE): void {
    if (!this.canCreate()) {
      this.showNotification('Acces refuse - role insuffisant', 'error');
      return;
    }

    this.submitLoading = true;
    this.tauxTpeService.submitForValidation(taux.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updated) => {
          this.submitLoading = false;
          this.createdTaux = updated;
          this.showNotification('Taux soumis pour validation', 'success');
          this.loadTauxByCommercantFromResult(updated);
          if (this.canValidate()) {
            this.loadTauxEnAttente();
          }
        },
        error: (error) => {
          console.error('Erreur lors de la soumission', error);
          this.showNotification(error.message || 'Erreur lors de la soumission', 'error');
          this.submitLoading = false;
        }
      });
  }

  loadTauxByCommercant(): void {
    if (!this.searchCommercantId || this.searchCommercantId <= 0) {
      this.showNotification('Veuillez saisir un commercantId valide', 'error');
      return;
    }

    this.listLoading = true;
    this.tauxTpeService.getTauxByCommercant(this.searchCommercantId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.tauxByCommercant = data || [];
          this.listLoading = false;
        },
        error: (error) => {
          console.error('Erreur lors du chargement des taux commercant', error);
          this.showNotification(error.message || 'Erreur lors du chargement', 'error');
          this.listLoading = false;
        }
      });
  }

  canCreate(): boolean {
    return this.authService.hasAnyRole([Role.MONETIQUE, Role.ADMIN]);
  }

  canValidate(): boolean {
    return this.authService.hasAnyRole([Role.MONETIQUE, Role.ADMIN]);
  }

  isBrouillon(taux: TauxTPE): boolean {
    return taux.statut === 'BROUILLON';
  }

  isInputerDifferent(taux: TauxTPE): boolean {
    return true;
  }

  private isCreateFormValid(): boolean {
    return this.createModel.commercantId !== null &&
      this.createModel.commercantId > 0 &&
      this.createModel.nouveauTauxCommission !== null &&
      this.createModel.nouveauTauxCommissionInter !== null;
  }

  private loadTauxByCommercantFromResult(taux: TauxTPE): void {
    if (!taux?.commercantId) {
      return;
    }

    this.searchCommercantId = taux.commercantId;
    this.loadTauxByCommercant();
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
