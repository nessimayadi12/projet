package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.dto.tpe.TPERequest;
import com.banque.abc.tpe.dto.tpe.TPEImportResult;
import com.banque.abc.tpe.dto.tpe.TPEImportRecordDTO;
import com.banque.abc.tpe.dto.tpe.TPEResponse;
import com.banque.abc.tpe.entity.enums.StatutTPE;
import com.banque.abc.tpe.service.TPEService;
import com.banque.abc.tpe.service.TPEExcelImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping({"/api/tpes", "/api/tpe"})
@RequiredArgsConstructor
public class TPEController {

    private final TPEService tpeService;
    private final TPEExcelImportService tpeExcelImportService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<TPEResponse> createTPE(@Valid @RequestBody TPERequest request) {
        TPEResponse response = tpeService.createTPE(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'AGENCE', 'ADMIN')")
    public ResponseEntity<TPEResponse> getTPEById(@PathVariable Long id) {
        TPEResponse response = tpeService.getTPEById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MONETIQUE', 'AGENCE', 'ADMIN')")
    public ResponseEntity<Page<TPEResponse>> getAllTPEs(Pageable pageable) {
        Page<TPEResponse> tpes = tpeService.getAllTPEs(pageable);
        return ResponseEntity.ok(tpes);
    }

    @GetMapping("/declaration-panne/search")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'AGENCE', 'ADMIN')")
    public ResponseEntity<List<TPEResponse>> searchTPEsForPanneDeclaration(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(tpeService.searchTPEsForPanneDeclaration(query, limit));
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'AGENCE', 'ADMIN')")
    public ResponseEntity<List<TPEResponse>> getTPEsByStatut(@PathVariable StatutTPE statut) {
        List<TPEResponse> tpes = tpeService.getTPEsByStatut(statut);
        return ResponseEntity.ok(tpes);
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<List<TPEResponse>> getTPEsDisponibles() {
        List<TPEResponse> tpes = tpeService.getTPEsDisponibles();
        return ResponseEntity.ok(tpes);
    }

    @GetMapping("/commercant/{commercantId}")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'AGENCE', 'ADMIN')")
    public ResponseEntity<List<TPEResponse>> getTPEsByCommercant(@PathVariable Long commercantId) {
        return ResponseEntity.ok(tpeService.getTPEsByCommercant(commercantId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<TPEResponse> updateTPE(@PathVariable Long id, 
                                                  @Valid @RequestBody TPERequest request) {
        TPEResponse response = tpeService.updateTPE(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<String> updateStatut(@PathVariable Long id,
                                                @RequestParam StatutTPE statut,
                                                @RequestParam(required = false) String commentaire) {
        tpeService.updateStatut(id, statut, commentaire);
        return ResponseEntity.ok("Statut mis à jour avec succès");
    }

    @PostMapping("/{id}/generate-tid")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<String> generateTID(@PathVariable Long id,
                                               @RequestParam String rib,
                                               @RequestParam String codeAgence) {
        String tid = tpeService.generateTIDForTPE(id, rib, codeAgence);
        return ResponseEntity.ok(tid);
    }

    @PostMapping("/generer-tid")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<String> genererTID(@RequestBody TPERequest request) {
        String rib = request.getRib() != null && !request.getRib().isBlank()
                ? request.getRib()
                : request.getNumeroCompte();
        String tid = tpeService.generateTID(rib, request.getCodeAgence());
        return ResponseEntity.ok(tid);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<TPEImportResult> importExcel(@RequestParam("file") MultipartFile file) {
        TPEImportResult result = tpeExcelImportService.importExcel(file);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/import-records")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<Page<TPEImportRecordDTO>> getImportRecords(Pageable pageable) {
        return ResponseEntity.ok(tpeExcelImportService.getImportRecords(pageable));
    }

    @GetMapping("/import-records/export")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<List<TPEImportRecordDTO>> getAllImportRecords() {
        return ResponseEntity.ok(tpeExcelImportService.getImportRecords(Pageable.unpaged()).getContent());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteTPE(@PathVariable Long id) {
        tpeService.deleteTPE(id);
        return ResponseEntity.ok("TPE supprimé avec succès");
    }
}
