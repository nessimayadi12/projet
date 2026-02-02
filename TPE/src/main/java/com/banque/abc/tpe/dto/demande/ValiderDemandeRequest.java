package com.banque.abc.tpe.dto.demande;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValiderDemandeRequest {
    
    @NotNull(message = "La validation est obligatoire")
    private Boolean approuver;
    
    private String commentaire;
    
    // Champs de validation Monetique (TPE Physique)
    private String mcc;
    private Double tauxCommission;
    private Double tauxCommissionInter;
    private Double loyer;
    private String serieTpe;
    private String numeroTerminal;
    private LocalDateTime valueDate;
}
