package com.banque.abc.tpe.dto.tpe;

import com.banque.abc.tpe.entity.enums.StatutTPE;
import com.banque.abc.tpe.entity.enums.TypeTPE;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TPEResponse {
    
    private Long id;
    private TypeTPE typeTPE;
    private String numeroSerie;
    private String numeroTerminal;
    private StatutTPE statut;
    private String marque;
    private String modele;
    private LocalDate dateAcquisition;
    private LocalDate dateMiseEnService;
    private String mcc;
    private String numeroAffiliation;
    private Long commercantId;
    private String commercantNom;
    private String commentaire;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
