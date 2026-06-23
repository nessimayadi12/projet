import { Component, OnInit } from '@angular/core';
import { AuditFieldChange, AuditLog, AuditStats } from '../../models/audit-log.model';
import { AuditFilters, AuditLogService } from '../../services/audit-log.service';

@Component({
  selector: 'app-audit-log',
  templateUrl: './audit-log.component.html',
  styleUrls: ['./audit-log.component.css']
})
export class AuditLogComponent implements OnInit {
  logs: AuditLog[] = [];
  selectedLog: AuditLog | null = null;
  stats: AuditStats | null = null;
  loading = false;
  page = 0;
  size = 25;
  totalElements = 0;
  totalPages = 0;

  filters: AuditFilters = {
    username: '',
    action: '',
    entityType: '',
    entityId: '',
    statut: '',
    dateDebut: '',
    dateFin: '',
    keyword: ''
  };

  readonly actions = [
    { value: '', label: 'Toutes' },
    { value: 'CREATE', label: 'Creation' },
    { value: 'UPDATE', label: 'Modification' },
    { value: 'UPDATE_STATUS', label: 'Statut' },
    { value: 'VALIDATE', label: 'Validation' },
    { value: 'REJECT', label: 'Rejet' },
    { value: 'AFFECT', label: 'Affectation' },
    { value: 'DELETE', label: 'Suppression' },
    { value: 'LOGIN', label: 'Connexion' }
  ];

  readonly entityTypes = [
    '',
    'Demande',
    'TPE',
    'Commercant',
    'Affectation',
    'Panne',
    'Taux',
    'User',
    'Screen',
    'ScreenRole'
  ];

  constructor(private auditLogService: AuditLogService) { }

  ngOnInit(): void {
    this.loadStats();
    this.loadLogs();
  }

  loadLogs(page: number = 0): void {
    this.loading = true;
    this.page = page;
    this.auditLogService.getLogs(this.filters, this.page, this.size).subscribe({
      next: (response) => {
        this.logs = response.content || [];
        this.totalElements = response.totalElements || 0;
        this.totalPages = response.totalPages || 0;
        this.selectedLog = this.logs.length ? this.logs[0] : null;
        this.loading = false;
      },
      error: () => {
        this.logs = [];
        this.selectedLog = null;
        this.loading = false;
      }
    });
  }

  loadStats(): void {
    this.auditLogService.getStats().subscribe({
      next: (stats) => this.stats = stats,
      error: () => this.stats = null
    });
  }

  applyFilters(): void {
    this.loadLogs(0);
    this.loadStats();
  }

  resetFilters(): void {
    this.filters = {
      username: '',
      action: '',
      entityType: '',
      entityId: '',
      statut: '',
      dateDebut: '',
      dateFin: '',
      keyword: ''
    };
    this.applyFilters();
  }

  selectLog(log: AuditLog): void {
    this.selectedLog = log;
  }

  nextPage(): void {
    if (this.page + 1 < this.totalPages) {
      this.loadLogs(this.page + 1);
    }
  }

  previousPage(): void {
    if (this.page > 0) {
      this.loadLogs(this.page - 1);
    }
  }

  getActionLabel(log: AuditLog): string {
    return log.actionLabel || log.action;
  }

  getChanges(log: AuditLog | null): AuditFieldChange[] {
    return log && log.changes ? log.changes : [];
  }

  getValueEntries(values?: { [key: string]: any }): Array<{ key: string; value: any }> {
    if (!values) {
      return [];
    }
    return Object.keys(values)
      .filter(key => values[key] !== undefined)
      .map(key => ({ key, value: values[key] }));
  }

  hasDetailedValues(log: AuditLog | null): boolean {
    if (!log) {
      return false;
    }
    return this.getValueEntries(log.oldValues).length > 0
      || this.getValueEntries(log.newValues).length > 0;
  }

  getNoChangesMessage(log: AuditLog | null): string {
    if (!log) {
      return 'Aucune trace selectionnee';
    }
    if (log.action === 'LOGIN') {
      return 'Connexion utilisateur : aucune donnee metier modifiee.';
    }
    if (!this.hasDetailedValues(log)) {
      return 'Trace historique sans valeurs avant/apres. Les nouveaux evenements metier auront un detail des champs.';
    }
    return 'Aucune difference detectee entre les valeurs avant/apres.';
  }

  formatValue(value: any): string {
    if (value === null || value === undefined || value === '') {
      return '-';
    }
    if (typeof value === 'object') {
      return JSON.stringify(value);
    }
    return String(value);
  }

  formatDate(value: string): string {
    if (!value) {
      return '-';
    }
    return new Date(value).toLocaleString('fr-FR');
  }

  exportCsv(): void {
    const headers = [
      'Date',
      'Utilisateur',
      'Action',
      'Module',
      'Entite',
      'Reference',
      'Statut',
      'Risque',
      'Details'
    ];
    const rows = this.logs.map(log => [
      this.formatDate(log.dateAction),
      log.username,
      this.getActionLabel(log),
      log.moduleName || log.entityType,
      log.entityType,
      log.entityReference || log.entityId || '',
      log.statut || '',
      log.riskLevel || '',
      log.details || ''
    ]);

    const csv = [headers, ...rows]
      .map(row => row.map(value => `"${String(value || '').replace(/"/g, '""')}"`).join(';'))
      .join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `audit-${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    URL.revokeObjectURL(link.href);
  }
}
