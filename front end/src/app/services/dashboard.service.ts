import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DashboardStats } from '../models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) { }

  // Statistiques globales
  getStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/stats`);
  }
  // Demandes par statut
  getDemandesParStatut(): Observable<any> {
    return this.http.get(`${this.apiUrl}/demandes-statut`);
  }

  // Pannes par type
  getPannesParType(): Observable<any> {
    return this.http.get(`${this.apiUrl}/pannes-type`);
  }

  // Top pannes les plus fréquentes
  getTopPannes(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/top-pannes`);
  }

  // Évolution mensuelle
  getEvolutionMensuelle(): Observable<any> {
    return this.http.get(`${this.apiUrl}/evolution-mensuelle`);
  }
  // R\u00e9partition du parc par statut
  getRepartitionParStatut(): Observable<any> {
    return this.http.get(`${this.apiUrl}/repartition-statut`);
  }

  // R\u00e9partition par type (Physique/E-commerce)
  getRepartitionParType(): Observable<any> {
    return this.http.get(`${this.apiUrl}/repartition-type`);
  }

  // Pannes par p\u00e9riode
  getPannesParPeriode(periode: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/pannes-periode?periode=${periode}`);
  }

  // Performance traitement demandes
  getPerformanceDemandes(): Observable<any> {
    return this.http.get(`${this.apiUrl}/performance-demandes`);
  }

  // Top commer\u00e7ants
  getTopCommercants(limit: number = 10): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/top-commercants?limit=${limit}`);
  }

  // Alertes actives
  getAlertes(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/alertes`);
  }

  // \u00c9volution du parc dans le temps
  getEvolutionParc(dateDebut: string, dateFin: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/evolution?debut=${dateDebut}&fin=${dateFin}`);
  }

  // Statistiques pour Mon\u00e9tique
  getStatsMonetique(): Observable<any> {
    return this.http.get(`${this.apiUrl}/stats-monetique`);
  }

  // Statistiques pour Agence
  getStatsAgence(agenceId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/stats-agence/${agenceId}`);
  }

  // Évolution du parc TPE par statut
  getEvolutionTpe(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/evolution-tpe`);
  }

  // Statistiques du parc par agence et statut
  getStatistiquesParAgence(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/stats-par-agence`);
  }

  // Heatmap des pannes par jour et plage horaire
  getHeatmapPannes(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/heatmap-pannes`);
  }

  // Export rapport dashboard
  exportRapport(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/export`, { responseType: 'blob' });
  }
}
