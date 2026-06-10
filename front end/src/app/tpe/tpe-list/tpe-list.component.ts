import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TpeService } from '../../services/tpe.service';
import { TPE, StatutTPE } from '../../models/tpe.model';
import { AuthService } from '../../services/auth.service';
import { ScreenService } from '../../services/screen.service';
import { ExcelExportService } from '../../services/excel-export.service';
import { Observable } from 'rxjs';
import * as XLSX from 'xlsx';

interface ParcTpeRow {
  typeTpe: string;
  modele: string;
  numeroSerie: string;
  statut: string;
  dateInscription: any;
  derniereConnexion: any;
  banque: string;
  idTerminal: string;
  operateur: string;
  numeroSerieSim: string;
  idMarchand: string;
  adresse: string;
  cite: string;
}

@Component({
  selector: 'app-tpe-list',
  templateUrl: './tpe-list.component.html',
  styleUrls: ['./tpe-list.component.css']
})
export class TpeListComponent implements OnInit {
  tpes: TPE[] = [];
  filteredTpes: TPE[] = [];
  pagedTpes: TPE[] = [];
  loading = true;
  error: string | null = null;
  importing = false;
  parcImporting = false;
  searchTerm = '';
  selectedStatut: StatutTPE | '' = '';
  statuts = Object.values(StatutTPE);
  page = 1;
  pageSize = 25;
  pageSizeOptions = [10, 25, 50, 100];

  // Permissions observables
  canCreateTPE$: Observable<boolean>;
  canEditTPE$: Observable<boolean>;
  canDeleteTPE$: Observable<boolean>;
  canExportTPE$: Observable<boolean>;

  constructor(
    private tpeService: TpeService,
    private router: Router,
    private authService: AuthService,
    private excelExportService: ExcelExportService,
    private screenService: ScreenService
  ) {
    // Initialiser les permissions
    this.canCreateTPE$ = this.screenService.hasPermission('CREER_TPE', 'canCreate');
    this.canEditTPE$ = this.screenService.hasPermission('MODIFIER_TPE', 'canEdit');
    this.canDeleteTPE$ = this.screenService.hasPermission('MODIFIER_TPE', 'canDelete');
    this.canExportTPE$ = this.screenService.hasPermission('LISTE_TPE', 'canExport');
  }

  ngOnInit(): void {
    this.loadTPEs();
  }

  loadTPEs(): void {
    this.loading = true;
    this.tpeService.getAllTPE().subscribe({
      next: (data) => {
        const items = Array.isArray(data) ? data : [];
        this.tpes = items;
        this.filteredTpes = items;
        this.updatePagedTpes();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des TPE:', err);
        this.error = this.getLoadErrorMessage(err);
        this.loading = false;
      }
    });
  }

  filterTPEs(): void {
    this.filteredTpes = this.tpes.filter(tpe => {
      const serie = (tpe.numeroSerie || '').toLowerCase();
      const terminal = (tpe.numeroTerminal || '').toLowerCase();
      const affiliation = (tpe.numeroAffiliation || '').toLowerCase();
      const marque = (tpe.marque || '').toLowerCase();
      const modele = (tpe.modele || '').toLowerCase();
      const typeTpe = this.getTypeTpeLabel(tpe).toLowerCase();
      const commercant = (tpe.commercantActuelNom || '').toLowerCase();
      const raisonSociale = (tpe.raisonSociale || '').toLowerCase();
      const query = (this.searchTerm || '').toLowerCase();
      const normalizedQuery = this.normalizeForSearch(query);

      const matchesSearch = !this.searchTerm || 
        serie.includes(query) ||
        terminal.includes(query) ||
        affiliation.includes(query) ||
        marque.includes(query) ||
        modele.includes(query) ||
        typeTpe.includes(query) ||
        commercant.includes(query) ||
        raisonSociale.includes(query) ||
        this.normalizeForSearch(serie).includes(normalizedQuery) ||
        this.normalizeForSearch(terminal).includes(normalizedQuery) ||
        this.normalizeForSearch(affiliation).includes(normalizedQuery);
      
      const matchesStatut = !this.selectedStatut || tpe.statut === this.selectedStatut;
      
      return matchesSearch && matchesStatut;
    });

    this.page = 1;
    this.updatePagedTpes();
  }

