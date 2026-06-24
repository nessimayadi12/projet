export enum StatutCommercant {
  ACTIF = 'ACTIF',
  INACTIF = 'INACTIF',
  SUSPENDU = 'SUSPENDU'
}

export enum TypeTPE {
  PHYSIQUE = 'PHYSIQUE',
  TPE = 'TPE',
  MOBILE = 'MOBILE'
}

export interface Commercant {
  id?: number;
  raisonSociale: string;
  siret?: string;
  identifiantUniqueRNE?: string;
  email: string;
  telephone: string;
  adresse: string;
  codePostal: string;
  ville: string;
  localite?: string;
  activite?: string;
  numeroCompte?: string;
  rib?: string;
  codeAgence?: string;
  loyer?: number;
  mcc?: string;
  webmaster?: string;
  typeCommerce?: TypeTPE;
  statut: StatutCommercant;
  nomContact?: string;
  prenomContact?: string;
  cheminFichierRNE?: string;
  nombreTpes?: number;
  nombreTPEs?: number;
  createdDate?: Date | string;
  lastModifiedDate?: Date | string;
  createdAt?: Date | string;
  updatedAt?: Date | string;
}
