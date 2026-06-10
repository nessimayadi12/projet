import { Component, OnInit } from '@angular/core';
import { DashboardService } from '../services/dashboard.service';
import { DashboardStats } from '../models/dashboard.model';
import { environment } from '../../environments/environment';
import * as Chartist from 'chartist';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  stats: DashboardStats | null = null;
  loading = true;
  error: string | null = null;

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
        this.initCharts();
      },
      error: (err) => {
        console.error('Erreur lors du chargement des statistiques:', err);
        this.error = 'Impossible de charger les statistiques';
        this.loading = false;
      }
    });
  }

  initCharts(): void {
    if (!this.stats) return;

    // Vérifier que les éléments existent avant d'initialiser les graphiques
    setTimeout(() => {
      // Graphique de répartition par statut TPE
      const tpeStatutElement = document.querySelector('#tpeStatutChart');
      if (tpeStatutElement) {
        const statutsData = {
          labels: ['Disponibles', 'Affectés', 'En Panne', 'Maintenance', 'Hors Service'],
          series: [
            this.stats!.tpeDisponibles,
            this.stats!.tpeAffectes,
            this.stats!.tpeEnPanne,
            this.stats!.tpeEnMaintenance,
            this.stats!.tpeHorsService
          ]
        };

        new Chartist.Pie('#tpeStatutChart', statutsData, {
          labelInterpolationFnc: function(value) {
            return value;
          }
        });
      }

      // Graphique de répartition par marque
      const marqueElement = document.querySelector('#marqueChart');
      if (marqueElement && this.stats?.repartitionParMarque) {
        const marques = Object.keys(this.stats.repartitionParMarque);
        const values = Object.values(this.stats.repartitionParMarque);
        
        new Chartist.Bar('#marqueChart', {
          labels: marques,
          series: [values]
        }, {
          seriesBarDistance: 10,
          axisX: {
            showGrid: false
          },
          height: '250px'
        });
      }

      // Graphique des pannes (si l'élément existe)
      const pannesElement = document.querySelector('#pannesChart');
      if (pannesElement && this.stats) {
        new Chartist.Pie('#pannesChart', {
          labels: ['En cours', 'Résolues'],
          series: [this.stats.pannesEnCours || this.stats.tpeEnPanne, this.stats.pannesResoluesCeMois]
        }, {
          labelInterpolationFnc: function(value) {
            return value;
          }
        });
      }
    }, 200);
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
