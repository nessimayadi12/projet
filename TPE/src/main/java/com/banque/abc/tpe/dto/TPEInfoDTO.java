package com.banque.abc.tpe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TPEInfoDTO {
    private String nAffiliation;
    private String nCompte;
    private boolean exists;
    private String branch;
    private String profitCentre;
    private String clientId;
    private String raisonSociale;
}
