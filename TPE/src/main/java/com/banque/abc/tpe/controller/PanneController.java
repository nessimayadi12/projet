package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.dto.panne.PanneResponse;
import com.banque.abc.tpe.entity.Panne;
import com.banque.abc.tpe.entity.enums.StatutPanne;
import com.banque.abc.tpe.service.PanneService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pannes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class PanneController {

    private final PanneService panneService;

    /**
     * Obtenir toutes les pannes
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE', 'AGENCE')")
    public ResponseEntity<List<PanneResponse>> getAllPannes() {
        return ResponseEntity.ok(panneService.getAllPannesDTO());
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
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE', 'AGENCE')")
    public ResponseEntity<PanneResponse> createPanne(@RequestBody Panne panne) {
        Panne nouvellePanne = panneService.createPanne(panne);
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
        try {
            Panne panne = panneService.changeStatut(id, statut);
            PanneResponse response = panneService.mapToResponse(panne);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
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
        try {
            String diagnostic = payload.get("diagnostic");
            Panne panne = panneService.diagnostiquer(id, diagnostic);
            PanneResponse response = panneService.mapToResponse(panne);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Marquer une panne comme en réparation
     */
    @PostMapping("/{id}/en-reparation")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<PanneResponse> marquerEnReparation(@PathVariable Long id) {
        try {
            Panne panne = panneService.marquerEnReparation(id);
            PanneResponse response = panneService.mapToResponse(panne);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Marquer une panne comme réparée
     */
    @PostMapping("/{id}/reparee")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<PanneResponse> marquerReparee(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String solution = payload.get("solution");
            Panne panne = panneService.marquerReparee(id, solution);
            PanneResponse response = panneService.mapToResponse(panne);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Résoudre une panne (marquer comme testée et fonctionnelle)
     */
    @PostMapping("/{id}/resoudre")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<PanneResponse> resoudrePanne(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String solution = payload.get("solution");
            Panne panne = panneService.resoudrePanne(id, solution);
            PanneResponse response = panneService.mapToResponse(panne);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Tester une panne après réparation
     */
    @PostMapping("/{id}/tester")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<PanneResponse> testerPanne(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        try {
            Boolean resultat = payload.get("resultat");
            Panne panne = panneService.testerPanne(id, resultat != null && resultat);
            PanneResponse response = panneService.mapToResponse(panne);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Affecter un TPE de remplacement
     */
    @PostMapping("/{panneId}/tpe-remplacement/{tpeRemplacementId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<PanneResponse> affecterTPERemplacement(
            @PathVariable Long panneId,
            @PathVariable Long tpeRemplacementId) {
        try {
            Panne panne = panneService.affecterTPERemplacement(panneId, tpeRemplacementId);
            PanneResponse response = panneService.mapToResponse(panne);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
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
}
