import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Notification {
  id?: number;
  titre: string;
  message: string;
  type: 'INFO' | 'SUCCESS' | 'WARNING' | 'ERROR';
  destinataireId: number;
  destinataireEmail?: string;
  dateEnvoi?: Date | string;
  lu?: boolean;
  demandeId?: number;
  panneId?: number;
  tpeId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = `${environment.apiUrl}/notifications`;

  constructor(private http: HttpClient) { }

  // R\u00e9cup\u00e9rer toutes les notifications de l'utilisateur
  getMyNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/mes-notifications`);
  }

  // R\u00e9cup\u00e9rer les notifications non lues
  getNotificationsNonLues(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/non-lues`);
  }

  // Nombre de notifications non lues
  getNombreNonLues(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/nombre-non-lues`);
  }

  // Marquer comme lue
  marquerCommeLue(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/marquer-lue`, {});
  }

  // Marquer toutes comme lues
  marquerToutesCommeLues(): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/marquer-toutes-lues`, {});
  }

  // Supprimer une notification
  supprimerNotification(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Envoyer une notification (pour admin/syst\u00e8me)
  envoyerNotification(notification: Notification): Observable<Notification> {
    return this.http.post<Notification>(this.apiUrl, notification);
  }
}
