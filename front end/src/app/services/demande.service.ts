import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { DemandeTPE, StatutDemande } from '../models/demande-tpe.model';

@Injectable({
  providedIn: 'root'
})
export class DemandeService {
  private apiUrl = `${environment.apiUrl}/demandes`;

  constructor(private http: HttpClient) { }

  getAllDemandes(): Observable<DemandeTPE[]> {
    const params = new HttpParams()
      .set('page', '0')
      .set('size', '5000')
      .set('sort', 'id,desc');

    return this.http.get<any>(this.apiUrl, { params }).pipe(
      map(response => {
        if (Array.isArray(response)) {
          return response as DemandeTPE[];
        }
        if (Array.isArray(response?.content)) {
          return response.content as DemandeTPE[];
        }
        if (Array.isArray(response?.data?.content)) {
          return response.data.content as DemandeTPE[];
        }
        if (Array.isArray(response?.data)) {
          return response.data as DemandeTPE[];
        }
        return [];
      })
    );
  }

  getDemandeById(id: number): Observable<DemandeTPE> {
    return this.http.get<DemandeTPE>(`${this.apiUrl}/${id}`);
  }

  getDemandesByStatut(statut: StatutDemande): Observable<DemandeTPE[]> {
    return this.http.get<DemandeTPE[]>(`${this.apiUrl}/statut/${statut}`);
  }

  getDemandesByCommercant(commercantId: number): Observable<DemandeTPE[]> {
    return this.http.get<DemandeTPE[]>(`${this.apiUrl}/commercant/${commercantId}`);
  }

  createDemande(demande: DemandeTPE): Observable<DemandeTPE> {
    return this.http.post<DemandeTPE>(this.apiUrl, demande);
  }

  updateDemande(id: number, demande: DemandeTPE): Observable<DemandeTPE> {
    return this.http.put<DemandeTPE>(`${this.apiUrl}/${id}`, demande);
  }

  changeStatut(id: number, statut: StatutDemande): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/statut/${statut}`, {});
  }

  affecterTPE(demandeId: number, tpeId: number): Observable<DemandeTPE> {
    return this.http.post<DemandeTPE>(`${environment.apiUrl}/affectations`, {
      demandeId,
      tpeId,
      commentaire: 'Affectation depuis workflow Demande'
    });
  }

  deleteDemande(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Valider une demande (par Monétique)
  validerDemande(id: number, validationData: any): Observable<DemandeTPE> {
    return this.http.post<DemandeTPE>(`${this.apiUrl}/${id}/valider`, validationData);
  }

  // Rejeter une demande
  rejeterDemande(id: number, motif: string): Observable<DemandeTPE> {
    return this.http.post<DemandeTPE>(`${this.apiUrl}/${id}/rejeter`, motif, {
      headers: new HttpHeaders({ 'Content-Type': 'text/plain' })
    });
  }

  // Clôturer une demande
  cloturerDemande(id: number): Observable<string> {
    return this.http.patch(`${this.apiUrl}/${id}/cloturer`, null, { responseType: 'text' });
  }

  // Ajouter un commentaire
  ajouterCommentaire(id: number, commentaire: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/commentaire`, { commentaire });
  }

  // Upload pièce jointe
  uploadPieceJointe(id: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiUrl}/${id}/piece-jointe`, formData);
  }

  // Récupérer les demandes par agence
  getDemandesByAgence(agenceId: number): Observable<DemandeTPE[]> {
    return this.http.get<DemandeTPE[]>(`${this.apiUrl}/agence/${agenceId}`);
  }

  // Récupérer les demandes en attente de validation
  getDemandesEnAttente(): Observable<DemandeTPE[]> {
    return this.http.get<DemandeTPE[]>(`${this.apiUrl}/en-attente`);
  }

  // Statistiques des demandes
  getStatistiques(): Observable<any> {
    return this.http.get(`${this.apiUrl}/statistiques`);
  }

  // Générer bon de livraison
  genererBonLivraison(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/bon-livraison`, { responseType: 'blob' });
  }

  // Générer contrat
  genererContrat(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/contrat`, { responseType: 'blob' });
  }
}
