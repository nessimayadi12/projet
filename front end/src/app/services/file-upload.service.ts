import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class FileUploadService {
  private apiUrl = `${environment.apiUrl}/file-upload`;

  constructor(private http: HttpClient) { }

  uploadFile(file: File): Observable<any> {
    const formData: FormData = new FormData();
    formData.append('file', file, file.name);

    return this.http.post<any>(`${this.apiUrl}/process`, formData);
  }

  // Récupérer les données de la table TPE_POSTING_comp
  getProcessedTransactions(limit: number = 1000): Observable<any> {
    return this.http.get<any>(`${environment.apiUrl}/tpe-posting`, {
      params: { limit: limit.toString() }
    });
  }

  // Récupérer les transactions récentes (dernières insérées)
  getRecentTransactions(limit: number = 500): Observable<any> {
    return this.http.get<any>(`${environment.apiUrl}/tpe-posting/recent`, {
      params: { limit: limit.toString() }
    });
  }
}
