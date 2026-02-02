package com.banque.abc.tpe.util;

import org.springframework.stereotype.Component;

@Component
public class TIDGenerator {

    /**
     * Génère un numéro de terminal (TID) selon la règle:
     * - 2 premiers chiffres = 2 premiers chiffres du RIB
     * - 3 chiffres suivants = Code agence
     * - 3 chiffres suivants = Compteur terminal
     * - Dernier chiffre = Clé Luhn
     * 
     * Exemple: 23 041 008 5
     */
    public String generateTID(String rib, String codeAgence, int compteur) {
        // Validation des entrées
        if (rib == null || rib.length() < 2) {
            throw new IllegalArgumentException("Le RIB doit contenir au moins 2 chiffres");
        }
        if (codeAgence == null || codeAgence.length() != 3) {
            throw new IllegalArgumentException("Le code agence doit contenir exactement 3 chiffres");
        }
        
        // Extraction des 2 premiers chiffres du RIB
        String deuxPremiersRIB = rib.substring(0, 2);
        
        // Formatage du compteur sur 3 chiffres
        String compteurFormate = String.format("%03d", compteur);
        
        // Construction du TID sans la clé de Luhn
        String tidSansCle = deuxPremiersRIB + codeAgence + compteurFormate;
        
        // Calcul de la clé de Luhn
        int cleLuhn = calculateLuhnCheckDigit(tidSansCle);
        
        // TID final
        return tidSansCle + cleLuhn;
    }

    /**
     * Calcule la clé de contrôle de Luhn
     */
    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = false;
        
        // Parcourir de droite à gauche
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        // La clé est ce qu'il faut ajouter pour arriver à un multiple de 10
        return (10 - (sum % 10)) % 10;
    }

    /**
     * Valide un TID complet avec sa clé de Luhn
     */
    public boolean validateTID(String tid) {
        if (tid == null || tid.length() != 9) {
            return false;
        }
        
        int sum = 0;
        boolean alternate = false;
        
        for (int i = tid.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(tid.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (sum % 10 == 0);
    }
}
