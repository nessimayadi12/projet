import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { Screen, ScreenPermissions, ScreenRole } from '../../models/screen.model';
import { RoleDTO, RoleService } from '../../services/role.service';
import { ScreenService } from '../../services/screen.service';

interface PermissionAction {
  key: keyof ScreenPermissions;
  label: string;
  icon: string;
}

interface PermissionHistory {
  id: number;
  dateAction: string;
  username: string;
  action: string;
  actionLabel?: string;
  details: string;
  changes?: Array<{ field: string; oldValue: any; newValue: any }>;
}

@Component({
  selector: 'app-screen-management',
  templateUrl: './screen-management.component.html',
  styleUrls: ['./screen-management.component.css']
})
export class ScreenManagementComponent implements OnInit {
  screens: Screen[] = [];
  filteredScreens: Screen[] = [];
  roles: RoleDTO[] = [];
  history: PermissionHistory[] = [];
  loading = true;
  copying = false;
  showHistory = false;
  searchTerm = '';
  sourceRoleId: number | null = null;
  targetRoleId: number | null = null;
  private matrix = new Map<string, ScreenRole>();
  private savingCells = new Set<string>();

  readonly actions: PermissionAction[] = [
    { key: 'canView', label: 'Voir', icon: 'visibility' },
    { key: 'canCreate', label: 'Créer', icon: 'add_circle' },
    { key: 'canEdit', label: 'Modifier', icon: 'edit' },
    { key: 'canDelete', label: 'Supprimer', icon: 'delete' },
    { key: 'canExport', label: 'Exporter', icon: 'download' }
  ];

  private readonly roleLabels: { [key: string]: string } = {
    ROLE_ADMIN: 'Administrateur',
    ROLE_MONETIQUE: 'Monétique',
    ROLE_AGENCE: 'Agence'
  };

  constructor(
    private screenService: ScreenService,
    private roleService: RoleService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadMatrix();
  }

  loadMatrix(): void {
    this.loading = true;
    forkJoin({
      screens: this.screenService.getAllScreens(),
      roles: this.roleService.getAllRoles(),
      matrix: this.screenService.getPermissionMatrix()
    }).subscribe({
      next: ({ screens, roles, matrix }) => {
        this.screens = screens
          .filter(screen => screen.actif)
          .sort((a, b) => (a.ordre || 999) - (b.ordre || 999));
        const order = ['ROLE_ADMIN', 'ROLE_MONETIQUE', 'ROLE_AGENCE'];
        this.roles = roles
          .filter(role => order.includes(role.name))
          .sort((a, b) => order.indexOf(a.name) - order.indexOf(b.name))
          .map(role => ({ ...role, label: this.roleLabels[role.name] || role.name }));
        this.matrix.clear();
        matrix.forEach(item => this.matrix.set(this.cellKey(item.screenId, item.roleId), item));
        this.filterScreens();
        this.sourceRoleId = this.sourceRoleId || this.roles[0]?.id || null;
        this.targetRoleId = this.targetRoleId || this.roles[1]?.id || null;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.notify('Impossible de charger la matrice des permissions', true);
      }
    });
  }

  filterScreens(): void {
    const term = this.searchTerm.trim().toLowerCase();
    this.filteredScreens = !term ? this.screens : this.screens.filter(screen =>
      screen.libelle.toLowerCase().includes(term) ||
      screen.code.toLowerCase().includes(term) ||
      screen.route.toLowerCase().includes(term)
    );
  }

  isGranted(screen: Screen, role: RoleDTO, action: keyof ScreenPermissions): boolean {
    const permission = this.matrix.get(this.cellKey(screen.id!, role.id));
    return permission ? permission[action] === true : false;
  }

  isSaving(screen: Screen, role: RoleDTO): boolean {
    return this.savingCells.has(this.cellKey(screen.id!, role.id));
  }

  togglePermission(screen: Screen, role: RoleDTO, action: keyof ScreenPermissions): void {
    if (!screen.id || this.isSaving(screen, role)) {
      return;
    }

    const key = this.cellKey(screen.id, role.id);
    const current = this.matrix.get(key);
    const previous = current ? { ...current } : undefined;
    const permissions = this.toPermissions(current);
    permissions[action] = !permissions[action];

    if (action === 'canView' && !permissions.canView) {
      permissions.canCreate = false;
      permissions.canEdit = false;
      permissions.canDelete = false;
      permissions.canExport = false;
    } else if (action !== 'canView' && permissions[action]) {
      permissions.canView = true;
    }

    this.matrix.set(key, {
      ...(current || { screenId: screen.id, roleId: role.id }),
      ...permissions
    });
    this.savingCells.add(key);

    this.screenService.assignRoleToScreen(screen.id, role.id, permissions).subscribe({
      next: saved => {
        this.matrix.set(key, saved);
        this.savingCells.delete(key);
        this.notify(`${screen.libelle} · ${role.label} mis à jour`);
        if (this.showHistory) {
          this.loadHistory();
        }
      },
      error: () => {
        if (previous) {
          this.matrix.set(key, previous);
        } else {
          this.matrix.delete(key);
        }
        this.savingCells.delete(key);
        this.notify('La permission n’a pas pu être enregistrée', true);
      }
    });
  }

  copyProfile(): void {
    if (!this.sourceRoleId || !this.targetRoleId || this.sourceRoleId === this.targetRoleId) {
      this.notify('Choisissez deux profils différents', true);
      return;
    }
    const source = this.roles.find(role => role.id === this.sourceRoleId);
    const target = this.roles.find(role => role.id === this.targetRoleId);
    if (!confirm(`Copier toutes les permissions ${source?.label} vers ${target?.label} ?`)) {
      return;
    }

    this.copying = true;
    this.screenService.copyRoleProfile(this.sourceRoleId, this.targetRoleId).subscribe({
      next: copied => {
        this.screens.forEach(screen => this.matrix.delete(this.cellKey(screen.id!, this.targetRoleId!)));
        copied.forEach(item => this.matrix.set(this.cellKey(item.screenId, item.roleId), item));
        this.copying = false;
        this.notify(`${copied.length} permissions copiées vers ${target?.label}`);
        this.loadHistory();
      },
      error: () => {
        this.copying = false;
        this.notify('La copie du profil a échoué', true);
      }
    });
  }

  toggleHistory(): void {
    this.showHistory = !this.showHistory;
    if (this.showHistory) {
      this.loadHistory();
    }
  }

  loadHistory(): void {
    this.screenService.getPermissionHistory(0, 20).subscribe({
      next: response => this.history = response.content || [],
      error: () => this.notify('Impossible de charger l’historique', true)
    });
  }

  trackScreen(_: number, screen: Screen): number | string {
    return screen.id || screen.code;
  }

  private cellKey(screenId: number, roleId: number): string {
    return `${screenId}:${roleId}`;
  }

  private toPermissions(item?: ScreenRole): ScreenPermissions {
    return {
      canView: item?.canView === true,
      canCreate: item?.canCreate === true,
      canEdit: item?.canEdit === true,
      canDelete: item?.canDelete === true,
      canExport: item?.canExport === true
    };
  }

  private notify(message: string, error = false): void {
    this.snackBar.open(message, 'Fermer', {
      duration: error ? 5000 : 2500,
      panelClass: error ? ['snackbar-error'] : ['snackbar-success']
    });
  }
}