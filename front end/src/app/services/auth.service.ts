import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, LoginRequest, Utilisateur, Role } from '../models/utilisateur.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private currentUserSubject: BehaviorSubject<Utilisateur | null>;
  public currentUser: Observable<Utilisateur | null>;

  constructor(private http: HttpClient) {
    // Nettoyer la session si aucun onglet n'est actif
    if (!sessionStorage.getItem('tabOpen')) {
      localStorage.removeItem('token');
      localStorage.removeItem('currentUser');
    }
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
    return localStorage.getItem('token');
  }

  public getCurrentUser(): Utilisateur | null {
    return this.currentUserValue;
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<any>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(response => {
          localStorage.setItem('token', response.token);
          
          // Le backend renvoie "roles" (pluriel) comme tableau
          let roleValue = response.roles || response.role;
          
          // Si c'est un tableau, prendre le premier élément
          if (Array.isArray(roleValue)) {
            roleValue = roleValue[0];
          }
          
          // Convertir le rôle du backend (ROLE_AGENCE) vers l'enum (AGENCE)
          if (typeof roleValue === 'string' && roleValue.startsWith('ROLE_')) {
            roleValue = roleValue.replace('ROLE_', '');
          }
          
          const user: Utilisateur = {
            username: response.username,
            email: response.email,
            role: roleValue as Role,
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
    localStorage.removeItem('token');
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  isLoggedIn(): boolean {
    return !!this.token;
  }

  hasRole(roles: Role[]): boolean {
    const user = this.currentUserValue;
    return user ? roles.includes(user.role) : false;
  }

  hasAnyRole(roles: Role[]): boolean {
    return this.hasRole(roles);
  }
}
