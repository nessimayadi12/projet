import { Component, OnInit } from '@angular/core';
import { DashboardService } from '../../services/dashboard.service';
import { Chart } from 'chart.js/auto';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-dashboard-demandes',
  templateUrl: './dashboard-demandes.component.html',
  styleUrls: ['./dashboard-demandes.component.css']
})
export class DashboardDemandesComponent implements OnInit {
  stats: any = null;
  demandesStatutData: any[] = [];
  evolutionData: any[] = [];
  performanceData: any = null;
  loading = true;
  error: string | null = null;

  // Charts
  statutDemandesChart: any;
  evolutionChart: any;
  delaiChart: any;
  funnelChart: any;

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    forkJoin({
      stats: this.dashboardService.getStats(),
      statuts: this.dashboardService.getDemandesParStatut(),
      evolution: this.dashboardService.getEvolutionMensuelle(),
      performance: this.dashboardService.getPerformanceDemandes()
    }).subscribe({
      next: ({ stats, statuts, evolution, performance }) => {
        this.stats = stats;
        this.demandesStatutData = statuts || [];
        this.evolutionData = evolution || [];
        this.performanceData = performance || {};
        this.loading = false;
        this.initCharts();
      },
      error: (err) => {
        console.error('Erreur chargement stats demandes:', err);
        this.error = 'Impossible de charger les statistiques';
        this.loading = false;
      }
    });
  }

  initCharts(): void {
    setTimeout(() => {
      this.createStatutDemandesChart();
      this.createEvolutionChart();
      this.createDelaiChart();
      this.createFunnelChart();
    }, 100);
  }

  createStatutDemandesChart(): void {
    const ctx = document.getElementById('statutDemandesChart') as HTMLCanvasElement;
    if (!ctx || !this.stats) return;

    if (this.statutDemandesChart) {
      this.statutDemandesChart.destroy();
    }

    const orderedStatuses = ['NOUVELLE', 'EN_COURS', 'VALIDEE_MONETIQUE', 'AFFECTEE', 'CLOTUREE', 'REJETEE'];
    const countsByStatus = orderedStatuses.map((status) => {
      const match = this.demandesStatutData.find((item) => item.statut === status);
      return match ? Number(match.count || 0) : 0;
    });

    this.statutDemandesChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: orderedStatuses,
        datasets: [{
          label: 'Nombre de demandes',
          data: countsByStatus,
          backgroundColor: [
            '#ffc107', // NOUVELLE - Orange
            '#17a2b8', // EN_COURS - Cyan
            '#28a745', // VALIDEE - Vert
            '#007bff', // APPROUVEE - Bleu
            '#6f42c1', // AFFECTEE - Violet
            '#28a745'  // CLOTUREE - Vert foncé
          ],
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
            beginAtZero: true
          }
        }
      }
    });
  }

  createEvolutionChart(): void {
    const ctx = document.getElementById('evolutionDemandesChart') as HTMLCanvasElement;
    if (!ctx) return;

    if (this.evolutionChart) {
      this.evolutionChart.destroy();
    }

    const mois = this.evolutionData.map((item) => item.mois);
    const demandes = this.evolutionData.map((item) => Number(item.demandes || 0));
    const pannes = this.evolutionData.map((item) => Number(item.pannes || 0));
    
    this.evolutionChart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: mois,
        datasets: [
          {
            label: 'Demandes',
            data: demandes,
            borderColor: '#ffc107',
            backgroundColor: 'rgba(255, 193, 7, 0.1)',
            tension: 0.4,
            fill: true
          },
          {
            label: 'Pannes',
            data: pannes,
            borderColor: '#28a745',
            backgroundColor: 'rgba(40, 167, 69, 0.1)',
            tension: 0.4,
            fill: true
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

  createDelaiChart(): void {
    const ctx = document.getElementById('delaiChart') as HTMLCanvasElement;
    if (!ctx) return;

    if (this.delaiChart) {
      this.delaiChart.destroy();
    }

    const totalDemandes = Number(this.performanceData?.totalDemandes || this.stats?.demandesEnAttente || 0);
    const demandesTraitees = Number(this.performanceData?.demandesTraitees || this.stats?.demandesEnCours || 0);
    const demandesRestantes = Math.max(totalDemandes - demandesTraitees, 0);

    this.delaiChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: ['Traitées', 'En attente'],
        datasets: [{
          label: 'Demandes',
          data: [demandesTraitees, demandesRestantes],
          backgroundColor: [
            '#28a745',
            '#ffc107'
          ],
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom'
          }
        }
      }
    });
  }

  createFunnelChart(): void {
    const ctx = document.getElementById('funnelChart') as HTMLCanvasElement;
    if (!ctx) return;

    if (this.funnelChart) {
      this.funnelChart.destroy();
    }

    const orderedStatuses = ['NOUVELLE', 'EN_COURS', 'VALIDEE_MONETIQUE', 'AFFECTEE', 'CLOTUREE', 'REJETEE'];
    const values = orderedStatuses.map((status) => {
      const match = this.demandesStatutData.find((item) => item.statut === status);
      return match ? Number(match.count || 0) : 0;
    });

    this.funnelChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: orderedStatuses,
        datasets: [{
          label: 'Nombre de demandes',
          data: values,
          backgroundColor: [
            '#ffc107',
            '#17a2b8',
            '#28a745',
            '#007bff',
            '#6f42c1',
            '#28a745'
          ],
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            callbacks: {
              label: function(context) {
                const value = context.parsed.y;
                const total = values.reduce((sum, current) => sum + current, 0) || 1;
                const percentage = ((value / total) * 100).toFixed(1);
                return `${value} demandes (${percentage}% du total)`;
              }
            }
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

  get delaiMoyenJours(): number {
    if (!this.stats || !this.stats.delaiMoyenTraitementHeures) return 0;
    return (this.stats.delaiMoyenTraitementHeures / 24);
  }

  get tauxConversion(): number {
    const total = Number(this.performanceData?.totalDemandes || 0);
    const taux = this.performanceData?.tauxConversion;
    if (taux !== undefined && taux !== null) return Number(taux);
    const traitees = Number(this.performanceData?.demandesConverties || 0);
    if (total === 0) return 0;
    return (traitees / total) * 100;
  }

  get demandesEnRetard(): number {
    if (this.performanceData?.demandesEnRetard !== undefined && this.performanceData?.demandesEnRetard !== null) {
      return Number(this.performanceData.demandesEnRetard || 0);
    }
    return this.demandesStatutData
      .filter((item) => ['NOUVELLE', 'EN_COURS', 'VALIDEE_MONETIQUE'].includes(item.statut))
      .reduce((sum, item) => sum + Number(item.count || 0), 0);
  }

  get demandesClotureesCeMois(): number {
    return Number(this.performanceData?.demandesClotureesCeMois || 0);
  }

  get slaRespect(): number {
    return Number(this.performanceData?.slaRespect || 0);
  }

  get demandesTraitees(): number {
    return Number(this.performanceData?.demandesTraitees || 0);
  }

  get currentMonthLabel(): string {
    return new Date().toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' });
  }

  exportData(): void {
    alert('Export des données demandes...');
  }
}
