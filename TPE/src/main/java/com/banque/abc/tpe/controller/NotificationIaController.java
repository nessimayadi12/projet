package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.dto.notification.NotificationIaRequest;
import com.banque.abc.tpe.dto.notification.NotificationIaResponse;
import com.banque.abc.tpe.service.NotificationIaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications-ia")
@RequiredArgsConstructor
public class NotificationIaController {

    private final NotificationIaService notificationIaService;

    @PostMapping("/generer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MONETIQUE', 'AGENCE')")
    public ResponseEntity<NotificationIaResponse> genererNotification(
            @Valid @RequestBody NotificationIaRequest request) {
        return ResponseEntity.ok(notificationIaService.generer(request));
    }
}
