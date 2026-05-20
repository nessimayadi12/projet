import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommercantService } from '../../services/commercant.service';
import { Commercant, StatutCommercant } from '../../models/commercant.model';
import { ExcelExportService } from '../../services/excel-export.service';
import { ScreenService } from '../../services/screen.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-commercant-list',
  templateUrl: './commercant-list.component.html',
  styleUrls: ['./commercant-list.component.css']
})
export class CommercantListComponent implements OnInit {
  commercants: Commercant[] = [];
  filteredCommercants: Commercant[] = [];
  pagedCommercants: Commercant[] = [];
  loading = true;
  error: string | null = null;
  searchTerm = '';
  selectedStatut: StatutCommercant | '' = '';
  statuts = Object.values(StatutCommercant);
  page = 1;
  pageSize = 25;
  pageSizeOptions = [10, 25, 50, 100];

  // Permissions observables
  canCreateCommercant$: Observable<boolean>;
  canEditCommercant$: Observable<boolean>;
  canDeleteCommercant$: Observable<boolean>;
  canExportCommercant$: Observable<boolean>;

  constructor(
    private commercantService: CommercantService,
    private router: Router,
    private excelExportService: ExcelExportService,
    private screenService: ScreenService
  ) {
    // Initialiser les permissions
    this.canCreateCommercant$ = this.screenService.hasPermission('CREER_COMMERCANT', 'canCreate');
    this.canEditCommercant$ = this.screenService.hasPermission('MODIFIER_COMMERCANT', 'canEdit');
    this.canDeleteCommercant$ = this.screenService.hasPermission('MODIFIER_COMMERCANT', 'canDelete');
    this.canExportCommercant$ = this.screenService.hasPermission('LISTE_COMMERCANTS', 'canExport');
  }

  ngOnInit(): void {
    this.loadCommercants();
  }

  loadCommercants(): void {
    this.loading = true;
    this.commercantService.getAllCommercants().subscribe({
      next: (data) => {
        const items = Array.isArray(data) ? data : [];
        this.commercants = items;
        this.filteredCommercants = items;
        this.updatePagedCommercants();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des commerçants:', err);
        this.error = 'Impossible de charger la liste des commerçants';
        this.loading = false;
      }
    });
  }

  filterCommercants(): void {
    this.filteredCommercants = this.commercants.filter(commercant => {
      const raisonSociale = (commercant.raisonSociale || '').toLowerCase();
      const adresse = (commercant.adresse || '').toLowerCase();
      const query = (this.searchTerm || '').toLowerCase();

      const matchesSearch = !this.searchTerm || 
        raisonSociale.includes(query) ||
        adresse.includes(query);
      
      const matchesStatut = !this.selectedStatut || commercant.statut === this.selectedStatut;
      
      return matchesSearch && matchesStatut;
    });

    this.page = 1;
    this.updatePagedCommercants();
  }

  get totalPages(): number {
    const total = Math.ceil(this.filteredCommercants.length / this.pageSize);
    return total > 0 ? total : 1;
  }

  onPageSizeChange(value: string): void {
    this.pageSize = Number(value);
    this.page = 1;
    this.updatePagedCommercants();
  }

  previousPage(): void {
    if (this.page > 1) {
      this.page--;
      this.updatePagedCommercants();
    }
  }

  nextPage(): void {
    if (this.page < this.totalPages) {
      this.page++;
      this.updatePagedCommercants();
    }
  }

  private updatePagedCommercants(): void {
    const startIndex = (this.page - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.pagedCommercants = this.filteredCommercants.slice(startIndex, endIndex);
  }

  viewDetails(id: number): void {
    this.router.navigate(['/commercants', id]);
  }

  addNewCommercant(): void {
    this.router.navigateByUrl('/commercants/new');
  }

  editCommercant(id: number): void {
    this.router.navigateByUrl(`/commercants/${id}/edit`);
  }

  deleteCommercant(id: number, raisonSociale: string): void {
    if (confirm(`Êtes-vous sûr de vouloir supprimer le commerçant ${raisonSociale} ?`)) {
      this.commercantService.deleteCommercant(id).subscribe({
        next: () => {
          this.loadCommercants();
          alert('Commerçant supprimé avec succès');
        },
        error: (err) => {
          console.error('Erreur lors de la suppression:', err);
          alert('Impossible de supprimer le commerçant');
        }
      });
    }
  }

  exportToExcel(): void {
    if (this.filteredCommercants.length === 0) {
      alert('Aucune donnée à exporter');
      return;
    }

    // Préparer les données pour l'export
    const dataToExport = this.filteredCommercants.map(commercant => ({
      'Raison Sociale': commercant.raisonSociale || '-',
      'N° Compte': commercant.numeroCompte || '-',
      'Adresse': commercant.adresse || '-',
      'Email': commercant.email || '-',
      'Téléphone': commercant.telephone || '-',
      'Nb TPE': commercant.nombreTpes || 0,
      'Statut': commercant.statut || '-'
    }));

    this.excelExportService.exportToExcel(dataToExport, 'liste_commercants', 'Commerçants');
  }

  getStatutClass(statut: StatutCommercant): string {
    switch(statut) {
      case StatutCommercant.ACTIF: return 'badge-success';
      case StatutCommercant.INACTIF: return 'badge-secondary';
      case StatutCommercant.SUSPENDU: return 'badge-warning';
      default: return 'badge-secondary';
    }
  }
}