  get totalPages(): number {
    const total = Math.ceil(this.filteredTpes.length / this.pageSize);
    return total > 0 ? total : 1;
  }

  onPageSizeChange(value: string): void {
    this.pageSize = Number(value);
    this.page = 1;
    this.updatePagedTpes();
  }

  previousPage(): void {
    if (this.page > 1) {
      this.page--;
      this.updatePagedTpes();
    }
  }

  nextPage(): void {
    if (this.page < this.totalPages) {
      this.page++;
      this.updatePagedTpes();
    }
  }

  private updatePagedTpes(): void {
    const startIndex = (this.page - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.pagedTpes = this.filteredTpes.slice(startIndex, endIndex);
  }

  viewDetails(id: number): void {
    this.router.navigate(['/tpe', id]);
  }

  addNewTPE(): void {
    this.router.navigate(['/tpe/new']);
  }

  viewImportRecords(): void {
    this.router.navigate(['/tpe/imports']);
  }

  editTPE(id: number): void {
    this.router.navigate(['/tpe', id, 'edit']);
  }

  deleteTPE(id: number, numeroSerie: string): void {
    if (confirm(`Êtes-vous sûr de vouloir supprimer le TPE ${numeroSerie} ?`)) {
      this.tpeService.deleteTPE(id).subscribe({
        next: () => {
          this.loadTPEs();
          alert('TPE supprimé avec succès');
        },
        error: (err) => {
          console.error('Erreur lors de la suppression:', err);
          alert('Impossible de supprimer le TPE');
        }
      });
    }
  }

  exportToExcel(): void {
    if (this.filteredTpes.length === 0) {
      alert('Aucune donnée à exporter');
      return;
    }

    // Préparer les données pour l'export
    const dataToExport = this.filteredTpes.map(tpe => ({
      'N° Série': tpe.numeroSerie,
      'Type TPE': this.getTypeTpeLabel(tpe),
      'Marque': tpe.marque,
      'Modèle': tpe.modele,
      'Statut': this.getStatutLabel(tpe.statut),
      'Commerçant': tpe.commercantActuelNom || '-',
      'Date Acquisition': tpe.dateAcquisition ? new Date(tpe.dateAcquisition).toLocaleDateString('fr-FR') : '-'
    }));

    this.excelExportService.exportToExcel(dataToExport, 'liste_tpe', 'TPE');
  }

  triggerImportFile(): void {
    const input = document.getElementById('tpe-import-input') as HTMLInputElement | null;
    input?.click();
  }

  triggerParcImportFile(): void {
    const input = document.getElementById('tpe-parc-import-input') as HTMLInputElement | null;
    input?.click();
  }

  onImportFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (!file) {
      return;
    }

    this.importing = true;
    this.tpeService.importTPE(file).subscribe({
      next: (result) => {
        const summary = result
          ? `Import terminé: ${result.storedRows ?? 0} lignes stockées, ${result.importedRows ?? 0} créés, ${result.updatedRows ?? 0} mis à jour, ${result.affectedRows ?? 0} affectés, ${result.skippedRows ?? 0} ignorés`
          : 'Import terminé avec succès';

        alert(summary);
        this.loadTPEs();
        input.value = '';
        this.importing = false;
      },
      error: (err) => {
        console.error('Erreur lors de l\'import Excel:', err);
        alert('Impossible d\'importer le fichier Excel');
        input.value = '';
        this.importing = false;
      }
    });
  }

  async onParcImportFileSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (!file) {
      return;
    }

    this.parcImporting = true;

