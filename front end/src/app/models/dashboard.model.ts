export interface DashboardStats {
  // Statistiques générales
  totalTPE: number;
  tpeDisponibles: number;
  tpeAffectes: number;
  tpeEnPanne: number;
  tpeEnMaintenance: number;
  tpeHorsService: number;

  // Taux de disponibilité
  tauxDisponibilite: number;

  // Demandes
  demandesNouvelles: number;
  demandesEnCours: number;
  demandesEnAttente: number;
  delaiMoyenTraitementHeures: number;

  // Pannes
  pannesEnCours: number;
  pannesResoluesCeMois: number;
  mttr: number; // Mean Time To Repair
  tauxPanne: number;

  // Affectations
  affectationsActives: number;
  affectationsCeMois: number;

  // Statistiques par marque
  repartitionParMarque: { [key: string]: number };

  // Top 10 commerçants
  top10Commercants: { [key: string]: number };
  
  // Statistiques commerçants
  totalCommercants: number;
  commercantsActifs: number;

  // Alertes
  alertesStockBas: number;
  alertesPannesDepassantSLA: number;
}
