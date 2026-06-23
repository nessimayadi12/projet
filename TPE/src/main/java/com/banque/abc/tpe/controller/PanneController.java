package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.dto.panne.PanneRequest;
import com.banque.abc.tpe.dto.panne.PanneDiagnosticIaRequest;
import com.banque.abc.tpe.dto.panne.PanneDiagnosticIaResponse;
import com.banque.abc.tpe.dto.panne.PanneDiagnosticKnowledgeRequest;
import com.banque.abc.tpe.dto.panne.PanneDiagnosticKnowledgeResponse;
import com.banque.abc.tpe.dto.panne.PanneResponse;
import com.banque.abc.tpe.entity.Panne;
import com.banque.abc.tpe.entity.enums.StatutPanne;
import com.banque.abc.tpe.service.PanneDiagnosticKnowledgeService;
import com.banque.abc.tpe.service.PanneDiagnosticIaService;
import com.banque.abc.tpe.service.PanneService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pannes")
@RequiredArgsConstructor
public class PanneController {

    private final PanneService panneService;
    private final PanneDiagnosticIaService panneDiagnosticIaService;
    private final PanneDiagnosticKnowledgeService panneDiagnosticKnowledgeService;

    /**
     * Obtenir toutes les pannes
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE', 'AGENCE')")
    public ResponseEntity<List<PanneResponse>> getAllPannes() {
        return ResponseEntity.ok(panneService.getAllPannesDTO());
    }

    /**
     * Assistant IA local: analyser une description libre et proposer un diagnostic
     */
    @PostMapping("/diagnostic-ia")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE', 'AGENCE')")
    public ResponseEntity<PanneDiagnosticIaResponse> analyserDescriptionPanne(
            @RequestBody @jakarta.validation.Valid PanneDiagnosticIaRequest request) {
        return ResponseEntity.ok(panneDiagnosticIaService.analyser(request));
    }

    /**
     * Lister les documents RAG utilises par l'assistant diagnostic
     */
    @GetMapping("/diagnostic-ia/connaissances")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<List<PanneDiagnosticKnowledgeResponse>> getDiagnosticKnowledge() {
        return ResponseEntity.ok(panneDiagnosticKnowledgeService.getAll());
    }

    /**
     * Ajouter un document RAG de diagnostic
     */
    @PostMapping("/diagnostic-ia/connaissances")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<PanneDiagnosticKnowledgeResponse> createDiagnosticKnowledge(
            @RequestBody @jakarta.validation.Valid PanneDiagnosticKnowledgeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(panneDiagnosticKnowledgeService.create(request));
    }

    /**
     * Generer ou mettre a jour les documents RAG depuis l'historique des pannes
     */
    @PostMapping("/diagnostic-ia/connaissances/generer-depuis-historique")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<List<PanneDiagnosticKnowledgeResponse>> generateDiagnosticKnowledgeFromHistory() {
        return ResponseEntity.ok(panneDiagnosticKnowledgeService.generateFromHistoriquePannes());
    }

    /**
     * Modifier un document RAG de diagnostic
     */
    @PutMapping("/diagnostic-ia/connaissances/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<PanneDiagnosticKnowledgeResponse> updateDiagnosticKnowledge(
            @PathVariable Long id,
            @RequestBody @jakarta.validation.Valid PanneDiagnosticKnowledgeRequest request) {
        return ResponseEntity.ok(panneDiagnosticKnowledgeService.update(id, request));
    }

    /**
     * Desactiver un document RAG sans le supprimer
     */
    @DeleteMapping("/diagnostic-ia/connaissances/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<PanneDiagnosticKnowledgeResponse> desactivateDiagnosticKnowledge(@PathVariable Long id) {
        return ResponseEntity.ok(panneDiagnosticKnowledgeService.desactivate(id));
    }

    /**
     * Obtenir une panne par ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE', 'AGENCE')")
    public ResponseEntity<PanneResponse> getPanneById(@PathVariable Long id) {
        try {
            PanneResponse response = panneService.getPanneDTOById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtenir les pannes par statut
     */
    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<List<PanneResponse>> getPannesByStatut(@PathVariable StatutPanne statut) {
        return ResponseEntity.ok(panneService.getPannesDTOByStatut(statut));
    }

    /**
     * Obtenir les pannes d'un TPE
     */
    @GetMapping("/tpe/{tpeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE', 'AGENCE')")
    public ResponseEntity<List<Panne>> getPannesByTPE(@PathVariable Long tpeId) {
        return ResponseEntity.ok(panneService.getPannesByTPE(tpeId));
    }

    /**
     * Obtenir les pannes d'un technicien
     */
    @GetMapping("/technicien/{technicienId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<List<Panne>> getPannesByTechnicien(@PathVariable Long technicienId) {
        return ResponseEntity.ok(panneService.getPannesByTechnicien(technicienId));
    }