    try {
      const convertedFile = await this.convertParcTpeFile(file);
      this.tpeService.importTPE(convertedFile).subscribe({
        next: (result) => {
          const summary = result
            ? `MAJ parc terminee: ${result.storedRows ?? 0} lignes stockees, ${result.importedRows ?? 0} crees, ${result.updatedRows ?? 0} mis a jour, ${result.affectedRows ?? 0} affectes, ${result.skippedRows ?? 0} ignores`
            : 'MAJ parc terminee avec succes';

          alert(summary);
          this.loadTPEs();
          input.value = '';
          this.parcImporting = false;
        },
        error: (err) => {
          console.error('Erreur lors de l\'import du parc TPE:', err);
          alert('Impossible d\'importer le fichier parc TPE');
          input.value = '';
          this.parcImporting = false;
        }
      });
    } catch (err) {
      console.error('Erreur lors de la lecture du fichier parc TPE:', err);
      alert('Le fichier selectionne ne correspond pas au format parc TPE attendu');
      input.value = '';
      this.parcImporting = false;
    }
  }

  getStatutClass(statut: StatutTPE): string {
    switch(statut) {
      case StatutTPE.DISPONIBLE: return 'badge-success';
      case StatutTPE.AFFECTE: return 'badge-primary';
      case StatutTPE.EN_PANNE: return 'badge-danger';
      case StatutTPE.MAINTENANCE: return 'badge-warning';
      case StatutTPE.HORS_SERVICE: return 'badge-dark';
      case StatutTPE.RESERVE: return 'badge-info';
      default: return 'badge-secondary';
    }
  }

  getStatutLabel(statut: StatutTPE): string {
    if (statut === StatutTPE.HORS_SERVICE) {
      return 'Cloture';
    }
    return (statut || '').replace(/_/g, ' ');
  }

  getTypeTpeLabel(tpe: TPE): string {
    return tpe.typeTPE || tpe.typeTpe || '-';
  }

  private normalizeForSearch(value: string): string {
    return (value || '').toLowerCase().replace(/[^a-z0-9]/g, '');
  }

  private getLoadErrorMessage(err: any): string {
    if (err?.status === 401) {
      return 'Session expiree. Veuillez vous reconnecter.';
    }

    if (err?.status === 403) {
      return 'Vous n avez pas les droits necessaires pour consulter la liste des TPE.';
    }

    if (err?.status === 0) {
      return 'Impossible de contacter le serveur backend sur localhost:8080.';
    }

    return 'Impossible de charger la liste des TPE';
  }

  private async convertParcTpeFile(file: File): Promise<File> {
    const buffer = await file.arrayBuffer();
    const workbook = XLSX.read(buffer, { type: 'array', cellDates: true });
    const firstSheet = workbook.Sheets[workbook.SheetNames[0]];

    if (this.isTpeReportWorkbook(firstSheet)) {
      return file;
    }

    const rows = XLSX.utils.sheet_to_json<any>(firstSheet, { defval: '' });

    if (!rows.length) {
      throw new Error('Fichier vide');
    }

    const convertedRows = rows
      .map(row => this.mapParcTpeRow(row))
      .filter(row => row.NUMERO_SERIE || row.N_TERMINAL);

    if (!convertedRows.length) {
      throw new Error('Aucune ligne TPE valide');
    }

    const outputWorkbook = XLSX.utils.book_new();
    const outputWorksheet = XLSX.utils.json_to_sheet(convertedRows, {
      header: [
        'IMPORT_MODE',
        'TYPE_TPE',
        'MARQUE',
        'NUMERO_SERIE',
        'N_TERMINAL',
        'RAISON_SOCIALE',
        'ACTIVE',
        'VALUE_DATE',
        'DATE_AFFILIATION',
        'CODE_TPE',
        'SERIE_PUCE',
        'N_AFFILIATION',
        'ADRESSE',
        'CODE_AGENCE',
        'TELEPHONE',
        'EMAIL',
        'OPERATEUR',
        'ACTIVITE',
        'MCC',
        'N_COMPTE',
        'LOYER',
        'N_COMPTE_INTERN',
        'GROUP',
        'NUM_SEQ'
      ]
    });

    XLSX.utils.book_append_sheet(outputWorkbook, outputWorksheet, 'TPE');
    const outputBuffer = XLSX.write(outputWorkbook, { bookType: 'xlsx', type: 'array' });
    const outputBlob = new Blob([outputBuffer], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });

    return new File([outputBlob], `parc_${file.name}`, { type: outputBlob.type });
  }

  private isTpeReportWorkbook(sheet: XLSX.WorkSheet): boolean {
    const rows = XLSX.utils.sheet_to_json<any[]>(sheet, { header: 1, defval: '' });
    const maxProbeRows = Math.min(rows.length, 20);

    for (let index = 0; index < maxProbeRows; index++) {
      const normalizedHeaders = (rows[index] || [])
        .map(value => this.normalizeHeader(this.cleanCell(value)))
        .filter(value => !!value);

      if (
        normalizedHeaders.includes('N_AFFILIATION') ||
        normalizedHeaders.includes('N_TERMINAL') ||
        normalizedHeaders.includes('SERIE_TPE') ||
        normalizedHeaders.includes('TYPE_TPE')
      ) {
        return true;
      }
    }

    return false;
  }

  private mapParcTpeRow(row: any): any {
    const parcRow = this.readParcTpeRow(row);
    const numeroSerie = this.cleanCell(parcRow.numeroSerie);
    const terminal = this.cleanTerminal(parcRow.idTerminal);
    const modele = this.cleanCell(parcRow.modele);
    const typeTpe = this.cleanCell(parcRow.typeTpe);
    const marchand = this.cleanCell(parcRow.idMarchand);
    const adresse = this.cleanAddress(parcRow.adresse, parcRow.cite);
    const dateInscription = this.toIsoDate(parcRow.dateInscription);
    const derniereConnexion = this.toIsoDate(parcRow.derniereConnexion);
    const operateur = this.cleanCell(parcRow.operateur);
    const numeroSerieSim = this.cleanCell(parcRow.numeroSerieSim);
    const banque = this.cleanCell(parcRow.banque);
    const importKey = this.buildParcImportKey(terminal, numeroSerie, marchand);
    const activeValue = this.resolveParcActiveValue(parcRow.statut);

    return {
      IMPORT_MODE: 'PARC_TPE_MAJ',
      TYPE_TPE: typeTpe || 'TPE',
      MARQUE: this.resolveMarqueFromModele(modele),
      NUMERO_SERIE: numeroSerie,
      N_TERMINAL: terminal,
      RAISON_SOCIALE: marchand || terminal || numeroSerie,
      ACTIVE: activeValue,
      VALUE_DATE: dateInscription,
      DATE_AFFILIATION: activeValue === 'OUI' ? dateInscription : '',
      CODE_TPE: modele,
      SERIE_PUCE: numeroSerieSim,
      N_AFFILIATION: importKey,
      ADRESSE: adresse,
      CODE_AGENCE: '',
      TELEPHONE: '',
      EMAIL: '',
      OPERATEUR: operateur,
      ACTIVITE: '',
      MCC: '',
      N_COMPTE: '',
      LOYER: '',
      N_COMPTE_INTERN: numeroSerieSim,
      GROUP: banque,
      NUM_SEQ: derniereConnexion
    };
  }

  private readParcTpeRow(row: any): ParcTpeRow {
    return {
      typeTpe: this.readColumn(row, ['TYPE TPE', 'TYPE_TPE', 'Type TPE', 'Type', 'Terminal Type']),
      modele: this.readColumn(row, ['Modele', 'MODELE']),
      numeroSerie: this.readColumn(row, ['Numero de serie TPE', 'N Serie']),
      statut: this.readColumn(row, ['Statut', 'STATUT', 'ACTIVE', 'Active']),
      dateInscription: this.readColumn(row, ['Date inscription', 'Date d inscription', 'DATE_INSCRIPTION']),
      derniereConnexion: this.readColumn(row, ['Derniere connexion', 'DERNIERE_CONNEXION']),
      banque: this.readColumn(row, ['Banque', 'BANQUE']),
      idTerminal: this.readColumn(row, ['ID Terminal', 'Id Terminal', 'ID_TERMINAL']),
      operateur: this.readColumn(row, ['Operateur', 'OPERATEUR']),
      numeroSerieSim: this.readColumn(row, ['No de serie. SIM', 'Numero serie SIM']),
      idMarchand: this.readColumn(row, ['ID Marchand', 'Id Marchand', 'ID_MARCHAND']),
      adresse: this.readColumn(row, ['Adresse', 'ADRESSE']),
      cite: this.readColumn(row, ['Cite', 'CITE'])
    };
  }

  private readColumn(row: any, labels: string[]): any {
    const normalizedRowKeys = Object.keys(row).reduce((result, key) => {
      result[this.normalizeHeader(key)] = key;
      return result;
    }, {} as { [key: string]: string });

    for (const label of labels) {
      const key = normalizedRowKeys[this.normalizeHeader(label)];
      if (key !== undefined) {
        return row[key];
      }
    }

    return '';
  }

  private cleanCell(value: any): string {
    if (value === null || value === undefined) {
      return '';
    }

    return String(value)
      .replace(/[\u200B-\u200D\uFEFF]/g, '')
      .trim();
  }

  private cleanTerminal(value: any): string {
    return this.cleanCell(value).replace(/\s+/g, '');
  }

  private buildParcImportKey(terminal: string, numeroSerie: string, marchand: string): string {
    return [terminal, numeroSerie]
      .filter(value => !!value)
      .join('-') || marchand;
  }

  private cleanAddress(adresse: any, cite: any): string {
    const parts = [
      ...this.cleanCell(adresse).split(':'),
      this.cleanCell(cite)
    ]
      .map(part => part.trim())
      .filter(part => !!part);

    const uniqueParts = parts.filter((part, index) =>
      parts.findIndex(item => item.toLowerCase() === part.toLowerCase()) === index
    );

    return uniqueParts.join(' ');
  }

  private resolveParcActiveValue(value: any): string {
    const status = this.normalizeHeader(this.cleanCell(value));
    if (['TERMINATED', 'TERMINETED', 'TERMIANTED', 'TERMINE', 'CLOTURE', 'CLOTUREE', 'RESILIE'].includes(status)) {
      return 'Termineted';
    }
    return ['ACTIVE', 'ACTIF', 'OUI', 'YES', '1', 'TRUE'].includes(status) ? 'OUI' : 'NON';
  }

  private resolveMarqueFromModele(modele: string): string {
    const normalized = this.normalizeHeader(modele);

    if (normalized.startsWith('V') || normalized.includes('VERIFONE')) {
      return 'Verifone';
    }
    if (normalized.includes('INGENICO') || normalized.startsWith('ICT') || normalized.startsWith('IWL') || normalized.includes('MOVE')) {
      return 'Ingenico';
    }
    if (normalized.includes('PAX')) {
      return 'PAX';
    }

    return 'Generic';
  }

  private toIsoDate(value: any): string {
    if (!value) {
      return '';
    }

    if (value instanceof Date && !isNaN(value.getTime())) {
      return this.dateToIso(value);
    }

    const raw = this.cleanCell(value);
    const match = raw.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})/);

    if (match) {
      const month = Number(match[1]);
      const day = Number(match[2]);
      const year = Number(match[3]);
      return this.dateToIso(new Date(year, month - 1, day));
    }

    const parsed = new Date(raw);
    return isNaN(parsed.getTime()) ? '' : this.dateToIso(parsed);
  }

  private dateToIso(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private normalizeHeader(value: string): string {
    return (value || '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toUpperCase()
      .replace(/[^A-Z0-9]+/g, '_')
      .replace(/^_+|_+$/g, '');
  }
}
