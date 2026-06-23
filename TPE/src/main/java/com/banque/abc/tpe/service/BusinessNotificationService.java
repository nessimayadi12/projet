package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.notification.*;
import com.banque.abc.tpe.entity.BusinessNotification;
import com.banque.abc.tpe.entity.User;
import com.banque.abc.tpe.entity.enums.RoleType;
import com.banque.abc.tpe.exception.BusinessException;
import com.banque.abc.tpe.repository.BusinessNotificationRepository;
import com.banque.abc.tpe.repository.UserRepository;
import com.banque.abc.tpe.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessNotificationService {

    private static final Set<NotificationIaEventType> MONETIQUE_EVENTS = EnumSet.of(
            NotificationIaEventType.DEMANDE_TPE_CREEE,
            NotificationIaEventType.PANNE_TPE_DECLAREE
    );

    private final NotificationIaService notificationIaService;
    private final ApplicationEventPublisher eventPublisher;
    private final BusinessNotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Prépare la notification dans la transaction métier courante. Sa livraison
     * sera déclenchée uniquement après le commit par BusinessNotificationEventListener.
     */
    public String publish(NotificationIaEventType type, Map<String, Object> context) {
        NotificationIaResponse draft = notificationIaService.generer(type, context);
        Source source = currentSource();
        String targetCodeAgence = contextValue(draft.getContexte(), "codeAgence");
        List<User> recipients = resolveRecipients(type, targetCodeAgence);
        List<BusinessNotificationDelivery> deliveries = new ArrayList<>();

        if (recipients.isEmpty()) {
            log.warn("Aucun destinataire actif pour l'événement {} (agence={})",
                    type, targetCodeAgence);
        }

        for (User recipient : recipients) {
            if (source.userId() != null && source.userId().equals(recipient.getId())) {
                continue;
            }

            BusinessNotification saved = notificationRepository.save(
                    BusinessNotification.builder()
                            .recipient(recipient)
                            .type(draft.getType())
                            .title(draft.getTitre())
                            .message(draft.getMessage())
                            .priority(draft.getPriorite())
                            .sourceUsername(source.username())
                            .sourceCodeAgence(source.codeAgence())
                            .actionUrl(actionUrl(type))
                            .read(false)
                            .build()
            );
            deliveries.add(new BusinessNotificationDelivery(
                    recipient.getUsername(),
                    toResponse(saved)
            ));
        }

        if (!deliveries.isEmpty()) {
            eventPublisher.publishEvent(new BusinessNotificationEvent(List.copyOf(deliveries)));
        }

        return draft.getMessage();
    }

    @Transactional(readOnly = true)
    public List<BusinessNotificationResponse> findCurrentUserNotifications() {
        Long userId = currentUserId();
        return notificationRepository.findTop50ByRecipientIdOrderByCreatedDateDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnreadForCurrentUser() {
        return notificationRepository.countByRecipientIdAndReadFalse(currentUserId());
    }

    @Transactional
    public BusinessNotificationResponse markAsRead(Long notificationId) {
        Long userId = currentUserId();
        BusinessNotification notification = notificationRepository
                .findByIdAndRecipientId(notificationId, userId)
                .orElseThrow(() -> new BusinessException("Notification introuvable"));

        if (!Boolean.TRUE.equals(notification.getRead())) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    @Transactional
    public int markAllAsRead() {
        return notificationRepository.markAllAsRead(currentUserId(), LocalDateTime.now());
    }

    private Source currentSource() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return new Source(null, "system", null);
        }

        User user = userRepository.findById(principal.getId()).orElse(null);
        return new Source(
                principal.getId(),
                principal.getUsername(),
                user != null ? user.getCodeAgence() : null
        );
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException("Utilisateur non authentifié");
        }
        return principal.getId();
    }

    private BusinessNotificationResponse toResponse(BusinessNotification notification) {
        return BusinessNotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .priority(notification.getPriority())
                .sourceUsername(notification.getSourceUsername())
                .sourceCodeAgence(notification.getSourceCodeAgence())
                .actionUrl(notification.getActionUrl())
                .read(Boolean.TRUE.equals(notification.getRead()))
                .createdAt(notification.getCreatedDate())
                .readAt(notification.getReadAt())
                .build();
    }

    private String contextValue(Map<String, Object> context, String key) {
        if (context == null || context.get(key) == null) {
            return null;
        }
        String value = String.valueOf(context.get(key)).trim();
        return value.isEmpty() ? null : value;
    }

    private List<User> resolveRecipients(NotificationIaEventType type, String codeAgence) {
        if (MONETIQUE_EVENTS.contains(type)) {
            return userRepository.findActiveUsersByRoles(
                    EnumSet.of(RoleType.ROLE_MONETIQUE, RoleType.ROLE_ADMIN)
            );
        }
        if (codeAgence == null || codeAgence.isBlank()) {
            return List.of();
        }
        return userRepository.findActiveUsersByRoleAndCodeAgence(
                RoleType.ROLE_AGENCE,
                codeAgence
        );
    }

    private String actionUrl(NotificationIaEventType type) {
        return switch (type) {
            case DEMANDE_TPE_CREEE, DEMANDE_TPE_VALIDEE, DEMANDE_TPE_REFUSEE,
                 DEMANDE_ATTENTE_COMPLEMENT_INFORMATION, TPE_AFFECTE_COMMERCANT -> "/demandes";
            case PANNE_TPE_DECLAREE, PANNE_TPE_DIAGNOSTIQUEE, TPE_REPARE, TPE_REMPLACE -> "/pannes";
        };
    }

    private record Source(Long userId, String username, String codeAgence) {
    }
}
