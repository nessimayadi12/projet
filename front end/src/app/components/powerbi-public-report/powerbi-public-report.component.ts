import { Component, Input, OnInit } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-powerbi-public-report',
  templateUrl: './powerbi-public-report.component.html',
  styleUrls: ['./powerbi-public-report.component.css']
})
export class PowerbiPublicReportComponent implements OnInit {
  @Input() publicUrl: string = '';
  @Input() width: string = '100%';
  @Input() height: string = '600px';
  
  safeUrl: SafeResourceUrl | null = null;
  loading = true;
  error: string | null = null;

  constructor(private sanitizer: DomSanitizer) { }

  ngOnInit(): void {
    if (this.publicUrl) {
      // Sanitize l'URL pour la sécurité
      this.safeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.publicUrl);
    } else {
      this.error = 'URL publique Power BI manquante';
      this.loading = false;
    }
  }

  onLoad(): void {
    this.loading = false;
  }

  onError(): void {
    this.error = 'Impossible de charger le rapport Power BI';
    this.loading = false;
  }
}
