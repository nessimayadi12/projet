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
  loading = true;
  error: string | null = null;
  searchTerm = '';
  selectedStatut: StatutCommercant | '' = '';
  statuts = Object.values(StatutCommercant);

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
        this.commercants = data;
        this.filteredCommercants = data;
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
      const matchesSearch = !this.searchTerm || 
        commercant.raisonSociale.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        (commercant.adresse && commercant.adresse.toLowerCase().includes(this.searchTerm.toLowerCase())) ||
        (commercant.nomContact && commercant.nomContact.toLowerCase().includes(this.searchTerm.toLowerCase())) ||
        (commercant.prenomContact && commercant.prenomContact.toLowerCase().includes(this.searchTerm.toLowerCase()));
      
      const matchesStatut = !this.selectedStatut || commercant.statut === this.selectedStatut;
      
      return matchesSearch && matchesStatut;
    });
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
      'Raison Sociale': commercant.raisonSociale,
      'N° Compte': commercant.numeroCompte || '-',
      'Adresse': commercant.adresse,
      'Nom Contact': `${commercant.nomContact} ${commercant.prenomContact}`,
      'Email': commercant.email,
      'Téléphone': commercant.telephone || '-',
      'Nb TPE': commercant.nombreTpes || 0,
      'Statut': commercant.statut
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
