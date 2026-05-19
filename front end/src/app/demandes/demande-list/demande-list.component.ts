import { Component, OnInit } from '@angular/core'; 
import { Router } from '@angular/router';
import { DemandeTPE, StatutDemande, TypeDemande, Urgence } from '../../models/demande-tpe.model';
import { DemandeService } from '../../services/demande.service';
import { AuthService } from '../../services/auth.service';
import { ScreenService } from '../../services/screen.service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DemandeValidationComponent } from '../demande-validation/demande-validation.component';
import { ExcelExportService } from '../../services/excel-export.service';
import { Observable } from 'rxjs';
import { Role } from '../../models/utilisateur.model';

@Component({
  selector: 'app-demande-list',
  templateUrl: './demande-list.component.html',
  styleUrls: ['./demande-list.component.css']
})
export class DemandeListComponent implements OnInit {
  demandes: DemandeTPE[] = [];
  demandesFiltrees: DemandeTPE[] = [];
  pagedDemandes: DemandeTPE[] = [];
  loading = false;
  detailLoading = false;
  selectedDemande: DemandeTPE | null = null;
  currentUserRole: string = '';
  currentUserId: number | null = null;

  // Permissions observables
  canCreateDemande$: Observable<boolean>;
  canEditDemande$: Observable<boolean>;
  canExportDemande$: Observable<boolean>;
  canAffecterTPE$: Observable<boolean>;

  // Filtres
  filtreStatut: string = 'TOUS';
  filtreType: string = 'TOUS';
  filtreUrgence: string = 'TOUS';
  rechercheText: string = '';
  page = 1;
  pageSize = 25;
  pageSizeOptions = [10, 25, 50, 100];

  // Options pour filtres
  statuts = ['TOUS', ...Object.values(StatutDemande)];
  types = ['TOUS', ...Object.values(TypeDemande)];
  urgences = ['TOUS', ...Object.values(Urgence)];

  displayedColumns: string[] = [
    'reference',
    'commercantNom',
    'typeDemande',
    'statut',
    'urgence',
    'createdAt',
    'actions'
  ];

  constructor(
    private demandeService: DemandeService,
    private authService: AuthService,
    private screenService: ScreenService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router,
    private excelExportService: ExcelExportService
  ) {
    const currentUser = this.authService.getCurrentUser();
    this.currentUserRole = currentUser?.role || '';
    this.currentUserId = currentUser?.id || null;
    
    // Initialiser les permissions
    this.canCreateDemande$ = this.screenService.hasPermission('CREER_DEMANDE', 'canCreate');
    this.canEditDemande$ = this.screenService.hasPermission('MODIFIER_DEMANDE', 'canEdit');
    this.canExportDemande$ = this.screenService.hasPermission('LISTE_DEMANDES', 'canExport');
    this.canAffecterTPE$ = this.screenService.hasPermission('AFFECTER_TPE', 'canView');
  }

  ngOnInit(): void {
    this.loadDemandes();
  }

  navigateToNew(): void {
    this.router.navigateByUrl('/demandes/new');
  }

  navigateToEdit(demande: DemandeTPE): void {
    if (!demande.id) {
      return;
    }
    this.router.navigate(['/demandes', demande.id, 'edit']);
  }

  canEditAfterAffectation(demande: DemandeTPE): boolean {
    if (demande.statut !== 'AFFECTEE') {
      return true;
    }
    return this.authService.hasAnyRole([Role.MONETIQUE, Role.ADMIN]);
  }

  canSaisirDonneesMonetiques(demande: DemandeTPE): boolean {
    return demande.statut === StatutDemande.NOUVELLE &&
      this.authService.hasAnyRole([Role.INPUTER, Role.ADMIN]);
  }

  canValiderDonneesMonetiques(demande: DemandeTPE): boolean {
    return demande.statut === StatutDemande.EN_COURS &&
      this.authService.hasAnyRole([Role.AUTHORIZER, Role.ADMIN]) &&
      !!this.currentUserId &&
      (!demande.inputerId || demande.inputerId !== this.currentUserId);
  }

