package com.tpe.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TPEInfo {
    private String nAffiliation;
    private String nCompte;
    private boolean exists;
    private String branch;
    private String profitCentre;
    private String clientId;
}
