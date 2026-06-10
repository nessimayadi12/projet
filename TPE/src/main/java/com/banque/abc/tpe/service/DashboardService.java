package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.DashboardStatsDTO;
import com.banque.abc.tpe.entity.HistoriqueStatut;
import com.banque.abc.tpe.entity.TPE;
import com.banque.abc.tpe.entity.enums.StatutDemande;
import com.banque.abc.tpe.entity.enums.StatutPanne;
import com.banque.abc.tpe.entity.enums.StatutTPE;
import com.banque.abc.tpe.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final HistoriqueStatutRepository historiqueStatutRepository;

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
            Long demandesNouvelles = safeCount(demandeRepository.countByStatut(StatutDemande.NOUVELLE));
            Long demandesEnCours = safeCount(demandeRepository.countByStatut(StatutDemande.EN_COURS));
            Long demandesValideesMonetique = safeCount(demandeRepository.countByStatut(StatutDemande.VALIDEE_MONETIQUE));
            Long demandesEnAttente = demandesNouvelles + demandesEnCours + demandesValideesMonetique;

            // Pannes en cours - utiliser une valeur par défaut
            Long pannesEnCours = 0L;
            try {
                pannesEnCours = panneRepository.countPannesEnCours();
                pannesEnCours = pannesEnCours != null ? pannesEnCours : 0L;
            } catch (Exception e) {
                // Ignorer l'erreur si la table pannes n'existe pas encore
            }

            Long pannesEnReparation = 0L;
            try {
                pannesEnReparation = panneRepository.countByStatut(StatutPanne.EN_REPARATION);
                pannesEnReparation = pannesEnReparation != null ? pannesEnReparation : 0L;
            } catch (Exception e) {
                // Ignorer l'erreur
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
            Map<String, Long> repartitionParMarque = new LinkedHashMap<>();
            try {
                List<Object[]> marques = tpeRepository.countByMarqueGrouped();
                if (marques != null) {
                    repartitionParMarque = marques.stream()
                        .filter(obj -> obj != null && obj.length >= 2 && obj[0] != null)
                        .collect(Collectors.toMap(
                            obj -> obj[0].toString(),
                            obj -> obj[1] != null ? ((Number) obj[1]).longValue() : 0L,
                            (a, b) -> a,
                            LinkedHashMap::new
                        ));
                }
            } catch (Exception e) {
                // Keep empty map on error
            }

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
            .delaiMoyenTraitementHeures(getAverageTreatmentDelayHours())
            .pannesEnCours(pannesEnCours)
            .pannesResoluesCeMois(pannesResoluesCeMois)
            .pannesEnReparation(pannesEnReparation)
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

    private Double getAverageTreatmentDelayHours() {
        try {
            Double value = demandeRepository.calculateAverageTreatmentDelayHours();
            return value != null ? value : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private long safeCount(Long value) {
        return value != null ? value : 0L;
    }

    private List<StatutDemande> pendingDemandStatuses() {
        return List.of(
            StatutDemande.NOUVELLE,
            StatutDemande.EN_COURS,
            StatutDemande.VALIDEE_MONETIQUE
        );
    }

    private List<StatutDemande> terminalDemandStatuses() {
        return List.of(
            StatutDemande.AFFECTEE,
            StatutDemande.CLOTUREE,
            StatutDemande.REJETEE
        );
    }

    private List<StatutDemande> convertedDemandStatuses() {
        return List.of(
            StatutDemande.AFFECTEE,
            StatutDemande.CLOTUREE
        );
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
        return java.util.stream.IntStream.rangeClosed(0, 5)
            .mapToObj(i -> {
                YearMonth month = YearMonth.from(now.minusMonths(5 - i));
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
        if (results == null) {
            return new ArrayList<>();
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

    public List<Map<String, Object>> getRepartitionParType() {
        List<Object[]> results = tpeRepository.countByTypeGrouped();
        if (results == null) {
            return new ArrayList<>();
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

    public List<Map<String, Object>> getEvolutionTpe() {
        LocalDate currentDate = LocalDate.now();
        YearMonth startMonth = YearMonth.from(currentDate.minusMonths(5));
        List<YearMonth> months = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            months.add(startMonth.plusMonths(i));
        }

        Map<YearMonth, Map<String, Long>> evolution = new LinkedHashMap<>();
        for (YearMonth month : months) {
            Map<String, Long> values = new LinkedHashMap<>();
            values.put(StatutTPE.DISPONIBLE.name(), 0L);
            values.put(StatutTPE.AFFECTE.name(), 0L);
            values.put(StatutTPE.EN_PANNE.name(), 0L);
            values.put(StatutTPE.MAINTENANCE.name(), 0L);
            values.put(StatutTPE.HORS_SERVICE.name(), 0L);
            evolution.put(month, values);
        }

        try {
            List<TPE> tpes = tpeRepository.findAll();
            Map<Long, List<HistoriqueStatut>> historiesByTpe = historiqueStatutRepository.findAll().stream()
                .filter(history -> history.getTpe() != null && history.getTpe().getId() != null)
                .collect(Collectors.groupingBy(history -> history.getTpe().getId()));

            historiesByTpe.values().forEach(histories ->
                histories.sort(Comparator.comparing(
                    HistoriqueStatut::getDateChangement,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ).reversed())
            );

            for (YearMonth month : months) {
                LocalDateTime fin = month.atEndOfMonth().atTime(23, 59, 59);
                Map<String, Long> values = evolution.get(month);

                for (TPE tpe : tpes) {
                    if (tpe.getCreatedDate() != null && tpe.getCreatedDate().isAfter(fin)) {
                        continue;
                    }

                    StatutTPE statut = resolveTpeStatusAt(
                        tpe,
                        historiesByTpe.getOrDefault(tpe.getId(), List.of()),
                        fin
                    );

                    if (statut != null && values.containsKey(statut.name())) {
                        values.put(statut.name(), values.get(statut.name()) + 1);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur chargement évolution TPE", e);
        }

        return evolution.entrySet().stream()
            .map(entry -> {
                Map<String, Object> result = new HashMap<>();
                result.put("mois", entry.getKey().toString());
                result.put("disponible", entry.getValue().get(StatutTPE.DISPONIBLE.name()));
                result.put("affecte", entry.getValue().get(StatutTPE.AFFECTE.name()));
                result.put("enPanne", entry.getValue().get(StatutTPE.EN_PANNE.name()));
                result.put("maintenance", entry.getValue().get(StatutTPE.MAINTENANCE.name()));
                result.put("horsService", entry.getValue().get(StatutTPE.HORS_SERVICE.name()));
                return result;
            })
            .collect(Collectors.toList());
    }

    private StatutTPE resolveTpeStatusAt(TPE tpe, List<HistoriqueStatut> histories, LocalDateTime endOfMonth) {
        StatutTPE status = tpe.getStatut();
        for (HistoriqueStatut history : histories) {
            if (history.getDateChangement() == null || !history.getDateChangement().isAfter(endOfMonth)) {
                break;
            }
            if (history.getAncienStatut() != null) {
                status = history.getAncienStatut();
            }
        }
        return status;
    }

    public List<Map<String, Object>> getStatistiquesParAgence() {
        List<Object[]> results = tpeRepository.countByAgenceAndStatutGrouped();
        if (results == null) {
            return new ArrayList<>();
        }

        return results.stream()
            .filter(result -> result != null && result.length >= 3)
            .map(result -> {
                Map<String, Object> map = new HashMap<>();
                map.put("agence", result[0] != null ? result[0].toString() : "INCONNU");
                map.put("statut", result[1] != null ? result[1].toString() : "INCONNU");
                map.put("count", result[2] != null ? result[2] : 0L);
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
        long totalDemandes = demandeRepository.count();
        long demandesTraitees = safeCount(demandeRepository.countByStatutIn(terminalDemandStatuses()));
        long demandesConverties = safeCount(demandeRepository.countByStatutIn(convertedDemandStatuses()));
        long demandesEnAttente = safeCount(demandeRepository.countByStatutIn(pendingDemandStatuses()));

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime debutMois = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime finMois = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        long demandesClotureesCeMois = safeCount(demandeRepository.countByStatutInAndDateClotureBetween(
            terminalDemandStatuses(), debutMois, finMois));
        long demandesEnRetard = safeCount(demandeRepository.countPendingOlderThan(
            pendingDemandStatuses(), LocalDateTime.now().minusHours(48)));
        long demandesAvecDelai = safeCount(demandeRepository.countTerminalWithCompletionDate());
        long demandesDansSla = safeCount(demandeRepository.countTerminalWithinSla(48));
        double slaRespect = demandesAvecDelai > 0 ? (demandesDansSla * 100.0) / demandesAvecDelai : 0.0;

        performance.put("totalDemandes", totalDemandes);
        performance.put("demandesTraitees", demandesTraitees);
        performance.put("demandesConverties", demandesConverties);
        performance.put("demandesEnAttente", demandesEnAttente);
        performance.put("demandesClotureesCeMois", demandesClotureesCeMois);
        performance.put("demandesEnRetard", demandesEnRetard);
        performance.put("slaRespect", slaRespect);
        performance.put("delaiMoyen", getAverageTreatmentDelayHours());
        performance.put("tauxConversion", totalDemandes > 0 ? (demandesConverties * 100.0) / totalDemandes : 0.0);
        performance.put("tauxSatisfaction", totalDemandes > 0 ? (demandesTraitees * 100.0) / totalDemandes : 0.0);
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
        if (results == null) {
            return new ArrayList<>();
        }
        return results.stream()
            .filter(result -> result != null && result.length >= 2 && result[0] != null)
            .map(result -> {
                Map<String, Object> map = new HashMap<>();
                map.put("type", result[0] != null ? result[0].toString() : "Inconnu");
                map.put("count", result[1] != null ? result[1] : 0L);
                return map;
            })
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getHeatmapPannes() {
        List<Object[]> results = panneRepository.countHeatmapByDayAndPeriod();
        if (results == null) {
            return new ArrayList<>();
        }

        return results.stream()
            .filter(result -> result != null && result.length >= 3 && result[0] != null && result[1] != null)
            .map(result -> {
                Map<String, Object> map = new HashMap<>();
                map.put("dayOfWeek", ((Number) result[0]).intValue());
                map.put("period", result[1].toString());
                map.put("count", result[2] != null ? result[2] : 0L);
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