  canRejeterDonneesMonetiques(demande: DemandeTPE): boolean {
    return this.canValiderDonneesMonetiques(demande);
  }

  loadDemandes(): void {
    this.loading = true;
    this.demandeService.getAllDemandes().subscribe({
      next: (data) => {
        this.demandes = Array.isArray(data) ? data : [];
        this.appliquerFiltres();
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur chargement demandes', error);
        this.showNotification('Erreur lors du chargement des demandes', 'error');
        this.loading = false;
      }
    });
  }

  appliquerFiltres(): void {
    this.demandesFiltrees = this.demandes.filter(demande => {
      const matchStatut = this.filtreStatut === 'TOUS' || demande.statut === this.filtreStatut;
      const matchType = this.filtreType === 'TOUS' || demande.typeDemande === this.filtreType;
      const matchUrgence = this.filtreUrgence === 'TOUS' || demande.urgence === this.filtreUrgence;
      const query = (this.rechercheText || '').toLowerCase();
      const matchRecherche = !this.rechercheText || 
        (demande.reference || '').toLowerCase().includes(query) ||
        (demande.commercantNom || '').toLowerCase().includes(query);

      return matchStatut && matchType && matchUrgence && matchRecherche;
    });

    this.page = 1;
    this.updatePagedDemandes();
  }

  get totalPages(): number {
    const total = Math.ceil(this.demandesFiltrees.length / this.pageSize);
    return total > 0 ? total : 1;
  }

  onPageSizeChange(value: string): void {
    this.pageSize = Number(value);
    this.page = 1;
    this.updatePagedDemandes();
  }

  previousPage(): void {
    if (this.page > 1) {
      this.page--;
      this.updatePagedDemandes();
    }
  }

  nextPage(): void {
    if (this.page < this.totalPages) {
      this.page++;
      this.updatePagedDemandes();
    }
  }

  private updatePagedDemandes(): void {
    const startIndex = (this.page - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.pagedDemandes = this.demandesFiltrees.slice(startIndex, endIndex);
  }

  onFiltreChange(): void {
    this.appliquerFiltres();
  }

  validerDemande(demande: DemandeTPE): void {
    const dialogRef = this.dialog.open(DemandeValidationComponent, {
      width: '800px',
      data: { demande: demande },
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.showNotification('Demande validée avec succès', 'success');
        this.loadDemandes();
      }
    });
  }

  rejeterDemande(demande: DemandeTPE): void {
    const motif = prompt('Motif du rejet:');
    if (motif) {
      this.demandeService.rejeterDemande(demande.id!, motif).subscribe({
        next: () => {
          this.showNotification('Demande rejetée', 'success');
          this.loadDemandes();
        },
        error: (error) => {
          console.error('Erreur rejet demande', error);
          this.showNotification('Erreur lors du rejet', 'error');
        }
      });
    }
  }

  affecterTPE(demande: DemandeTPE): void {
    this.router.navigate(['/demandes', demande.id, 'affecter']);
  }

  afficherDetails(demande: DemandeTPE): void {
    this.selectedDemande = demande;

    if (!demande.id) {
      return;
    }

    this.detailLoading = true;
    this.demandeService.getDemandeById(demande.id).subscribe({
      next: (detail) => {
        this.selectedDemande = {
          ...demande,
          ...detail
        };
        this.detailLoading = false;
      },
      error: (error) => {
        console.error('Erreur chargement detail demande', error);
        this.detailLoading = false;
        this.showNotification('Impossible de charger le detail de la demande', 'error');
      }
    });
  }

  fermerDetails(): void {
    this.selectedDemande = null;
    this.detailLoading = false;
  }

