import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ScreenPermissions } from '../models/screen.model';
import { ScreenService } from '../services/screen.service';

@Injectable({ providedIn: 'root' })
export class PermissionGuard implements CanActivate {
  constructor(private screenService: ScreenService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    const screenCode = route.data['screenCode'] as string;
    const permission = (route.data['permission'] || 'canView') as keyof ScreenPermissions;

    if (!screenCode) {
      return of(false);
    }

    return this.screenService.ensurePermissionsLoaded().pipe(
      map(() => {
        const canView = this.screenService.hasPermissionCached(screenCode, 'canView');
        const hasAction = permission === 'canView' || this.screenService.hasPermissionCached(screenCode, permission);
        if (canView && hasAction) {
          return true;
        }
        if (!state.url.startsWith('/user-profile')) {
          this.router.navigate(['/user-profile'], {
            queryParams: { accessDenied: screenCode, returnUrl: state.url }
          });
        }
        return false;
      }),
      catchError(() => {
        if (!state.url.startsWith('/user-profile')) {
          this.router.navigate(['/user-profile'], { queryParams: { accessDenied: screenCode } });
        }
        return of(false);
      })
    );
  }
}