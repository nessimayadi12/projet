import { Component, OnInit } from '@angular/core';
import { DashboardService } from '../services/dashboard.service';
import { DashboardStats } from '../models/dashboard.model';
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

  // Statistiques détaillées
  demandesParStatut: any[] = [];
  pannesParType: any[] = [];
  evolutionMensuelle: any[] = [];

  constructor(private dashboardService: DashboardService) { }

  ngOnInit() {
    this.loadStats();
    this.loadDemandesStats();
    this.loadPannesStats();
    this.loadEvolution();
  }

  loadStats(): void {
    this.loading = true;
    this.dashboardService.getStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.loading = false;
        setTimeout(() => {
          this.initCharts();
        }, 100);
      },
      error: (err) => {
        console.error('Erreur lors du chargement des statistiques:', err);
        this.error = 'Impossible de charger les statistiques';
        this.loading = false;
      }
    });
  }

  loadDemandesStats(): void {
    this.dashboardService.getDemandesParStatut().subscribe({
      next: (data) => {
        this.demandesParStatut = data;
        this.initDemandesChart();
      },
      error: (err) => console.error('Erreur stats demandes:', err)
    });
  }

  loadPannesStats(): void {
    this.dashboardService.getPannesParType().subscribe({
      next: (data) => {
        this.pannesParType = data;
        this.initPannesChart();
      },
      error: (err) => console.error('Erreur stats pannes:', err)
    });
  }

  loadEvolution(): void {
    this.dashboardService.getEvolutionMensuelle().subscribe({
      next: (data) => {
        this.evolutionMensuelle = data;
        this.initEvolutionChart();
      },
      error: (err) => console.error('Erreur évolution:', err)
    });
  }

  initCharts(): void {
    if (!this.stats) return;

    // Graphique de répartition par statut TPE
    const statutsData = {
      labels: ['Disponibles', 'Affectés', 'En Panne', 'Maintenance', 'Hors Service'],
      series: [
        this.stats.tpeDisponibles,
        this.stats.tpeAffectes,
        this.stats.tpeEnPanne,
        this.stats.tpeEnMaintenance,
        this.stats.tpeHorsService
      ]
    };

    new Chartist.Pie('#tpeStatutChart', statutsData, {
      labelInterpolationFnc: function(value) {
        return value;
      }
    });

    // Graphique de répartition par marque
    if (this.stats.repartitionParMarque) {
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
  }

  initDemandesChart(): void {
    if (this.demandesParStatut.length === 0) return;

    setTimeout(() => {
      const element = document.querySelector('#demandesChart');
      if (!element) {
        console.warn('Element #demandesChart not found');
        return;
      }

      const labels = this.demandesParStatut.map(d => d.statut);
      const series = this.demandesParStatut.map(d => d.count);

      new Chartist.Bar('#demandesChart', {
        labels: labels,
        series: [series]
      }, {
        distributeSeries: true,
        height: '250px'
      });
    }, 200);
  }

  initPannesChart(): void {
    if (this.pannesParType.length === 0) return;

    setTimeout(() => {
      const element = document.querySelector('#pannesChart');
      if (!element) {
        console.warn('Element #pannesChart not found');
        return;
      }

      const labels = this.pannesParType.map(p => p.type);
      const series = this.pannesParType.map(p => p.count);

      new Chartist.Pie('#pannesChart', {
        labels: labels,
        series: series
      });
    }, 200);
  }

  initEvolutionChart(): void {
    if (this.evolutionMensuelle.length === 0) return;

    setTimeout(() => {
      const element = document.querySelector('#evolutionChart');
      if (!element) {
        console.warn('Element #evolutionChart not found');
        return;
      }

      const labels = this.evolutionMensuelle.map(e => e.mois);
      const demandesSeries = this.evolutionMensuelle.map(e => e.demandes);
      const pannesSeries = this.evolutionMensuelle.map(e => e.pannes);

      new Chartist.Line('#evolutionChart', {
        labels: labels,
        series: [demandesSeries, pannesSeries]
      }, {
        fullWidth: true,
        chartPadding: {
          right: 40
        },
        height: '300px'
      });
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
