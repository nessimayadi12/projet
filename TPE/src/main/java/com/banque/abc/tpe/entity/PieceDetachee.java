package com.banque.abc.tpe.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pieces_detachees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PieceDetachee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "panne_id", nullable = false)
    private Panne panne;

    @Column(nullable = false)
    private String designation;

    private String reference;

    @Column(nullable = false)
    private Integer quantite = 1;

    @Column(name = "prix_unitaire")
    private Double prixUnitaire;

    @Column(name = "prix_total")
    private Double prixTotal;
}
