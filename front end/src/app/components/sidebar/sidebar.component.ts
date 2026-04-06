import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Role } from '../../models/utilisateur.model';

declare const $: any;
declare interface RouteInfo {
    path: string;
    title: string;
    icon: string;
    class: string;
    roles?: Role[];
}
export const ROUTES: RouteInfo[] = [
    { path: '/dashboard', title: 'Tableau de Bord',  icon: 'dashboard', class: '', roles: [Role.ADMIN, Role.MONETIQUE] },
    { path: '/tpe', title: 'Gestion TPE',  icon:'devices', class: '' },
  { path: '/tpe/imports', title: 'Lignes importées',  icon:'table_view', class: '', roles: [Role.ADMIN, Role.MONETIQUE] },
    { path: '/commercants', title: 'Commerçants',  icon:'store', class: '' },
    { path: '/demandes', title: 'Demandes TPE',  icon:'assignment', class: '' },
    { path: '/pannes', title: 'Maintenance',  icon:'build', class: '' },
    { path: '/file-upload', title: 'Upload Transactions',  icon:'cloud_upload', class: '', roles: [Role.ADMIN, Role.MONETIQUE] },
    { path: '/admin/screens', title: 'Gestion Permissions',  icon:'admin_panel_settings', class: '', roles: [Role.ADMIN] },
    { path: '/user-profile', title: 'Mon Profil',  icon:'person', class: '' }
];

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  menuItems: any[];

  constructor(private authService: AuthService) { }

  ngOnInit() {
    this.menuItems = ROUTES.filter(menuItem => {
      if (menuItem.roles && menuItem.roles.length > 0) {
        return this.authService.hasAnyRole(menuItem.roles);
      }
      return true;
    });
  }
  isMobileMenu() {
      if ($(window).width() > 991) {
          return false;
      }
      return true;
  };
}
