import { Component, OnInit } from '@angular/core';
import { DashboardService } from '../../services/dashboard.service';
import { Chart } from 'chart.js/auto';

@Component({
  selector: 'app-dashboard-demandes',
  templateUrl: './dashboard-demandes.component.html',
  styleUrls: ['./dashboard-demandes.component.css']
})
export class DashboardDemandesComponent implements OnInit {
  stats: any = null;
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
    this.dashboardService.getStats().subscribe({
      next: (data) => {
        this.stats = data;
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

    // Données simulées pour les différents statuts de demandes
    const statutsData = {
      'NOUVELLE': this.stats.demandesNouvelles || 24,
      'EN_COURS': this.stats.demandesEnCours || 18,
      'VALIDEE': 42,
      'APPROUVEE': 35,
      'AFFECTEE': 28,
      'CLOTUREE': 156
    };

    this.statutDemandesChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: Object.keys(statutsData),
        datasets: [{
          label: 'Nombre de demandes',
          data: Object.values(statutsData),
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

    const mois = ['Sep', 'Oct', 'Nov', 'Déc', 'Jan', 'Fév'];
    
    this.evolutionChart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: mois,
        datasets: [
          {
            label: 'Nouvelles',
            data: [35, 42, 38, 45, 48, this.stats?.demandesNouvelles || 24],
            borderColor: '#ffc107',
            backgroundColor: 'rgba(255, 193, 7, 0.1)',
            tension: 0.4,
            fill: true
          },
          {
            label: 'Clôturées',
            data: [120, 135, 128, 142, 148, 156],
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

    // Délais par étape (en heures)
    this.delaiChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: ['Validation Initiale', 'Approbation Monétique', 'Affectation TPE', 'Clôture'],
        datasets: [{
          label: 'Délai moyen (heures)',
          data: [8, 12, 6, 10],
          backgroundColor: [
            '#007bff',
            '#17a2b8',
            '#28a745',
            '#6c757d'
          ],
          borderWidth: 1
        }]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          }
        },
        scales: {
          x: {
            beginAtZero: true,
            title: {
              display: true,
              text: 'Heures'
            }
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

    // Pipeline des demandes (entonnoir de conversion)
    this.funnelChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: ['NOUVELLE', 'EN_COURS', 'VALIDEE', 'APPROUVEE', 'AFFECTEE', 'CLOTUREE'],
        datasets: [{
          label: 'Nombre de demandes',
          data: [85, 65, 58, 45, 38, 32],
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
                const percentage = ((value / 85) * 100).toFixed(1);
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
    // Taux de conversion (demandes clôturées / demandes nouvelles)
    return 37.6; // À calculer depuis les vraies données
  }

  get demandesEnRetard(): number {
    // Demandes dépassant le SLA de 48h
    return 3; // À récupérer depuis le backend
  }

  exportData(): void {
    alert('Export des données demandes...');
  }
}
