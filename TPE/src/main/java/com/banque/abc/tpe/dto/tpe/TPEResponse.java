package com.banque.abc.tpe.dto.tpe;

import com.banque.abc.tpe.entity.enums.StatutTPE;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TPEResponse {
    
    private Long id;
    private String typeTPE;
    private String numeroSerie;
    private String numeroTerminal;
    private StatutTPE statut;
    private String marque;
    private String modele;
    private LocalDate dateAcquisition;
    private LocalDate dateMiseEnService;
    private String mcc;
    private String numeroAffiliation;
    private String raisonSociale;
    private String activite;
    private Double tauxCommission;
    private Double tauxCommissionInter;
    private String numeroCompte;
    private String codeAgence;
    private String serieTpe;
    private Integer valueDate;
    private Double loyer;
    private String urlSiteMarchand;
    private String webhookUrl;
    private String typeCommerce;
    private String cartesAcceptees;
    private Boolean modeTest;
    private Long commercantId;
    private String commercantNom;
    private String commentaire;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
