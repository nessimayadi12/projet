package com.banque.abc.tpe.dto.tpe;

import com.banque.abc.tpe.entity.enums.StatutTPE;
import com.banque.abc.tpe.entity.enums.TypeTPE;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TPERequest {
    
    private TypeTPE typeTPE;
    
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

    private LocalDate valueDate;

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
