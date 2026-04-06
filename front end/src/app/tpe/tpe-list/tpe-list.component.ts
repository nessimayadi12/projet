import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TpeService } from '../../services/tpe.service';
import { TPE, StatutTPE } from '../../models/tpe.model';
import { AuthService } from '../../services/auth.service';
import { ScreenService } from '../../services/screen.service';
import { ExcelExportService } from '../../services/excel-export.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-tpe-list',
  templateUrl: './tpe-list.component.html',
  styleUrls: ['./tpe-list.component.css']
})
export class TpeListComponent implements OnInit {
  tpes: TPE[] = [];
  filteredTpes: TPE[] = [];
  loading = true;
  error: string | null = null;
  importing = false;
  searchTerm = '';
  selectedStatut: StatutTPE | '' = '';
  statuts = Object.values(StatutTPE);

  // Permissions observables
  canCreateTPE$: Observable<boolean>;
  canEditTPE$: Observable<boolean>;
  canDeleteTPE$: Observable<boolean>;
  canExportTPE$: Observable<boolean>;

  constructor(
    private tpeService: TpeService,
    private router: Router,
    private authService: AuthService,
    private excelExportService: ExcelExportService,
    private screenService: ScreenService
  ) {
    // Initialiser les permissions
    this.canCreateTPE$ = this.screenService.hasPermission('CREER_TPE', 'canCreate');
    this.canEditTPE$ = this.screenService.hasPermission('MODIFIER_TPE', 'canEdit');
    this.canDeleteTPE$ = this.screenService.hasPermission('MODIFIER_TPE', 'canDelete');
    this.canExportTPE$ = this.screenService.hasPermission('LISTE_TPE', 'canExport');
  }

  ngOnInit(): void {
    this.loadTPEs();
  }

  loadTPEs(): void {
    this.loading = true;
    this.tpeService.getAllTPE().subscribe({
      next: (data) => {
        const items = Array.isArray(data) ? data : [];
        this.tpes = items;
        this.filteredTpes = items;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des TPE:', err);
        this.error = 'Impossible de charger la liste des TPE';
        this.loading = false;
      }
    });
  }

  filterTPEs(): void {
    this.filteredTpes = this.tpes.filter(tpe => {
      const serie = (tpe.numeroSerie || '').toLowerCase();
      const marque = (tpe.marque || '').toLowerCase();
      const modele = (tpe.modele || '').toLowerCase();
      const commercant = (tpe.commercantActuelNom || '').toLowerCase();
      const query = (this.searchTerm || '').toLowerCase();

      const matchesSearch = !this.searchTerm || 
        serie.includes(query) ||
        marque.includes(query) ||
        modele.includes(query) ||
        commercant.includes(query);
      
      const matchesStatut = !this.selectedStatut || tpe.statut === this.selectedStatut;
      
      return matchesSearch && matchesStatut;
    });
  }

  viewDetails(id: number): void {
    this.router.navigate(['/tpe', id]);
  }

  addNewTPE(): void {
    this.router.navigate(['/tpe/new']);
  }

  viewImportRecords(): void {
    this.router.navigate(['/tpe/imports']);
  }

  editTPE(id: number): void {
    this.router.navigate(['/tpe', id, 'edit']);
  }

  deleteTPE(id: number, numeroSerie: string): void {
    if (confirm(`Êtes-vous sûr de vouloir supprimer le TPE ${numeroSerie} ?`)) {
      this.tpeService.deleteTPE(id).subscribe({
        next: () => {
          this.loadTPEs();
          alert('TPE supprimé avec succès');
        },
        error: (err) => {
          console.error('Erreur lors de la suppression:', err);
          alert('Impossible de supprimer le TPE');
        }
      });
    }
  }

  exportToExcel(): void {
    if (this.filteredTpes.length === 0) {
      alert('Aucune donnée à exporter');
      return;
    }

    // Préparer les données pour l'export
    const dataToExport = this.filteredTpes.map(tpe => ({
      'N° Série': tpe.numeroSerie,
      'Marque': tpe.marque,
      'Modèle': tpe.modele,
      'Statut': this.getStatutLabel(tpe.statut),
      'Commerçant': tpe.commercantActuelNom || '-',
      'Date Acquisition': tpe.dateAcquisition ? new Date(tpe.dateAcquisition).toLocaleDateString('fr-FR') : '-'
    }));

    this.excelExportService.exportToExcel(dataToExport, 'liste_tpe', 'TPE');
  }

  triggerImportFile(): void {
    const input = document.getElementById('tpe-import-input') as HTMLInputElement | null;
    input?.click();
  }

  onImportFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (!file) {
      return;
    }

    this.importing = true;
    this.tpeService.importTPE(file).subscribe({
      next: (result) => {
        const summary = result
          ? `Import terminé: ${result.storedRows ?? 0} lignes stockées, ${result.importedRows ?? 0} créés, ${result.updatedRows ?? 0} mis à jour, ${result.affectedRows ?? 0} affectés, ${result.skippedRows ?? 0} ignorés`
          : 'Import terminé avec succès';

        alert(summary);
        this.loadTPEs();
        input.value = '';
        this.importing = false;
      },
      error: (err) => {
        console.error('Erreur lors de l\'import Excel:', err);
        alert('Impossible d\'importer le fichier Excel');
        input.value = '';
        this.importing = false;
      }
    });
  }

  getStatutClass(statut: StatutTPE): string {
    switch(statut) {
      case StatutTPE.DISPONIBLE: return 'badge-success';
      case StatutTPE.AFFECTE: return 'badge-primary';
      case StatutTPE.EN_PANNE: return 'badge-danger';
      case StatutTPE.EN_MAINTENANCE: return 'badge-warning';
      case StatutTPE.HORS_SERVICE: return 'badge-dark';
      case StatutTPE.RESERVE: return 'badge-info';
      default: return 'badge-secondary';
    }
  }

  getStatutLabel(statut: StatutTPE): string {
    return (statut || '').replace(/_/g, ' ');
  }
}
