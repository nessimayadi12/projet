package com.banque.abc.tpe.dto.assistant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantResponseDTO {

    private String question;
    private String reponseIA;
    private String sqlGenere;
    private String explication;

    @Builder.Default
    private List<Map<String, Object>> donnees = List.of();

    private int nombreResultats;
    private boolean erreur;
    private String messageErreur;
}
