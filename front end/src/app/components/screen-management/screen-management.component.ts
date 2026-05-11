import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ScreenService } from '../../services/screen.service';
import { AuthService } from '../../services/auth.service';
import { RoleService, RoleDTO } from '../../services/role.service';
import { Screen, ScreenRole, ScreenPermissions } from '../../models/screen.model';
import { Role } from '../../models/utilisateur.model';

@Component({
  selector: 'app-screen-management',
  templateUrl: './screen-management.component.html',
  styleUrls: ['./screen-management.component.css']
})
export class ScreenManagementComponent implements OnInit {
  screens: Screen[] = [];
  selectedScreen: Screen | null = null;
  screenRoles: ScreenRole[] = [];
  loading = false;
  isAdmin = false;

  // Liste des rôles disponibles (chargée dynamiquement depuis le backend)
  availableRoles: RoleDTO[] = [];
  allRoles: RoleDTO[] = [];

  // Labels pour les rôles
  private roleLabels: { [key: string]: string } = {
    'ROLE_ADMIN': 'Administrateur',
    'ROLE_MONETIQUE': 'Monétique',
    'ROLE_AGENCE': 'Agence',
    'ROLE_INPUTER': 'Saisisseur',
    'ROLE_AUTHORIZER': 'Valideur',
    'ROLE_COMMERCANT': 'Commerçant',
  };

  // Formulaire pour ajouter/modifier un screen
  screenForm: Screen = {
    code: '',
    libelle: '',
    description: '',
    route: '',
    icon: '',
    ordre: 0,
    actif: true
  };

  // Formulaire pour assigner des permissions
  permissionForm: any = {
    roleId: null,
    permissions: {
      canView: true,
      canCreate: false,
      canEdit: false,
      canDelete: false,
      canExport: false
    }
  };

  constructor(
    private screenService: ScreenService,
    private authService: AuthService,
    private roleService: RoleService,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    this.isAdmin = this.authService.hasAnyRole([Role.ADMIN]);

    if (!this.isAdmin) {
      this.showNotification('Accès refusé - Vous devez être administrateur pour accéder à cette page', 'error');
      return;
    }

    this.loadRoles();
    this.loadScreens();
  }

  loadRoles(): void {
    this.roleService.getAllRoles().subscribe({
      next: (roles) => {
        // Ajouter les labels aux rôles
        this.allRoles = roles.map(role => ({
          ...role,
          label: this.roleLabels[role.name] || role.name
        }));
        this.updateAvailableRoles();
      },
      error: (error) => {
        this.showNotification('Erreur - Impossible de charger les rôles', 'error');
      }
    });
  }

  loadScreens(): void {
    this.loading = true;
    this.screenService.getAllScreens().subscribe({
      next: (screens) => {
        this.screens = screens;
        this.loading = false;
      },
      error: (error) => {
        this.showNotification('Erreur - Impossible de charger les screens', 'error');
        this.loading = false;
      }
    });
  }

  selectScreen(screen: Screen): void {
    this.selectedScreen = screen;
    this.screenRoles = [];
    this.resetPermissionForm();
    if (screen.id) {
      this.loadScreenRoles(screen.id);
    }
  }

  loadScreenRoles(screenId: number): void {
    this.screenService.getScreenRoles(screenId).subscribe({
      next: (roles) => {
        this.screenRoles = roles;
        this.updateAvailableRoles();
      },
      error: (error) => {
        this.showNotification('Erreur - Impossible de charger les permissions du screen', 'error');
      }
    });
  }

  // Filtrer les rôles déjà assignés au screen sélectionné
  updateAvailableRoles(): void {
    if (!this.allRoles.length) {
      return;
    }

    if (!this.selectedScreen || !this.screenRoles.length) {
      this.availableRoles = [...this.allRoles];
      return;
    }

    const assignedRoleIds = this.screenRoles.map(sr => sr.roleId);
    this.availableRoles = this.allRoles.filter(role => !assignedRoleIds.includes(role.id));
  }

