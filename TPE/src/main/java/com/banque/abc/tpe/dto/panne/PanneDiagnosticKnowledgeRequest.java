package com.banque.abc.tpe.dto.panne;

import com.banque.abc.tpe.entity.enums.TypePanne;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanneDiagnosticKnowledgeRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    private TypePanne typePanne;

    private List<String> motsCles;

    private String symptomes;

    @NotBlank(message = "Le diagnostic est obligatoire")
    private String diagnostic;

    @NotBlank(message = "L'action corrective est obligatoire")
    private String actionCorrective;

    @NotBlank(message = "L'urgence est obligatoire")
    private String urgence;

    private List<String> recommandations;

    private Boolean remplacementRecommande;

    private Boolean actif;

    private Integer priorite;
}
