import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Role } from '../../models/utilisateur.model';
import { ScreenService } from '../../services/screen.service';

declare interface RouteInfo {
    path: string;
    title: string;
    icon: string;
    class: string;
    roles?: Role[];
    screenCode?: string;
}

export const ROUTES: RouteInfo[] = [
    { path: '/dashboard', title: 'Tableau de Bord', icon: 'dashboard', class: '', roles: [Role.ADMIN, Role.MONETIQUE], screenCode: 'DASHBOARD' },
    { path: '/tpe', title: 'Gestion TPE', icon: 'devices', class: '', screenCode: 'LISTE_TPE' },
    { path: '/tpe/imports', title: 'Lignes importees', icon: 'table_view', class: '', roles: [Role.ADMIN, Role.MONETIQUE], screenCode: 'LISTE_TPE' },
    { path: '/commercants', title: 'Commercants', icon: 'store', class: '', screenCode: 'LISTE_COMMERCANTS' },
    { path: '/demandes', title: 'Demandes TPE', icon: 'assignment', class: '', screenCode: 'LISTE_DEMANDES' },
    { path: '/pannes', title: 'Maintenance', icon: 'build', class: '', screenCode: 'LISTE_PANNES' },
    { path: '/assistant-ia', title: 'Assistant IA', icon: 'psychology', class: '', roles: [Role.ADMIN, Role.MONETIQUE, Role.AGENCE], screenCode: 'ASSISTANT_IA' },
    { path: '/file-upload', title: 'Upload Transactions', icon: 'cloud_upload', class: '', roles: [Role.ADMIN, Role.MONETIQUE], screenCode: 'UPLOAD_FICHIER_BANCAIRE' },
    { path: '/audit', title: 'Audit avance', icon: 'manage_search', class: '', roles: [Role.ADMIN, Role.MONETIQUE], screenCode: 'AUDIT_AVANCE' },
    { path: '/admin/screens', title: 'Gestion Permissions', icon: 'admin_panel_settings', class: '', roles: [Role.ADMIN], screenCode: 'GESTION_PERMISSIONS' }
];

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  menuItems: RouteInfo[] = [];

  constructor(
    private authService: AuthService,
    private screenService: ScreenService
  ) { }

  ngOnInit(): void {
    this.menuItems = this.filterByRoles(ROUTES);

    this.screenService.ensurePermissionsLoaded().subscribe({
      next: (userScreens) => {
        const allowedScreens = new Set(userScreens.screens.map(screen => screen.code));
        this.menuItems = this.filterByRoles(ROUTES)
          .filter(menuItem => !menuItem.screenCode || allowedScreens.has(menuItem.screenCode));
      },
      error: () => {
        this.menuItems = this.filterByRoles(ROUTES);
      }
    });
  }

  private filterByRoles(routes: RouteInfo[]): RouteInfo[] {
    return routes.filter(menuItem => {
      if (menuItem.roles && menuItem.roles.length > 0) {
        return this.authService.hasAnyRole(menuItem.roles);
      }
      return true;
    });
  }
}
