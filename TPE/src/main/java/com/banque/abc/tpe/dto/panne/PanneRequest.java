package com.banque.abc.tpe.dto.panne;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanneRequest {
    
    @NotNull(message = "Le TPE est obligatoire")
    private Long tpeId;
    
    @NotBlank(message = "La description de la panne est obligatoire")
    private String description;
    
    private Long tpeRemplacementId;
}
