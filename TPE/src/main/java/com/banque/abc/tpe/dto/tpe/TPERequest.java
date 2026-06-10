package com.banque.abc.tpe.dto.tpe;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TPERequest {
    
    private String typeTPE;
    
    private String numeroSerie;
    
    private String marque;
    
    private String modele;
    
    private LocalDate dateAcquisition;

    private LocalDate dateMiseEnService;
    
    private String mcc;
    
    private String numeroAffiliation;

    private String numeroTerminal;

    private String raisonSociale;

    private String activite;

    private Double tauxCommission;

    private Double tauxCommissionInter;

    private String numeroCompte;

    private String serieTpe;

    @Min(value = 1, message = "La value date doit etre 1 ou 2")
    @Max(value = 2, message = "La value date doit etre 1 ou 2")
    private Integer valueDate = 1;

    private Double loyer;

    private String urlSiteMarchand;

    private String webhookUrl;

    private String cleApi;

    private String typeCommerce;

    private String cartesAcceptees;

    private Boolean modeTest;
    
    private String rib;
    
    private String codeAgence;
    
    private String commentaire;
}
