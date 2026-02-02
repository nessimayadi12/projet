package com.banque.abc.tpe.entity;

import com.banque.abc.tpe.entity.enums.StatutTPE;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historique_statuts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueStatut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tpe_id", nullable = false)
    private TPE tpe;

    @Enumerated(EnumType.STRING)
    @Column(name = "ancien_statut")
    private StatutTPE ancienStatut;

    @Enumerated(EnumType.STRING)
    @Column(name = "nouveau_statut", nullable = false)
    private StatutTPE nouveauStatut;

    @Column(name = "date_changement", nullable = false)
    private LocalDateTime dateChangement;

    @Column(name = "change_par")
    private String changePar;

    @Column(columnDefinition = "TEXT")
    private String commentaire;
}
