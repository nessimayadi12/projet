import { Component, OnInit } from '@angular/core';
import { DashboardService } from '../services/dashboard.service';
import { DashboardStats } from '../models/dashboard.model';
import { environment } from '../../environments/environment';
import { ChartData, ChartOptions } from 'chart.js';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  stats: DashboardStats | null = null;
  loading = true;
  error: string | null = null;

  tpeStatutChartData: ChartData<'doughnut', number[], string> = {
    labels: ['Disponibles', 'Affectés', 'En panne', 'Maintenance', 'Hors service'],
    datasets: [{
      data: [0, 0, 0, 0, 0],
      backgroundColor: ['#22c55e', '#3b82f6', '#ef4444', '#f59e0b', '#64748b'],
      hoverBackgroundColor: ['#16a34a', '#2563eb', '#dc2626', '#d97706', '#475569'],
      borderColor: '#ffffff',
      borderWidth: 2,
      hoverOffset: 8
    }]
  };

  marqueChartData: ChartData<'bar', number[], string> = {
    labels: [],
    datasets: [{
      label: 'Nombre de TPE',
      data: [],
      backgroundColor: '#8b5cf6',
      hoverBackgroundColor: '#7c3aed',
      borderRadius: 6,
      borderSkipped: false
    }]
  };

  pannesChartData: ChartData<'doughnut', number[], string> = {
    labels: ['En cours', 'Résolues'],
    datasets: [{
      data: [0, 0],
      backgroundColor: ['#ef4444', '#22c55e'],
      hoverBackgroundColor: ['#dc2626', '#16a34a'],
      borderColor: '#ffffff',
      borderWidth: 2,
      hoverOffset: 8
    }]
  };

  readonly doughnutChartOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '58%',
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          color: '#ffffff',
          usePointStyle: true,
          padding: 18,
          font: {
            size: 13,
            weight: 600
          }
        }
      },
      tooltip: {
        enabled: true,
        backgroundColor: 'rgba(15, 23, 42, 0.95)',
        titleColor: '#ffffff',
        bodyColor: '#ffffff',
        padding: 12,
        callbacks: {
          label: (context) => {
            const value = Number(context.raw) || 0;
            const values = context.dataset.data.map(Number);
            const total = values.reduce((sum, item) => sum + item, 0);
            const percentage = total ? Math.round((value / total) * 100) : 0;
            return context.label + ': ' + value + ' (' + percentage + '%)';
          }
        }
      }
    }
  };

  readonly barChartOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: {
      intersect: false,
      mode: 'index'
    },
    plugins: {
      legend: {
        display: false
      },
      tooltip: {
        enabled: true,
        backgroundColor: 'rgba(15, 23, 42, 0.95)',
        titleColor: '#ffffff',
        bodyColor: '#ffffff',
        padding: 12
      }
    },
    scales: {
      x: {
        grid: {
          display: false
        },
        border: {
          color: 'rgba(255, 255, 255, 0.65)'
        },
        ticks: {
          color: '#ffffff',
          font: {
            size: 13,
            weight: 600
          }
        }
      },
      y: {
        beginAtZero: true,
        border: {
          color: 'rgba(255, 255, 255, 0.65)'
        },
        grid: {
          color: 'rgba(255, 255, 255, 0.18)'
        },
        ticks: {
          color: '#ffffff',
          precision: 0,
          font: {
            size: 13,
            weight: 600
          }
        }
      }
    }
  };

  // Configuration Power BI depuis environment
  powerBIEnabled = environment.powerBI?.enabled || false;
  powerBIReportId = environment.powerBI?.reportId || '';
  powerBIEmbedUrl = environment.powerBI?.embedUrl || '';
  powerBIPublicUrl = environment.powerBI?.publicUrl || '';

  constructor(private dashboardService: DashboardService) { }

  ngOnInit() {
    this.loadStats();
  }

  loadStats(): void {
    this.loading = true;
    this.dashboardService.getStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.loading = false;
        this.updateChartData();
      },
      error: (err) => {
        console.error('Erreur lors du chargement des statistiques:', err);
        this.error = 'Impossible de charger les statistiques';
        this.loading = false;
      }
    });
  }

  private updateChartData(): void {
    if (!this.stats) return;

    this.tpeStatutChartData = {
      ...this.tpeStatutChartData,
      datasets: [{
        ...this.tpeStatutChartData.datasets[0],
        data: [
          this.stats.tpeDisponibles,
          this.stats.tpeAffectes,
          this.stats.tpeEnPanne,
          this.stats.tpeEnMaintenance,
          this.stats.tpeHorsService
        ]
      }]
    };

    const repartitionParMarque = this.stats.repartitionParMarque || {};
    this.marqueChartData = {
      ...this.marqueChartData,
      labels: Object.keys(repartitionParMarque),
      datasets: [{
        ...this.marqueChartData.datasets[0],
        data: Object.values(repartitionParMarque)
      }]
    };

    this.pannesChartData = {
      ...this.pannesChartData,
      datasets: [{
        ...this.pannesChartData.datasets[0],
        data: [this.stats.pannesEnCours || this.stats.tpeEnPanne, this.stats.pannesResoluesCeMois]
      }]
    };
  }

  getTauxDisponibilite(): number {
    if (!this.stats) return 0;
    const total = this.stats.totalTPE;
    if (total === 0) return 0;
    return Math.round((this.stats.tpeDisponibles / total) * 100);
  }

  getTauxPannes(): number {
    if (!this.stats) return 0;
    const total = this.stats.totalTPE;
    if (total === 0) return 0;
    return Math.round((this.stats.tpeEnPanne / total) * 100);
  }

  get tauxDisponibiliteFormatted(): string {
    return this.stats ? this.stats.tauxDisponibilite.toFixed(1) : '0';
  }

  get tauxPanneFormatted(): string {
    return this.stats ? this.stats.tauxPanne.toFixed(1) : '0';
  }

  get mttrFormatted(): string {
    return this.stats ? this.stats.mttr.toFixed(1) : '0';
  }

  get delaiMoyenFormatted(): string {
    return this.stats ? this.stats.delaiMoyenTraitementHeures.toFixed(1) : '0';
  }
}
