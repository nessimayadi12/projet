package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.notification.BusinessNotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BusinessNotificationEventListener {

    private final BusinessNotificationDeliveryService deliveryService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void onNotificationCreated(BusinessNotificationEvent event) {
        deliveryService.deliver(event);
    }
}
