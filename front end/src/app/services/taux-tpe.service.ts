import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TauxTPE } from '../models/taux-tpe.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TauxTpeService {
  private apiUrl = `${environment.apiUrl}/taux`;

  constructor(private http: HttpClient) { }

  /**
   * Saisir les taux par un Inputer
   */
  saisirTaux(taux: TauxTPE, inputerId: number): Observable<TauxTPE> {
    const params = new HttpParams().set('inputerId', inputerId.toString());
    return this.http.post<TauxTPE>(`${this.apiUrl}/saisir`, taux, { params });
  }

  /**
   * Soumettre les taux pour validation
   */
  soumettreValidation(tauxId: number, inputerId: number): Observable<TauxTPE> {
    const params = new HttpParams().set('inputerId', inputerId.toString());
    return this.http.put<TauxTPE>(`${this.apiUrl}/${tauxId}/soumettre`, null, { params });
  }

  /**
   * Valider les taux par un Authorizer
   */
  validerTaux(tauxId: number, authorizerId: number, commentaires?: string): Observable<TauxTPE> {
    let params = new HttpParams().set('authorizerId', authorizerId.toString());
    if (commentaires) {
      params = params.set('commentaires', commentaires);
    }
    return this.http.put<TauxTPE>(`${this.apiUrl}/${tauxId}/valider`, null, { params });
  }

  /**
   * Rejeter les taux par un Authorizer
   */
  rejeterTaux(tauxId: number, authorizerId: number, motifRejet: string): Observable<TauxTPE> {
    const params = new HttpParams()
      .set('authorizerId', authorizerId.toString())
      .set('motifRejet', motifRejet);
    return this.http.put<TauxTPE>(`${this.apiUrl}/${tauxId}/rejeter`, null, { params });
  }

  /**
   * Obtenir tous les taux en attente de validation
   */
  getTauxEnAttenteValidation(): Observable<TauxTPE[]> {
    return this.http.get<TauxTPE[]>(`${this.apiUrl}/en-attente`);
  }

  /**
   * Obtenir l'historique des taux pour un TPE
   */
  getHistoriqueTaux(tpeId: number): Observable<TauxTPE[]> {
    return this.http.get<TauxTPE[]>(`${this.apiUrl}/historique/${tpeId}`);
  }
}
