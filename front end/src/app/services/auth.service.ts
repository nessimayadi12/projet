import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, finalize, map, shareReplay, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AuthResponse, LoginRequest, Utilisateur, Role } from '../models/utilisateur.model';
import { ScreenService } from './screen.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private currentUserSubject: BehaviorSubject<Utilisateur | null>;
  private sessionCheck$?: Observable<Utilisateur | null>;
  public currentUser: Observable<Utilisateur | null>;

  constructor(private http: HttpClient, private screenService: ScreenService) {
    sessionStorage.setItem('tabOpen', 'true');
    this.clearLegacyLocalStorage();

    this.currentUserSubject = new BehaviorSubject<Utilisateur | null>(null);
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): Utilisateur | null {
    return this.currentUserSubject.value;
  }

  public getCurrentUser(): Utilisateur | null {
    return this.currentUserValue;
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(response => this.setAuthenticatedUser(response))
      );
  }

  loadCurrentUser(force = false): Observable<Utilisateur | null> {
    if (!force && this.currentUserValue) {
      return of(this.currentUserValue);
    }
    if (!force && this.sessionCheck$) {
      return this.sessionCheck$;
    }

    this.sessionCheck$ = this.http.get<AuthResponse>(`${this.apiUrl}/me`).pipe(
      map(response => this.toUser(response)),
      tap(user => {
        this.currentUserSubject.next(user);
        sessionStorage.setItem('tabOpen', 'true');
      }),
      catchError(() => {
        this.clearClientState();
        return of(null);
      }),
      finalize(() => this.sessionCheck$ = undefined),
      shareReplay(1)
    );

    return this.sessionCheck$;
  }

  register(userData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, userData);
  }

  logout(): void {
    this.clearClientState();
    this.http.post<void>(`${this.apiUrl}/logout`, {}).subscribe({
      error: () => undefined
    });
  }

  isLoggedIn(): boolean {
    return !!this.currentUserValue;
  }

  hasRole(roles: Role[]): boolean {
    const user = this.currentUserValue;
    if (!user) {
      return false;
    }

    const userRoles = user.roles && user.roles.length ? user.roles : [user.role];
    return roles.some(role => userRoles.includes(role));
  }

  hasAnyRole(roles: Role[]): boolean {
    return this.hasRole(roles);
  }

  private setAuthenticatedUser(response: AuthResponse): void {
    this.screenService.clearCache();
    const user = this.toUser(response);
    sessionStorage.setItem('tabOpen', 'true');
    this.currentUserSubject.next(user);
  }

  private toUser(response: AuthResponse): Utilisateur {
    const roles = this.normalizeRoles(response.roles || response.role);
    const primaryRole = this.resolvePrimaryRole(roles);

    return {
      id: response.id,
      username: response.username,
      email: response.email,
      role: primaryRole,
      roles,
      nom: '',
      prenom: ''
    };
  }

  private clearClientState(): void {
    this.screenService.clearCache();
    this.currentUserSubject.next(null);
    this.clearLegacyLocalStorage();
  }

  private clearLegacyLocalStorage(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('currentUser');
  }

  private normalizeRoles(roleValue: any): Role[] {
    const values = Array.isArray(roleValue) ? roleValue : [roleValue];
    const roles = values
      .filter(role => !!role)
      .map(role => String(role).replace(/^ROLE_/, '') as Role)
      .filter((role, index, allRoles) => Object.values(Role).includes(role) && allRoles.indexOf(role) === index);

    return roles.length ? roles : [Role.AGENCE];
  }

  private resolvePrimaryRole(roles: Role[]): Role {
    const priority = [
      Role.ADMIN,
      Role.MONETIQUE,
      Role.AGENCE
    ];

    return priority.find(role => roles.includes(role)) || roles[0] || Role.AGENCE;
  }
}
