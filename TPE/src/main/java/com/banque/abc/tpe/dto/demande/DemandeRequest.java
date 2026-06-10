package com.banque.abc.tpe.dto.demande;

import com.banque.abc.tpe.entity.enums.TypeTPE;
import com.banque.abc.tpe.entity.enums.Urgence;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeRequest {
    
    @NotNull(message = "Le type de demande est obligatoire")
    private TypeTPE typeDemande;
    
    // commercantId optionnel : créé après validation Monétique
    private Long commercantId;
    
    private String description;
    
    private Urgence urgence = Urgence.NORMALE;
    
    // Champs de demande agence (TPE)
    private String raisonSociale;
    private String activite;
    private String numeroCompte;
    private String adresse;
    private String codePostal;
    private String codeAgence;
    private String telephone;

    // Champs de validation Monetique (TPE)
    private String mcc;
    @DecimalMin(value = "0.0", message = "Le taux de commission doit etre positif")
    @DecimalMax(value = "100.0", message = "Le taux de commission ne doit pas depasser 100")
    private Double tauxCommission;
    @DecimalMin(value = "0.0", message = "Le taux de commission inter doit etre positif")
    @DecimalMax(value = "100.0", message = "Le taux de commission inter ne doit pas depasser 100")
    private Double tauxCommissionInter;
    @DecimalMin(value = "0.0", message = "Le loyer doit etre positif")
    private Double loyer;
    private String serieTpe;
    private String numeroTerminal;
    @Min(value = 1, message = "La value date doit etre 1 ou 2")
    @Max(value = 2, message = "La value date doit etre 1 ou 2")
    private Integer valueDate;
    
    // Champs spécifiques Mobile
    private String localite;
    private String rib;
    private String webmaster;
    private String contactTechnique;
    private String urlSiteMarchand;
}
