package com.banque.abc.tpe.dto.panne;

import com.banque.abc.tpe.entity.enums.StatutPanne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanneResponse {
    
    private Long id;
    private String reference;
    private Long tpeId;
    private String tpeNumeroSerie;
    private StatutPanne statut;
    private String description;
    private LocalDateTime dateDeclaration;
    private LocalDateTime dateDiagnostic;
    private LocalDateTime dateReparation;
    private LocalDateTime dateResolution;
    private String declarantNom;
    private String technicienNom;
    private String diagnostic;
    private String actionCorrective;
    private String commentaireTechnicien;
    private Long tpeRemplacementId;
    private String tpeRemplacementNumero;
    private Double coutReparation;
    private Boolean sousGarantie;
    private LocalDateTime createdDate;
}
