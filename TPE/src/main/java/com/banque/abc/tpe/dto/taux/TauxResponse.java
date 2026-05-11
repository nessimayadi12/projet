package com.banque.abc.tpe.dto.taux;

import com.banque.abc.tpe.entity.enums.StatutTaux;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TauxResponse {
    
    private Long id;
    private Long commercantId;
    private String commercantNom;
    private Double ancienTauxCommission;
    private Double nouveauTauxCommission;
    private Double ancienTauxCommissionInter;
    private Double nouveauTauxCommissionInter;
    private StatutTaux statut;
    private Long inputerId;
    private String inputerNom;
    private Long authorizerId;
    private String authorizerNom;
    private LocalDateTime dateSaisie;
    private LocalDateTime dateValidation;
    private String motifRejet;
    private String commentaire;
    private LocalDateTime dateApplication;
    private Boolean actif;
}
