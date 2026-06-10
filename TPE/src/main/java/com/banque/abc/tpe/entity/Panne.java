package com.banque.abc.tpe.entity;

import com.banque.abc.tpe.entity.enums.StatutPanne;
import com.banque.abc.tpe.entity.enums.TypePanne;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pannes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Panne extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String reference;

    @ManyToOne
    @JoinColumn(name = "tpe_id", nullable = false)
    private TPE tpe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPanne statut = StatutPanne.DECLAREE;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_panne")
    private TypePanne typePanne;

    @Column(name = "date_declaration", nullable = false)
    private LocalDateTime dateDeclaration;

    @Column(name = "date_diagnostic")
    private LocalDateTime dateDiagnostic;

    @Column(name = "date_reparation")
    private LocalDateTime dateReparation;

    @Column(name = "date_resolution")
    private LocalDateTime dateResolution;

    @ManyToOne
    @JoinColumn(name = "declarant_id", nullable = false)
    private User declarant;

    @ManyToOne
    @JoinColumn(name = "technicien_id")
    private User technicien;

    @Column(columnDefinition = "TEXT")
    private String diagnostic;

    @Column(columnDefinition = "TEXT")
    private String actionCorrective;

    @Column(columnDefinition = "TEXT")
    private String commentaireTechnicien;

    @ManyToOne
    @JoinColumn(name = "tpe_remplacement_id")
    private TPE tpeRemplacement;

    @OneToMany(mappedBy = "panne", cascade = CascadeType.ALL)
    private List<PieceDetachee> piecesUtilisees = new ArrayList<>();

    @Column(name = "cout_reparation")
    private Double coutReparation;

    @Column(name = "sous_garantie")
    private Boolean sousGarantie = false;
}
