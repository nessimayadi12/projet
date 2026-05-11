import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { saveAs } from 'file-saver';

@Injectable({
  providedIn: 'root'
})
export class PDFService {
  private apiUrl = `${environment.apiUrl}/pdf`;

  constructor(private http: HttpClient) { }

  genererContrat(commercantId: number, tpeId: number): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/contrat`, 
      { commercantId, tpeId }, 
      { responseType: 'blob' }
    );
  }

  genererBonLivraison(demandeId: number, tpeId: number): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/bon-livraison`, 
      { demandeId, tpeId }, 
      { responseType: 'blob' }
    );
  }

  telechargerContrat(commercantId: number, tpeId: number): void {
    this.genererContrat(commercantId, tpeId).subscribe({
      next: (blob) => {
        saveAs(blob, `contrat_${commercantId}_${tpeId}_${Date.now()}.pdf`);
      },
      error: (error) => {
        console.error('Erreur téléchargement contrat', error);
      }
    });
  }

  telechargerBonLivraison(demandeId: number, tpeId: number): void {
    this.genererBonLivraison(demandeId, tpeId).subscribe({
      next: (blob) => {
        saveAs(blob, `bon_livraison_${demandeId}_${tpeId}_${Date.now()}.pdf`);
      },
      error: (error) => {
        console.error('Erreur téléchargement bon de livraison', error);
      }
    });
  }
}