  createScreen(): void {
    if (!this.validateScreenForm()) {
      return;
    }

    this.loading = true;
    this.screenService.createScreen(this.screenForm).subscribe({
      next: (screen) => {
        this.showNotification('Screen créé avec succès', 'success');
        this.loadScreens();
        this.resetScreenForm();
        this.loading = false;
      },
      error: (error) => {
        this.showNotification('Erreur - Impossible de créer le screen', 'error');
        this.loading = false;
      }
    });
  }

  updateScreen(): void {
    if (!this.selectedScreen || !this.selectedScreen.id) {
      return;
    }

    this.loading = true;
    this.screenService.updateScreen(this.selectedScreen.id, this.selectedScreen).subscribe({
      next: (screen) => {
        this.showNotification('Screen modifié avec succès', 'success');
        this.loadScreens();
        this.loading = false;
      },
      error: (error) => {
        this.showNotification('Erreur - Impossible de modifier le screen', 'error');
        this.loading = false;
      }
    });
  }

  deleteScreen(screenId: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce screen ?')) {
      this.loading = true;
      this.screenService.deleteScreen(screenId).subscribe({
        next: () => {
          this.showNotification('Screen supprimé avec succès', 'success');
          this.loadScreens();
          this.selectedScreen = null;
          this.loading = false;
        },
        error: (error) => {
          this.showNotification('Erreur - Impossible de supprimer le screen', 'error');
          this.loading = false;
        }
      });
    }
  }

  assignRoleToScreen(): void {
    if (!this.selectedScreen || !this.selectedScreen.id || !this.permissionForm.roleId) {
      this.showNotification('Erreur - Veuillez sélectionner un screen et un rôle', 'error');
      return;
    }

    this.loading = true;
    this.screenService.assignRoleToScreen(
      this.selectedScreen.id,
      this.permissionForm.roleId,
      this.permissionForm.permissions
    ).subscribe({
      next: (screenRole) => {
        this.showNotification('Permissions assignées avec succès', 'success');
        // Vider le cache pour forcer le rechargement des permissions
        this.screenService.clearCache();
        this.loadScreenRoles(this.selectedScreen!.id!);
        this.resetPermissionForm();
        this.loading = false;
      },
      error: (error) => {
        this.showNotification('Erreur - Impossible d\'assigner les permissions', 'error');
        this.loading = false;
      }
    });
  }

  removeRoleFromScreen(screenId: number, roleId: number): void {
    if (confirm('Êtes-vous sûr de vouloir retirer ce rôle ?')) {
      this.loading = true;
      this.screenService.removeRoleFromScreen(screenId, roleId).subscribe({
        next: () => {
          this.showNotification('Rôle retiré avec succès', 'success');
          // Vider le cache pour forcer le rechargement des permissions
          this.screenService.clearCache();
          this.loadScreenRoles(screenId);
          this.loading = false;
        },
        error: (error) => {
          this.showNotification('Erreur - Impossible de retirer le rôle', 'error');
          this.loading = false;
        }
      });
    }
  }

  validateScreenForm(): boolean {
    if (!this.screenForm.code || !this.screenForm.libelle || !this.screenForm.route) {
      this.showNotification('Erreur - Veuillez remplir tous les champs obligatoires', 'error');
      return false;
    }
    return true;
  }

  resetScreenForm(): void {
    this.screenForm = {
      code: '',
      libelle: '',
      description: '',
      route: '',
      icon: '',
      ordre: 0,
      actif: true
    };
  }

  resetPermissionForm(): void {
    this.permissionForm = {
      roleId: null,
      permissions: {
        canView: true,
        canCreate: false,
        canEdit: false,
        canDelete: false,
        canExport: false
      }
    };
  }

  editScreen(screen: Screen): void {
    this.selectedScreen = { ...screen };
  }

  cancelEdit(): void {
    this.selectedScreen = null;
    this.resetScreenForm();
  }

  private showNotification(message: string, type: 'success' | 'error' | 'info'): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: [`snackbar-${type}`]
    });
  }
}