  imprimerDemande(demande: DemandeTPE): void {
    // Ouvrir une nouvelle fenêtre pour l'impression
    const printWindow = window.open('', '_blank', 'width=800,height=600');
    if (!printWindow) {
      this.showNotification('Veuillez autoriser les pop-ups pour imprimer', 'error');
      return;
    }

    this.writePrintLoading(printWindow);

    if (!demande.id) {
      this.writePrintContent(printWindow, demande);
      return;
    }

    this.demandeService.getDemandeById(demande.id).subscribe({
      next: (detail) => {
        this.writePrintContent(printWindow, {
          ...demande,
          ...detail
        });
      },
      error: (error) => {
        console.error('Erreur chargement detail impression', error);
        this.writePrintContent(printWindow, demande);
        this.showNotification('Impression avec les informations deja chargees', 'info');
      }
    });
  }

  private generatePrintContent(demande: DemandeTPE): string {
    return this.generateFullPrintContent(demande);
    return `
      <!DOCTYPE html>
      <html>
      <head>
        <title>Demande TPE - ${demande.reference}</title>
        <style>
          @media print {
            @page { margin: 2cm; }
          }
          body {
            font-family: Arial, sans-serif;
            padding: 20px;
            color: #333;
          }
          .header {
            text-align: center;
            border-bottom: 3px solid #00695c;
            padding-bottom: 20px;
            margin-bottom: 30px;
          }
          .header h1 {
            color: #00695c;
            margin: 0;
          }
          .section {
            margin-bottom: 25px;
            page-break-inside: avoid;
          }
          .section-title {
            background-color: #00695c;
            color: white;
            padding: 8px 15px;
            font-size: 16px;
            font-weight: bold;
            margin-bottom: 15px;
          }
          .info-row {
            display: flex;
            padding: 8px 0;
            border-bottom: 1px solid #eee;
          }
          .info-label {
            font-weight: bold;
            width: 200px;
            color: #555;
          }
          .info-value {
            flex: 1;
          }
          .badge {
            display: inline-block;
            padding: 5px 10px;
            border-radius: 3px;
            font-weight: bold;
            color: white;
          }
          .badge-info { background-color: #17a2b8; }
          .badge-warning { background-color: #ffc107; color: #333; }
          .badge-primary { background-color: #007bff; }
          .badge-success { background-color: #28a745; }
          .badge-danger { background-color: #dc3545; }
          .footer {
            margin-top: 40px;
            text-align: center;
            font-size: 12px;
            color: #666;
            border-top: 1px solid #ddd;
            padding-top: 20px;
          }
        </style>
      </head>
      <body>
        <div class="header">
          <h1>BANK ABC</h1>
          <h2>Demande d'Affectation TPE</h2>
          <p><strong>Référence:</strong> ${demande.reference}</p>
        </div>

        <div class="section">
          <div class="section-title">Informations Générales</div>
          <div class="info-row">
            <div class="info-label">Type de demande:</div>
            <div class="info-value">${demande.typeDemande === 'PHYSIQUE' ? 'TPE Physique' : 'E-Commerce'}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Statut:</div>
            <div class="info-value"><span class="badge ${this.getStatutClass(demande.statut)}">${this.getStatutLabel(demande.statut)}</span></div>
          </div>
          <div class="info-row">
            <div class="info-label">Urgence:</div>
            <div class="info-value">${demande.urgence}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Date de création:</div>
            <div class="info-value">${new Date(demande.createdDate || demande.createdAt!).toLocaleDateString('fr-FR', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' })}</div>
          </div>
        </div>

        <div class="section">
          <div class="section-title">Informations du Commerçant</div>
          <div class="info-row">
            <div class="info-label">Raison Sociale:</div>
            <div class="info-value">${demande.raisonSociale || '-'}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Activité:</div>
            <div class="info-value">${demande.activite || '-'}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Adresse:</div>
            <div class="info-value">${demande.adresse || '-'}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Code Postal:</div>
            <div class="info-value">${demande.codePostal || '-'}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Téléphone:</div>
            <div class="info-value">${demande.telephone || '-'}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Email:</div>
            <div class="info-value">${demande.emailNotification || '-'}</div>
          </div>
          ${demande.typeDemande === 'PHYSIQUE' ? `
          <div class="info-row">
            <div class="info-label">N° Compte:</div>
            <div class="info-value">${demande.numeroCompte || '-'}</div>
          </div>
          ` : `
          <div class="info-row">
            <div class="info-label">RIB:</div>
            <div class="info-value">${demande.rib || '-'}</div>
          </div>
          <div class="info-row">
            <div class="info-label">URL Site Marchand:</div>
            <div class="info-value">${demande.urlSiteMarchand || '-'}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Webmaster:</div>
            <div class="info-value">${demande.webmaster || '-'}</div>
          </div>
          `}
        </div>

        ${demande.mcc ? `
        <div class="section">
          <div class="section-title">Informations Validation Monétique</div>
          <div class="info-row">
            <div class="info-label">MCC:</div>
            <div class="info-value">${demande.mcc}</div>
          </div>
          <div class="info-row">
            <div class="info-label">N° Terminal:</div>
            <div class="info-value">${demande.numeroTerminal || '-'}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Taux Commission:</div>
            <div class="info-value">${demande.tauxCommission || '-'}%</div>
          </div>
          <div class="info-row">
            <div class="info-label">Loyer:</div>
            <div class="info-value">${demande.loyer || '-'}</div>
          </div>
        </div>
        ` : ''}

        <div class="footer">
          <p>Document généré le ${new Date().toLocaleDateString('fr-FR')} à ${new Date().toLocaleTimeString('fr-FR')}</p>
          <p>© ${new Date().getFullYear()} Bank ABC - Tous droits réservés</p>
        </div>
      </body>
      </html>
    `;
  }

