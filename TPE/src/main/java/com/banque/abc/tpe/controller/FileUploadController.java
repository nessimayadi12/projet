package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/file-upload")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/process")
    @PreAuthorize("hasAnyRole('MONETIQUE', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> uploadAndProcessFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Le fichier est vide");
                return ResponseEntity.badRequest().body(response);
            }

            log.info("Début du traitement du fichier: {}", file.getOriginalFilename());
            fileUploadService.uploadAndProcessFile(file);
            
            response.put("success", true);
            response.put("message", "Fichier traité avec succès");
            response.put("filename", file.getOriginalFilename());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors du traitement du fichier: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Erreur lors du traitement du fichier: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
