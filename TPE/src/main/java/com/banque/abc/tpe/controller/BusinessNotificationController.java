package com.banque.abc.tpe.controller;

import com.banque.abc.tpe.dto.notification.BusinessNotificationResponse;
import com.banque.abc.tpe.dto.notification.UnreadNotificationCountResponse;
import com.banque.abc.tpe.service.BusinessNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class BusinessNotificationController {

    private final BusinessNotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<BusinessNotificationResponse>> list() {
        return ResponseEntity.ok(notificationService.findCurrentUserNotifications());
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadNotificationCountResponse> unreadCount() {
        return ResponseEntity.ok(
                new UnreadNotificationCountResponse(
                        notificationService.countUnreadForCurrentUser()
                )
        );
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<BusinessNotificationResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }
}
