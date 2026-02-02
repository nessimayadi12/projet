package com.banque.abc.tpe.entity;

import com.banque.abc.tpe.entity.enums.StatutTaux;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "taux")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Taux extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "commercant_id", nullable = false)
    private Commercant commercant;

    @Column(name = "ancien_taux_commission")
    private Double ancienTauxCommission;

    @Column(name = "nouveau_taux_commission", nullable = false)
    private Double nouveauTauxCommission;

    @Column(name = "ancien_taux_commission_inter")
    private Double ancienTauxCommissionInter;

    @Column(name = "nouveau_taux_commission_inter", nullable = false)
    private Double nouveauTauxCommissionInter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTaux statut = StatutTaux.BROUILLON;

    @ManyToOne
    @JoinColumn(name = "inputer_id", nullable = false)
    private User inputer;

    @ManyToOne
    @JoinColumn(name = "authorizer_id")
    private User authorizer;

    @Column(name = "date_saisie", nullable = false)
    private LocalDateTime dateSaisie;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Column(columnDefinition = "TEXT")
    private String motifRejet;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "date_application")
    private LocalDateTime dateApplication;

    @Column(name = "actif")
    private Boolean actif = false;
}
