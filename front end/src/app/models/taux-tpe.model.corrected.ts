/**
 * Statuts possibles pour un taux
 */
export enum StatutTaux {
  BROUILLON = 'BROUILLON',                    // Créé mais pas soumis
  EN_ATTENTE_VALIDATION = 'EN_ATTENTE_VALIDATION',  // En attente AUTHORIZER
  VALIDE = 'VALIDE',                          // Approuvé par AUTHORIZER
  REJETE = 'REJETE'                           // Rejeté par AUTHORIZER
}

/**
 * Interface TauxTPE - Synchronisée avec backend Taux.java
 * Représente un taux de commission pour un commerçant
 * 
 * Processus 4 yeux:
 * 1. INPUTER crée (statut: BROUILLON)
 * 2. INPUTER soumet (statut: EN_ATTENTE_VALIDATION)
 * 3. AUTHORIZER ≠ INPUTER valide/rejette
 *    - Approuver → VALIDE + actif=true
 *    - Rejeter → REJETE + motifRejet
 */
export interface TauxTPE {
  // Identifiants
  id: number;
  commercantId: number;
  commercantNom: string;
  tpeId?: number;
  numeroTerminal?: string;

  // Taux: Ancien vs Nouveau
  ancienTauxCommission?: number;              // Commission avant modification (%)
  nouveauTauxCommission: number;              // Nouvelle commission (%)
  ancienTauxCommissionInter?: number;         // Commission inter avant (%)
  nouveauTauxCommissionInter: number;         // Nouvelle commission inter (%)

  // Acteurs
  inputerId: number;                          // Qui a créé le taux
  inputerNom: string;                         // Nom du créateur
  
  authorizerId?: number;                      // Qui a validé (si applicable)
  authorizerNom?: string;                     // Nom du validateur

  // Dates
  dateSaisie: string;                         // Date création (ISO 8601)
  dateValidation?: string;                    // Date validation (ISO 8601)
  dateApplication?: string;                   // Date application si approuvé

  // Détails de rejet (si statut = REJETE)
  motifRejet?: string;
  
  // Autres
  commentaire?: string;                       // Commentaires
  statut: StatutTaux;                         // État courant
  actif: boolean;                             // Taux actuellement appliqué?
}

/**
 * Réponse de validation d'un taux
 */
export interface ValiderTauxResponse {
  success: boolean;
  message: string;
  taux?: TauxTPE;
  error?: string;
}

/**
 * Requête de création de taux
 */
export interface CreateTauxRequest {
  commercantId: number;
  nouveauTauxCommission: number;
  nouveauTauxCommissionInter: number;
  commentaire?: string;
}

/**
 * Requête de validation/rejet de taux
 */
export interface ValiderTauxRequest {
  approuver: boolean;
  motifRejet?: string;
}
