export enum Role {
  ADMIN = 'ADMIN',
  MONETIQUE = 'MONETIQUE',
  AGENCE = 'AGENCE',
  INPUTER = 'INPUTER',
  AUTHORIZER = 'AUTHORIZER',
  TECHNICIEN = 'TECHNICIEN',
  COMMERCANT = 'COMMERCANT',
  LOGISTIQUE = 'LOGISTIQUE'
}

export interface Utilisateur {
  id?: number;
  username: string;
  email: string;
  nom: string;
  prenom: string;
  role: Role;
  actif?: boolean;
  createdAt?: Date | string;
  updatedAt?: Date | string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  username: string;
  email: string;
  role: Role;
}
