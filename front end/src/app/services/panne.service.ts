import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Panne, StatutPanne } from '../models/panne.model';

@Injectable({
  providedIn: 'root'
})
export class PanneService {
  private apiUrl = `${environment.apiUrl}/pannes`;

  constructor(private http: HttpClient) { }

  getAllPannes(): Observable<Panne[]> {
    return this.http.get<Panne[]>(this.apiUrl);
  }

  getPanneById(id: number): Observable<Panne> {
    return this.http.get<Panne>(`${this.apiUrl}/${id}`);
  }

  getPannesByStatut(statut: StatutPanne): Observable<Panne[]> {
    return this.http.get<Panne[]>(`${this.apiUrl}/statut/${statut}`);
  }

  getPannesByTPE(tpeId: number): Observable<Panne[]> {
    return this.http.get<Panne[]>(`${this.apiUrl}/tpe/${tpeId}`);
  }

  getPannesByTechnicien(technicienId: number): Observable<Panne[]> {
    return this.http.get<Panne[]>(`${this.apiUrl}/technicien/${technicienId}`);
  }

  createPanne(panne: Panne): Observable<Panne> {
    return this.http.post<Panne>(this.apiUrl, panne);
  }

  updatePanne(id: number, panne: Panne): Observable<Panne> {
    return this.http.put<Panne>(`${this.apiUrl}/${id}`, panne);
  }

  changeStatut(id: number, statut: StatutPanne): Observable<Panne> {
    return this.http.put<Panne>(`${this.apiUrl}/${id}/statut/${statut}`, {});
  }

  assignerTechnicien(panneId: number, technicienId: number): Observable<Panne> {
    return this.http.post<Panne>(`${this.apiUrl}/${panneId}/assigner/${technicienId}`, {});
  }

  deletePanne(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Déclarer une panne
  declarerPanne(panne: Panne): Observable<Panne> {
    return this.http.post<Panne>(this.apiUrl, panne);
  }

  // Résoudre une panne
  resoudrePanne(id: number, solution: string): Observable<Panne> {
    return this.http.post<Panne>(`${this.apiUrl}/${id}/resoudre`, { solution });
  }

  // Diagnostiquer une panne
  diagnostiquer(id: number, diagnostic: string): Observable<Panne> {
    return this.http.post<Panne>(`${this.apiUrl}/${id}/diagnostiquer`, { diagnostic });
  }

  // Marquer comme en réparation
  marquerEnReparation(id: number): Observable<Panne> {
    return this.http.post<Panne>(`${this.apiUrl}/${id}/en-reparation`, {});
  }

  // Marquer comme réparée
  marquerReparee(id: number, solution: string): Observable<Panne> {
    return this.http.post<Panne>(`${this.apiUrl}/${id}/reparee`, { solution });
  }

  // Tester après réparation
  testerPanne(id: number, resultat: boolean): Observable<Panne> {
    return this.http.post<Panne>(`${this.apiUrl}/${id}/tester`, { resultat });
  }

  // Clôturer une panne
  cloturerPanne(id: number): Observable<Panne> {
    return this.http.post<Panne>(`${this.apiUrl}/${id}/cloturer`, {});
  }

  // Affecter un TPE de remplacement
  affecterTPERemplacement(panneId: number, tpeRemplacementId: number): Observable<Panne> {
    return this.http.post<Panne>(`${this.apiUrl}/${panneId}/tpe-remplacement/${tpeRemplacementId}`, {});
  }

  // Récupérer pannes par période
  getPannesByPeriode(dateDebut: string, dateFin: string): Observable<Panne[]> {
    return this.http.get<Panne[]>(`${this.apiUrl}/periode?debut=${dateDebut}&fin=${dateFin}`);
  }

  // Statistiques pannes
  getStatistiquesPannes(): Observable<any> {
    return this.http.get(`${this.apiUrl}/statistiques`);
  }

  // Temps moyen de résolution
  getTempsMoyenResolution(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/temps-moyen`);
  }

  // Export rapport pannes
  exportRapportPannes(dateDebut?: string, dateFin?: string): Observable<Blob> {
    let url = `${this.apiUrl}/export`;
    if (dateDebut && dateFin) {
      url += `?debut=${dateDebut}&fin=${dateFin}`;
    }
    return this.http.get(url, { responseType: 'blob' });
  }
}
