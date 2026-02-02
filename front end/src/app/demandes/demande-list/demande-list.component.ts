import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { DemandeTPE, StatutDemande, TypeDemande, Urgence } from '../../models/demande-tpe.model';
import { DemandeService } from '../../services/demande.service';
import { AuthService } from '../../services/auth.service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DemandeValidationComponent } from '../demande-validation/demande-validation.component';

@Component({
  selector: 'app-demande-list',
  templateUrl: './demande-list.component.html',
  styleUrls: ['./demande-list.component.css']
})
export class DemandeListComponent implements OnInit {
  demandes: DemandeTPE[] = [];
  demandesFiltrees: DemandeTPE[] = [];
  loading = false;
  currentUserRole: string = '';

  // Filtres
  filtreStatut: string = 'TOUS';
  filtreType: string = 'TOUS';
  filtreUrgence: string = 'TOUS';
  rechercheText: string = '';

  // Options pour filtres
  statuts = ['TOUS', ...Object.values(StatutDemande)];
  types = ['TOUS', ...Object.values(TypeDemande)];
  urgences = ['TOUS', ...Object.values(Urgence)];

  displayedColumns: string[] = [
    'reference',
    'commercantNom',
    'typeDemande',
    'statut',
    'urgence',
    'createdAt',
    'actions'
  ];

  constructor(
    private demandeService: DemandeService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router
  ) {
    const currentUser = this.authService.getCurrentUser();
    this.currentUserRole = currentUser?.role || '';
  }

  ngOnInit(): void {
    this.loadDemandes();
  }

  navigateToNew(): void {
    this.router.navigateByUrl('/demandes/new');
  }

  loadDemandes(): void {
    this.loading = true;
    this.demandeService.getAllDemandes().subscribe({
      next: (data) => {
        this.demandes = data;
        this.appliquerFiltres();
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur chargement demandes', error);
        this.showNotification('Erreur lors du chargement des demandes', 'error');
        this.loading = false;
      }
    });
  }

  appliquerFiltres(): void {
    this.demandesFiltrees = this.demandes.filter(demande => {
      const matchStatut = this.filtreStatut === 'TOUS' || demande.statut === this.filtreStatut;
      const matchType = this.filtreType === 'TOUS' || demande.typeDemande === this.filtreType;
      const matchUrgence = this.filtreUrgence === 'TOUS' || demande.urgence === this.filtreUrgence;
      const matchRecherche = !this.rechercheText || 
        demande.reference?.toLowerCase().includes(this.rechercheText.toLowerCase()) ||
        demande.commercantNom?.toLowerCase().includes(this.rechercheText.toLowerCase());

      return matchStatut && matchType && matchUrgence && matchRecherche;
    });
  }

  onFiltreChange(): void {
    this.appliquerFiltres();
  }

