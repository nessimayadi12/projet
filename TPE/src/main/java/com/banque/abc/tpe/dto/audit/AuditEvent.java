package com.banque.abc.tpe.dto.audit;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AuditEvent {

    private String action;
    private String actionLabel;
    private String moduleName;
    private String entityType;
    private String entityId;
    private String entityReference;
    private String details;
    private Map<String, Object> oldValues;
    private Map<String, Object> newValues;
    private List<AuditFieldChange> changes;
    private String statut;
    private String riskLevel;
}
