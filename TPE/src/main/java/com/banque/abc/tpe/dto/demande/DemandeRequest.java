package com.banque.abc.tpe.dto.demande;

import com.banque.abc.tpe.entity.enums.TypeTPE;
import com.banque.abc.tpe.entity.enums.Urgence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    
    // Champs de demande agence (TPE Physique)
    private String raisonSociale;
    private String activite;
    private String numeroCompte;
    private String adresse;
    private String codePostal;
    private String codeAgence;
    private String telephone;
    private String emailNotification;
    
    // Champs spécifiques E-commerce
    private String localite;
    private String rib;
    private String webmaster;
    private String contactTechnique;
    private String urlSiteMarchand;
}
