package com.banque.abc.tpe.dto.demande;

import com.banque.abc.tpe.entity.enums.StatutDemande;
import com.banque.abc.tpe.entity.enums.TypeTPE;
import com.banque.abc.tpe.entity.enums.Urgence;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeResponse {
    
    private Long id;
    private String reference;
    private TypeTPE typeDemande;
    private StatutDemande statut;
    private Long commercantId;
    private String commercantNom;
    private Long demandeurId;
    private String demandeurNom;
    private Long inputerId;
    private String inputerNom;
    private Long valideurId;
    private String valideurNom;
    private LocalDateTime dateValidation;
    private LocalDateTime dateSaisieTaux;
    private LocalDateTime dateCloture;
    private String description;
    private String commentaireValidation;
    private Urgence urgence;
    
    // Champs de demande agence (TPE)
    private String raisonSociale;
    private String activite;
    private String numeroCompte;
    private String adresse;
    private String codePostal;
    private String codeAgence;
    private String telephone;
    private String rneFilePath;
    
    // Champs de validation Monétique (TPE)
    private String mcc;
    private Double tauxCommission;
    private Double tauxCommissionInter;
    private Double loyer;
    private String serieTpe;
    private String numeroTerminal;
    private Integer valueDate;

    // Affectation / remplacement TPE
    private Long tpeAffecteId;
    private String tpeAffecteNumeroSerie;
    private String tpeAffecteStatut;
    private LocalDate dateAffectation;
    private Long tpeRemplacementId;
    private String tpeRemplacementNumeroSerie;
    private String nouvelleSerieTpe;
    
    // Champs spécifiques Mobile
    private String localite;
    private String rib;
    private String webmaster;
    private String contactTechnique;
    private String urlSiteMarchand;
    
    // Pièces jointes
    private List<String> piecesJointes;
    
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
