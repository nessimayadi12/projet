package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.service.FichierBancaireService;
import com.banque.abc.tpe.service.RapportFichierBancaireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
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
            
            String originalFilename = file.getOriginalFilename();
            String safeFilename = originalFilename != null ? originalFilename : "fichier";
            
            // Si pas de date de session fournie, utiliser la date du jour
            if (sessionDate == null || sessionDate.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                sessionDate = LocalDate.now().format(formatter);
            }
            
            List<String> lignes = readProcessableLines(file);

            if (lignes.isEmpty()) {
                response.put("success", false);
                response.put("error", "Aucune ligne exploitable trouvee dans le fichier");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("Fichier reçu: {} ({} lignes)", safeFilename, lignes.size());
            
            // Traiter le fichier
            int nbEcritures = fichierBancaireService.traiterFichierBancaire(lignes, sessionDate);
            
            response.put("success", true);
            response.put("filename", safeFilename);
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

    private List<String> readProcessableLines(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        String lowerFilename = filename != null ? filename.toLowerCase() : "";

        if (lowerFilename.endsWith(".xls") || lowerFilename.endsWith(".xlsx")) {
            return readExcelLines(file);
        }

        return readTextLines(file);
    }

    private List<String> readTextLines(MultipartFile file) throws IOException {
        List<String> lignes = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                addLineIfNotBlank(lignes, ligne);
            }
        }

        return lignes;
    }

    private List<String> readExcelLines(MultipartFile file) throws Exception {
        List<String> lignes = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            if (workbook.getNumberOfSheets() == 0) {
                return lignes;
            }

            for (Row row : workbook.getSheetAt(0)) {
                if (row.getFirstCellNum() < 0) {
                    continue;
                }

                StringBuilder lineBuilder = new StringBuilder();
                for (int cellIndex = row.getFirstCellNum(); cellIndex < row.getLastCellNum(); cellIndex++) {
                    String cellValue = formatter.formatCellValue(row.getCell(cellIndex));
                    if (cellValue != null) {
                        lineBuilder.append(cellValue.trim());
                    }
                }
                addLineIfNotBlank(lignes, lineBuilder.toString());
            }
        }

        return lignes;
    }

    private void addLineIfNotBlank(List<String> lignes, String ligne) {
        if (ligne == null) {
            return;
        }

        String normalizedLine = ligne.startsWith("\uFEFF") ? ligne.substring(1) : ligne;
        if (!normalizedLine.trim().isEmpty()) {
            lignes.add(normalizedLine);
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