    /**
     * Créer une nouvelle panne (déclaration)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENCE')")
    public ResponseEntity<PanneResponse> createPanne(@RequestBody @jakarta.validation.Valid PanneRequest request) {
        Panne nouvellePanne = panneService.createPanne(request);
        PanneResponse response = panneService.mapToResponse(nouvellePanne);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Mettre à jour une panne
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<PanneResponse> updatePanne(@PathVariable Long id, @RequestBody Panne panneDetails) {
        try {
            Panne updatedPanne = panneService.updatePanne(id, panneDetails);
            PanneResponse response = panneService.mapToResponse(updatedPanne);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Changer le statut d'une panne
     */
    @PutMapping("/{id}/statut/{statut}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<PanneResponse> changeStatut(@PathVariable Long id, @PathVariable StatutPanne statut) {
        Panne panne = panneService.changeStatut(id, statut);
        PanneResponse response = panneService.mapToResponse(panne);
        return ResponseEntity.ok(response);
    }

    /**
     * Assigner un technicien à une panne
     */
    @PostMapping("/{panneId}/assigner/{technicienId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<PanneResponse> assignerTechnicien(@PathVariable Long panneId, @PathVariable Long technicienId) {
        try {
            Panne panne = panneService.assignerTechnicien(panneId, technicienId);
            PanneResponse response = panneService.mapToResponse(panne);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Diagnostiquer une panne
     */
    @PostMapping("/{id}/diagnostiquer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<PanneResponse> diagnostiquer(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String diagnostic = payload.get("diagnostic");
        Panne panne = panneService.diagnostiquer(id, diagnostic);
        PanneResponse response = panneService.mapToResponse(panne);
        return ResponseEntity.ok(response);
    }

    /**
     * Marquer une panne comme en réparation
     */
    @PostMapping("/{id}/en-reparation")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<PanneResponse> marquerEnReparation(@PathVariable Long id) {
        Panne panne = panneService.marquerEnReparation(id);
        PanneResponse response = panneService.mapToResponse(panne);
        return ResponseEntity.ok(response);
    }

    /**
     * Marquer une panne comme réparée
     */
    @PostMapping("/{id}/reparee")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<PanneResponse> marquerReparee(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String solution = payload.get("solution");
        Panne panne = panneService.marquerReparee(id, solution);
        PanneResponse response = panneService.mapToResponse(panne);
        return ResponseEntity.ok(response);
    }

    /**
     * Marquer une panne comme irrécupérable avec remplacement du TPE
     */
    @PostMapping("/{id}/irrecuperable")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<PanneResponse> marquerIrrecuperableAvecRemplacement(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String nouveauNumeroSerie = payload.get("nouveauNumeroSerie");
        String nouveauTypeTPE = firstNonBlank(payload.get("nouveauTypeTPE"), payload.get("typeTPE"));
        String nouvelleMarque = firstNonBlank(payload.get("nouvelleMarque"), payload.get("marque"));
        String nouveauModele = firstNonBlank(payload.get("nouveauModele"), payload.get("modele"));
        String commentaire = payload.get("commentaire");
        Panne panne = panneService.marquerIrrecuperableAvecRemplacement(
                id,
                nouveauNumeroSerie,
                nouveauTypeTPE,
                nouvelleMarque,
                nouveauModele,
                commentaire
        );
        PanneResponse response = panneService.mapToResponse(panne);
        return ResponseEntity.ok(response);
    }

    /**
     * Résoudre une panne (marquer comme testée et fonctionnelle)
     */
    @PostMapping("/{id}/resoudre")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<PanneResponse> resoudrePanne(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String solution = payload.get("solution");
        Panne panne = panneService.resoudrePanne(id, solution);
        PanneResponse response = panneService.mapToResponse(panne);
        return ResponseEntity.ok(response);
    }

    /**
     * Tester une panne après réparation
     */
    @PostMapping("/{id}/tester")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<PanneResponse> testerPanne(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        Boolean resultat = payload.get("resultat");
        Panne panne = panneService.testerPanne(id, resultat != null && resultat);
        PanneResponse response = panneService.mapToResponse(panne);
        return ResponseEntity.ok(response);
    }

    /**
     * Affecter un TPE de remplacement
     */
    @PostMapping("/{panneId}/tpe-remplacement/{tpeRemplacementId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<PanneResponse> affecterTPERemplacement(
            @PathVariable Long panneId,
            @PathVariable Long tpeRemplacementId) {
        Panne panne = panneService.affecterTPERemplacement(panneId, tpeRemplacementId);
        PanneResponse response = panneService.mapToResponse(panne);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtenir les pannes par période
     */
    @GetMapping("/periode")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<List<Panne>> getPannesByPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(panneService.getPannesByPeriode(debut, fin));
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE', 'AGENCE')")
    public ResponseEntity<byte[]> exportPannesExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin)
            throws Exception {
        byte[] content = panneService.exportPannesExcel(debut, fin);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pannes_tpe.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    @GetMapping("/export/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE', 'AGENCE')")
    public ResponseEntity<byte[]> exportPannesPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin)
            throws Exception {
        byte[] content = panneService.exportPannesPdf(debut, fin);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pannes_tpe.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(content);
    }

    /**
     * Supprimer une panne
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePanne(@PathVariable Long id) {
        try {
            panneService.deletePanne(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String firstNonBlank(String primary, String fallback) {
        return primary != null && !primary.isBlank() ? primary : fallback;
    }
}
