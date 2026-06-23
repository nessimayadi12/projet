package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.dto.powerbi.PowerBIReportInfo;
import com.banque.abc.tpe.dto.powerbi.PowerBITokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/powerbi")
@Tag(name = "Power BI", description = "API pour l'intégration Power BI")
@Slf4j
public class PowerBIController {

    @Value("${powerbi.workspace.id:}")
    private String workspaceId;

    @Value("${powerbi.report.id:}")
    private String reportId;

    @Value("${powerbi.embed.url:}")
    private String embedUrl;

    @Value("${powerbi.enabled:false}")
    private boolean powerBIEnabled;

    /**
     * Récupère le token d'embed pour un rapport Power BI
     * 
     * Note: Cette implémentation est un exemple simplifié.
     * En production, utilisez l'API Power BI REST avec Azure AD pour générer de vrais tokens.
     */
    @Operation(summary = "Récupérer le token d'embed", description = "Génère un token d'embed pour un rapport Power BI")
    @GetMapping("/token/{reportId}")
    public ResponseEntity<PowerBITokenResponse> getEmbedToken(@PathVariable String reportId) {
        log.info("Demande de token d'embed pour le rapport: {}", reportId);

        if (!powerBIEnabled) {
            log.warn("Power BI n'est pas activé dans la configuration");
            return ResponseEntity.badRequest().build();
        }

        // TODO: Implémenter l'authentification Azure AD et la génération de token réel
        // Cette version est un mock pour la démonstration
        
        PowerBITokenResponse response = PowerBITokenResponse.builder()
                .token("MOCK_TOKEN_" + UUID.randomUUID().toString())
                .tokenId(UUID.randomUUID().toString())
                .expiration(LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_DATE_TIME))
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Récupère la liste des rapports Power BI disponibles
     */
    @Operation(summary = "Liste des rapports", description = "Récupère la liste des rapports Power BI disponibles")
    @GetMapping("/reports")
    public ResponseEntity<List<PowerBIReportInfo>> getReports() {
        log.info("Récupération de la liste des rapports Power BI");

        List<PowerBIReportInfo> reports = new ArrayList<>();

        if (powerBIEnabled && reportId != null && !reportId.isEmpty()) {
            PowerBIReportInfo report = PowerBIReportInfo.builder()
                    .id(reportId)
                    .name("Dashboard TPE Management")
                    .embedUrl(embedUrl)
                    .webUrl("https://app.powerbi.com/groups/" + workspaceId + "/reports/" + reportId)
                    .build();
            reports.add(report);
        }

        return ResponseEntity.ok(reports);
    }

    /**
     * Récupère les détails d'un rapport spécifique
     */
    @Operation(summary = "Détails d'un rapport", description = "Récupère les détails d'un rapport Power BI")
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<PowerBIReportInfo> getReport(@PathVariable String reportId) {
        log.info("Récupération du rapport: {}", reportId);

        if (!powerBIEnabled) {
            return ResponseEntity.badRequest().build();
        }

        PowerBIReportInfo report = PowerBIReportInfo.builder()
                .id(reportId)
                .name("Dashboard TPE Management")
                .embedUrl(embedUrl)
                .webUrl("https://app.powerbi.com/groups/" + workspaceId + "/reports/" + reportId)
                .build();

        return ResponseEntity.ok(report);
    }

    /**
     * Vérifie si Power BI est correctement configuré
     */
    @Operation(summary = "Status Power BI", description = "Vérifie la configuration Power BI")
    @GetMapping("/status")
    public ResponseEntity<Boolean> getPowerBIStatus() {
        return ResponseEntity.ok(powerBIEnabled);
    }
}
