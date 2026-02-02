export enum StatutTaux {
  BROUILLON = 'BROUILLON',
  EN_ATTENTE_VALIDATION = 'EN_ATTENTE_VALIDATION',
  VALIDE = 'VALIDE',
  REJETE = 'REJETE'
}

export interface TauxTPE {
  id?: number;
  tpeId: number;
  numeroTerminal?: string;
  ancienTauxCommission?: number;
  ancienTauxCommissionInter?: number;
  nouveauTauxCommission: number;
  nouveauTauxCommissionInter: number;
  statut: StatutTaux;
  inputerId?: number;
  inputerNom?: string;
  dateSaisie?: Date | string;
  authorizerId?: number;
  authorizerNom?: string;
  dateValidation?: Date | string;
  motifRejet?: string;
  commentaires?: string;
}
