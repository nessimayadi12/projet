package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.service.FichierBancaireService;
import com.banque.abc.tpe.service.RapportFichierBancaireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fichier-bancaire")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class FichierBancaireController {

    private final FichierBancaireService fichierBancaireService;
    private final RapportFichierBancaireService rapportService;

    /**
     * Upload et traitement d'un fichier bancaire
     * POST /api/fichier-bancaire/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFichierBancaire(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sessionDate", required = false) String sessionDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validation du fichier
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "Le fichier est vide");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Vérifier l'extension du fichier
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".txt")) {
                response.put("success", false);
                response.put("error", "Le fichier doit être au format .txt");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Si pas de date de session fournie, utiliser la date du jour
            if (sessionDate == null || sessionDate.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                sessionDate = LocalDate.now().format(formatter);
            }
            
            // Lire le contenu du fichier ligne par ligne
            List<String> lignes = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                
                String ligne;
                while ((ligne = reader.readLine()) != null) {
                    if (!ligne.trim().isEmpty()) {
                        lignes.add(ligne);
                    }
                }
            }
            
            log.info("Fichier reçu: {} ({} lignes)", originalFilename, lignes.size());
            
            // Traiter le fichier
            int nbEcritures = fichierBancaireService.traiterFichierBancaire(lignes, sessionDate);
            
            response.put("success", true);
            response.put("filename", originalFilename);
            response.put("lignesLues", lignes.size());
            response.put("ecrituresCreees", nbEcritures);
            response.put("sessionDate", sessionDate);
            response.put("message", "Fichier traité avec succès: " + nbEcritures + " écritures créées");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors du traitement du fichier bancaire", e);
            response.put("success", false);
            response.put("error", "Erreur lors du traitement: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtenir les statistiques de traitement pour une date de session
     * GET /api/fichier-bancaire/stats/{sessionDate}
     */
    @GetMapping("/stats/{sessionDate}")
    public ResponseEntity<Map<String, Object>> getStatistiques(@PathVariable String sessionDate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int count = fichierBancaireService.getTransactionCount(sessionDate);
            
            response.put("success", true);
            response.put("sessionDate", sessionDate);
            response.put("transactionCount", count);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Récupère les transactions pour une session donnée
     * GET /api/fichier-bancaire/transactions/{sessionDate}
     */
    @GetMapping("/transactions/{sessionDate}")
    public ResponseEntity<Map<String, Object>> getTransactions(@PathVariable String sessionDate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Map<String, Object>> transactions = fichierBancaireService.getTransactions(sessionDate);
            
            response.put("success", true);
            response.put("sessionDate", sessionDate);
            response.put("count", transactions.size());
            response.put("transactions", transactions);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des transactions", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Test de l'API
     * GET /api/fichier-bancaire/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "API Fichier Bancaire fonctionnelle");
        response.put("timestamp", LocalDate.now().toString());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Génère un rapport PDF pour une session
     * GET /api/fichier-bancaire/rapport/pdf/{sessionDate}
     */
    @GetMapping("/rapport/pdf/{sessionDate}")
    public ResponseEntity<byte[]> genererRapportPDF(@PathVariable String sessionDate) {
        try {
            byte[] pdfBytes = rapportService.genererRapportPDF(sessionDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    "rapport_fichier_bancaire_" + sessionDate + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            log.error("Erreur lors de la génération du rapport PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Génère un rapport texte pour une session
     * GET /api/fichier-bancaire/rapport/text/{sessionDate}
     */
    @GetMapping("/rapport/text/{sessionDate}")
    public ResponseEntity<String> genererRapportTexte(@PathVariable String sessionDate) {
        try {
            String rapport = rapportService.genererRapportTexte(sessionDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", 
                    "rapport_fichier_bancaire_" + sessionDate + ".txt");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(rapport);
                    
        } catch (Exception e) {
            log.error("Erreur lors de la génération du rapport texte", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
