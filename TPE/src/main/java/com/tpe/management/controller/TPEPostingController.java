package com.tpe.management.controller;

import com.tpe.management.dto.EcritureComptableDTO;
import com.tpe.management.dto.PorteurInfo;
import com.tpe.management.dto.TPEInfo;
import com.tpe.management.service.TPEPostingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tpe-posting")
@CrossOrigin(origins = "http://localhost:4200")
public class TPEPostingController {

    @Autowired
    private TPEPostingService tpePostingService;

    /**
     * Vérifier si un TPE existe
     * GET /api/tpe-posting/verify-tpe/{affiliation}
     */
    @GetMapping("/verify-tpe/{affiliation}")
    public ResponseEntity<TPEInfo> verifyTPE(@PathVariable String affiliation) {
        try {
            TPEInfo tpeInfo = tpePostingService.verifyTPE(affiliation);
            return ResponseEntity.ok(tpeInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TPEInfo(affiliation, null, false, null, null, null));
        }
    }

    /**
     * Vérifier si un porteur existe
     * GET /api/tpe-posting/verify-porteur/{ncarte}
     */
    @GetMapping("/verify-porteur/{ncarte}")
    public ResponseEntity<PorteurInfo> verifyPorteur(@PathVariable String ncarte) {
        try {
            PorteurInfo porteurInfo = tpePostingService.verifyPorteur(ncarte);
            return ResponseEntity.ok(porteurInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PorteurInfo(ncarte, null, "TND", "TND", 1.0, 3, false, null, null, null));
        }
    }

    /**
     * Insérer des écritures comptables
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
