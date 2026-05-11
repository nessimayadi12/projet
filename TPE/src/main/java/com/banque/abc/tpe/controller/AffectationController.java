package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.dto.affectation.AffectationRequest;
import com.banque.abc.tpe.dto.affectation.AffectationResponse;
import com.banque.abc.tpe.service.AffectationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/affectations")
@RequiredArgsConstructor
public class AffectationController {

    private final AffectationService affectationService;

    /**
     * Affecter un TPE à un commerçant suite à une demande validée
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<AffectationResponse> affecterTPE(@Valid @RequestBody AffectationRequest request) {
        AffectationResponse response = affectationService.affecterTPE(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtenir toutes les affectations (paginées)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE', 'AGENCE')")
    public ResponseEntity<Page<AffectationResponse>> getAllAffectations(Pageable pageable) {
        Page<AffectationResponse> affectations = affectationService.getAllAffectations(pageable);
        return ResponseEntity.ok(affectations);
    }

    /**
     * Obtenir une affectation par ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE', 'AGENCE')")
    public ResponseEntity<AffectationResponse> getAffectationById(@PathVariable Long id) {
        AffectationResponse response = affectationService.getAffectationById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtenir les affectations d'un commerçant
     */
    @GetMapping("/commercant/{commercantId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE', 'AGENCE')")
    public ResponseEntity<List<AffectationResponse>> getAffectationsByCommercant(@PathVariable Long commercantId) {
        List<AffectationResponse> affectations = affectationService.getAffectationsByCommercant(commercantId);
        return ResponseEntity.ok(affectations);
    }

    /**
     * Obtenir toutes les affectations actives
     */
    @GetMapping("/actives")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE', 'AGENCE')")
    public ResponseEntity<List<AffectationResponse>> getAffectationsActives() {
        List<AffectationResponse> affectations = affectationService.getAffectationsActives();
        return ResponseEntity.ok(affectations);
    }

    /**
     * Mettre un TPE en service
     */
    @PostMapping("/{id}/mise-en-service")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<AffectationResponse> mettreEnService(
            @PathVariable Long id,
            @RequestParam LocalDate dateMiseEnService) {
        AffectationResponse response = affectationService.mettreEnService(id, dateMiseEnService);
        return ResponseEntity.ok(response);
    }

    /**
     * Désaffecter un TPE
     */
    @PostMapping("/{id}/desaffecter")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<Void> desaffecterTPE(
            @PathVariable Long id,
            @RequestParam String motif) {
        affectationService.desaffecterTPE(id, motif);
        return ResponseEntity.noContent().build();
    }
}
