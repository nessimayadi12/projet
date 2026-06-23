package com.banque.abc.tpe.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditStatsResponse {

    private long totalActions;
    private long actionsReussies;
    private long actionsEchouees;
    private long creations;
    private long modifications;
    private long validations;
    private long rejets;
    private long affectations;
    private long actionsAujourdhui;
}
