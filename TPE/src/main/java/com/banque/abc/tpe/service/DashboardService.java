package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.DashboardStatsDTO;
import com.banque.abc.tpe.entity.enums.StatutDemande;
import com.banque.abc.tpe.entity.enums.StatutTPE;
import com.banque.abc.tpe.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final TPERepository tpeRepository;
    private final DemandeRepository demandeRepository;
    private final PanneRepository panneRepository;
    private final AffectationRepository affectationRepository;
    private final CommercantRepository commercantRepository;

    public DashboardStatsDTO getGlobalStats() {
        // Statistiques TPE
        Long totalTPE = tpeRepository.count();
            Long tpeDisponibles = tpeRepository.countByStatut(StatutTPE.DISPONIBLE);
            Long tpeAffectes = tpeRepository.countByStatut(StatutTPE.AFFECTE);
            Long tpeEnPanne = tpeRepository.countByStatut(StatutTPE.EN_PANNE);
            Long tpeEnMaintenance = tpeRepository.countByStatut(StatutTPE.MAINTENANCE);
            Long tpeHorsService = tpeRepository.countByStatut(StatutTPE.HORS_SERVICE);
            
            // Ensure nulls become zeros
            totalTPE = totalTPE != null ? totalTPE : 0L;
            tpeDisponibles = tpeDisponibles != null ? tpeDisponibles : 0L;
            tpeAffectes = tpeAffectes != null ? tpeAffectes : 0L;
            tpeEnPanne = tpeEnPanne != null ? tpeEnPanne : 0L;
            tpeEnMaintenance = tpeEnMaintenance != null ? tpeEnMaintenance : 0L;
            tpeHorsService = tpeHorsService != null ? tpeHorsService : 0L;

            // Taux de disponibilité
            Double tauxDisponibilite = totalTPE > 0 
                ? ((tpeDisponibles + tpeAffectes) * 100.0) / totalTPE 
                : 0.0;

            // Demandes
            Long demandesNouvelles = demandeRepository.countByStatut(StatutDemande.NOUVELLE);
            Long demandesEnCours = demandeRepository.countByStatut(StatutDemande.EN_COURS);
            Long demandesEnAttente = demandesNouvelles;
            demandesNouvelles = demandesNouvelles != null ? demandesNouvelles : 0L;
            demandesEnCours = demandesEnCours != null ? demandesEnCours : 0L;
            demandesEnAttente = demandesEnAttente != null ? demandesEnAttente : 0L;

            // Pannes en cours - utiliser une valeur par défaut
            Long pannesEnCours = 0L;
            try {
                pannesEnCours = panneRepository.countPannesEnCours();
                pannesEnCours = pannesEnCours != null ? pannesEnCours : 0L;
            } catch (Exception e) {
                // Ignorer l'erreur si la table pannes n'existe pas encore
            }

            // Pannes résolues ce mois
            LocalDateTime debutMois = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            Long pannesResoluesCeMois = 0L;
            try {
                pannesResoluesCeMois = panneRepository.countPannesResoluesDansLaPeriode(debutMois, LocalDateTime.now());
                pannesResoluesCeMois = pannesResoluesCeMois != null ? pannesResoluesCeMois : 0L;
            } catch (Exception e) {
                // Ignorer l'erreur
            }

            // Calculer MTTR (Mean Time To Repair) - moyenne en heures
            Double mttr = 0.0;
            try {
                mttr = panneRepository.calculateMTTR().orElse(0.0);
            } catch (Exception e) {
                // Ignorer l'erreur
            }

            // Taux de panne
            Double tauxPanne = totalTPE > 0 ? (tpeEnPanne * 100.0) / totalTPE : 0.0;

            // Affectations - utiliser des valeurs par défaut
            Long affectationsActives = 0L;
            Long affectationsCeMois = 0L;
            try {
                affectationsActives = affectationRepository.countAffectationsActives();
                affectationsCeMois = affectationRepository.countAffectationsCeMois(debutMois);
                affectationsActives = affectationsActives != null ? affectationsActives : 0L;
                affectationsCeMois = affectationsCeMois != null ? affectationsCeMois : 0L;
            } catch (Exception e) {
                // Ignorer l'erreur
            }

            // Répartition par marque - using empty map for now to avoid loading all TPE
            Map<String, Long> repartitionParMarque = new HashMap<>();

        // Top 10 commerçants (par nombre de TPE)
        Map<String, Long> top10Commercants = new LinkedHashMap<>();
        try {
            List<Object[]> topCommercants = affectationRepository.findTop10CommercantsByTPECount();
            if (topCommercants != null) {
                top10Commercants = topCommercants.stream()
                    .limit(10)
                    .filter(obj -> obj != null && obj.length >= 2 && obj[0] != null && obj[1] != null)
                    .collect(Collectors.toMap(
                        obj -> obj[0].toString(),
                        obj -> ((Number) obj[1]).longValue(),
                        (a, b) -> a,
                        LinkedHashMap::new
                    ));
            }
        } catch (Exception e) {
            // Keep empty map on error
        }

        // Statistiques commerçants
        Long totalCommercants = 0L;
        Long commercantsActifs = 0L;
        try {
            totalCommercants = commercantRepository.count();
            commercantsActifs = commercantRepository.countCommercantsByStatutActif(true);
            totalCommercants = totalCommercants != null ? totalCommercants : 0L;
            commercantsActifs = commercantsActifs != null ? commercantsActifs : 0L;
        } catch (Exception e) {
            // Ignorer l'erreur
        }

        // Alertes
        Long alertesStockBas = tpeDisponibles < 10 ? 1L : 0L;
        Long alertesPannesDepassantSLA = 0L;
        try {
            alertesPannesDepassantSLA = panneRepository.countPannesDepassantSLA();
            alertesPannesDepassantSLA = alertesPannesDepassantSLA != null ? alertesPannesDepassantSLA : 0L;
        } catch (Exception e) {
            // Ignorer l'erreur
        }

        return DashboardStatsDTO.builder()
            .totalTPE(totalTPE)
            .tpeDisponibles(tpeDisponibles)
            .tpeAffectes(tpeAffectes)
            .tpeEnPanne(tpeEnPanne)
            .tpeEnMaintenance(tpeEnMaintenance)
            .tpeHorsService(tpeHorsService)
            .tauxDisponibilite(tauxDisponibilite)
            .demandesNouvelles(demandesNouvelles)
            .demandesEnCours(demandesEnCours)
            .demandesEnAttente(demandesEnAttente)
            .delaiMoyenTraitementHeures(0.0) // À implémenter si nécessaire
            .pannesEnCours(pannesEnCours)
            .pannesResoluesCeMois(pannesResoluesCeMois)
            .mttr(mttr)
            .tauxPanne(tauxPanne)
            .affectationsActives(affectationsActives)
            .affectationsCeMois(affectationsCeMois)
            .repartitionParMarque(repartitionParMarque)
            .top10Commercants(top10Commercants)
            .totalCommercants(totalCommercants)
            .commercantsActifs(commercantsActifs)
            .alertesStockBas(alertesStockBas)
            .alertesPannesDepassantSLA(alertesPannesDepassantSLA)
            .build();
    }

    public List<Map<String, Object>> getDemandesParStatut() {
        List<Object[]> results = demandeRepository.countByStatutGrouped();
        if (results == null) {
            return new java.util.ArrayList<>();
        }
        return results.stream()
            .filter(result -> result != null && result.length >= 2 && result[0] != null)
            .map(result -> {
                Map<String, Object> map = new HashMap<>();
                map.put("statut", result[0].toString());
                map.put("count", result[1] != null ? result[1] : 0L);
                return map;
            })
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getPannesParType() {
        List<Object[]> results = panneRepository.countByTypeGrouped();
        if (results == null) {
            return new java.util.ArrayList<>();
        }
        return results.stream()
            .filter(result -> result != null && result.length >= 2)
            .map(result -> {
                Map<String, Object> map = new HashMap<>();
                map.put("type", result[0] != null ? result[0].toString() : "Inconnu");
                map.put("count", result[1] != null ? result[1] : 0L);
                return map;
            })
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getEvolutionMensuelle() {
        // Évolution des 6 derniers mois
        LocalDate now = LocalDate.now();
        return java.util.stream.IntStream.range(0, 6)
            .mapToObj(i -> {
                YearMonth month = YearMonth.from(now.minusMonths(i));
                LocalDateTime debut = month.atDay(1).atStartOfDay();
                LocalDateTime fin = month.atEndOfMonth().atTime(23, 59, 59);

                Long demandesCount = 0L;
                Long pannesCount = 0L;
                Long affectationsCount = 0L;

                try {
                    demandesCount = demandeRepository.countByDateBetween(debut, fin);
                    demandesCount = demandesCount != null ? demandesCount : 0L;
                } catch (Exception e) {
                    log.error("Erreur comptage demandes pour le mois {}", month, e);
                }

                try {
                    pannesCount = panneRepository != null ? panneRepository.countByDateBetween(debut, fin) : 0L;
                    pannesCount = pannesCount != null ? pannesCount : 0L;
                } catch (Exception e) {
                    log.error("Erreur comptage pannes pour le mois {}", month, e);
                }

                try {
                    affectationsCount = affectationRepository.countByDateBetween(debut, fin);
                    affectationsCount = affectationsCount != null ? affectationsCount : 0L;
                } catch (Exception e) {
                    log.error("Erreur comptage affectations pour le mois {}", month, e);
                }

                Map<String, Object> result = new HashMap<>();
                result.put("mois", month.toString());
                result.put("demandes", demandesCount);
                result.put("pannes", pannesCount);
                result.put("affectations", affectationsCount);
                return result;
            })
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getRepartitionParStatut() {
        List<Object[]> results = tpeRepository.countByStatutGrouped();
        return results.stream()
            .map(result -> {
                Map<String, Object> map = new HashMap<>();
                map.put("statut", result[0].toString());
                map.put("count", result[1]);
                return map;
            })
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getRepartitionParType() {
        List<Object[]> results = tpeRepository.countByTypeGrouped();
        return results.stream()
            .map(result -> {
                Map<String, Object> map = new HashMap<>();
                map.put("type", result[0] != null ? result[0].toString() : "Inconnu");
                map.put("count", result[1]);
                return map;
            })
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getPannesParPeriode(String periode) {
        LocalDateTime debut;
        LocalDateTime fin = LocalDateTime.now();

        switch (periode.toLowerCase()) {
            case "semaine":
                debut = fin.minusWeeks(1);
                break;
            case "mois":
                debut = fin.minusMonths(1);
                break;
            case "trimestre":
                debut = fin.minusMonths(3);
                break;
            case "annee":
                debut = fin.minusYears(1);
                break;
            default:
                debut = fin.minusMonths(1);
        }

        List<Object[]> results = panneRepository.countByDateBetweenGrouped(debut, fin);
        return results.stream()
            .map(result -> {
                Map<String, Object> map = new HashMap<>();
                map.put("type", result[0] != null ? result[0].toString() : "Inconnu");
                map.put("count", result[1]);
                return map;
            })
            .collect(Collectors.toList());
    }

    public Map<String, Object> getPerformanceDemandes() {
        Map<String, Object> performance = new HashMap<>();
        performance.put("totalDemandes", demandeRepository.count());
        performance.put("demandesTraitees", demandeRepository.countByStatut(StatutDemande.CLOTUREE));
        performance.put("delaiMoyen", 0.0); // À implémenter
        performance.put("tauxSatisfaction", 95.0); // À implémenter
        return performance;
    }

    public Map<String, Object> getTauxUtilisation() {
        Long total = tpeRepository.count();
        Long affectes = tpeRepository.countByStatut(StatutTPE.AFFECTE);
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("affectes", affectes);
        result.put("taux", total > 0 ? (affectes * 100.0) / total : 0.0);
        return result;
    }

    public List<Map<String, Object>> getTopPannes() {
        List<Object[]> results = panneRepository.findTopPannesByFrequency();
        return results.stream()
            .map(result -> {
                Map<String, Object> map = new HashMap<>();
                map.put("type", result[0] != null ? result[0].toString() : "Inconnu");
                map.put("count", result[1]);
                return map;
            })
            .collect(Collectors.toList());
    }

    public Map<String, Object> getStatsCommerçants() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCommercants", commercantRepository.count());
        stats.put("commercantsActifs", commercantRepository.countCommercantsByStatutActif(true));
        stats.put("nouveauxCeMois", commercantRepository.countNewCommercantsCeMois(
            LocalDate.now().withDayOfMonth(1).atStartOfDay()
        ));
        return stats;
    }
}
