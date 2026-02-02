package com.banque.abc.tpe.dto.taux;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValiderTauxRequest {
    
    @NotNull(message = "La décision est obligatoire")
    private Boolean approuver;
    
    private String motifRejet;
}
