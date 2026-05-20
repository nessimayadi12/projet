import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { TPE, StatutTPE, TypeTPE, TPEHistorique } from '../models/tpe.model';

export interface TPEImportRecord {
  id: number;
  nAffiliation: string;
  sourceRowNumber: number;
  sourceFileName?: string;
  typeTPE?: string;
  numeroSerie?: string;
  numeroTerminal?: string;
  raisonSociale?: string;
  activite?: string;
  mcc?: string;
  numeroCompte?: string;
  codeAgence?: string;
  adresse?: string;
  codePostal?: string;
  telephone?: string;
  email?: string;
  privilegeSecteur?: string;
  tauxCommission?: string;
  tauxCommissionInter?: string;
  loyer?: string;
  nCompteIntern?: string;
  groupe?: string;
  numSeq?: string;
  active?: boolean;
  valueDate?: Date | string;
  dateAffiliation?: Date | string;
  createdDate?: Date | string;
  lastModifiedDate?: Date | string;
}

@Injectable({
  providedIn: 'root'
})
export class TpeService {
  private apiUrl = `${environment.apiUrl}/tpe`;

  constructor(private http: HttpClient) { }

  // Récupérer tous les TPE
  getAllTPE(): Observable<TPE[]> {
    const params = new HttpParams()
      .set('page', '0')
      .set('size', '5000')
      .set('sort', 'id,desc');

    return this.http.get<any>(this.apiUrl, { params }).pipe(
      map(response => this.extractTPEArray(response))
    );
  }

  // Récupérer un TPE par ID
  getTPEById(id: number): Observable<TPE> {
    return this.http.get<any>(`${this.apiUrl}/${id}`).pipe(
      map(response => this.normalizeTPE(response))
    );
  }

  // Récupérer un TPE par numéro de série
  getTPEByNumeroSerie(numeroSerie: string): Observable<TPE> {
    return this.http.get<any>(`${this.apiUrl}/numero-serie/${numeroSerie}`).pipe(
      map(response => this.normalizeTPE(response))
    );
  }

  // Récupérer TPE par statut
  getTPEByStatut(statut: StatutTPE): Observable<TPE[]> {
    return this.http.get<any>(`${this.apiUrl}/statut/${statut}`).pipe(
      map(response => this.extractTPEArray(response))
    );
  }

  // Récupérer TPE par type
  getTPEByType(type: TypeTPE): Observable<TPE[]> {
    return this.http.get<any>(`${this.apiUrl}/type/${type}`).pipe(
      map(response => this.extractTPEArray(response))
    );
  }

  // Récupérer TPE disponibles
  getTPEDisponibles(): Observable<TPE[]> {
    return this.http.get<any>(`${this.apiUrl}/disponibles`).pipe(
      map(response => this.extractTPEArray(response))
    );
  }

  // Récupérer TPE par commerçant
  getTPEByCommercant(commercantId: number): Observable<TPE[]> {
    return this.http.get<any>(`${this.apiUrl}/commercant/${commercantId}`).pipe(
      map(response => this.extractTPEArray(response))
    );
  }

  // Recherche multicritère
  searchTPE(criteria: any): Observable<TPE[]> {
    let params = new HttpParams();
    Object.keys(criteria).forEach(key => {
      if (criteria[key]) {
        params = params.append(key, criteria[key]);
      }
    });
    return this.http.get<any>(`${this.apiUrl}/search`, { params }).pipe(
      map(response => this.extractTPEArray(response))
    );
  }

  // Créer TPE (Physique ou E-commerce)
  createTPE(tpe: TPE): Observable<TPE> {
    return this.http.post<any>(`${this.apiUrl}`, tpe).pipe(
      map(response => this.normalizeTPE(response))
    );
  }

  // Créer TPE Physique (alias pour compatibilité)
  createTPEPhysique(tpe: TPE): Observable<TPE> {
    return this.createTPE(tpe);
  }

