import { Component, OnInit } from '@angular/core';
import { DashboardService } from '../../services/dashboard.service';
import { Chart } from 'chart.js/auto';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-dashboard-pannes',
  templateUrl: './dashboard-pannes.component.html',
  styleUrls: ['./dashboard-pannes.component.css']
})
export class DashboardPannesComponent implements OnInit {
  stats: any = null;
  evolutionData: any[] = [];
  typePanneData: any[] = [];
  paretoData: any[] = [];
  heatmapData: any[] = [];
  loading = true;
  error: string | null = null;

  // Charts
  evolutionPannesChart: any;
  typePanneChart: any;
  paretoChart: any;
  heatmapChart: any;

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    forkJoin({
      stats: this.dashboardService.getStats(),
      evolution: this.dashboardService.getEvolutionMensuelle(),
      types: this.dashboardService.getPannesParType(),
      pareto: this.dashboardService.getTopPannes(),
      heatmap: this.dashboardService.getHeatmapPannes()
    }).subscribe({
      next: (result: { stats: any; evolution: any[]; types: any[]; pareto: any[]; heatmap: any[] }) => {
        const { stats, evolution, types, pareto, heatmap } = result;
        this.stats = stats;
        this.evolutionData = evolution || [];
        this.typePanneData = types || [];
        this.paretoData = pareto || [];
        this.heatmapData = heatmap || [];
        this.loading = false;
        this.initCharts();
      },
      error: (err) => {
        console.error('Erreur chargement stats pannes:', err);
        this.error = 'Impossible de charger les statistiques';
        this.loading = false;
      }
    });
  }

  initCharts(): void {
    setTimeout(() => {
      this.createEvolutionPannesChart();
      this.createTypePanneChart();
      this.createParetoChart();
      this.createHeatmapChart();
    }, 100);
  }

  createEvolutionPannesChart(): void {
    const ctx = document.getElementById('evolutionPannesChart') as HTMLCanvasElement;
    if (!ctx) return;

    if (this.evolutionPannesChart) {
      this.evolutionPannesChart.destroy();
    }

    const mois = this.evolutionData.map((item) => item.mois);
    const pannes = this.evolutionData.map((item) => Number(item.pannes || 0));
    
    this.evolutionPannesChart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: mois,
        datasets: [
          {
            label: 'Pannes',
            data: pannes,
            borderColor: '#dc3545',
            backgroundColor: 'rgba(220, 53, 69, 0.1)',
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

  createTypePanneChart(): void {
    const ctx = document.getElementById('typePanneChart') as HTMLCanvasElement;
    if (!ctx) return;

    if (this.typePanneChart) {
      this.typePanneChart.destroy();
    }

    const labels = this.typePanneData.map((item) => item.type || 'Inconnu');
    const values = this.typePanneData.map((item) => Number(item.count || 0));

    this.typePanneChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels,
        datasets: [{
          data: values,
          backgroundColor: [
            '#dc3545', // Rouge
            '#ffc107', // Orange
            '#17a2b8', // Cyan
            '#6c757d'  // Gris
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
            position: 'bottom'
          },
          tooltip: {
            callbacks: {
              label: function(context) {
                const value = context.parsed || 0;
                const total = (context.dataset.data as number[]).reduce((sum, current) => sum + Number(current || 0), 0) || 1;
                const percentage = ((Number(value) / total) * 100).toFixed(1);
                return `${context.label}: ${value} panne(s) (${percentage}%)`;
              }
            }
          }
        }
      }
    });
  }

  createParetoChart(): void {
    const ctx = document.getElementById('paretoChart') as HTMLCanvasElement;
    if (!ctx) return;

    if (this.paretoChart) {
      this.paretoChart.destroy();
    }

    const tpes = this.paretoData.map((item) => item.type || 'Inconnu');
    const pannes = this.paretoData.map((item) => Number(item.count || 0));
    
    // Calculer pourcentage cumulé
    const total = pannes.reduce((a, b) => a + b, 0);
    let cumul = 0;
    const cumulPourcentage = pannes.map(p => {
      cumul += p;
      return total > 0 ? (cumul / total * 100) : 0;
    });

    this.paretoChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: tpes,
        datasets: [
          {
            type: 'bar',
            label: 'Nombre de pannes',
            data: pannes,
            backgroundColor: '#dc3545',
            borderColor: '#c82333',
            borderWidth: 1,
            yAxisID: 'y'
          },
          {
            type: 'line',
            label: '% Cumulé',
            data: cumulPourcentage,
            borderColor: '#28a745',
            backgroundColor: 'rgba(40, 167, 69, 0.1)',
            tension: 0.4,
            yAxisID: 'y1',
            borderWidth: 2
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom'
          },
          tooltip: {
            mode: 'index',
            intersect: false
          }
        },
        scales: {
          y: {
            type: 'linear',
            display: true,
            position: 'left',
            beginAtZero: true,
            title: {
              display: true,
              text: 'Nombre de pannes'
            }
          },
          y1: {
            type: 'linear',
            display: true,
            position: 'right',
            beginAtZero: true,
            max: 100,
            title: {
              display: true,
              text: 'Pourcentage cumulé (%)'
            },
            grid: {
              drawOnChartArea: false
            }
          }
        }
      }
    });
  }

  createHeatmapChart(): void {
    const ctx = document.getElementById('heatmapChart') as HTMLCanvasElement;
    if (!ctx) return;

    if (this.heatmapChart) {
      this.heatmapChart.destroy();
    }

    const jours = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
    const plages = ['00h-06h', '06h-12h', '12h-18h', '18h-24h'];
    const dayIndexToLabel: Record<number, string> = {
      1: 'Dim',
      2: 'Lun',
      3: 'Mar',
      4: 'Mer',
      5: 'Jeu',
      6: 'Ven',
      7: 'Sam'
    };

    const periodOrder = ['00h-06h', '06h-12h', '12h-18h', '18h-24h'];
    const matrix: Record<string, Record<string, number>> = {};
    jours.forEach((day) => {
      matrix[day] = {};
      periodOrder.forEach((period) => {
        matrix[day][period] = 0;
      });
    });

    (this.heatmapData || []).forEach((item) => {
      const day = dayIndexToLabel[Number(item.dayOfWeek)];
      const period = item.period;
      if (day && period in matrix[day]) {
        matrix[day][period] = Number(item.count || 0);
      }
    });
    
    this.heatmapChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: jours,
        datasets: [
          {
            label: '00h-06h',
            data: jours.map((day) => matrix[day]['00h-06h']),
            backgroundColor: 'rgba(23, 162, 184, 0.3)'
          },
          {
            label: '06h-12h',
            data: jours.map((day) => matrix[day]['06h-12h']),
            backgroundColor: 'rgba(255, 193, 7, 0.5)'
          },
          {
            label: '12h-18h',
            data: jours.map((day) => matrix[day]['12h-18h']),
            backgroundColor: 'rgba(220, 53, 69, 0.7)'
          },
          {
            label: '18h-24h',
            data: jours.map((day) => matrix[day]['18h-24h']),
            backgroundColor: 'rgba(0, 123, 255, 0.5)'
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
          x: {
            stacked: true
          },
          y: {
            stacked: true,
            beginAtZero: true,
            title: {
              display: true,
              text: 'Nombre de pannes'
            }
          }
        }
      }
    });
  }

  get tauxResolution(): number {
    if (!this.stats) return 0;
    const total = (this.stats.pannesEnCours || 0) + (this.stats.pannesResoluesCeMois || 0);
    if (total === 0) return 0;
    return ((this.stats.pannesResoluesCeMois || 0) / total * 100);
  }

  get peakHeatmapLabel(): string {
    if (!this.heatmapData || this.heatmapData.length === 0) {
      return 'aucune donnée';
    }

    const dayIndexToLabel: Record<number, string> = {
      1: 'Dimanche',
      2: 'Lundi',
      3: 'Mardi',
      4: 'Mercredi',
      5: 'Jeudi',
      6: 'Vendredi',
      7: 'Samedi'
    };

    const peak = this.heatmapData.reduce((best, current) =>
      Number(current.count || 0) > Number(best.count || 0) ? current : best
    );
    const count = Number(peak.count || 0);
    if (count === 0) return 'aucune donnée';

    const day = dayIndexToLabel[Number(peak.dayOfWeek)] || 'Jour inconnu';
    return `${day} ${peak.period} (${count} panne(s))`;
  }

  exportData(): void {
    alert('Export des données pannes...');
  }
}
