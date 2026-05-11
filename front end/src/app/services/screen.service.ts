import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Screen, ScreenPermissions, ScreenRole, UserScreens } from '../models/screen.model';

@Injectable({
  providedIn: 'root'
})
export class ScreenService {
  private apiUrl = `${environment.apiUrl}/screens`;
  
  // Cache des screens de l'utilisateur
  private userScreensSubject = new BehaviorSubject<Screen[]>([]);
  public userScreens$ = this.userScreensSubject.asObservable();
  
  // Cache des permissions par screen
  private permissionsCache = new Map<string, ScreenPermissions>();

  constructor(private http: HttpClient) { }

  // Récupérer tous les screens
  getAllScreens(): Observable<Screen[]> {
    return this.http.get<Screen[]>(this.apiUrl);
  }

  // Récupérer les screens actifs
  getActiveScreens(): Observable<Screen[]> {
    return this.http.get<Screen[]>(`${this.apiUrl}/active`);
  }

  // Récupérer un screen par ID
  getScreenById(id: number): Observable<Screen> {
    return this.http.get<Screen>(`${this.apiUrl}/${id}`);
  }

  // Récupérer un screen par code
  getScreenByCode(code: string): Observable<Screen> {
    return this.http.get<Screen>(`${this.apiUrl}/code/${code}`);
  }

  // Récupérer les screens d'un utilisateur spécifique
  getScreensForUser(username: string): Observable<UserScreens> {
    return this.http.get<UserScreens>(`${this.apiUrl}/user/${username}`);
  }

  // Récupérer les screens de l'utilisateur connecté
  getMyScreens(): Observable<UserScreens> {
    return this.http.get<UserScreens>(`${this.apiUrl}/me`).pipe(
      tap(userScreens => {
        this.userScreensSubject.next(userScreens.screens);
        // Mettre en cache les permissions de chaque screen
        userScreens.screens.forEach(screen => {
          if (screen.permissions) {
            this.permissionsCache.set(screen.code, screen.permissions);
          }
        });
      })
    );
  }

  // Récupérer les permissions de l'utilisateur sur un screen
  getMyPermissions(screenCode: string): Observable<ScreenPermissions> {
    // Vérifier le cache d'abord
    const cached = this.permissionsCache.get(screenCode);
    if (cached) {
      return new Observable(observer => {
        observer.next(cached);
        observer.complete();
      });
    }

    return this.http.get<ScreenPermissions>(`${this.apiUrl}/permissions/${screenCode}`).pipe(
      tap(permissions => {
        this.permissionsCache.set(screenCode, permissions);
      })
    );
  }

  // Vérifier si l'utilisateur peut accéder à un screen
  canAccessScreen(screenCode: string): Observable<boolean> {
    return this.getMyPermissions(screenCode).pipe(
      map(permissions => permissions.canView === true)
    );
  }

  // Vérifier une permission spécifique
  hasPermission(screenCode: string, permissionType: 'canView' | 'canCreate' | 'canEdit' | 'canDelete' | 'canExport'): Observable<boolean> {
    return this.getMyPermissions(screenCode).pipe(
      map(permissions => permissions[permissionType] === true)
    );
  }

  // Créer un nouveau screen (Admin seulement)
  createScreen(screen: Screen): Observable<Screen> {
    return this.http.post<Screen>(this.apiUrl, screen);
  }

  // Mettre à jour un screen (Admin seulement)
  updateScreen(id: number, screen: Screen): Observable<Screen> {
    return this.http.put<Screen>(`${this.apiUrl}/${id}`, screen);
  }

  // Supprimer un screen (Admin seulement)
  deleteScreen(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Assigner un rôle à un screen avec permissions
  assignRoleToScreen(screenId: number, roleId: number, permissions: ScreenPermissions): Observable<ScreenRole> {
    return this.http.post<ScreenRole>(`${this.apiUrl}/${screenId}/roles/${roleId}`, permissions);
  }

  // Retirer un rôle d'un screen
  removeRoleFromScreen(screenId: number, roleId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${screenId}/roles/${roleId}`);
  }

  // Récupérer tous les rôles d'un screen
  getScreenRoles(screenId: number): Observable<ScreenRole[]> {
    return this.http.get<ScreenRole[]>(`${this.apiUrl}/${screenId}/roles`);
  }

  // Vider le cache
  clearCache(): void {
    this.permissionsCache.clear();
    this.userScreensSubject.next([]);
  }

  // Recharger les screens de l'utilisateur
  refreshUserScreens(): void {
    this.getMyScreens().subscribe();
  }
}
