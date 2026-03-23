package com.tpe.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PorteurInfo {
    private String ncarte;
    private String compte;
    private String devise;
    private String ccyId;
    private double ccyRate;
    private int deciPlaces;
    private boolean exists;
    private String branch;
    private String profitCentre;
    private String clientId;
}
