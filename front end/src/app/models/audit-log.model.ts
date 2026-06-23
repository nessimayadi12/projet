export interface AuditFieldChange {
  field: string;
  oldValue: any;
  newValue: any;
}

export interface AuditLog {
  id: number;
  dateAction: string;
  username: string;
  actorUserId?: number;
  actorRoles?: string;
  action: string;
  actionLabel?: string;
  moduleName?: string;
  entityType: string;
  entityId?: string;
  entityReference?: string;
  details?: string;
  oldValues?: { [key: string]: any };
  newValues?: { [key: string]: any };
  changes?: AuditFieldChange[];
  ipAddress?: string;
  userAgent?: string;
  statut?: string;
  riskLevel?: string;
  requestMethod?: string;
  requestUri?: string;
  correlationId?: string;
}

export interface AuditStats {
  totalActions: number;
  actionsReussies: number;
  actionsEchouees: number;
  creations: number;
  modifications: number;
  validations: number;
  rejets: number;
  affectations: number;
  actionsAujourdhui: number;
}

export interface AuditPage {
  content: AuditLog[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
