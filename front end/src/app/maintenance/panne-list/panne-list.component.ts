import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Panne, TypePanne, StatutPanne } from '../../models/panne.model';
import { StatutTPE } from '../../models/tpe.model';
import { PanneService } from '../../services/panne.service';
import { AuthService } from '../../services/auth.service';
import { TpeService } from '../../services/tpe.service';
import { ExcelExportService } from '../../services/excel-export.service';
import { Role } from '../../models/utilisateur.model';

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
    private snackBar: MatSnackBar,
    private excelExportService: ExcelExportService
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
          tpe.statut === StatutTPE.MAINTENANCE
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
        // Enrichir les données avec les informations complètes des TPE
        this.enrichirPannesAvecTPE();
      },
      error: (error) => {
        console.error('Erreur chargement pannes', error);
        this.showNotification('Erreur lors du chargement des pannes', 'error');
        this.loading = false;
      }
    });
  }

  private enrichirPannesAvecTPE(): void {
    if (this.pannes.length === 0) {
      this.appliquerFiltres();
      this.loading = false;
      return;
    }

    this.tpeService.getAllTPE().subscribe({
      next: (tpes) => {
        // Enrichir chaque panne avec les infos du TPE
        this.pannes.forEach(panne => {
          const tpe = tpes.find(t => t.id === panne.tpeId);
          if (tpe) {
            panne.tpeNumeroSerie = tpe.numeroSerie;
            panne.commercantNom = tpe.commercantActuelNom || 'Non affecté';
          }
        });
        this.appliquerFiltres();
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur enrichissement pannes', error);
        this.appliquerFiltres();
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

  diagnostiquerPanne(panne: Panne): void {
    const diagnostic = prompt('Diagnostic de la panne:');
    if (diagnostic) {
      this.panneService.diagnostiquer(panne.id!, diagnostic).subscribe({
        next: () => {
          this.showNotification('Diagnostic enregistre avec succes', 'success');
          this.loadPannes();
        },
        error: (error) => {
          console.error('Erreur diagnostic panne', error);
          this.showNotification('Erreur lors du diagnostic', 'error');
        }
      });
    }
  }

  demarrerReparation(panne: Panne): void {
    this.panneService.marquerEnReparation(panne.id!).subscribe({
      next: () => {
        this.showNotification('Panne marquee en reparation', 'success');
        this.loadPannes();
      },
      error: (error) => {
        console.error('Erreur passage en reparation', error);
        this.showNotification('Erreur lors du changement de statut', 'error');
      }
    });
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

  marquerIrrecuperable(panne: Panne): void {
    if (confirm('Confirmer que ce TPE est irrecuperable ?')) {
      this.panneService.changeStatut(panne.id!, StatutPanne.IRRECUPERABLE).subscribe({
        next: () => {
          this.showNotification('Panne marquee irrecuperable', 'success');
          this.loadPannes();
        },
        error: (error) => {
          console.error('Erreur statut irrecuperable', error);
          this.showNotification('Erreur lors du changement de statut', 'error');
        }
      });
    }
  }

  canAssigner(): boolean {
    return this.authService.hasAnyRole([Role.ADMIN, Role.MONETIQUE]);
  }

  canResoudre(): boolean {
    return this.authService.hasAnyRole([Role.ADMIN, Role.MONETIQUE]);
  }

  exportToExcel(): void {
    if (this.pannesFiltrees.length === 0) {
      this.showNotification('Aucune donnée à exporter', 'info');
      return;
    }

    const dataToExport = this.pannesFiltrees.map(panne => ({
      'TPE': panne.tpeNumeroSerie || '-',
      'Commerçant': panne.commercantNom || 'Non affecté',
      'Type Panne': panne.typePanne,
      'Description': panne.description,
      'Urgence': panne.urgence,
      'Statut': panne.statut,
      'Déclarant': panne.declarantNom || '-',
      'Technicien': panne.technicienNom || '-',
      'Date Déclaration': panne.dateDeclaration ? new Date(panne.dateDeclaration).toLocaleDateString('fr-FR') : '-',
      'Date Résolution': panne.dateResolution ? new Date(panne.dateResolution).toLocaleDateString('fr-FR') : '-',
      'Temps Résolution (h)': panne.tempsResolutionHeures || '-',
      'Diagnostic': panne.diagnostic || '-',
      'Solution': panne.solution || '-'
    }));

    this.excelExportService.exportToExcel(dataToExport, 'pannes_tpe', 'Pannes');
    this.showNotification('Export Excel effectué avec succès', 'success');
  }

  exportToPDF(): void {
    if (this.pannesFiltrees.length === 0) {
      this.showNotification('Aucune donnée à exporter', 'info');
      return;
    }

    const printContent = this.generatePrintContent();
    const printWindow = window.open('', '_blank');
    
    if (printWindow) {
      printWindow.document.write(printContent);
      printWindow.document.close();
      
      setTimeout(() => {
        printWindow.print();
      }, 250);
      
      this.showNotification('Génération du PDF en cours...', 'success');
    }
  }

  private generatePrintContent(): string {
    const today = new Date().toLocaleDateString('fr-FR');
    const pannesHTML = this.pannesFiltrees.map(panne => `
      <tr>
        <td>${panne.tpeNumeroSerie || '-'}</td>
        <td>${panne.commercantNom || 'Non affecté'}</td>
        <td>${panne.typePanne}</td>
        <td>${panne.description}</td>
        <td><span class="badge badge-${this.getStatutBadgeClass(panne.statut)}">${panne.statut}</span></td>
        <td>${panne.urgence}</td>
        <td>${panne.technicienNom || 'Non assigné'}</td>
        <td>${panne.dateDeclaration ? new Date(panne.dateDeclaration).toLocaleDateString('fr-FR') : '-'}</td>
      </tr>
    `).join('');

    return `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <title>Liste des Pannes TPE</title>
        <style>
          body {
            font-family: Arial, sans-serif;
            margin: 20px;
          }
          .header {
            text-align: center;
            margin-bottom: 30px;
            border-bottom: 3px solid #2196F3;
            padding-bottom: 10px;
          }
          .header h1 {
            color: #2196F3;
            margin: 0;
          }
          .header p {
            color: #666;
            margin: 5px 0;
          }
          table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
            font-size: 12px;
          }
          th, td {
            border: 1px solid #ddd;
            padding: 8px;
            text-align: left;
          }
          th {
            background-color: #2196F3;
            color: white;
            font-weight: bold;
          }
          tr:nth-child(even) {
            background-color: #f9f9f9;
          }
          .badge {
            padding: 3px 8px;
            border-radius: 3px;
            font-size: 10px;
            font-weight: bold;
            color: white;
          }
          .badge-warning { background-color: #ff9800; }
          .badge-primary { background-color: #2196F3; }
          .badge-success { background-color: #4CAF50; }
          .badge-secondary { background-color: #9e9e9e; }
          .footer {
            margin-top: 30px;
            text-align: center;
            font-size: 11px;
            color: #666;
          }
          @media print {
            body { margin: 10px; }
            .no-print { display: none; }
          }
        </style>
      </head>
      <body>
        <div class="header">
          <h1>🛠️ Liste des Pannes TPE</h1>
          <p>Généré le ${today}</p>
          <p>Nombre total de pannes: ${this.pannesFiltrees.length}</p>
        </div>
        <table>
          <thead>
            <tr>
              <th>TPE</th>
              <th>Commerçant</th>
              <th>Type</th>
              <th>Description</th>
              <th>Statut</th>
              <th>Urgence</th>
              <th>Technicien</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            ${pannesHTML}
          </tbody>
        </table>
        <div class="footer">
          <p>Système de Gestion du Parc TPE Bancaire - Document confidentiel</p>
        </div>
      </body>
      </html>
    `;
  }

  private getStatutBadgeClass(statut: string): string {
    const classes: { [key: string]: string } = {
      'DECLAREE': 'warning',
      'DIAGNOSTIQUEE': 'primary',
      'EN_REPARATION': 'primary',
      'REPAREE': 'success',
      'TESTEE': 'success',
      'IRRECUPERABLE': 'secondary'
    };
    return classes[statut] || 'secondary';
  }

  getStatutClass(statut: string): string {
    const classes: { [key: string]: string } = {
      'DECLAREE': 'badge-warning',
      'DIAGNOSTIQUEE': 'badge-primary',
      'EN_REPARATION': 'badge-primary',
      'REPAREE': 'badge-success',
      'TESTEE': 'badge-success',
      'IRRECUPERABLE': 'badge-secondary'
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
