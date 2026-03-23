package com.tpe.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EcritureComptableDTO {
    private String branch;
    private String profitCentre;
    private String clientId;
    private String accountNo;
    private String accountName;
    private String accountType;
    private String ccy;
    private String seqNo;
    private String referenceNo;
    private String rbTranType;
    private String valueDate;
    private String amount;
    private String dc;
    private String narrative;
    private String tranType;
    private String rbGl;
    private String sessionDate;
}
