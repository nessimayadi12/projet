package com.banque.abc.tpe.dto.notification;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessNotificationResponse {
    private Long id;
    private NotificationIaEventType type;
    private String title;
    private String message;
    private String priority;
    private String sourceUsername;
    private String sourceCodeAgence;
    private String actionUrl;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
