import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PowerBIConfig, PowerBIEmbedToken, PowerBIReport } from '../models/powerbi.model';
import * as powerbi from 'powerbi-client';

@Injectable({
  providedIn: 'root'
})
export class PowerBIService {
  private apiUrl = `${environment.apiUrl}/powerbi`;
  private powerbiService: powerbi.service.Service;

  constructor(private http: HttpClient) {
    // Initialiser le service Power BI
    this.powerbiService = new powerbi.service.Service(
      powerbi.factories.hpmFactory,
      powerbi.factories.wpmpFactory,
      powerbi.factories.routerFactory
    );
  }

  /**
   * Récupère le token d'embed depuis le backend
   */
  getEmbedToken(reportId: string): Observable<PowerBIEmbedToken> {
    return this.http.get<PowerBIEmbedToken>(`${this.apiUrl}/token/${reportId}`);
  }

  /**
   * Récupère la liste des rapports disponibles
   */
  getReports(): Observable<PowerBIReport[]> {
    return this.http.get<PowerBIReport[]>(`${this.apiUrl}/reports`);
  }

  /**
   * Récupère les détails d'un rapport spécifique
   */
  getReport(reportId: string): Observable<PowerBIReport> {
    return this.http.get<PowerBIReport>(`${this.apiUrl}/reports/${reportId}`);
  }

  /**
   * Embed un rapport Power BI dans un élément HTML
   */
  embedReport(
    container: HTMLElement,
    config: PowerBIConfig
  ): powerbi.Embed {
    // Configuration par défaut
    const embedConfig: powerbi.IEmbedConfiguration = {
      type: config.type,
      id: config.id,
      embedUrl: config.embedUrl,
      accessToken: config.accessToken,
      tokenType: powerbi.models.TokenType[config.tokenType || 'Embed'],
      permissions: powerbi.models.Permissions[config.permissions || 'Read'],
      viewMode: powerbi.models.ViewMode[config.viewMode || 'View'],
      settings: {
        filterPaneEnabled: config.settings?.filterPaneEnabled ?? true,
        navContentPaneEnabled: config.settings?.navContentPaneEnabled ?? true,
        layoutType: powerbi.models.LayoutType[config.settings?.layoutType || 'Master'],
        background: powerbi.models.BackgroundType[config.settings?.background || 'Default']
      }
    };

    // Embed le rapport
    const report = this.powerbiService.embed(container, embedConfig);

    // Gérer l'événement de chargement
    report.on('loaded', () => {
      console.log('Power BI Report loaded successfully');
    });

    // Gérer les erreurs
    report.on('error', (event: any) => {
      console.error('Power BI Error:', event.detail);
    });

    return report;
  }

  /**
   * Réinitialise tous les rapports embedés
   */
  reset(container: HTMLElement): void {
    this.powerbiService.reset(container);
  }

  /**
   * Obtient le service Power BI
   */
  getService(): powerbi.service.Service {
    return this.powerbiService;
  }
}
