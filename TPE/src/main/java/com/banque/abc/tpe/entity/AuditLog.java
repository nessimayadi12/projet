package com.banque.abc.tpe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_action", nullable = false)
    private LocalDateTime dateAction;

    @Column(nullable = false)
    private String username;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "actor_roles", length = 500)
    private String actorRoles;

    @Column(nullable = false)
    private String action; // CREATE, UPDATE, DELETE, LOGIN, LOGOUT, etc.

    @Column(name = "action_label")
    private String actionLabel;

    @Column(name = "module_name")
    private String moduleName;

    @Column(nullable = false)
    private String entityType; // TPE, Commercant, Demande, etc.

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "entity_reference")
    private String entityReference;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;

    @Column(name = "changed_fields", columnDefinition = "TEXT")
    private String changedFields;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "statut")
    private String statut; // SUCCESS, FAILED

    @Column(name = "risk_level")
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "request_method")
    private String requestMethod;

    @Column(name = "request_uri", length = 1000)
    private String requestUri;

    @Column(name = "correlation_id")
    private String correlationId;
}
