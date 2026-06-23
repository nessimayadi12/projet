package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.notification.BusinessNotificationDelivery;
import com.banque.abc.tpe.dto.notification.BusinessNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessNotificationDeliveryService {

    private final SimpMessagingTemplate messagingTemplate;

    public void deliver(BusinessNotificationEvent event) {
        for (BusinessNotificationDelivery delivery : event.deliveries()) {
            try {
                messagingTemplate.convertAndSendToUser(
                        delivery.username(),
                        "/queue/notifications",
                        delivery.notification()
                );
            } catch (RuntimeException exception) {
                log.warn("Notification {} conservée en base mais non livrée en temps réel",
                        delivery.notification().getId(), exception);
            }
        }
    }
}