  private generateFullPrintContent(demande: DemandeTPE): string {
    const piecesJointes = demande.piecesJointes?.length
      ? demande.piecesJointes
          .map(piece => `<div class="attachment-item">${this.escapeHtml(piece)}</div>`)
          .join('')
      : '<div class="empty-value">Aucune piece jointe</div>';

    return `
      <!DOCTYPE html>
      <html>
      <head>
        <title>Demande TPE - ${this.escapeHtml(demande.reference || '')}</title>
        <style>
          @media print { @page { margin: 2cm; } }
          body { font-family: Arial, sans-serif; padding: 20px; color: #333; font-size: 13px; }
          .header { text-align: center; border-bottom: 3px solid #00695c; padding-bottom: 20px; margin-bottom: 30px; }
          .header h1 { color: #00695c; margin: 0; }
          .header h2 { margin: 8px 0; }
          .section { margin-bottom: 25px; page-break-inside: avoid; }
          .section-title { background-color: #00695c; color: white; padding: 8px 15px; font-size: 16px; font-weight: bold; margin-bottom: 15px; }
          .info-row { display: flex; gap: 16px; padding: 8px 0; border-bottom: 1px solid #eee; }
          .info-label { flex: 0 0 220px; font-weight: bold; color: #555; }
          .info-value { flex: 1; overflow-wrap: anywhere; }
          .badge { display: inline-block; padding: 5px 10px; border-radius: 3px; font-weight: bold; color: white; }
          .badge-info { background-color: #17a2b8; }
          .badge-warning { background-color: #ffc107; color: #333; }
          .badge-primary { background-color: #007bff; }
          .badge-success { background-color: #28a745; }
          .badge-secondary { background-color: #757575; }
          .badge-danger { background-color: #dc3545; }
          .badge-default { background-color: #607d8b; }
          .attachment-item, .empty-value { padding: 8px 0; border-bottom: 1px solid #eee; }
          .footer { margin-top: 40px; text-align: center; font-size: 12px; color: #666; border-top: 1px solid #ddd; padding-top: 20px; }
        </style>
      </head>
      <body>
        <div class="header">
          <h1>BANK ABC</h1>
          <h2>Demande TPE</h2>
          <p><strong>Reference:</strong> ${this.escapeHtml(demande.reference || '-')}</p>
        </div>

        <div class="section">
          <div class="section-title">Informations generales</div>
          ${this.generatePrintRows([
            ['ID demande', demande.id],
            ['Reference', demande.reference],
            ['Type de demande', demande.typeDemande === 'PHYSIQUE' ? 'TPE Physique' : 'E-Commerce'],
            ['Statut', `<span class="badge ${this.getStatutClass(demande.statut)}">${this.escapeHtml(this.getStatutLabel(demande.statut))}</span>`],
            ['Urgence', demande.urgence],
            ['Description', demande.description],
            ['Date creation', this.formatPrintDate(demande.createdDate || demande.createdAt)],
            ['Derniere modification', this.formatPrintDate(demande.lastModifiedDate || demande.updatedAt)]
          ])}
        </div>

        <div class="section">
          <div class="section-title">Informations commercant</div>
          ${this.generatePrintRows([
            ['ID commercant', demande.commercantId],
            ['Nom commercant', demande.commercantNom],
            ['Raison sociale', demande.raisonSociale],
            ['Activite', demande.activite],
            ['Numero compte', demande.numeroCompte],
            ['Code agence', demande.codeAgence],
            ['Adresse', demande.adresse],
            ['Localite', demande.localite],
            ['Code postal', demande.codePostal],
            ['Telephone', demande.telephone],
            ['Email notification', demande.emailNotification],
            ['Fichier RNE', demande.rneFilePath || demande.rneFile]
          ])}
        </div>

        <div class="section">
          <div class="section-title">Validation monetique</div>
          ${this.generatePrintRows([
            ['MCC', demande.mcc],
            ['Taux commission', this.formatPercent(demande.tauxCommission)],
            ['Taux commission inter', this.formatPercent(demande.tauxCommissionInter)],
            ['Loyer', demande.loyer],
            ['Serie TPE', demande.serieTpe],
            ['Numero terminal', demande.numeroTerminal],
            ['Value date', this.formatPrintDate(demande.valueDate)],
            ['Date saisie taux', this.formatPrintDate(demande.dateSaisieTaux)],
            ['Commentaire validation', demande.commentaireValidation]
          ])}
        </div>

        <div class="section">
          <div class="section-title">E-commerce</div>
          ${this.generatePrintRows([
            ['RIB', demande.rib],
            ['Webmaster', demande.webmaster],
            ['Contact technique', demande.contactTechnique],
            ['URL site marchand', demande.urlSiteMarchand]
          ])}
        </div>

        <div class="section">
          <div class="section-title">Workflow</div>
          ${this.generatePrintRows([
            ['Demandeur', demande.demandeurNom],
            ['Inputer', demande.inputerNom],
            ['Valideur', demande.valideurNom || demande.monetiqueValideurNom],
            ['Date validation', this.formatPrintDate(demande.dateValidation)],
            ['Date affectation', this.formatPrintDate(demande.dateAffectation)],
            ['Date cloture', this.formatPrintDate(demande.dateCloture)],
            ['ID TPE affecte', demande.tpeAffecteId],
            ['TPE affecte', demande.tpeAffecteNumeroSerie],
            ['Commentaires', demande.commentaires]
          ])}
        </div>

        <div class="section">
          <div class="section-title">Pieces jointes</div>
          ${piecesJointes}
        </div>

        <div class="footer">
          <p>Document genere le ${new Date().toLocaleDateString('fr-FR')} a ${new Date().toLocaleTimeString('fr-FR')}</p>
          <p>${new Date().getFullYear()} Bank ABC - Tous droits reserves</p>
        </div>
      </body>
      </html>
    `;
  }

