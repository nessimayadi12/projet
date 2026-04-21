package com.banque.abc.tpe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    // Statistiques générales
    private Long totalTPE;
    private Long tpeDisponibles;
    private Long tpeAffectes;
    private Long tpeEnPanne;
    private Long tpeEnMaintenance;
    private Long tpeHorsService;

    // Taux de disponibilité
    private Double tauxDisponibilite;

    // Demandes
    private Long demandesNouvelles;
    private Long demandesEnCours;
    private Long demandesEnAttente;
    private Double delaiMoyenTraitementHeures;

    // Pannes
    private Long pannesEnCours;
    private Long pannesResoluesCeMois;
    private Long pannesEnReparation;
    private Double mttr; // Mean Time To Repair
    private Double tauxPanne;

    // Affectations
    private Long affectationsActives;
    private Long affectationsCeMois;

    // Statistiques par marque
    private Map<String, Long> repartitionParMarque;

    // Top 10 commerçants
    private Map<String, Long> top10Commercants;
    
    // Statistiques commerçants
    private Long totalCommercants;
    private Long commercantsActifs;

    // Alertes
    private Long alertesStockBas;
    private Long alertesPannesDepassantSLA;
}
