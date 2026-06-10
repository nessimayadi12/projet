package com.banque.abc.tpe.entity;

import com.banque.abc.tpe.entity.enums.StatutTPE;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tpes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TPE extends BaseEntity {

    @Column(nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private String typeTPE;

    @Column(nullable = false, unique = true)
    private String numeroSerie;

    @Column(unique = true)
    private String numeroTerminal; // TID - sera généré automatiquement

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTPE statut = StatutTPE.DISPONIBLE;

    private String marque;

    private String modele;

    @Column(name = "date_acquisition")
    private LocalDate dateAcquisition;

    @Column(name = "date_mise_en_service")
    private LocalDate dateMiseEnService;

    // Données monétiques
    private String mcc; // Merchant Category Code

    @Column(name = "numero_affiliation")
    private String numeroAffiliation;

    @Column(name = "cle_api")
    private String cleApi;

    @ManyToOne
    @JoinColumn(name = "commercant_id")
    private Commercant commercant;

    @OneToMany(mappedBy = "tpe", cascade = CascadeType.ALL)
    private List<Affectation> affectations = new ArrayList<>();

    @OneToMany(mappedBy = "tpe", cascade = CascadeType.ALL)
    private List<Panne> pannes = new ArrayList<>();

    @OneToMany(mappedBy = "tpe", cascade = CascadeType.ALL)
    private List<HistoriqueStatut> historiqueStatuts = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String commentaire;
}
