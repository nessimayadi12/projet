import { Component, OnInit } from '@angular/core';
import { DashboardService } from '../../services/dashboard.service';
import { Chart } from 'chart.js/auto';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-dashboard-tpe',
  templateUrl: './dashboard-tpe.component.html',
  styleUrls: ['./dashboard-tpe.component.css']
})
export class DashboardTpeComponent implements OnInit {
  stats: any = null;
  evolutionData: any[] = [];
  agenceData: any[] = [];
  loading = true;
  error: string | null = null;

  // Filtres
  selectedStatut: string = 'TOUS';
  selectedMarque: string = 'TOUTES';
  selectedAgence: string = 'TOUTES';

  // Charts
  statutChart: any;
  marqueChart: any;
  evolutionChart: any;
  agenceChart: any;

  statuts = ['TOUS', 'DISPONIBLE', 'AFFECTE', 'EN_PANNE', 'MAINTENANCE', 'HORS_SERVICE'];
  marques: string[] = [];
  agences: string[] = ['TOUTES'];

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    forkJoin({
      stats: this.dashboardService.getStats(),
      evolution: this.dashboardService.getEvolutionTpe(),
      agences: this.dashboardService.getStatistiquesParAgence()
    }).subscribe({
      next: ({ stats, evolution, agences }) => {
        this.stats = stats;
        this.evolutionData = evolution || [];
        this.agenceData = agences || [];
        this.marques = ['TOUTES', ...Object.keys(stats.repartitionParMarque || {})];
        this.agences = ['TOUTES', ...Array.from(new Set((this.agenceData || []).map((item) => item.agence).filter(Boolean)))];
        this.loading = false;
        this.initCharts();
      },
      error: (err) => {
        console.error('Erreur chargement stats TPE:', err);
        this.error = 'Impossible de charger les statistiques';
        this.loading = false;
      }
    });
  }

  initCharts(): void {
    setTimeout(() => {
      this.createStatutChart();
      this.createMarqueChart();
      this.createEvolutionChart();
      this.createAgenceChart();
    }, 100);
  }

  createStatutChart(): void {
    const ctx = document.getElementById('statutChart') as HTMLCanvasElement;
    if (!ctx || !this.stats) return;

    if (this.statutChart) {
      this.statutChart.destroy();
    }

    this.statutChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: ['Disponibles', 'Affectés', 'En Panne', 'Maintenance', 'Hors Service'],
        datasets: [{
          data: [
            this.stats.tpeDisponibles,
            this.stats.tpeAffectes,
            this.stats.tpeEnPanne,
            this.stats.tpeEnMaintenance,
            this.stats.tpeHorsService
          ],
          backgroundColor: [
            '#28a745', // Vert - Disponible
            '#007bff', // Bleu - Affecté
            '#dc3545', // Rouge - En Panne
            '#ffc107', // Orange - Maintenance
            '#6c757d'  // Gris - Hors Service
          ],
          borderWidth: 2,
          borderColor: '#fff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 15,
              font: {
                size: 12
              }
            }
          },
          tooltip: {
            callbacks: {
              label: function(context) {
                const label = context.label || '';
                const value = context.parsed || 0;
                const total = context.dataset.data.reduce((a: any, b: any) => a + b, 0);
                const percentage = ((value / total) * 100).toFixed(1);
                return `${label}: ${value} (${percentage}%)`;
              }
            }
          }
        }
      }
    });
  }

  createMarqueChart(): void {
    const ctx = document.getElementById('marqueChart') as HTMLCanvasElement;
    if (!ctx || !this.stats || !this.stats.repartitionParMarque) return;

    if (this.marqueChart) {
      this.marqueChart.destroy();
    }

    const marques = Object.keys(this.stats.repartitionParMarque);
    const values = Object.values(this.stats.repartitionParMarque);

    this.marqueChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: marques,
        datasets: [{
          label: 'Nombre de TPE',
          data: values,
          backgroundColor: '#007bff',
          borderColor: '#0056b3',
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              stepSize: 1
            }
          }
        }
      }
    });
  }

  createEvolutionChart(): void {
    const ctx = document.getElementById('evolutionChart') as HTMLCanvasElement;
    if (!ctx) return;

    if (this.evolutionChart) {
      this.evolutionChart.destroy();
    }

    const derniersMois = this.evolutionData.map((item) => item.mois);
    const disponible = this.evolutionData.map((item) => Number(item.disponible || 0));
    const affecte = this.evolutionData.map((item) => Number(item.affecte || 0));
    const enPanne = this.evolutionData.map((item) => Number(item.enPanne || 0));
    const maintenance = this.evolutionData.map((item) => Number(item.maintenance || 0));
    const horsService = this.evolutionData.map((item) => Number(item.horsService || 0));
    
    this.evolutionChart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: derniersMois,
        datasets: [
          {
            label: 'Disponibles',
            data: disponible,
            borderColor: '#28a745',
            backgroundColor: 'rgba(40, 167, 69, 0.1)',
            tension: 0.4
          },
          {
            label: 'Affectés',
            data: affecte,
            borderColor: '#007bff',
            backgroundColor: 'rgba(0, 123, 255, 0.1)',
            tension: 0.4
          },
          {
            label: 'En Panne',
            data: enPanne,
            borderColor: '#dc3545',
            backgroundColor: 'rgba(220, 53, 69, 0.1)',
            tension: 0.4
          },
          {
            label: 'Maintenance',
            data: maintenance,
            borderColor: '#ffc107',
            backgroundColor: 'rgba(255, 193, 7, 0.1)',
            tension: 0.4
          },
          {
            label: 'Hors Service',
            data: horsService,
            borderColor: '#6c757d',
            backgroundColor: 'rgba(108, 117, 125, 0.1)',
            tension: 0.4
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom'
          }
        },
        scales: {
          y: {
            beginAtZero: true
          }
        }
      }
    });
  }

  createAgenceChart(): void {
    const ctx = document.getElementById('agenceChart') as HTMLCanvasElement;
    if (!ctx) return;

    if (this.agenceChart) {
      this.agenceChart.destroy();
    }

    const agences = Array.from(new Set((this.agenceData || []).map((item) => item.agence))).filter(Boolean);
    const statuts = ['DISPONIBLE', 'AFFECTE', 'EN_PANNE', 'MAINTENANCE', 'HORS_SERVICE'];
    const colors: Record<string, string> = {
      DISPONIBLE: '#28a745',
      AFFECTE: '#007bff',
      EN_PANNE: '#dc3545',
      MAINTENANCE: '#ffc107',
      HORS_SERVICE: '#6c757d'
    };
    const datasets = statuts.map((statut) => ({
      label: statut,
      data: agences.map((agence) => {
        const match = this.agenceData.find((item) => item.agence === agence && item.statut === statut);
        return match ? Number(match.count || 0) : 0;
      }),
      backgroundColor: colors[statut]
    }));

    this.agenceChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: agences,
        datasets
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom'
          }
        },
        scales: {
          x: {
            stacked: true
          },
          y: {
            stacked: true,
            beginAtZero: true
          }
        }
      }
    });
  }

  applyFilters(): void {
    // Cette méthode sera appelée quand les filtres changent
    console.log('Filtres appliqués:', {
      statut: this.selectedStatut,
      marque: this.selectedMarque,
      agence: this.selectedAgence
    });
    // TODO: Recharger les données avec les filtres
  }

  exportData(): void {
    // TODO: Implémenter l'export des données
    alert('Export des données TPE...');
  }

  get tauxDisponibilite(): number {
    if (!this.stats || this.stats.totalTPE === 0) return 0;
    return ((this.stats.tpeDisponibles + this.stats.tpeAffectes) / this.stats.totalTPE * 100);
  }

  get tauxAffectation(): number {
    if (!this.stats || this.stats.totalTPE === 0) return 0;
    return (this.stats.tpeAffectes / this.stats.totalTPE * 100);
  }

  get tauxPanne(): number {
    if (!this.stats || this.stats.totalTPE === 0) return 0;
    return (this.stats.tpeEnPanne / this.stats.totalTPE * 100);
  }
}
