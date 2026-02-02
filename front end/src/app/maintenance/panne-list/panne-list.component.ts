import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Panne, TypePanne, StatutPanne } from '../../models/panne.model';
import { StatutTPE } from '../../models/tpe.model';
import { PanneService } from '../../services/panne.service';
import { AuthService } from '../../services/auth.service';
import { TpeService } from '../../services/tpe.service';

@Component({
  selector: 'app-panne-list',
  templateUrl: './panne-list.component.html',
  styleUrls: ['./panne-list.component.css']
})
export class PanneListComponent implements OnInit {
  pannes: Panne[] = [];
  pannesFiltrees: Panne[] = [];
  tpes: any[] = [];
  loading = false;
  showDeclarationForm = false;
  declarationForm: FormGroup;
  currentUserRole: string = '';

  // Filtres
  filtreStatut: string = 'TOUS';

  statuts = ['TOUS', ...Object.values(StatutPanne)];

  displayedColumns: string[] = [
    'tpeNumeroSerie',
    'typePanne',
    'statut',
    'dateDeclaration',
    'technicienAssigne',
    'actions'
  ];

  constructor(
    private fb: FormBuilder,
    private panneService: PanneService,
    private tpeService: TpeService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    const currentUser = this.authService.getCurrentUser();
    this.currentUserRole = currentUser?.role || '';

    this.declarationForm = this.fb.group({
      tpeId: ['', Validators.required],
      description: ['', [Validators.required, Validators.minLength(10)]]
    });
  }

  ngOnInit(): void {
    this.loadPannes();
    this.loadTPEs();
  }

  loadTPEs(): void {
    // Charger les TPE avec statut AFFECTE (ceux qui ont été acceptés par monétique)
    this.tpeService.getAllTPE().subscribe({
      next: (data) => {
        // Filtrer uniquement les TPE affectés ou en service
        this.tpes = data.filter(tpe => 
          tpe.statut === StatutTPE.AFFECTE || 
          tpe.statut === StatutTPE.EN_PANNE || 
          tpe.statut === StatutTPE.EN_MAINTENANCE
        );
        console.log('TPEs chargés:', this.tpes.length, this.tpes);
        if (this.tpes.length === 0) {
          console.warn('Aucun TPE affecté trouvé. Les TPE doivent être affectés via une demande acceptée.');
        }
      },
      error: (error) => {
        console.error('Erreur chargement TPEs', error);
        this.showNotification('Erreur lors du chargement des TPE', 'error');
      }
    });
  }

  afficherAideTPE(): void {
    if (this.tpes.length === 0) {
      alert('Aucun TPE affecté disponible.\n\nPour déclarer une panne sur un TPE :\n1. Le TPE doit d\'abord être affecté à un commerçant via une demande\n2. La demande doit être acceptée par le monétique\n3. Une fois le TPE affecté, il apparaîtra dans cette liste\n\nAllez dans "Demandes TPE" pour créer et traiter des demandes.');
      return;
    }
    
    let message = 'TPE affectés disponibles:\n\n';
    this.tpes.slice(0, 10).forEach(tpe => {
      message += `ID: ${tpe.id} - ${tpe.numeroSerie} (${tpe.marque} ${tpe.modele}) - Statut: ${tpe.statut}\n`;
    });
    
    if (this.tpes.length > 10) {
      message += `\n... et ${this.tpes.length - 10} autres TPE`;
    }
    
    alert(message);
  }

  loadPannes(): void {
    this.loading = true;
    this.panneService.getAllPannes().subscribe({
      next: (data) => {
        this.pannes = data;
        this.appliquerFiltres();
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur chargement pannes', error);
        this.showNotification('Erreur lors du chargement des pannes', 'error');
        this.loading = false;
      }
    });
  }

  appliquerFiltres(): void {
    this.pannesFiltrees = this.pannes.filter(panne => {
      const matchStatut = this.filtreStatut === 'TOUS' || panne.statut === this.filtreStatut;
      return matchStatut;
    });
  }

  onFiltreChange(): void {
    this.appliquerFiltres();
  }

  toggleDeclarationForm(): void {
    this.showDeclarationForm = !this.showDeclarationForm;
    if (!this.showDeclarationForm) {
      this.declarationForm.reset();
    }
  }

  declarerPanne(): void {
    if (this.declarationForm.invalid) {
      this.showNotification('Formulaire invalide', 'error');
      return;
    }

    const panneData = {
      tpe: {
        id: this.declarationForm.value.tpeId
      },
      description: this.declarationForm.value.description
    };

    this.panneService.declarerPanne(panneData as any).subscribe({
      next: () => {
        this.showNotification('Panne déclarée avec succès', 'success');
        this.declarationForm.reset();
        this.showDeclarationForm = false;
        this.loadPannes();
      },
      error: (error) => {
        console.error('Erreur déclaration panne', error);
        let message = 'Erreur lors de la déclaration';
        if (error.error && error.error.message) {
          if (error.error.message.includes('TPE non trouvé')) {
            message = `TPE introuvable. Vérifiez l'ID du TPE (${this.declarationForm.value.tpeId}). Allez dans "Gestion TPE" pour voir les TPE disponibles.`;
          } else {
            message = error.error.message;
          }
        }
        this.showNotification(message, 'error');
      }
    });
  }

  assignerTechnicien(panne: Panne): void {
    const technicienId = prompt('ID du technicien à assigner:');
    if (technicienId) {
      this.panneService.assignerTechnicien(panne.id!, parseInt(technicienId)).subscribe({
        next: () => {
          this.showNotification('Technicien assigné avec succès', 'success');
          this.loadPannes();
        },
        error: (error) => {
          console.error('Erreur assignation technicien', error);
          this.showNotification('Erreur lors de l\'assignation', 'error');
        }
      });
    }
  }

  resoudrePanne(panne: Panne): void {
    const solution = prompt('Description de la solution:');
    if (solution) {
      this.panneService.resoudrePanne(panne.id!, solution).subscribe({
        next: () => {
          this.showNotification('Panne résolue avec succès', 'success');
          this.loadPannes();
        },
        error: (error) => {
          console.error('Erreur résolution panne', error);
          this.showNotification('Erreur lors de la résolution', 'error');
        }
      });
    }
  }

  canAssigner(): boolean {
    return this.currentUserRole === 'ADMIN' || this.currentUserRole === 'MONETIQUE';
  }

  canResoudre(): boolean {
    return this.currentUserRole === 'TECHNICIEN' || this.currentUserRole === 'ADMIN';
  }

  getStatutClass(statut: string): string {
    const classes: { [key: string]: string } = {
      'DECLAREE': 'badge-warning',
      'EN_COURS': 'badge-primary',
      'RESOLUE': 'badge-success',
      'FERMEE': 'badge-secondary'
    };
    return classes[statut] || 'badge-default';
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
