import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Commercant, StatutCommercant } from '../models/commercant.model';

@Injectable({
  providedIn: 'root'
})
export class CommercantService {
  private apiUrl = `${environment.apiUrl}/commercants`;

  constructor(private http: HttpClient) { }

  getAllCommercants(): Observable<Commercant[]> {
    const params = new HttpParams()
      .set('page', '0')
      .set('size', '5000')
      .set('sort', 'id,desc');

    return this.http.get<any>(this.apiUrl, { params }).pipe(
      map(response => {
        if (Array.isArray(response)) {
          return response as Commercant[];
        }
        if (Array.isArray(response?.content)) {
          return response.content as Commercant[];
        }
        if (Array.isArray(response?.data?.content)) {
          return response.data.content as Commercant[];
        }
        if (Array.isArray(response?.data)) {
          return response.data as Commercant[];
        }
        return [];
      })
    );
  }

  getCommercantById(id: number): Observable<Commercant> {
    return this.http.get<Commercant>(`${this.apiUrl}/${id}`);
  }

  getCommercantBySiret(siret: string): Observable<Commercant> {
    return this.http.get<Commercant>(`${this.apiUrl}/siret/${siret}`);
  }

  getCommercantsByStatut(statut: StatutCommercant): Observable<Commercant[]> {
    return this.http.get<Commercant[]>(`${this.apiUrl}/statut/${statut}`);
  }

  searchCommercants(keyword: string): Observable<Commercant[]> {
    return this.http.get<Commercant[]>(`${this.apiUrl}/search?query=${keyword}`);
  }

  createCommercant(commercant: Commercant): Observable<Commercant> {
    return this.http.post<Commercant>(this.apiUrl, commercant);
  }

  updateCommercant(id: number, commercant: Commercant): Observable<Commercant> {
    return this.http.put<Commercant>(`${this.apiUrl}/${id}`, commercant);
  }

  deleteCommercant(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Récupérer l'historique TPE d'un commerçant
  getHistoriqueTPE(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${id}/historique-tpe`);
  }

  // Changer le statut d'un commerçant
  changeStatut(id: number, statut: StatutCommercant): Observable<Commercant> {
    return this.http.put<Commercant>(`${this.apiUrl}/${id}/statut/${statut}`, {});
  }

  // Upload fichier RNE
  uploadFichierRNE(id: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiUrl}/${id}/upload-rne`, formData);
  }

  // Obtenir les commerçants avec le plus de TPE
  getTopCommercants(limit: number = 10): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/top?limit=${limit}`);
  }

  // Import massif
  importCommercants(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiUrl}/import`, formData);
  }

  // Export Excel
  exportCommercants(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/export`, { responseType: 'blob' });
  }
}
