package com.banque.abc.tpe.entity;

import com.banque.abc.tpe.entity.enums.StatutDemande;
import com.banque.abc.tpe.entity.enums.TypeTPE;
import com.banque.abc.tpe.entity.enums.Urgence;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "demandes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Demande extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String reference; // Ex: DEM-2026-001

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeTPE typeDemande;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutDemande statut = StatutDemande.NOUVELLE;

    @ManyToOne
    @JoinColumn(name = "commercant_id", nullable = false)
    private Commercant commercant;

    @ManyToOne
    @JoinColumn(name = "demandeur_id", nullable = false)
    private User demandeur;

    @ManyToOne
    @JoinColumn(name = "valideur_id")
    private User valideur;

    @ManyToOne
    @JoinColumn(name = "inputer_id")
    private User inputer;

    @Column(name = "date_saisie_taux")
    private LocalDateTime dateSaisieTaux;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Column(name = "date_cloture")
    private LocalDateTime dateCloture;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String commentaireValidation;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgence")
    @Builder.Default
    private Urgence urgence = Urgence.NORMALE;

    // Champs de demande agence (TPE Physique)
    private String raisonSociale;
    private String activite;
    private String numeroCompte;
    private String adresse;
    private String codePostal;
    private String codeAgence;
    private String telephone;
    @Column(name = "rne_file_path")
    private String rneFilePath;
    private String emailNotification;
    
    // Champs de validation Monetique (TPE Physique)
    private String mcc;
    private Double tauxCommission;
    private Double tauxCommissionInter;
    private Double loyer;
    private String serieTpe;
    private String numeroTerminal; // généré automatiquement
    @Column(name = "value_date")
    private LocalDateTime valueDate;
    
    // Champs spécifiques E-commerce
    private String localite;
    private String rib;
    private String webmaster;
    private String contactTechnique;
    private String urlSiteMarchand;

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PieceJointe> piecesJointes = new ArrayList<>();

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Commentaire> commentaires = new ArrayList<>();

    @OneToOne(mappedBy = "demande")
    private Affectation affectation;
}
