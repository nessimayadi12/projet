package com.banque.abc.tpe.dto.affectation;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffectationRequest {
    
    @NotNull(message = "La demande est obligatoire")
    private Long demandeId;
    
    // TPE optionnel - si null, sera créé ou trouvé automatiquement
    private Long tpeId;
    
    // Optionnels pour la création de TPE
    private String marque;
    private String modele;
    
    private String commentaire;
}
