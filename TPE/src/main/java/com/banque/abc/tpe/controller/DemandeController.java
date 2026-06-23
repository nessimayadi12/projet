package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.dto.demande.DemandeRequest;
import com.banque.abc.tpe.dto.demande.DemandeResponse;
import com.banque.abc.tpe.dto.demande.ValiderDemandeRequest;
import com.banque.abc.tpe.service.DemandeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'AGENCE', 'ADMIN')")
    public ResponseEntity<DemandeResponse> updateDemande(@PathVariable Long id,
                                                         @Valid @RequestBody DemandeRequest request) {
        DemandeResponse response = demandeService.updateDemande(id, request);
        return ResponseEntity.ok(response);
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

    @PostMapping("/{id}/attente-complement")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<DemandeResponse> mettreEnAttenteComplement(@PathVariable Long id,
                                                                     @RequestBody Map<String, String> payload) {
        String commentaire = payload != null ? payload.get("commentaire") : null;
        DemandeResponse response = demandeService.mettreEnAttenteComplement(id, commentaire);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cloturer")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<String> cloturerDemande(@PathVariable Long id) {
        demandeService.cloturerDemande(id);
        return ResponseEntity.ok("Demande clôturée avec succès");
    }

    @PostMapping("/{id}/piece-jointe")
    @PreAuthorize("hasAnyRole('AGENCE', 'MONETIQUE', 'ADMIN')")
    public ResponseEntity<String> uploadPieceJointe(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            demandeService.uploadPieceJointe(id, file);
            return ResponseEntity.ok("Fichier uploadé avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'upload: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/piece-jointe/{fileName:.+}")
    @PreAuthorize("hasAnyRole('AGENCE', 'MONETIQUE', 'ADMIN')")
    public ResponseEntity<byte[]> downloadPieceJointe(
            @PathVariable Long id,
            @PathVariable String fileName) {
        try {
            byte[] fileContent = demandeService.downloadPieceJointe(id, fileName);
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            String asciiFileName = fileName
                    .replaceAll("[\\r\\n\"]", "_")
                    .replaceAll("[^\\x20-\\x7E]", "_");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + asciiFileName + "\"; filename*=UTF-8''" + encodedFileName)
                    .body(fileContent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
