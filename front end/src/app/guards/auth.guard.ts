import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ScreenService } from '../services/screen.service';
import { Role } from '../models/utilisateur.model';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private authService: AuthService, 
    private screenService: ScreenService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | boolean {
    if (!this.authService.isLoggedIn()) {
      // Pas authentifié, rediriger vers login
      this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
      return false;
    }

    // Si un screenCode est spécifié dans les données de la route, utiliser le système de permissions dynamiques
    if (route.data['screenCode']) {
      const screenCode = route.data['screenCode'];
      return this.screenService.canAccessScreen(screenCode).pipe(
        map(canAccess => {
          if (canAccess) {
            return true;
          } else {
            console.warn(`Accès refusé à l'écran ${screenCode}`);
            this.redirectToDefaultPage();
            return false;
          }
        }),
        catchError((error) => {
          // En cas d'erreur API (backend non disponible), refuser l'accès par sécurité
          console.error(`Erreur lors de la vérification des permissions pour ${screenCode}:`, error);
          this.redirectToDefaultPage();
          return of(false);
        })
      );
    }

    // Aucune restriction, autoriser l'accès
    return true;
  }

  private redirectToDefaultPage(): void {
    // Rediriger vers le profil utilisateur que tous les rôles peuvent voir
    this.router.navigate(['/user-profile']);
  }
}
