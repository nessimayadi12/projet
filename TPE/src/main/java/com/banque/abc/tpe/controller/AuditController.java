package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.dto.audit.AuditLogResponse;
import com.banque.abc.tpe.dto.audit.AuditStatsResponse;
import com.banque.abc.tpe.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/api/audit", "/api/audit-logs"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<Page<AuditLogResponse>> searchLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 25, sort = "dateAction", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditService.searchLogs(
                username, action, entityType, entityId, statut, dateDebut, dateFin, keyword, pageable));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<AuditStatsResponse> getStats() {
        return ResponseEntity.ok(auditService.getStats());
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<List<AuditLogResponse>> getEntityHistory(@PathVariable String entityType,
                                                                   @PathVariable String entityId) {
        return ResponseEntity.ok(auditService.getEntityHistory(entityType, entityId));
    }
}