  validerDemande(demande: DemandeTPE): void {
    const dialogRef = this.dialog.open(DemandeValidationComponent, {
      width: '800px',
      data: { demande: demande },
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.showNotification('Demande validée avec succès', 'success');
        this.loadDemandes();
      }
    });
  }

  rejeterDemande(demande: DemandeTPE): void {
    const motif = prompt('Motif du rejet:');
    if (motif) {
      this.demandeService.rejeterDemande(demande.id!, motif).subscribe({
        next: () => {
          this.showNotification('Demande rejetée', 'success');
          this.loadDemandes();
        },
        error: (error) => {
          console.error('Erreur rejet demande', error);
          this.showNotification('Erreur lors du rejet', 'error');
        }
      });
    }
  }

  affecterTPE(demande: DemandeTPE): void {
    // Rediriger vers le composant d'affectation
    // À implémenter avec routing
    console.log('Affecter TPE pour demande:', demande.id);
  }

  canValider(): boolean {
    return this.currentUserRole === 'MONETIQUE' || 
           this.currentUserRole === 'ADMIN';
  }

  canAffecter(): boolean {
    return this.currentUserRole === 'MONETIQUE' || 
           this.currentUserRole === 'ADMIN';
  }

  imprimerDemande(demande: DemandeTPE): void {
    // Ouvrir une nouvelle fenêtre pour l'impression
    const printWindow = window.open('', '_blank', 'width=800,height=600');
    if (!printWindow) {
      this.showNotification('Veuillez autoriser les pop-ups pour imprimer', 'error');
      return;
    }

    const content = this.generatePrintContent(demande);
    printWindow.document.write(content);
    printWindow.document.close();
    
    // Attendre que le contenu soit chargé puis lancer l'impression
    printWindow.onload = () => {
      printWindow.print();
      printWindow.onafterprint = () => printWindow.close();
    };
  }

  private generatePrintContent(demande: DemandeTPE): string {
    return `
      <!DOCTYPE html>
      <html>
      <head>
        <title>Demande TPE - ${demande.reference}</title>
        <style>
          @media print {
            @page { margin: 2cm; }
          }
          body {
            font-family: Arial, sans-serif;
            padding: 20px;
            color: #333;
          }
          .header {
            text-align: center;
            border-bottom: 3px solid #00695c;
            padding-bottom: 20px;
            margin-bottom: 30px;
          }
          .header h1 {
            color: #00695c;
            margin: 0;
          }
          .section {
            margin-bottom: 25px;
            page-break-inside: avoid;
          }
          .section-title {
            background-color: #00695c;
            color: white;
            padding: 8px 15px;
            font-size: 16px;
            font-weight: bold;
            margin-bottom: 15px;
          }
          .info-row {
            display: flex;
            padding: 8px 0;
            border-bottom: 1px solid #eee;
          }
          .info-label {
            font-weight: bold;
            width: 200px;
            color: #555;
          }
          .info-value {
            flex: 1;
          }
          .badge {
            display: inline-block;
            padding: 5px 10px;
            border-radius: 3px;
            font-weight: bold;
            color: white;
          }
          .badge-info { background-color: #17a2b8; }
          .badge-warning { background-color: #ffc107; color: #333; }
          .badge-primary { background-color: #007bff; }
          .badge-success { background-color: #28a745; }
          .badge-danger { background-color: #dc3545; }
          .footer {
            margin-top: 40px;
            text-align: center;
            font-size: 12px;
            color: #666;
            border-top: 1px solid #ddd;
            padding-top: 20px;
          }
        </style>
      </head>
      <body>
        <div class="header">
          <h1>BANK ABC</h1>
          <h2>Demande d'Affectation TPE</h2>
          <p><strong>Référence:</strong> ${demande.reference}</p>
        </div>

        <div class="section">
          <div class="section-title">Informations Générales</div>
          <div class="info-row">
            <div class="info-label">Type de demande:</div>
            <div class="info-value">${demande.typeDemande === 'PHYSIQUE' ? 'TPE Physique' : 'E-Commerce'}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Statut:</div>
            <div class="info-value"><span class="badge ${this.getStatutClass(demande.statut)}">${this.getStatutLabel(demande.statut)}</span></div>
          </div>
          <div class="info-row">
            <div class="info-label">Urgence:</div>
            <div class="info-value">${demande.urgence}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Date de création:</div>
            <div class="info-value">${new Date(demande.createdDate || demande.createdAt!).toLocaleDateString('fr-FR', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' })}</div>
          </div>
        </div>

        <div class="section">
          <div class="section-title">Informations du Commerçant</div>
          <div class="info-row">
            <div class="info-label">Raison Sociale:</div>
            <div class="info-value">${demande.raisonSociale || '-'}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Activité:</div>
            <div class="info-value">${demande.activite || '-'}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Adresse:</div>
            <div class="info-value">${demande.adresse || '-'}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Code Postal:</div>
            <div class="info-value">${demande.codePostal || '-'}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Téléphone:</div>
            <div class="info-value">${demande.telephone || '-'}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Email:</div>
            <div class="info-value">${demande.emailNotification || '-'}</div>
          </div>
          ${demande.typeDemande === 'PHYSIQUE' ? `
          <div class="info-row">
            <div class="info-label">N° Compte:</div>
            <div class="info-value">${demande.numeroCompte || '-'}</div>
          </div>
          ` : `
          <div class="info-row">
            <div class="info-label">RIB:</div>
            <div class="info-value">${demande.rib || '-'}</div>
          </div>
          <div class="info-row">
            <div class="info-label">URL Site Marchand:</div>
            <div class="info-value">${demande.urlSiteMarchand || '-'}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Webmaster:</div>
            <div class="info-value">${demande.webmaster || '-'}</div>
          </div>
          `}
        </div>

        ${demande.mcc ? `
        <div class="section">
          <div class="section-title">Informations Validation Monétique</div>
          <div class="info-row">
            <div class="info-label">MCC:</div>
            <div class="info-value">${demande.mcc}</div>
          </div>
          <div class="info-row">
            <div class="info-label">N° Terminal:</div>
            <div class="info-value">${demande.numeroTerminal || '-'}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Taux Commission:</div>
            <div class="info-value">${demande.tauxCommission || '-'}%</div>
          </div>
          <div class="info-row">
            <div class="info-label">Loyer:</div>
            <div class="info-value">${demande.loyer || '-'} MAD</div>
          </div>
        </div>
        ` : ''}

        <div class="footer">
          <p>Document généré le ${new Date().toLocaleDateString('fr-FR')} à ${new Date().toLocaleTimeString('fr-FR')}</p>
          <p>© ${new Date().getFullYear()} Bank ABC - Tous droits réservés</p>
        </div>
      </body>
      </html>
    `;
  }

  getStatutClass(statut: string): string {
    const classes: { [key: string]: string } = {
      'NOUVELLE': 'badge-info',
      'EN_COURS': 'badge-warning',
      'VALIDEE_MONETIQUE': 'badge-primary',
      'VALIDEE_AGENCE': 'badge-primary',
      'AFFECTEE': 'badge-success',
      'CLOTUREE': 'badge-secondary',
      'REJETEE': 'badge-danger'
    };
    return classes[statut] || 'badge-default';
  }

  getStatutLabel(statut: string): string {
    const labels: { [key: string]: string } = {
      'NOUVELLE': 'Nouvelle',
      'EN_COURS': 'En Cours',
      'VALIDEE_MONETIQUE': 'Validée Monétique',
      'VALIDEE_AGENCE': 'Validée Agence',
      'AFFECTEE': 'Affectée',
      'CLOTUREE': 'Clôturée',
      'REJETEE': 'Rejetée'
    };
    return labels[statut] || statut;
  }

  getUrgenceClass(urgence: string): string {
    const classes: { [key: string]: string } = {
      'BASSE': 'urgence-basse',
      'NORMALE': 'urgence-normale',
      'HAUTE': 'urgence-haute',
      'CRITIQUE': 'urgence-critique'
    };
    return classes[urgence] || '';
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
