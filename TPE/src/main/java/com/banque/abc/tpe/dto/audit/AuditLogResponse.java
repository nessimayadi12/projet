package com.banque.abc.tpe.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private Long id;
    private LocalDateTime dateAction;
    private String username;
    private Long actorUserId;
    private String actorRoles;
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
    private String ipAddress;
    private String userAgent;
    private String statut;
    private String riskLevel;
    private String requestMethod;
    private String requestUri;
    private String correlationId;
}
