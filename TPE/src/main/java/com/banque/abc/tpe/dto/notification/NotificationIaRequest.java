package com.banque.abc.tpe.dto.notification;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationIaRequest {

    @NotNull(message = "Le type d'evenement est obligatoire")
    private NotificationIaEventType type;

    private String destinataire;
    private Map<String, Object> contexte;
}
