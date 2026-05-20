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
  ECOMMERCE = 'ECOMMERCE'
}

export interface TPE {
  id?: number;
  numeroSerie: string;
  marque?: string;
  modele?: string;
  statut: StatutTPE;
  typeTpe: TypeTPE;
  typeTPE?: TypeTPE;
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
  valueDate?: Date | string;
  numeroTerminal?: string; // TID

  // Champs E-commerce
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
