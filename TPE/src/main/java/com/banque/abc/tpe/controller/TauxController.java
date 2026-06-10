package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.dto.taux.TauxRequest;
import com.banque.abc.tpe.dto.taux.TauxResponse;
import com.banque.abc.tpe.dto.taux.ValiderTauxRequest;
import com.banque.abc.tpe.service.TauxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/taux")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class TauxController {

    private final TauxService tauxService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<TauxResponse> createTaux(@Valid @RequestBody TauxRequest request) {
        TauxResponse response = tauxService.createTaux(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<TauxResponse> getTauxById(@PathVariable Long id) {
        TauxResponse response = tauxService.getTauxById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/soumettre")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<TauxResponse> soumettreValidation(@PathVariable Long id) {
        TauxResponse response = tauxService.soumettreValidation(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<TauxResponse> validerTaux(@PathVariable Long id,
                                                     @Valid @RequestBody ValiderTauxRequest request) {
        TauxResponse response = tauxService.validerTaux(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/en-attente")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<List<TauxResponse>> getTauxEnAttenteValidation() {
        List<TauxResponse> taux = tauxService.getTauxEnAttenteValidation();
        return ResponseEntity.ok(taux);
    }

    @GetMapping("/commercant/{commercantId}")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<List<TauxResponse>> getTauxByCommercant(@PathVariable Long commercantId) {
        List<TauxResponse> taux = tauxService.getTauxByCommercant(commercantId);
        return ResponseEntity.ok(taux);
    }
}
