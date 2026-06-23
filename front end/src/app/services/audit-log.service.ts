import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuditLog, AuditPage, AuditStats } from '../models/audit-log.model';

export interface AuditFilters {
  username?: string;
  action?: string;
  entityType?: string;
  entityId?: string;
  statut?: string;
  dateDebut?: string;
  dateFin?: string;
  keyword?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuditLogService {
  private apiUrl = `${environment.apiUrl}/audit`;

  constructor(private http: HttpClient) { }

  getLogs(filters: AuditFilters, page: number, size: number): Observable<AuditPage> {
    let params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sort', 'dateAction,desc');

    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && String(value).trim() !== '') {
        const normalizedValue = key === 'dateDebut' || key === 'dateFin'
          ? this.normalizeDateTime(String(value))
          : String(value);
        params = params.set(key, normalizedValue);
      }
    });

    return this.http.get<AuditPage>(this.apiUrl, { params });
  }

  getStats(): Observable<AuditStats> {
    return this.http.get<AuditStats>(`${this.apiUrl}/stats`);
  }

  getEntityHistory(entityType: string, entityId: string): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.apiUrl}/entity/${entityType}/${entityId}`);
  }

  private normalizeDateTime(value: string): string {
    return value.length === 16 ? `${value}:00` : value;
  }
}
