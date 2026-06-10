package com.banque.abc.tpe.dto.demande;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValiderDemandeRequest {
    
    @NotNull(message = "La validation est obligatoire")
    private Boolean approuver;
    
    private String commentaire;
    
    // Champs de validation Monetique (TPE)
    private String mcc;
    private Double tauxCommission;
    private Double tauxCommissionInter;
    private Double loyer;
    private String serieTpe;
    private String numeroTerminal;
    @Min(value = 1, message = "La value date doit etre 1 ou 2")
    @Max(value = 2, message = "La value date doit etre 1 ou 2")
    private Integer valueDate = 1;
}
