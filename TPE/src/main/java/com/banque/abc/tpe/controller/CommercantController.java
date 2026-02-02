package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.dto.commercant.CommercantRequest;
import com.banque.abc.tpe.dto.commercant.CommercantResponse;
import com.banque.abc.tpe.entity.enums.StatutCommercant;
import com.banque.abc.tpe.service.CommercantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/commercants", "/api/commercant"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class CommercantController {

    private final CommercantService commercantService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MONETIQUE', 'AGENCE', 'ADMIN')")
    public ResponseEntity<CommercantResponse> createCommercant(@Valid @RequestBody CommercantRequest request) {
        CommercantResponse response = commercantService.createCommercant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'AGENCE', 'ADMIN')")
    public ResponseEntity<CommercantResponse> getCommercantById(@PathVariable Long id) {
        CommercantResponse response = commercantService.getCommercantById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MONETIQUE', 'AGENCE', 'ADMIN')")
    public ResponseEntity<List<CommercantResponse>> getAllCommercants() {
        List<CommercantResponse> commercants = commercantService.getAllCommercantsList();
        return ResponseEntity.ok(commercants);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'AGENCE', 'ADMIN')")
    public ResponseEntity<List<CommercantResponse>> searchCommercants(@RequestParam String query) {
        List<CommercantResponse> commercants = commercantService.searchCommercants(query);
        return ResponseEntity.ok(commercants);
    }

    @GetMapping("/agence/{codeAgence}")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'AGENCE', 'ADMIN')")
    public ResponseEntity<List<CommercantResponse>> getCommercantsByAgence(@PathVariable String codeAgence) {
        List<CommercantResponse> commercants = commercantService.getCommercantsByCodeAgence(codeAgence);
        return ResponseEntity.ok(commercants);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'AGENCE', 'ADMIN')")
    public ResponseEntity<CommercantResponse> updateCommercant(@PathVariable Long id,
                                                                @Valid @RequestBody CommercantRequest request) {
        CommercantResponse response = commercantService.updateCommercant(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<String> updateStatut(@PathVariable Long id, @RequestParam StatutCommercant statut) {
        commercantService.updateStatut(id, statut);
        return ResponseEntity.ok("Statut mis à jour avec succès");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteCommercant(@PathVariable Long id) {
        commercantService.deleteCommercant(id);
        return ResponseEntity.ok("Commerçant supprimé avec succès");
    }
}
