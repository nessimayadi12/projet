export enum StatutPanne {
  DECLAREE = 'DECLAREE',
  DIAGNOSTIQUEE = 'DIAGNOSTIQUEE',
  EN_REPARATION = 'EN_REPARATION',
  REPAREE = 'REPAREE',
  TESTEE = 'TESTEE',
  IRRECUPERABLE = 'IRRECUPERABLE'
}

export enum TypePanne {
  COURT_CIRCUIT = 'COURT_CIRCUIT',
  DEFAUT_ISOLEMENT = 'DEFAUT_ISOLEMENT',
  SURCHARGE = 'SURCHARGE',
  COUPURE_SECTEUR = 'COUPURE_SECTEUR',
  RUPTURE_USURE_PIECES = 'RUPTURE_USURE_PIECES',
  DEFAUT_LUBRIFICATION = 'DEFAUT_LUBRIFICATION',
  GRIPPAGE = 'GRIPPAGE',
  HARDWARE = 'HARDWARE',
  SOFTWARE = 'SOFTWARE',
  INCIDENT_0044_0088 = 'INCIDENT_0044_0088',
  INCIDENT_0060_CENTRE_BANCAIRE_NON_ATTEINT = 'INCIDENT_0060_CENTRE_BANCAIRE_NON_ATTEINT',
  INCIDENT_001 = 'INCIDENT_001',
  INCIDENT_0074 = 'INCIDENT_0074',
  INCIDENT_020E_0067 = 'INCIDENT_020E_0067',
  INCIDENT_0050 = 'INCIDENT_0050',
  ALERTE_IRRUPTION = 'ALERTE_IRRUPTION',
  PROBLEME_BATTERIE_CHARGE = 'PROBLEME_BATTERIE_CHARGE',
  IMPRIMANTE_BLOQUEE = 'IMPRIMANTE_BLOQUEE',
  INCIDENT_0060_ERREUR_CARTE = 'INCIDENT_0060_ERREUR_CARTE',
  ERREUR_SAISIE_PIN = 'ERREUR_SAISIE_PIN'
}

export enum UrgencePanne {
  FAIBLE = 'FAIBLE',
  MOYENNE = 'MOYENNE',
  HAUTE = 'HAUTE',
  CRITIQUE = 'CRITIQUE'
}

export interface Panne {
  id?: number;
  reference?: string;
  tpeId: number;
  tpeNumeroSerie?: string;
  commercantNom?: string;
  typePanne?: TypePanne | string;
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
  actionCorrective?: string;
  commentaireTechnicien?: string;
  tpeRemplacementId?: number;
  tpeRemplacementNumero?: string;
  tpeRemplacementNumeroSerie?: string;
  tempsResolutionHeures?: number;
  coutReparation?: number;
  sousGarantie?: boolean;
  createdDate?: Date | string;
  createdAt?: Date | string;
  updatedAt?: Date | string;
}

export interface DiagnosticIaSource {
  id?: number;
  titre?: string;
  typePanne?: TypePanne | string;
  score?: number;
  extrait?: string;
  indices?: string[];
}

export interface DiagnosticIaPanne {
  typePanneSuggere?: TypePanne | string;
  diagnosticPropose?: string;
  actionCorrectiveProposee?: string;
  urgence?: string;
  scoreConfiance?: number;
  indicesDetectes?: string[];
  recommandations?: string[];
  remplacementRecommande?: boolean;
  contexteRag?: string;
  sourcesRetenues?: DiagnosticIaSource[];
}
