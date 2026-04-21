package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.dto.DashboardStatsDTO;
import com.banque.abc.tpe.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Obtenir les statistiques globales du dashboard
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<DashboardStatsDTO> getGlobalStats() {
        return ResponseEntity.ok(dashboardService.getGlobalStats());
    }

    /**
     * Obtenir le nombre de demandes par statut
     */
    @GetMapping("/demandes-statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<List<Map<String, Object>>> getDemandesParStatut() {
        return ResponseEntity.ok(dashboardService.getDemandesParStatut());
    }

    /**
     * Obtenir le nombre de pannes par type
     */
    @GetMapping("/pannes-type")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<List<Map<String, Object>>> getPannesParType() {
        return ResponseEntity.ok(dashboardService.getPannesParType());
    }

    /**
     * Obtenir l'évolution mensuelle (demandes, pannes, affectations)
     */
    @GetMapping("/evolution-mensuelle")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<List<Map<String, Object>>> getEvolutionMensuelle() {
        return ResponseEntity.ok(dashboardService.getEvolutionMensuelle());
    }

    /**
     * Obtenir la répartition des TPE par statut
     */
    @GetMapping("/repartition-statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<List<Map<String, Object>>> getRepartitionParStatut() {
        return ResponseEntity.ok(dashboardService.getRepartitionParStatut());
    }

    /**
     * Obtenir la répartition des TPE par type (Physique/E-commerce)
     */
    @GetMapping("/repartition-type")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<List<Map<String, Object>>> getRepartitionParType() {
        return ResponseEntity.ok(dashboardService.getRepartitionParType());
    }

    /**
     * Obtenir l'évolution du parc TPE par statut sur les 6 derniers mois
     */
    @GetMapping("/evolution-tpe")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<List<Map<String, Object>>> getEvolutionTpe() {
        return ResponseEntity.ok(dashboardService.getEvolutionTpe());
    }

    /**
     * Obtenir les statistiques du parc TPE par agence et statut
     */
    @GetMapping("/stats-par-agence")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE', 'AGENCE')")
    public ResponseEntity<List<Map<String, Object>>> getStatistiquesParAgence() {
        return ResponseEntity.ok(dashboardService.getStatistiquesParAgence());
    }

    /**
     * Obtenir les pannes par période
     */
    @GetMapping("/pannes-periode")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<List<Map<String, Object>>> getPannesParPeriode(
            @RequestParam(defaultValue = "mois") String periode) {
        return ResponseEntity.ok(dashboardService.getPannesParPeriode(periode));
    }

    /**
     * Obtenir les performances de traitement des demandes
     */
    @GetMapping("/performance-demandes")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<Map<String, Object>> getPerformanceDemandes() {
        return ResponseEntity.ok(dashboardService.getPerformanceDemandes());
    }

    /**
     * Obtenir le taux d'utilisation des TPE
     */
    @GetMapping("/taux-utilisation")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<Map<String, Object>> getTauxUtilisation() {
        return ResponseEntity.ok(dashboardService.getTauxUtilisation());
    }

    /**
     * Obtenir le top des pannes les plus fréquentes
     */
    @GetMapping("/top-pannes")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<List<Map<String, Object>>> getTopPannes() {
        return ResponseEntity.ok(dashboardService.getTopPannes());
    }

    /**
     * Obtenir la heatmap des pannes par jour et plage horaire
     */
    @GetMapping("/heatmap-pannes")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<List<Map<String, Object>>> getHeatmapPannes() {
        return ResponseEntity.ok(dashboardService.getHeatmapPannes());
    }

    /**
     * Obtenir les statistiques des commerçants
     */
    @GetMapping("/stats-commercants")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE', 'AGENCE')")
    public ResponseEntity<Map<String, Object>> getStatsCommercants() {
        return ResponseEntity.ok(dashboardService.getStatsCommerçants());
    }
}