  private writePrintLoading(printWindow: Window): void {
    printWindow.document.open();
    printWindow.document.write(`
      <!DOCTYPE html>
      <html>
      <head><title>Preparation impression</title></head>
      <body style="font-family: Arial, sans-serif; padding: 24px;">Preparation de l'impression...</body>
      </html>
    `);
    printWindow.document.close();
  }

  private writePrintContent(printWindow: Window, demande: DemandeTPE): void {
    printWindow.document.open();
    printWindow.document.write(this.generatePrintContent(demande));
    printWindow.document.close();
    printWindow.setTimeout(() => {
      printWindow.focus();
      printWindow.print();
      printWindow.onafterprint = () => printWindow.close();
    }, 250);
  }

  private generatePrintRows(rows: Array<[string, any]>): string {
    return rows
      .map(([label, value]) => `
        <div class="info-row">
          <div class="info-label">${this.escapeHtml(label)}:</div>
          <div class="info-value">${this.formatPrintValue(value)}</div>
        </div>
      `)
      .join('');
  }

  private formatPrintValue(value: any): string {
    if (value === null || value === undefined || value === '') {
      return '-';
    }
    if (typeof value === 'string' && value.startsWith('<span')) {
      return value;
    }
    return this.escapeHtml(String(value));
  }

