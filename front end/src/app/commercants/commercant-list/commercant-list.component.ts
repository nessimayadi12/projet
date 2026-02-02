import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommercantService } from '../../services/commercant.service';
import { Commercant, StatutCommercant } from '../../models/commercant.model';

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

  constructor(
    private commercantService: CommercantService,
    private router: Router
  ) { }

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

  getStatutClass(statut: StatutCommercant): string {
    switch(statut) {
      case StatutCommercant.ACTIF: return 'badge-success';
      case StatutCommercant.INACTIF: return 'badge-secondary';
      case StatutCommercant.SUSPENDU: return 'badge-warning';
      default: return 'badge-secondary';
    }
  }
}