  // Créer TPE E-commerce (alias pour compatibilité)
  createTPEEcommerce(tpe: TPE): Observable<TPE> {
    return this.createTPE(tpe);
  }

  // Mettre à jour un TPE
  updateTPE(id: number, tpe: TPE): Observable<TPE> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, tpe).pipe(
      map(response => this.normalizeTPE(response))
    );
  }

  // Changer le statut d'un TPE
  changeStatut(id: number, statut: StatutTPE): Observable<string> {
    const params = new HttpParams().set('statut', statut);
    return this.http.patch(`${this.apiUrl}/${id}/statut`, null, {
      params,
      responseType: 'text'
    });
  }

  // Affecter un TPE à un commerçant
  affecterTPE(tpeId: number, commercantId: number): Observable<TPE> {
    return this.http.post<TPE>(`${this.apiUrl}/${tpeId}/affecter/${commercantId}`, {});
  }

  // Libérer un TPE
  libererTPE(tpeId: number): Observable<TPE> {
    return this.http.post<TPE>(`${this.apiUrl}/${tpeId}/liberer`, {});
  }

  // Générer le numéro de terminal (TID)
  genererNumeroTerminal(data: any): Observable<string> {
    return this.http.post<string>(`${this.apiUrl}/generer-tid`, data);
  }

  // Récupérer l'historique d'un TPE
  getHistorique(tpeId: number): Observable<TPEHistorique[]> {
    return this.http.get<TPEHistorique[]>(`${this.apiUrl}/${tpeId}/historique`);
  }

  // Import massif
  importTPE(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiUrl}/import`, formData);
  }

  // Lister les lignes importées dans le staging
  getImportRecords(page: number = 0, size: number = 50): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'sourceRowNumber,asc');
    return this.http.get<any>(`${this.apiUrl}/import-records`, { params });
  }

  // Export Excel
  exportTPE(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/export`, { responseType: 'blob' });
  }

  // Supprimer un TPE
  deleteTPE(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Statistiques
  getStatistiques(): Observable<any> {
    return this.http.get(`${this.apiUrl}/statistiques`);
  }

  // Alertes stock bas
  getAlertesStockBas(): Observable<any> {
    return this.http.get(`${this.apiUrl}/alertes/stock-bas`);
  }

  private extractTPEArray(response: any): TPE[] {
    if (Array.isArray(response)) {
      return response.map(tpe => this.normalizeTPE(tpe));
    }
    if (Array.isArray(response?.content)) {
      return response.content.map((tpe: any) => this.normalizeTPE(tpe));
    }
    if (Array.isArray(response?.data?.content)) {
      return response.data.content.map((tpe: any) => this.normalizeTPE(tpe));
    }
    if (Array.isArray(response?.data)) {
      return response.data.map((tpe: any) => this.normalizeTPE(tpe));
    }
    return [];
  }

  private normalizeTPE(tpe: any): TPE {
    if (!tpe) {
      return tpe;
    }

    const commercantNom = tpe.commercantActuelNom || tpe.commercantNom;

    return {
      ...tpe,
      typeTpe: tpe.typeTpe || tpe.typeTPE,
      commercantActuelId: tpe.commercantActuelId ?? tpe.commercantId,
      commercantActuelNom: commercantNom,
      raisonSociale: tpe.raisonSociale || commercantNom,
      numeroCompte: tpe.numeroCompte || tpe.rib,
      serieTpe: tpe.serieTpe || tpe.numeroSerie,
      tauxCommission: this.toNumberOrOriginal(tpe.tauxCommission),
      tauxCommissionInter: this.toNumberOrOriginal(tpe.tauxCommissionInter),
      loyer: this.toNumberOrOriginal(tpe.loyer),
      cartesAcceptees: tpe.cartesAcceptees || tpe.typeCartesAcceptees
    } as TPE;
  }

  private toNumberOrOriginal(value: any): any {
    if (value === null || value === undefined || value === '') {
      return value;
    }

    const numericValue = Number(value);
    return Number.isNaN(numericValue) ? value : numericValue;
  }
}
