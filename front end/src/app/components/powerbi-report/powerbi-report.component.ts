import { Component, OnInit, OnDestroy, ViewChild, ElementRef, Input } from '@angular/core';
import { PowerBIService } from '../../services/powerbi.service';
import { PowerBIConfig } from '../../models/powerbi.model';
import * as powerbi from 'powerbi-client';

@Component({
  selector: 'app-powerbi-report',
  templateUrl: './powerbi-report.component.html',
  styleUrls: ['./powerbi-report.component.css']
})
export class PowerbiReportComponent implements OnInit, OnDestroy {
  @ViewChild('reportContainer', { static: true }) reportContainer!: ElementRef;
  
  @Input() reportId: string = '';
  @Input() embedUrl: string = '';
  @Input() showFilters: boolean = true;
  @Input() showNav: boolean = true;
  
  loading = true;
  error: string | null = null;
  private embeddedReport: powerbi.Embed | null = null;

  constructor(private powerBIService: PowerBIService) { }

  ngOnInit(): void {
    if (this.reportId && this.embedUrl) {
      this.loadReport();
    } else {
      this.error = 'Configuration du rapport Power BI manquante';
      this.loading = false;
    }
  }

  ngOnDestroy(): void {
    // Nettoyer le rapport embedé
    if (this.reportContainer && this.reportContainer.nativeElement) {
      this.powerBIService.reset(this.reportContainer.nativeElement);
    }
  }

  loadReport(): void {
    this.loading = true;
    this.error = null;

    // Récupérer le token d'embed depuis le backend
    this.powerBIService.getEmbedToken(this.reportId).subscribe({
      next: (tokenData) => {
        // Configuration de l'embed
        const config: PowerBIConfig = {
          type: 'report',
          id: this.reportId,
          embedUrl: this.embedUrl,
          accessToken: tokenData.token,
          tokenType: 'Embed',
          permissions: 'Read',
          viewMode: 'View',
          settings: {
            filterPaneEnabled: this.showFilters,
            navContentPaneEnabled: this.showNav,
            layoutType: 'Master',
            background: 'Default'
          }
        };

        // Embed le rapport
        try {
          this.embeddedReport = this.powerBIService.embedReport(
            this.reportContainer.nativeElement,
            config
          );
          
          // Écouter l'événement de chargement
          this.embeddedReport.on('loaded', () => {
            this.loading = false;
          });

          // Écouter les erreurs
          this.embeddedReport.on('error', (event: any) => {
            this.error = 'Erreur lors du chargement du rapport Power BI';
            this.loading = false;
            console.error('Power BI error:', event.detail);
          });

        } catch (err) {
          this.error = 'Erreur lors de l\'initialisation du rapport Power BI';
          this.loading = false;
          console.error('Embed error:', err);
        }
      },
      error: (err) => {
        console.error('Erreur lors de la récupération du token:', err);
        this.error = 'Impossible de charger le rapport Power BI. Vérifiez votre configuration.';
        this.loading = false;
      }
    });
  }

  refresh(): void {
    if (this.embeddedReport) {
      (this.embeddedReport as powerbi.Report).refresh();
    }
  }

  fullscreen(): void {
    if (this.embeddedReport) {
      (this.embeddedReport as powerbi.Report).fullscreen();
    }
  }
}
