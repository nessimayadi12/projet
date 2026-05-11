import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { TauxTPE } from '../models/taux-tpe.model';
import { environment } from '../../environments/environment';

/**
 * Service pour gérer les taux avec processus 4 yeux
 * Inputer: Saisit les taux
 * Authorizer: Valide/Rejette (DOIT être différent de l'Inputer)
 */
@Injectable({
  providedIn: 'root'
})
export class TauxTpeService {
  private apiUrl = `${environment.apiUrl}/taux`;

  constructor(private http: HttpClient) { }

  /**
   * Créer nouveau taux (INPUTER SEULEMENT)
   * Statut: BROUILLON
   */
  createTaux(request: {
    commercantId: number;
    nouveauTauxCommission: number;
    nouveauTauxCommissionInter: number;
    commentaire?: string;
  }): Observable<TauxTPE> {
    return this.http.post<TauxTPE>(`${this.apiUrl}`, request)
      .pipe(catchError(this.handleError));
  }

  /**
   * Soumettre taux pour validation (INPUTER SEULEMENT)
   * Statut: BROUILLON → EN_ATTENTE_VALIDATION
   */
  submitForValidation(tauxId: number): Observable<TauxTPE> {
    return this.http.post<TauxTPE>(`${this.apiUrl}/${tauxId}/soumettre`, {})
      .pipe(catchError(this.handleError));
  }

  /**
   * Valider ou rejeter taux (AUTHORIZER SEULEMENT)
   * ✅ IMPORTANT: Backend extrait automatiquement l'userId du JWT
   * ✅ RÈGLE 4 YEUX: INPUTER ≠ AUTHORIZER (imposé au backend)
   * 
   * Statut transitions:
   * - Approuver: EN_ATTENTE_VALIDATION → VALIDE (actif=true)
   * - Rejeter: EN_ATTENTE_VALIDATION → REJETE + motifRejet
   */
  validateTaux(tauxId: number, approuver: boolean, motifRejet?: string): Observable<TauxTPE> {
    const body = {
      approuver: approuver,
      motifRejet: motifRejet || null
    };
    return this.http.post<TauxTPE>(`${this.apiUrl}/${tauxId}/valider`, body)
      .pipe(catchError(this.handleError));
  }

  /**
   * Rejeter taux (méthode convenience)
   */
  rejectTaux(tauxId: number, motif: string): Observable<TauxTPE> {
    return this.validateTaux(tauxId, false, motif);
  }

  /**
   * Approuver taux (méthode convenience)
   */
  approveTaux(tauxId: number): Observable<TauxTPE> {
    return this.validateTaux(tauxId, true);
  }

  /**
   * Obtenir tous les taux en attente de validation
   * (AUTHORIZER SEULEMENT - filtré par backend via @PreAuthorize)
   */
  getTauxEnAttenteValidation(): Observable<TauxTPE[]> {
    return this.http.get<TauxTPE[]>(`${this.apiUrl}/en-attente`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Obtenir le détail d'un taux
   */
  getTauxById(tauxId: number): Observable<TauxTPE> {
    return this.http.get<TauxTPE>(`${this.apiUrl}/${tauxId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Obtenir tous les taux d'un commerçant
   */
  getTauxByCommercant(commercantId: number): Observable<TauxTPE[]> {
    return this.http.get<TauxTPE[]>(`${this.apiUrl}/commercant/${commercantId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Gestion centralisée des erreurs HTTP
   */
  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Une erreur est survenue';

    if (error.error instanceof ErrorEvent) {
      // Erreur client/réseau
      errorMessage = `Erreur: ${error.error.message}`;
    } else {
      // Erreur serveur
      switch (error.status) {
        case 400:
          errorMessage = error.error?.message || 'Requête invalide';
          break;
        case 401:
          errorMessage = 'Session expirée - reconnexion requise';
          break;
        case 403:
          errorMessage = 'Accès refusé - rôle insuffisant';
          break;
        case 404:
          errorMessage = 'Taux non trouvé';
          break;
        case 409:
          errorMessage = 'Ce taux a été modifié par quelqu\'un d\'autre';
          break;
        default:
          errorMessage = `Erreur serveur (${error.status}): ${error.error?.message}`;
      }
    }

    console.error('❌ Erreur API Taux:', errorMessage, error);
    return throwError(() => new Error(errorMessage));
  }
}
