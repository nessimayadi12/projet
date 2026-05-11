export enum StatutDemande {
  NOUVELLE = 'NOUVELLE',
  EN_COURS = 'EN_COURS',
  VALIDEE_MONETIQUE = 'VALIDEE_MONETIQUE',
  VALIDEE_AGENCE = 'VALIDEE_AGENCE',
  AFFECTEE = 'AFFECTEE',
  CLOTUREE = 'CLOTUREE',
  REJETEE = 'REJETEE'
}

export enum Urgence {
  BASSE = 'BASSE',
  NORMALE = 'NORMALE',
  HAUTE = 'HAUTE',
  CRITIQUE = 'CRITIQUE'
}

export enum TypeDemande {
  PHYSIQUE = 'PHYSIQUE',
  ECOMMERCE = 'ECOMMERCE'
}

// Alias pour compatibilité (à supprimer si non utilisé ailleurs)
export const TypeDemande_LEGACY = {
  TPE_PHYSIQUE: 'PHYSIQUE' as TypeDemande,
  E_COMMERCE: 'ECOMMERCE' as TypeDemande
};

export interface DemandeTPE {
  id?: number;
  reference?: string;
  commercantId?: number; // Optionnel : créé après validation Monétique
  commercantNom?: string;
  typeDemande: TypeDemande;
  typeTpeRequis?: string;
  urgence: Urgence;
  statut: StatutDemande;
  description?: string;
  agenceDemandeurId?: number;
  agenceDemandeurNom?: string;
  monetiqueValideurId?: number;
  monetiqueValideurNom?: string;
  valideurId?: number;
  valideurNom?: string;
  inputerId?: number;
  inputerNom?: string;
  tpeAffecteId?: number;
  tpeAffecteNumeroSerie?: string;
  dateValidation?: Date | string;
  dateSaisieTaux?: Date | string;
  dateAffectation?: Date | string;
  dateCloture?: Date | string;
  commentaires?: string;
  commentaireValidation?: string;
  piecesJointes?: string[];
  createdDate?: Date | string; // Backend utilise createdDate
  lastModifiedDate?: Date | string; // Backend utilise lastModifiedDate
  // Aliases pour compatibilité
  createdAt?: Date | string;
  updatedAt?: Date | string;
  
  // Champs de demande agence (TPE Physique)
  raisonSociale?: string;
  activite?: string;
  numeroCompte?: string;
  adresse?: string;
  codePostal?: string;
  codeAgence?: string;
  telephone?: string;
  rneFile?: File | string;
  emailNotification?: string;
  
  // Champs de validation Monetique (TPE Physique)
  mcc?: string;
  tauxCommission?: number;
  tauxCommissionInter?: number;
  loyer?: number;
  serieTpe?: string;
  numeroTerminal?: string; // généré automatiquement
  valueDate?: Date | string;
  
  // Champs spécifiques E-commerce
  localite?: string;
  rib?: string;
  webmaster?: string;
  contactTechnique?: string;
  urlSiteMarchand?: string;
}
