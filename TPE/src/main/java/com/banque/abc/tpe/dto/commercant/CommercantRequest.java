package com.banque.abc.tpe.dto.commercant;

import com.banque.abc.tpe.entity.enums.TypeTPE;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommercantRequest {
    
    @NotBlank(message = "La raison sociale est obligatoire")
    private String raisonSociale;
    
    @NotBlank(message = "L'activité est obligatoire")
    private String activite;
    
    @NotBlank(message = "Le numéro de compte est obligatoire")
    private String numeroCompte;
    
    private String adresse;
    
    private String localite;
    
    private String codePostal;
    
    @NotBlank(message = "Le code agence est obligatoire")
    private String codeAgence;
    
    private String telephone;
    
    @Email(message = "L'email doit être valide")
    private String email;
    
    // TPE Physique
    private Double loyer;
    private String emailNotification;
    
    // E-commerce
    private TypeTPE typeCommerce;
    private String urlSiteMarchand;
    private String webhookUrl;
    private String webmaster;
    private String contactTechnique;
    private String typeCartesAcceptees;
    private Boolean modeTest;
}
