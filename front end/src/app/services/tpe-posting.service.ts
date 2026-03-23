import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent, HttpEventType, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface TPEInfo {
  nAffiliation: string;
  nCompte: string;
  exists: boolean;
  branch: string;
  profitCentre: string;
  clientId: string;
}

export interface PorteurInfo {
  ncarte: string;
  compte: string;
  devise: string;
  ccyId: string;
  ccyRate: number;
  deciPlaces: number;
  exists: boolean;
  branch: string;
  profitCentre: string;
  clientId: string;
}

export interface EcritureComptable {
  branch: string;
  profitCentre: string;
  clientId: string;
  accountNo: string;
  accountName: string;
  accountType: string;
  ccy: string;
  seqNo: string;
  referenceNo: string;
  rbTranType: string;
  valueDate: string;
  amount: string;
  dc: string;
  narrative: string;
  tranType?: string;
  rbGl?: string;
  sessionDate?: string;
}

export interface FichierBancaireResult {
  success: boolean;
  filename?: string;
  lignesLues?: number;
  ecrituresCreees?: number;
  sessionDate?: string;
  message?: string;
  error?: string;
}

export interface FichierBancaireStats {
  success: boolean;
  sessionDate: string;
  transactionCount: number;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class TPEPostingService {
  private apiUrl = `${environment.apiUrl}/tpe-posting`;
  private fichierBancaireUrl = `${environment.apiUrl}/fichier-bancaire`;

  constructor(private http: HttpClient) { }

  /**
   * Vérifie si un TPE existe dans la base
   */
  verifyTPE(nAffiliation: string): Observable<TPEInfo> {
    return this.http.get<TPEInfo>(`${this.apiUrl}/verify-tpe/${nAffiliation}`);
  }

  /**
   * Vérifie si un porteur existe dans la base
   */
  verifyPorteur(ncarte: string): Observable<PorteurInfo> {
    return this.http.get<PorteurInfo>(`${this.apiUrl}/verify-porteur/${ncarte}`);
  }

  /**
   * Insère des écritures comptables dans la base
   */
  insertPostings(ecritures: EcritureComptable[]): Observable<any> {
    return this.http.post(`${this.apiUrl}/insert-postings`, ecritures);
  }

  /**
   * Upload et traite un fichier bancaire
   * @param file Fichier à uploader
   * @param sessionDate Date de session au format yyyyMMdd (optionnel)
   */
  uploadFichierBancaire(file: File, sessionDate?: string): Observable<FichierBancaireResult> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    
    if (sessionDate) {
      formData.append('sessionDate', sessionDate);
    }

    return this.http.post<FichierBancaireResult>(
      `${this.fichierBancaireUrl}/upload`, 
      formData
    );
  }

  /**
   * Récupère les statistiques de traitement pour une date de session
   * @param sessionDate Date de session au format yyyyMMdd
   */
  getStatistiquesFichierBancaire(sessionDate: string): Observable<FichierBancaireStats> {
    return this.http.get<FichierBancaireStats>(
      `${this.fichierBancaireUrl}/stats/${sessionDate}`
    );
  }

  /**
   * Récupère les transactions pour une date de session
   * @param sessionDate Date de session au format yyyyMMdd
   */
  getTransactions(sessionDate: string): Observable<any> {
    return this.http.get<any>(
      `${this.fichierBancaireUrl}/transactions/${sessionDate}`
    );
  }

  /**
   * Test de l'API fichier bancaire
   */
  testApiFichierBancaire(): Observable<any> {
    return this.http.get(`${this.fichierBancaireUrl}/test`);
  }

  /**
   * Génère et télécharge un rapport PDF
   * @param sessionDate Date de session au format yyyyMMdd
   */
  telechargerRapportPDF(sessionDate: string): Observable<Blob> {
    return this.http.get(
      `${this.fichierBancaireUrl}/rapport/pdf/${sessionDate}`,
      { responseType: 'blob' }
    );
  }

  /**
   * Génère et télécharge un rapport texte
   * @param sessionDate Date de session au format yyyyMMdd
   */
  telechargerRapportTexte(sessionDate: string): Observable<Blob> {
    return this.http.get(
      `${this.fichierBancaireUrl}/rapport/text/${sessionDate}`,
      { responseType: 'blob' }
    );
  }
}
