package com.banque.abc.tpe.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ReferenceGenerator {

    /**
     * Génère une référence de demande
     * Format: DEM-YYYY-NNN (ex: DEM-2026-001)
     */
    public String generateDemandeReference(int compteur) {
        int year = LocalDateTime.now().getYear();
        return String.format("DEM-%d-%03d", year, compteur);
    }

    /**
     * Génère une référence de panne
     * Format: PAN-YYYY-NNN (ex: PAN-2026-001)
     */
    public String generatePanneReference(int compteur) {
        int year = LocalDateTime.now().getYear();
        return String.format("PAN-%d-%03d", year, compteur);
    }

    /**
     * Génère un nom de fichier unique
     */
    public String generateUniqueFileName(String originalFileName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String extension = "";
        
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        return timestamp + "_" + System.nanoTime() + extension;
    }
}
