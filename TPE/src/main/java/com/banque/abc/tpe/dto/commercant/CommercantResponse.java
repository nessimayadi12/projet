package com.banque.abc.tpe.dto.commercant;

import com.banque.abc.tpe.entity.enums.StatutCommercant;
import com.banque.abc.tpe.entity.enums.TypeTPE;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommercantResponse {
    
    private Long id;
    private String raisonSociale;
    private String activite;
    private String numeroCompte;
    private String adresse;
    private String localite;
    private String codePostal;
    private String codeAgence;
    private String telephone;
    private String email;
    private StatutCommercant statut;
    private Double loyer;
    private TypeTPE typeCommerce;
    private String urlSiteMarchand;
    private String webhookUrl;
    private String webmaster;
    private String contactTechnique;
    private String typeCartesAcceptees;
    private Boolean modeTest;
    private Integer nombreTPEs;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
