package com.banque.abc.tpe.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pieces_jointes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PieceJointe extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "demande_id", nullable = false)
    private Demande demande;

    @Column(nullable = false)
    private String nomFichier;

    @Column(nullable = false)
    private String cheminFichier;

    @Column(nullable = false)
    private String typeMime;

    private Long tailleFichier;

    @Column(columnDefinition = "TEXT")
    private String description;
}
