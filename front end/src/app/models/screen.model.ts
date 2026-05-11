export interface Screen {
  id?: number;
  code: string;
  libelle: string;
  description?: string;
  route: string;
  icon?: string;
  ordre?: number;
  parentId?: number;
  actif: boolean;
  roles?: string[];
  permissions?: ScreenPermissions;
}

export interface ScreenPermissions {
  canView: boolean;
  canCreate: boolean;
  canEdit: boolean;
  canDelete: boolean;
  canExport: boolean;
}

export interface ScreenRole {
  id?: number;
  screenId: number;
  screenCode?: string;
  screenLibelle?: string;
  roleId: number;
  roleName?: string;
  canView: boolean;
  canCreate: boolean;
  canEdit: boolean;
  canDelete: boolean;
  canExport: boolean;
}

export interface UserScreens {
  username: string;
  role: string;
  roles?: string[];
  screens: Screen[];
}

export interface PermissionRequest {
  screenId: number;
  roleId: number;
  permissions: ScreenPermissions;
}
