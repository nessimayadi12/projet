package com.banque.abc.tpe.dto.notification;

import java.util.List;

public record BusinessNotificationEvent(
        List<BusinessNotificationDelivery> deliveries
) {
}
