package com.banque.abc.tpe.dto.panne;

import com.banque.abc.tpe.entity.enums.TypePanne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PanneDiagnosticIaResponse {

    private TypePanne typePanneSuggere;
    private String diagnosticPropose;
    private String actionCorrectiveProposee;
    private String urgence;
    private Integer scoreConfiance;
    private List<String> indicesDetectes;
    private List<String> recommandations;
    private Boolean remplacementRecommande;
    private String contexteRag;
    private List<PanneDiagnosticIaSource> sourcesRetenues;
}
