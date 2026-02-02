package com.banque.abc.tpe.dto.affectation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffectationResponse {
    
    private Long id;
    private Long tpeId;
    private String numeroTerminal;
    private String numeroSerie;
    private String tpeNumeroSerie;
    private String tpeNumeroTerminal;
    private Long commercantId;
    private String commercantNom;
    private Long demandeId;
    private String demandeReference;
    private Long affecteParId;
    private String affecteParNom;
    private LocalDate dateAffectation;
    private LocalDate dateMiseEnService;
    private LocalDate dateFin;
    private Boolean actif;
    private String bonLivraisonPath;
    private String contratPath;
    private String commentaire;
    private LocalDateTime createdDate;
}
