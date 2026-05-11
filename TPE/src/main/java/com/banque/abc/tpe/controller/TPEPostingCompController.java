package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.dto.EcritureComptableDTO;
import com.banque.abc.tpe.dto.PorteurInfoDTO;
import com.banque.abc.tpe.dto.TPEInfoDTO;
import com.banque.abc.tpe.entity.TPEPostingComp;
import com.banque.abc.tpe.repository.TPEPostingCompRepository;
import com.banque.abc.tpe.service.TPEPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tpe-posting")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TPEPostingCompController {

    private final TPEPostingCompRepository tpePostingCompRepository;
    private final TPEPostingService tpePostingService;

    /**
     * Récupère toutes les transactions
     */
    @GetMapping
    public ResponseEntity<List<TPEPostingComp>> getAllTransactions(
            @RequestParam(defaultValue = "1000") int limit) {
        
        Pageable pageable = PageRequest.of(0, limit);
        List<TPEPostingComp> transactions = tpePostingCompRepository.findAll(pageable).getContent();
        
        return ResponseEntity.ok(transactions);
    }

    /**
     * Récupère les transactions les plus récentes (dernières insérées)
     */
    @GetMapping("/recent")
    public ResponseEntity<List<TPEPostingComp>> getRecentTransactions(
            @RequestParam(defaultValue = "500") int limit) {
        
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
        List<TPEPostingComp> transactions = tpePostingCompRepository.findAll(pageable).getContent();
        
        return ResponseEntity.ok(transactions);
    }

    /**
     * Compte le nombre total de transactions
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getTotalCount() {
        long count = tpePostingCompRepository.count();
        
        Map<String, Long> response = new HashMap<>();
        response.put("totalCount", count);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère une transaction par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TPEPostingComp> getTransactionById(@PathVariable Long id) {
        return tpePostingCompRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Supprime toutes les transactions (pour tests uniquement)
     */
    @DeleteMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteAll() {
        tpePostingCompRepository.deleteAll();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Toutes les transactions ont été supprimées");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Vérifier si un TPE existe et récupérer ses informations
     * GET /api/tpe-posting/verify-tpe/{affiliation}
     */
    @GetMapping("/verify-tpe/{affiliation}")
    public ResponseEntity<TPEInfoDTO> verifyTPE(@PathVariable String affiliation) {
        try {
            TPEInfoDTO tpeInfo = tpePostingService.verifyTPE(affiliation);
            return ResponseEntity.ok(tpeInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TPEInfoDTO(affiliation, null, false, null, null, null, null));
        }
    }

    /**
     * Vérifier si un porteur existe et récupérer ses informations de devise
     * GET /api/tpe-posting/verify-porteur/{ncarte}
     */
    @GetMapping("/verify-porteur/{ncarte}")
    public ResponseEntity<PorteurInfoDTO> verifyPorteur(@PathVariable String ncarte) {
        try {
            PorteurInfoDTO porteurInfo = tpePostingService.verifyPorteur(ncarte);
            return ResponseEntity.ok(porteurInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PorteurInfoDTO(ncarte, null, "TND", "TND", 1.0, 3, false, null, null, null));
        }
    }

    /**
     * Insérer des écritures comptables dans TPE_POSTING_comp
     * POST /api/tpe-posting/insert-postings
     */
    @PostMapping("/insert-postings")
    public ResponseEntity<Map<String, Object>> insertPostings(
            @RequestBody List<EcritureComptableDTO> ecritures,
            Principal principal) {
        
        try {
            String sessionUser = principal != null ? principal.getName() : System.getProperty("user.name");
            int count = tpePostingService.insertPostings(ecritures, sessionUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("insertedCount", count);
            response.put("message", count + " écritures insérées avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
