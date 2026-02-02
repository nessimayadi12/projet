package com.banque.abc.tpe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "affectations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Affectation extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "tpe_id", nullable = false)
    private TPE tpe;

    @ManyToOne
    @JoinColumn(name = "commercant_id", nullable = false)
    private Commercant commercant;

    @OneToOne
    @JoinColumn(name = "demande_id")
    private Demande demande;

    @Column(name = "date_affectation", nullable = false)
    private LocalDate dateAffectation;

    @Column(name = "date_mise_en_service")
    private LocalDate dateMiseEnService;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "actif")
    private Boolean actif = true;

    @Column(name = "bon_livraison_path")
    private String bonLivraisonPath;

    @Column(name = "contrat_path")
    private String contratPath;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @ManyToOne
    @JoinColumn(name = "affecte_par_id")
    private User affectePar;
}
