import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/utilisateur.model';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (this.authService.isLoggedIn()) {
      // Vérifier les rôles si spécifiés dans la route
      if (route.data['roles']) {
        const roles = route.data['roles'];
        if (this.authService.hasAnyRole(roles)) {
          return true;
        } else {
          // Rediriger vers une page accessible selon le rôle
          const currentUser = this.authService.getCurrentUser();
          if (currentUser?.role === Role.AGENCE) {
            this.router.navigate(['/demandes']);
          } else if (currentUser?.role === Role.LOGISTIQUE) {
            this.router.navigate(['/tpe']);
          } else {
            this.router.navigate(['/dashboard']);
          }
          return false;
        }
      }
      return true;
    }

    // Pas authentifié, rediriger vers login
    this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }
}
