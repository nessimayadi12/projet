package com.banque.abc.tpe.dto.notification;

public record BusinessNotificationDelivery(
        String username,
        BusinessNotificationResponse notification
) {
}
