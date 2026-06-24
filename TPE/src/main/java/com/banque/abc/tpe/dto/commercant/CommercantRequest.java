package com.banque.abc.tpe.dto.commercant;

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
}
