package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.dto.ScreenDTO;
import com.banque.abc.tpe.dto.ScreenPermissionsDTO;
import com.banque.abc.tpe.dto.ScreenRoleDTO;
import com.banque.abc.tpe.dto.UserScreensDTO;
import com.banque.abc.tpe.dto.audit.AuditLogResponse;
import com.banque.abc.tpe.service.AuditService;
import com.banque.abc.tpe.service.ScreenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/screens")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ScreenController {

    private final ScreenService screenService;
    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<List<ScreenDTO>> getAllScreens() {
        List<ScreenDTO> screens = screenService.getAllScreens();
        return ResponseEntity.ok(screens);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ScreenDTO>> getActiveScreens() {
        List<ScreenDTO> screens = screenService.getActiveScreens();
        return ResponseEntity.ok(screens);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<ScreenDTO> getScreenById(@PathVariable Long id) {
        ScreenDTO screen = screenService.getScreenById(id);
        return ResponseEntity.ok(screen);
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<ScreenDTO> getScreenByCode(@PathVariable String code) {
        ScreenDTO screen = screenService.getScreenByCode(code);
        return ResponseEntity.ok(screen);
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<UserScreensDTO> getScreensForUser(@PathVariable String username) {
        UserScreensDTO userScreens = screenService.getScreensForUser(username);
        return ResponseEntity.ok(userScreens);
    }

    @GetMapping("/me")
    public ResponseEntity<UserScreensDTO> getMyScreens(Authentication authentication) {
        String username = authentication.getName();
        UserScreensDTO userScreens = screenService.getScreensForUser(username);
        return ResponseEntity.ok(userScreens);
    }

    @GetMapping("/permissions/{screenCode}")
    public ResponseEntity<ScreenPermissionsDTO> getMyPermissions(
            @PathVariable String screenCode,
            Authentication authentication) {
        String username = authentication.getName();
        ScreenPermissionsDTO permissions = screenService.getPermissionsForUserOnScreen(username, screenCode);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/matrix")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ScreenRoleDTO>> getPermissionMatrix() {
        return ResponseEntity.ok(screenService.getPermissionMatrix());
    }

    @PostMapping("/roles/copy")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ScreenRoleDTO>> copyRoleProfile(@RequestBody Map<String, Long> request) {
        Long sourceRoleId = request.get("sourceRoleId");
        Long targetRoleId = request.get("targetRoleId");
        if (sourceRoleId == null || targetRoleId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(screenService.copyRoleProfile(sourceRoleId, targetRoleId));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> getPermissionHistory(Pageable pageable) {
        return ResponseEntity.ok(auditService.searchLogs(
                null, null, "ScreenRole", null, null, null, null, null, pageable));
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScreenDTO> createScreen(@Valid @RequestBody ScreenDTO screenDTO) {
        ScreenDTO created = screenService.createScreen(screenDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScreenDTO> updateScreen(
            @PathVariable Long id,
            @Valid @RequestBody ScreenDTO screenDTO) {
        ScreenDTO updated = screenService.updateScreen(id, screenDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteScreen(@PathVariable Long id) {
        screenService.deleteScreen(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{screenId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScreenRoleDTO> assignRoleToScreen(
            @PathVariable Long screenId,
            @PathVariable Long roleId,
            @RequestBody ScreenPermissionsDTO permissions) {
        ScreenRoleDTO screenRole = screenService.assignRoleToScreen(screenId, roleId, permissions);
        return ResponseEntity.status(HttpStatus.CREATED).body(screenRole);
    }

    @DeleteMapping("/{screenId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeRoleFromScreen(
            @PathVariable Long screenId,
            @PathVariable Long roleId) {
        screenService.removeRoleFromScreen(screenId, roleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{screenId}/roles")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE')")
    public ResponseEntity<List<ScreenRoleDTO>> getScreenRoles(@PathVariable Long screenId) {
        List<ScreenRoleDTO> screenRoles = screenService.getScreenRoles(screenId);
        return ResponseEntity.ok(screenRoles);
    }
}
