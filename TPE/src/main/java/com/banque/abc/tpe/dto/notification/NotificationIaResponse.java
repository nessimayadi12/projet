package com.banque.abc.tpe.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationIaResponse {

    private NotificationIaEventType type;
    private String titre;
    private String message;
    private String priorite;
    private String destinataire;
    private List<String> actionsRecommandees;
    private Map<String, Object> contexte;
    private LocalDateTime generatedAt;
}
