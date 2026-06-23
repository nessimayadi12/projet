package com.banque.abc.tpe.dto.panne;

import com.banque.abc.tpe.entity.enums.TypePanne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PanneDiagnosticKnowledgeResponse {

    private Long id;
    private String titre;
    private TypePanne typePanne;
    private List<String> motsCles;
    private String symptomes;
    private String diagnostic;
    private String actionCorrective;
    private String urgence;
    private List<String> recommandations;
    private Boolean remplacementRecommande;
    private Boolean actif;
    private Integer priorite;
    private LocalDateTime lastModifiedDate;
}
