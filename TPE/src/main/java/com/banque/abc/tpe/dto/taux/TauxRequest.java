package com.banque.abc.tpe.dto.taux;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TauxRequest {
    
    @NotNull(message = "Le commerçant est obligatoire")
    private Long commercantId;
    
    @NotNull(message = "Le nouveau taux de commission est obligatoire")
    private Double nouveauTauxCommission;
    
    @NotNull(message = "Le nouveau taux de commission inter est obligatoire")
    private Double nouveauTauxCommissionInter;
    
    private String commentaire;
}
