package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.dto.demande.DemandeRequest;
import com.banque.abc.tpe.dto.demande.DemandeResponse;
import com.banque.abc.tpe.dto.demande.ValiderDemandeRequest;
import com.banque.abc.tpe.service.DemandeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/demandes", "/api/demande"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class DemandeController {

    private final DemandeService demandeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('AGENCE', 'ADMIN')")
    public ResponseEntity<DemandeResponse> createDemande(@Valid @RequestBody DemandeRequest request) {
        DemandeResponse response = demandeService.createDemande(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'AGENCE', 'ADMIN')")
    public ResponseEntity<DemandeResponse> getDemandeById(@PathVariable Long id) {
        DemandeResponse response = demandeService.getDemandeById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MONETIQUE', 'AGENCE', 'ADMIN')")
    public ResponseEntity<Page<DemandeResponse>> getAllDemandes(Pageable pageable) {
        Page<DemandeResponse> demandes = demandeService.getAllDemandes(pageable);
        return ResponseEntity.ok(demandes);
    }

    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<DemandeResponse> validerDemande(@PathVariable Long id,
                                                           @Valid @RequestBody ValiderDemandeRequest request) {
        DemandeResponse response = demandeService.validerDemande(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/rejeter")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<DemandeResponse> rejeterDemande(@PathVariable Long id,
                                                           @RequestBody String commentaire) {
        DemandeResponse response = demandeService.rejeterDemande(id, commentaire);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cloturer")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<String> cloturerDemande(@PathVariable Long id) {
        demandeService.cloturerDemande(id);
        return ResponseEntity.ok("Demande clôturée avec succès");
    }
}
