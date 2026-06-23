import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, LoginRequest, Utilisateur, Role } from '../models/utilisateur.model';
import { ScreenService } from './screen.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private currentUserSubject: BehaviorSubject<Utilisateur | null>;
  public currentUser: Observable<Utilisateur | null>;

  constructor(private http: HttpClient, private screenService: ScreenService) {
    sessionStorage.setItem('tabOpen', 'true');
    
    const storedUser = localStorage.getItem('currentUser');
    this.currentUserSubject = new BehaviorSubject<Utilisateur | null>(
      storedUser ? JSON.parse(storedUser) : null
    );
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): Utilisateur | null {
    return this.currentUserSubject.value;
  }

  public get token(): string | null {
    const token = localStorage.getItem('token');
    return token && token !== 'undefined' && token !== 'null' ? token : null;
  }

  public getCurrentUser(): Utilisateur | null {
    return this.currentUserValue;
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<any>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(response => {
          this.screenService.clearCache();
          localStorage.setItem('token', response.token);
          
          // Le backend renvoie "roles" (pluriel) comme tableau
          const roles = this.normalizeRoles(response.roles || response.role);
          const primaryRole = this.resolvePrimaryRole(roles);
          
          // Si c'est un tableau, prendre le premier élément
          
          // Convertir le rôle du backend (ROLE_AGENCE) vers l'enum (AGENCE)
          
          const user: Utilisateur = {
            id: response.id,
            username: response.username,
            email: response.email,
            role: primaryRole,
            roles,
            nom: '',
            prenom: ''
          };
          localStorage.setItem('currentUser', JSON.stringify(user));
          sessionStorage.setItem('tabOpen', 'true');
          this.currentUserSubject.next(user);
        })
      );
  }

  register(userData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, userData);
  }

  logout(): void {
    this.screenService.clearCache();
    localStorage.removeItem('token');
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  isLoggedIn(): boolean {
    return !!this.token;
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
