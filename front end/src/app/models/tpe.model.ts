export enum StatutTPE {
  DISPONIBLE = 'DISPONIBLE',
  RESERVE = 'RESERVE',
  AFFECTE = 'AFFECTE',
  EN_PANNE = 'EN_PANNE',
  MAINTENANCE = 'MAINTENANCE',
  HORS_SERVICE = 'HORS_SERVICE'
}

export enum TypeTPE {
  PHYSIQUE = 'PHYSIQUE',
  TPE = 'TPE',
  MOBILE = 'MOBILE',
  MPOS = 'MPOS',
  FIXE_GPRS = 'FIXE GPRS',
  E_COMMERCE = 'E COMMERCE',
  CASH_ADVANCE = 'CASH ADVANCE',
  PAX = 'PAX'
}

export interface TPE {
  id?: number;
  numeroSerie: string;
  marque?: string;
  modele?: string;
  statut: StatutTPE;
  typeTpe: string;
  typeTPE?: string;
  dateAcquisition: Date | string;
  dateMiseEnService?: Date | string;
  commercantId?: number;
  commercantNom?: string;
  commercantActuelId?: number;
  commercantActuelNom?: string;
  
  // Champs Monétiques
  raisonSociale?: string;
  activite?: string;
  mcc?: string;
  tauxCommission?: number;
  tauxCommissionInter?: number;
  numeroCompte?: string;
  rib?: string;
  codeAgence?: string;
  serieTpe?: string;
  valueDate?: number;
  numeroTerminal?: string; // TID

  // Champs Mobile
  urlSiteMarchand?: string;
  webhookUrl?: string;
  cleApi?: string;
  numeroAffiliation?: string;
  typeCommerce?: string;
  cartesAcceptees?: string;
  typeCartesAcceptees?: string;
  modeTest?: boolean;

  // Champs administratifs
  commentaire?: string;
  loyer?: number;
  createdDate?: Date | string;
  lastModifiedDate?: Date | string;
  createdAt?: Date | string;
  updatedAt?: Date | string;
}

export interface TPEHistorique {
  id: number;
  tpeId: number;
  action: string;
  ancieneValeur?: string;
  nouvelleValeur?: string;
  utilisateurId: number;
  utilisateurNom: string;
  date: Date | string;
}
