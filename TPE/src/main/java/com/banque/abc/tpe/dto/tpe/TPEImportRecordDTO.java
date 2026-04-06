package com.banque.abc.tpe.dto.tpe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TPEImportRecordDTO {

    private Long id;
    private String nAffiliation;
    private Integer sourceRowNumber;
    private String sourceFileName;
    private String typeTPE;
    private String numeroSerie;
    private String numeroTerminal;
    private String raisonSociale;
    private String activite;
    private String mcc;
    private String numeroCompte;
    private String codeAgence;
    private String adresse;
    private String codePostal;
    private String telephone;
    private String email;
    private String privilegeSecteur;
    private String tauxCommission;
    private String tauxCommissionInter;
    private String loyer;
    private String nCompteIntern;
    private String groupe;
    private String numSeq;
    private Boolean active;
    private LocalDate valueDate;
    private LocalDate dateAffiliation;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}