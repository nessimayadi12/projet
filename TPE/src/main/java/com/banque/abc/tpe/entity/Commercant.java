package com.banque.abc.tpe.entity;

import com.banque.abc.tpe.entity.enums.StatutCommercant;
import com.banque.abc.tpe.entity.enums.TypeTPE;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commercants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commercant extends BaseEntity {

    @Column(nullable = false)
    private String raisonSociale;

    @Column(nullable = false)
    private String activite;

    @Column(nullable = false)
    private String numeroCompte;

    private String adresse;

    private String localite;

    private String codePostal;

    @Column(nullable = false)
    private String codeAgence;

    private String telephone;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCommercant statut = StatutCommercant.ACTIF;

    // Spécifique TPE
    private Double loyer;

    @Column(name = "rne_file_path")
    private String rneFilePath;

    // Spécifique Mobile
    @Enumerated(EnumType.STRING)
    @Column(name = "type_commerce", length = 50, columnDefinition = "VARCHAR(50)")
    private TypeTPE typeCommerce;

    private String urlSiteMarchand;

    private String webhookUrl;

    private String webmaster;

    private String contactTechnique;

    @Column(name = "type_cartes_acceptees")
    private String typeCartesAcceptees;

    @Column(name = "mode_test")
    private Boolean modeTest = false;

    @OneToMany(mappedBy = "commercant", cascade = CascadeType.ALL)
    private List<TPE> tpes = new ArrayList<>();

    @OneToMany(mappedBy = "commercant", cascade = CascadeType.ALL)
    private List<Demande> demandes = new ArrayList<>();

    @OneToMany(mappedBy = "commercant", cascade = CascadeType.ALL)
    private List<Taux> taux = new ArrayList<>();

    @PrePersist
    @PreUpdate
    private void normalizeTypeCommerce() {
        if (typeCommerce != null) {
            typeCommerce = typeCommerce.canonical();
        }
    }
}
