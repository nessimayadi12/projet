import { Component, OnInit } from '@angular/core';
import { DashboardService } from '../../services/dashboard.service';
import { Chart } from 'chart.js/auto';

@Component({
  selector: 'app-dashboard-pannes',
  templateUrl: './dashboard-pannes.component.html',
  styleUrls: ['./dashboard-pannes.component.css']
})
export class DashboardPannesComponent implements OnInit {
  stats: any = null;
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
    this.dashboardService.getStats().subscribe({
      next: (data) => {
        this.stats = data;
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

    const mois = ['Sep', 'Oct', 'Nov', 'Déc', 'Jan', 'Fév'];
    
    this.evolutionPannesChart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: mois,
        datasets: [
          {
            label: 'Pannes Matérielles',
            data: [15, 18, 12, 14, 11, 13],
            borderColor: '#dc3545',
            backgroundColor: 'rgba(220, 53, 69, 0.1)',
            tension: 0.4,
            fill: true
          },
          {
            label: 'Pannes Logicielles',
            data: [8, 10, 7, 9, 6, 8],
            borderColor: '#ffc107',
            backgroundColor: 'rgba(255, 193, 7, 0.1)',
            tension: 0.4,
            fill: true
          },
          {
            label: 'Pannes Réseau',
            data: [5, 7, 4, 6, 5, 6],
            borderColor: '#17a2b8',
            backgroundColor: 'rgba(23, 162, 184, 0.1)',
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

    this.typePanneChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: ['Matériel', 'Logiciel', 'Réseau', 'Autre'],
        datasets: [{
          data: [45, 28, 18, 9],
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
                return `${context.label}: ${value}%`;
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

    // Top 10 TPE problématiques
    const tpes = ['TPE-123', 'TPE-456', 'TPE-789', 'TPE-234', 'TPE-567', 
                  'TPE-890', 'TPE-345', 'TPE-678', 'TPE-901', 'TPE-432'];
    const pannes = [15, 12, 11, 9, 8, 7, 6, 5, 4, 3];
    
    // Calculer pourcentage cumulé
    const total = pannes.reduce((a, b) => a + b, 0);
    let cumul = 0;
    const cumulPourcentage = pannes.map(p => {
      cumul += p;
      return (cumul / total * 100);
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

    // Simulation de carte thermique par plage horaire
    const jours = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
    const plages = ['00h-06h', '06h-12h', '12h-18h', '18h-24h'];
    
    // Données simulées (en réalité, ce serait une vraie heatmap)
    this.heatmapChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: jours,
        datasets: [
          {
            label: '00h-06h',
            data: [1, 0, 1, 1, 0, 0, 0],
            backgroundColor: 'rgba(23, 162, 184, 0.3)'
          },
          {
            label: '06h-12h',
            data: [3, 4, 3, 4, 5, 2, 1],
            backgroundColor: 'rgba(255, 193, 7, 0.5)'
          },
          {
            label: '12h-18h',
            data: [6, 5, 4, 6, 8, 3, 2],
            backgroundColor: 'rgba(220, 53, 69, 0.7)'
          },
          {
            label: '18h-24h',
            data: [3, 2, 3, 2, 3, 2, 1],
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

  exportData(): void {
    alert('Export des données pannes...');
  }
}
