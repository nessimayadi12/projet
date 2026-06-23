package com.banque.abc.tpe.entity;

import com.banque.abc.tpe.entity.enums.TypePanne;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "panne_diagnostic_knowledge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PanneDiagnosticKnowledge extends BaseEntity {

    @Column(nullable = false, length = 160)
    private String titre;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_panne")
    private TypePanne typePanne;

    @Column(name = "mots_cles", columnDefinition = "TEXT")
    private String motsCles;

    @Column(columnDefinition = "TEXT")
    private String symptomes;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String diagnostic;

    @Column(name = "action_corrective", nullable = false, columnDefinition = "TEXT")
    private String actionCorrective;

    @Column(nullable = false, length = 20)
    private String urgence;

    @Column(columnDefinition = "TEXT")
    private String recommandations;

    @Column(name = "remplacement_recommande")
    private Boolean remplacementRecommande = false;

    @Column(nullable = false)
    private Boolean actif = true;

    @Column(nullable = false)
    private Integer priorite = 0;
}
