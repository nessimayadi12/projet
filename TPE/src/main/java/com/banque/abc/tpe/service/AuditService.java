package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.audit.AuditEvent;
import com.banque.abc.tpe.dto.audit.AuditFieldChange;
import com.banque.abc.tpe.dto.audit.AuditLogResponse;
import com.banque.abc.tpe.dto.audit.AuditStatsResponse;
import com.banque.abc.tpe.entity.AuditLog;
import com.banque.abc.tpe.repository.AuditLogRepository;
import com.banque.abc.tpe.security.UserPrincipal;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final String SUCCESS = "SUCCESS";
    private static final String FAILED = "FAILED";

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String action, String entityType, String entityId, String details, String statut) {
        saveEvent(AuditEvent.builder()
                .action(action)
                .actionLabel(resolveActionLabel(action))
                .moduleName(entityType)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .statut(statut)
                .riskLevel(resolveRiskLevel(action))
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCreation(String entityType,
                            String entityId,
                            String entityReference,
                            Map<String, Object> newValues,
                            String details) {
        saveEvent(AuditEvent.builder()
                .action("CREATE")
                .actionLabel("Creation")
                .moduleName(entityType)
                .entityType(entityType)
                .entityId(entityId)
                .entityReference(entityReference)
                .details(details)
                .newValues(newValues)
                .statut(SUCCESS)
                .riskLevel("MEDIUM")
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logUpdate(String entityType,
                          String entityId,
                          String entityReference,
                          Map<String, Object> oldValues,
                          Map<String, Object> newValues,
                          String details) {
        saveEvent(AuditEvent.builder()
                .action("UPDATE")
                .actionLabel("Modification")
                .moduleName(entityType)
                .entityType(entityType)
                .entityId(entityId)
                .entityReference(entityReference)
                .details(details)
                .oldValues(oldValues)
                .newValues(newValues)
                .changes(resolveChanges(oldValues, newValues))
                .statut(SUCCESS)
                .riskLevel("HIGH")
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logStatusChange(String entityType,
                                String entityId,
                                String entityReference,
                                Object oldStatus,
                                Object newStatus,
                                String details) {
        Map<String, Object> oldValues = values("statut", oldStatus);
        Map<String, Object> newValues = values("statut", newStatus);
        saveEvent(AuditEvent.builder()
                .action("UPDATE_STATUS")
                .actionLabel("Changement de statut")
                .moduleName(entityType)
                .entityType(entityType)
                .entityId(entityId)
                .entityReference(entityReference)
                .details(details)
                .oldValues(oldValues)
                .newValues(newValues)
                .changes(resolveChanges(oldValues, newValues))
                .statut(SUCCESS)
                .riskLevel("HIGH")
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logValidationDecision(String entityType,
                                      String entityId,
                                      String entityReference,
                                      boolean approved,
                                      Map<String, Object> oldValues,
                                      Map<String, Object> newValues,
                                      String details) {
        saveEvent(AuditEvent.builder()
                .action(approved ? "VALIDATE" : "REJECT")
                .actionLabel(approved ? "Validation" : "Rejet")
                .moduleName(entityType)
                .entityType(entityType)
                .entityId(entityId)
                .entityReference(entityReference)
                .details(details)
                .oldValues(oldValues)
                .newValues(newValues)
                .changes(resolveChanges(oldValues, newValues))
                .statut(SUCCESS)
                .riskLevel("CRITICAL")
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logBusinessEvent(AuditEvent event) {
        saveEvent(event);
    }

    public Map<String, Object> values(Object... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            return Collections.emptyMap();
        }
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("Les valeurs d'audit doivent etre fournies par paires cle/valeur");
        }

        Map<String, Object> values = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            values.put(String.valueOf(keyValues[i]), normalizeValue(keyValues[i + 1]));
        }
        return values;
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> searchLogs(String username,
                                             String action,
                                             String entityType,
                                             String entityId,
                                             String statut,
                                             LocalDateTime dateDebut,
                                             LocalDateTime dateFin,
                                             String keyword,
                                             Pageable pageable) {
        return auditLogRepository.findAll(buildSpecification(
                        username, action, entityType, entityId, statut, dateDebut, dateFin, keyword),
                pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getEntityHistory(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByDateActionDesc(entityType, entityId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AuditStatsResponse getStats() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        long updateCount = auditLogRepository.countByAction("UPDATE")
                + auditLogRepository.countByAction("UPDATE_STATUS");

        return AuditStatsResponse.builder()
                .totalActions(auditLogRepository.count())
                .actionsReussies(auditLogRepository.countByStatut(SUCCESS))
                .actionsEchouees(auditLogRepository.countByStatut(FAILED))
                .creations(auditLogRepository.countByAction("CREATE"))
                .modifications(updateCount)
                .validations(auditLogRepository.countByAction("VALIDATE"))
                .rejets(auditLogRepository.countByAction("REJECT"))
                .affectations(auditLogRepository.countByAction("AFFECT"))
                .actionsAujourdhui(auditLogRepository.countByDateActionBetween(startOfDay, endOfDay))
                .build();
    }

    private void saveEvent(AuditEvent event) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        List<AuditFieldChange> changes = event.getChanges() != null
                ? event.getChanges()
                : resolveChanges(event.getOldValues(), event.getNewValues());

        AuditLog auditLog = AuditLog.builder()
                .dateAction(LocalDateTime.now())
                .username(resolveUsername(authentication))
                .actorUserId(resolveActorUserId(authentication))
                .actorRoles(resolveActorRoles(authentication))
                .action(defaultString(event.getAction(), "ACTION"))
                .actionLabel(defaultString(event.getActionLabel(), resolveActionLabel(event.getAction())))
                .moduleName(defaultString(event.getModuleName(), event.getEntityType()))
                .entityType(defaultString(event.getEntityType(), "System"))
                .entityId(event.getEntityId())
                .entityReference(event.getEntityReference())
                .details(event.getDetails())
                .oldValues(toJson(redact(event.getOldValues())))
                .newValues(toJson(redact(event.getNewValues())))
                .changedFields(toJson(redactChanges(changes)))
                .ipAddress(getClientIP(request))
                .userAgent(getUserAgent(request))
                .statut(defaultString(event.getStatut(), SUCCESS))
                .riskLevel(defaultString(event.getRiskLevel(), resolveRiskLevel(event.getAction())))
                .requestMethod(request != null ? request.getMethod() : "SYSTEM")
                .requestUri(request != null ? request.getRequestURI() : "system")
                .correlationId(resolveCorrelationId(request))
                .build();

        auditLogRepository.save(auditLog);
    }

    private Specification<AuditLog> buildSpecification(String username,
                                                       String action,
                                                       String entityType,
                                                       String entityId,
                                                       String statut,
                                                       LocalDateTime dateDebut,
                                                       LocalDateTime dateFin,
                                                       String keyword) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(username)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("username")),
                        "%" + username.toLowerCase(Locale.ROOT).trim() + "%"
                ));
            }
            if (hasText(action)) {
                predicates.add(criteriaBuilder.equal(root.get("action"), action.trim()));
            }
            if (hasText(entityType)) {
                predicates.add(criteriaBuilder.equal(root.get("entityType"), entityType.trim()));
            }
            if (hasText(entityId)) {
                predicates.add(criteriaBuilder.equal(root.get("entityId"), entityId.trim()));
            }
            if (hasText(statut)) {
                predicates.add(criteriaBuilder.equal(root.get("statut"), statut.trim()));
            }
            if (dateDebut != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateAction"), dateDebut));
            }
            if (dateFin != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateAction"), dateFin));
            }
            if (hasText(keyword)) {
                String pattern = "%" + keyword.toLowerCase(Locale.ROOT).trim() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("details"), "")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("entityReference"), "")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("entityId"), "")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.<String>get("requestUri"), "")), pattern)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private AuditLogResponse mapToResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .dateAction(auditLog.getDateAction())
                .username(auditLog.getUsername())
                .actorUserId(auditLog.getActorUserId())
                .actorRoles(auditLog.getActorRoles())
                .action(auditLog.getAction())
                .actionLabel(auditLog.getActionLabel())
                .moduleName(auditLog.getModuleName())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .entityReference(auditLog.getEntityReference())
                .details(auditLog.getDetails())
                .oldValues(readMap(auditLog.getOldValues()))
                .newValues(readMap(auditLog.getNewValues()))
                .changes(readChanges(auditLog.getChangedFields()))
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .statut(auditLog.getStatut())
                .riskLevel(auditLog.getRiskLevel())
                .requestMethod(auditLog.getRequestMethod())
                .requestUri(auditLog.getRequestUri())
                .correlationId(auditLog.getCorrelationId())
                .build();
    }

    private List<AuditFieldChange> resolveChanges(Map<String, Object> oldValues, Map<String, Object> newValues) {
        Map<String, Object> safeOldValues = oldValues != null ? oldValues : Collections.emptyMap();
        Map<String, Object> safeNewValues = newValues != null ? newValues : Collections.emptyMap();
        Set<String> fields = new LinkedHashSet<>();
        fields.addAll(safeOldValues.keySet());
        fields.addAll(safeNewValues.keySet());

        List<AuditFieldChange> changes = new ArrayList<>();
        for (String field : fields) {
            Object oldValue = safeOldValues.get(field);
            Object newValue = safeNewValues.get(field);
            if (!Objects.equals(oldValue, newValue)) {
                changes.add(AuditFieldChange.builder()
                        .field(field)
                        .oldValue(normalizeValue(oldValue))
                        .newValue(normalizeValue(newValue))
                        .build());
            }
        }
        return changes;
    }

    private Map<String, Object> redact(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return values;
        }

        Map<String, Object> redacted = new LinkedHashMap<>();
        values.forEach((key, value) -> redacted.put(key, isSensitiveKey(key) ? "***" : redactValue(value)));
        return redacted;
    }

    private List<AuditFieldChange> redactChanges(List<AuditFieldChange> changes) {
        if (changes == null) {
            return null;
        }

        return changes.stream()
                .map(change -> AuditFieldChange.builder()
                        .field(change.getField())
                        .oldValue(isSensitiveKey(change.getField()) ? "***" : redactValue(change.getOldValue()))
                        .newValue(isSensitiveKey(change.getField()) ? "***" : redactValue(change.getNewValue()))
                        .build())
                .toList();
    }

    @SuppressWarnings("unchecked")
    private Object redactValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> redacted = new LinkedHashMap<>();
            map.forEach((key, entryValue) -> redacted.put(String.valueOf(key),
                    isSensitiveKey(String.valueOf(key)) ? "***" : redactValue(entryValue)));
            return redacted;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(this::redactValue).toList();
        }
        return normalizeValue(value);
    }

    private Object normalizeValue(Object value) {
        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        return value;
    }

    private boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String normalizedKey = key.toLowerCase(Locale.ROOT).replace("_", "");
        return normalizedKey.contains("password")
                || normalizedKey.contains("token")
                || normalizedKey.contains("secret")
                || normalizedKey.contains("cleapi")
                || normalizedKey.contains("apikey");
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    private Map<String, Object> readMap(String json) {
        if (!hasText(json)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Collections.singletonMap("raw", json);
        }
    }

    private List<AuditFieldChange> readChanges(String json) {
        if (!hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<AuditFieldChange>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String resolveUsername(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymousUser";
        }
        return authentication.getName();
    }

    private Long resolveActorUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        return null;
    }

    private String resolveActorRoles(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return null;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .collect(Collectors.joining(","));
    }

    private String getClientIP(HttpServletRequest request) {
        if (request != null) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (hasText(xForwardedFor)) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (hasText(xRealIp)) {
                return xRealIp.trim();
            }

            return request.getRemoteAddr();
        }
        return "unknown";
    }

    private String getUserAgent(HttpServletRequest request) {
        if (request != null) {
            return request.getHeader("User-Agent");
        }
        return "unknown";
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String requestId = request.getHeader("X-Correlation-ID");
        if (!hasText(requestId)) {
            requestId = request.getHeader("X-Request-ID");
        }
        return hasText(requestId) ? requestId.trim() : null;
    }

    private String resolveActionLabel(String action) {
        if (action == null) {
            return "Action";
        }
        return switch (action) {
            case "CREATE" -> "Creation";
            case "UPDATE" -> "Modification";
            case "UPDATE_STATUS" -> "Changement de statut";
            case "VALIDATE" -> "Validation";
            case "REJECT" -> "Rejet";
            case "AFFECT" -> "Affectation";
            case "DELETE" -> "Suppression";
            case "LOGIN" -> "Connexion";
            case "UPLOAD" -> "Depot de fichier";
            case "DOWNLOAD" -> "Telechargement";
            case "SUBMIT" -> "Soumission";
            case "CLOSE" -> "Cloture";
            default -> action;
        };
    }

    private String resolveRiskLevel(String action) {
        if (action == null) {
            return "LOW";
        }
        return switch (action) {
            case "VALIDATE", "REJECT", "DELETE", "AFFECT", "UPDATE_STATUS" -> "CRITICAL";
            case "UPDATE", "CREATE", "SUBMIT", "CLOSE" -> "HIGH";
            case "LOGIN", "UPLOAD", "DOWNLOAD" -> "MEDIUM";
            default -> "LOW";
        };
    }

    private String defaultString(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
