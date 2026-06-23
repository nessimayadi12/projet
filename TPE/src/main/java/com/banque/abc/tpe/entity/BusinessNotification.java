package com.banque.abc.tpe.entity;

import com.banque.abc.tpe.dto.notification.NotificationIaEventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "business_notifications", indexes = {
        @Index(name = "idx_notification_recipient_created", columnList = "recipient_id, created_date"),
        @Index(name = "idx_notification_recipient_read", columnList = "recipient_id, is_read")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessNotification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 80)
    private NotificationIaEventType type;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, length = 20)
    private String priority;

    @Column(name = "source_username", length = 100)
    private String sourceUsername;

    @Column(name = "source_code_agence", length = 50)
    private String sourceCodeAgence;

    @Column(name = "action_url", length = 255)
    private String actionUrl;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean read = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}
