package com.banque.abc.tpe.dto.panne;

import com.banque.abc.tpe.entity.enums.TypePanne;
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
    
    private String description;

    private TypePanne typePanne;
    
    private Long tpeRemplacementId;
}
