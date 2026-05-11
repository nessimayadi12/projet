export enum StatutPanne {
  DECLAREE = 'DECLAREE',
  DIAGNOSTIQUEE = 'DIAGNOSTIQUEE',
  EN_REPARATION = 'EN_REPARATION',
  REPAREE = 'REPAREE',
  TESTEE = 'TESTEE',
  IRRECUPERABLE = 'IRRECUPERABLE'
}

export enum TypePanne {
  MATERIEL = 'MATERIEL',
  LOGICIEL = 'LOGICIEL',
  RESEAU = 'RESEAU',
  AUTRE = 'AUTRE'
}

export enum UrgencePanne {
  FAIBLE = 'FAIBLE',
  MOYENNE = 'MOYENNE',
  HAUTE = 'HAUTE',
  CRITIQUE = 'CRITIQUE'
}

export interface Panne {
  id?: number;
  tpeId: number;
  tpeNumeroSerie?: string;
  commercantNom?: string;
  typePanne: string;
  description: string;
  urgence: UrgencePanne;
  statut: StatutPanne;
  declarantId?: number;
  declarantNom?: string;
  technicienId?: number;
  technicienNom?: string;
  dateDeclaration?: Date | string;
  dateDiagnostic?: Date | string;
  dateReparation?: Date | string;
  dateResolution?: Date | string;
  diagnostic?: string;
  solution?: string;
  tpeRemplacementId?: number;
  tpeRemplacementNumeroSerie?: string;
  tempsResolutionHeures?: number;
  createdAt?: Date | string;
  updatedAt?: Date | string;
}
