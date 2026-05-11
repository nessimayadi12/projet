import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TauxTPE, StatutTaux } from '../../models/taux-tpe.model';
import { TauxTpeService } from '../../services/taux-tpe.service';
import { AuthService } from '../../services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';

/**
 * Composante de validation des taux (Rôle: AUTHORIZER)
 * - Affiche les taux en attente de validation
 * - Permet d'approuver ou rejeter
 * - Applique la RÈGLE 4 YEUX: INPUTER ≠ AUTHORIZER
 */
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
  
  // ✅ Subject pour fermer les subscriptions automatiquement
  private destroy$ = new Subject<void>();

  displayedColumns: string[] = [
    'numeroTerminal',
    'commercant',
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
    this.loadTauxEnAttente();
    
    // Optionnel: Rafraîchir toutes les 30 secondes
    // setInterval(() => this.loadTauxEnAttente(), 30000);
  }

  /**
   * ✅ IMPORTANT: Fermer les subscriptions pour éviter les memory leaks
   */
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Charger les taux en attente de validation
   */
  loadTauxEnAttente(): void {
    this.loading = true;
    
    this.tauxTpeService.getTauxEnAttenteValidation()
      .pipe(takeUntil(this.destroy$))  // ✅ Auto-fermer la subscription
      .subscribe({
        next: (data) => {
          this.tauxEnAttente = data;
          this.loading = false;
          
          if (data.length === 0) {
            this.showNotification('✅ Aucun taux en attente de validation', 'info');
          }
        },
        error: (error) => {
          console.error('❌ Erreur chargement taux:', error);
          this.loading = false;
          
          if (error.message.includes('Session expirée')) {
            this.showNotification('❌ Votre session a expiré', 'error');
            this.authService.logout();
          } else if (error.message.includes('Accès refusé')) {
            this.showNotification('❌ Seuls les AUTHORIZER peuvent voir les taux à valider', 'error');
          } else {
            this.showNotification('❌ Erreur: ' + error.message, 'error');
          }
        }
      });
  }

  /**
   * Approuver un taux
   * ✅ RÈGLE 4 YEUX: Bouton disabled si l'AUTHORIZER = l'INPUTER
   */
  validerTaux(taux: TauxTPE): void {
    // Vérification côté client (double sécurité)
    if (!this.isInputerDifferent(taux)) {
      this.showNotification(
        '❌ RÈGLE 4 YEUX: Vous ne pouvez pas valider vos propres saisies!',
        'error'
      );
      return;
    }

    const confirmation = confirm(
      `⚠️ Confirmer la validation des taux pour ${taux.commercantNom || 'ce commerçant'}?\n\n` +
      `Ancien taux: ${taux.ancienTauxCommission}% / ${taux.ancienTauxCommissionInter}%\n` +
      `Nouveau taux: ${taux.nouveauTauxCommission}% / ${taux.nouveauTauxCommissionInter}%`
    );

    if (!confirmation) {
      return;
    }

    this.loading = true;

    this.tauxTpeService.approveTaux(taux.id)
      .pipe(takeUntil(this.destroy$))  // ✅ Auto-fermer
      .subscribe({
        next: (response) => {
          this.loading = false;
          this.showNotification(
            `✅ Taux validés avec succès pour ${response.commercantNom}`,
            'success'
          );
          this.loadTauxEnAttente();  // Rafraîchir la liste
        },
        error: (error) => {
          this.loading = false;
          console.error('❌ Erreur validation:', error);

          // Gestion spécifique des erreurs métier
          if (error.message.includes('4 yeux') || error.message.includes('propres saisies')) {
            this.showNotification(
              '❌ RÈGLE 4 YEUX: Vous ne pouvez pas valider vos propres saisies!',
              'error'
            );
          } else if (error.message.includes('ne peut pas être validé')) {
            this.showNotification(
              '❌ Ce taux ne peut pas être validé dans son état actuel',
              'error'
            );
            this.loadTauxEnAttente();  // Rafraîchir
          } else if (error.message.includes('modifié')) {
            this.showNotification(
              '❌ Ce taux a été validé/modifié par quelqu\'un d\'autre',
              'error'
            );
            this.loadTauxEnAttente();
          } else {
            this.showNotification('❌ Erreur: ' + error.message, 'error');
          }
        }
      });
  }

  /**
   * Rejeter un taux avec motif obligatoire
   */
  rejeterTaux(taux: TauxTPE): void {
    // Vérification côté client
    if (!this.isInputerDifferent(taux)) {
      this.showNotification(
        '❌ RÈGLE 4 YEUX: Vous ne pouvez pas valider vos propres saisies!',
        'error'
      );
      return;
    }

    const motif = prompt(
      `Motif du rejet des taux pour ${taux.commercantNom || 'ce commerçant'} :\n\n` +
      `Ancien: ${taux.ancienTauxCommission}% → Nouveau: ${taux.nouveauTauxCommission}%\n\n` +
      `(Minimum 10 caractères)`
    );

    if (!motif) {
      return;  // Annulé
    }

    if (motif.trim().length < 10) {
      this.showNotification('❌ Le motif doit faire au moins 10 caractères', 'error');
      return;
    }

    this.loading = true;

    this.tauxTpeService.rejectTaux(taux.id, motif)
      .pipe(takeUntil(this.destroy$))  // ✅ Auto-fermer
      .subscribe({
        next: (response) => {
          this.loading = false;
          this.showNotification(
            `✅ Taux rejeté avec succès pour ${response.commercantNom}\nMotif: ${motif}`,
            'success'
          );
          this.loadTauxEnAttente();  // Rafraîchir
        },
        error: (error) => {
          this.loading = false;
          console.error('❌ Erreur rejet:', error);
          this.showNotification('❌ Erreur: ' + error.message, 'error');
        }
      });
  }

  /**
   * ✅ RÈGLE 4 YEUX: Désactiver boutons si même personne
   */
  isInputerDifferent(taux: TauxTPE): boolean {
    // Si l'AUTHORIZER actuel = l'INPUTER du taux → disabled (return false)
    return taux.inputerId !== this.currentUserId;
  }

  /**
   * Afficher notification snackbar
   */
  private showNotification(
    message: string,
    type: 'success' | 'error' | 'info' = 'info'
  ): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 6000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: [`snackbar-${type}`]
    });
  }

  /**
   * Nombre de taux en attente
   */
  get tauxCount(): number {
    return this.tauxEnAttente.length;
  }

  /**
   * Afficher détails d'un taux
   */
  viewDetails(taux: TauxTPE): void {
    console.log('Détails taux:', taux);
    alert(`Taux ID: ${taux.id}\nCommerçant: ${taux.commercantNom}\nInputer: ${taux.inputerNom}\nStatut: ${taux.statut}`);
  }
}