  private formatPrintDate(value: Date | string | undefined): string {
    if (!value) {
      return '-';
    }
    const date = new Date(value);
    return isNaN(date.getTime()) ? '-' : date.toLocaleString('fr-FR');
  }

  private formatPercent(value: number | undefined): string {
    return value === null || value === undefined ? '-' : `${value} %`;
  }

  private escapeHtml(value: string): string {
    return value
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  getStatutClass(statut: string): string {
    const classes: { [key: string]: string } = {
      'NOUVELLE': 'badge-info',
      'EN_COURS': 'badge-warning',
      'VALIDEE_MONETIQUE': 'badge-primary',
      'VALIDEE_AGENCE': 'badge-primary',
      'AFFECTEE': 'badge-success',
      'CLOTUREE': 'badge-secondary',
      'REJETEE': 'badge-danger'
    };
    return classes[statut] || 'badge-default';
  }

  getStatutLabel(statut: string): string {
    const labels: { [key: string]: string } = {
      'NOUVELLE': 'Nouvelle',
      'EN_COURS': 'En Cours',
      'VALIDEE_MONETIQUE': 'Validée Monétique',
      'VALIDEE_AGENCE': 'Validée Agence',
      'AFFECTEE': 'Affectée',
      'CLOTUREE': 'Clôturée',
      'REJETEE': 'Rejetée'
    };
    return labels[statut] || statut;
  }

  getUrgenceClass(urgence: string): string {
    const classes: { [key: string]: string } = {
      'BASSE': 'urgence-basse',
      'NORMALE': 'urgence-normale',
      'HAUTE': 'urgence-haute',
      'CRITIQUE': 'urgence-critique'
    };
    return classes[urgence] || '';
  }

  exportToExcel(): void {
    if (this.demandesFiltrees.length === 0) {
      this.showNotification('Aucune donnée à exporter', 'info');
      return;
    }

    // Préparer les données pour l'export
    const dataToExport = this.demandesFiltrees.map(demande => ({
      'Référence': demande.reference,
      'Commerçant': demande.commercantNom,
      'Type Demande': demande.typeDemande === 'PHYSIQUE' ? 'TPE Physique' : 
                       demande.typeDemande === 'ECOMMERCE' ? 'E-Commerce' : demande.typeDemande,
      'Statut': demande.statut,
      'Urgence': demande.urgence,
      'Date Création': demande.createdAt ? new Date(demande.createdAt).toLocaleDateString('fr-FR') : '-',
      'TPE Affecté': demande.tpeAffecteNumeroSerie || '-',
      'Description': demande.description || '-',
      'Commentaires': demande.commentaires || '-'
    }));

    this.excelExportService.exportToExcel(dataToExport, 'liste_demandes_tpe', 'Demandes');
    this.showNotification('Export Excel effectué avec succès', 'success');
  }

  private showNotification(message: string, type: 'success' | 'error' | 'info'): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: [`snackbar-${type}`]
    });
  }
}
