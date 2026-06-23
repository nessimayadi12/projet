package com.banque.abc.tpe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GroqAiService {

    private static final String SQL_SYSTEM_PROMPT_TEMPLATE = """
            Tu es expert MySQL pour une application bancaire de gestion du parc TPE.
            L'utilisateur pose une question en francais naturel. Tu dois comprendre le sens reel
            et generer le SELECT MySQL le plus adapte.

            Utilise exclusivement ces tables et colonnes reelles lues dans la base active :
            %s

            Relations metier utiles :
            - affectations.tpe_id -> tpes.id
            - affectations.commercant_id -> commercants.id
            - affectations.demande_id -> demandes.id
            - pannes.tpe_id -> tpes.id
            - pannes.technicien_id -> users.id
            - pannes.declarant_id -> users.id
            - demandes.commercant_id -> commercants.id
            - demandes.demandeur_id, demandes.valideur_id, demandes.inputer_id -> users.id
            - historique_statuts.tpe_id -> tpes.id

            Statuts TPE possibles : DISPONIBLE, RESERVE, AFFECTE, EN_PANNE, MAINTENANCE, HORS_SERVICE.
            Statuts demande possibles : NOUVELLE, EN_COURS, EN_ATTENTE_COMPLEMENT,
            VALIDEE_MONETIQUE, AFFECTEE, CLOTUREE, REJETEE.
            Statuts panne possibles : DECLAREE, DIAGNOSTIQUEE, EN_REPARATION,
            REPAREE, TESTEE, IRRECUPERABLE.

            Regles obligatoires :
            - Reponds uniquement avec un JSON valide, sans markdown.
            - Utilise uniquement SELECT.
            - Utilise uniquement les noms de colonnes affiches dans le schema reel ci-dessus.
            - Interdit : INSERT, UPDATE, DELETE, DROP, TRUNCATE, ALTER, CREATE, REPLACE.
            - Ajoute un LIMIT 100 pour les listes detaillees.
            - Pour les TPE affectes sur une periode, utilise affectations.date_affectation.
            - Pour les questions sur les techniciens, utilise users via pannes.technicien_id.
            - Pour les commercants sans TPE affecte, utilise commercants et affectations actives.
            - Pour les demandes validees non affectees, statut = VALIDEE_MONETIQUE et aucune affectation active.
            - Si la question est hors perimetre TPE, retourne :
              {"erreur":"Question hors perimetre de l application TPE"}

            Format exact attendu :
            {
              "sql": "SELECT ...",
              "explication": "Ce que je recherche en une phrase",
              "colonnes": ["col1", "col2"]
            }
            """;

    private static final String REFORMULATION_SYSTEM_PROMPT = """
            Tu es un assistant interne bancaire. Tu recois des donnees brutes issues
            du systeme de gestion TPE et tu les reformules en reponse claire,
            professionnelle et lisible en francais pour un agent bancaire non technique.
            - Utilise des listes a puces si plusieurs elements sont presents.
            - Mets en valeur les chiffres importants.
            - Termine par une observation utile si tu en vois une.
            - Ne mentionne jamais le SQL ni la base de donnees.
            - Si 0 resultat : dis-le clairement et propose une alternative.
            """;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiUrl;
    private final String model;

    public GroqAiService(
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper,
            @Value("${groq.api.key:}") String apiKey,
            @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}") String apiUrl,
            @Value("${groq.model:llama-3.3-70b-versatile}") String model) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
    }

    /**
     * Demande a Groq de transformer la question libre en JSON contenant un SELECT MySQL.
     */
    public String genererSQL(String question, String schema) {
        return callGroq(List.of(
                Map.of("role", "system", "content", buildSqlSystemPrompt(schema)),
                Map.of("role", "user", "content", "Question : " + question)
        ), true);
    }

    /**
     * Demande a Groq de corriger un SELECT refuse par MySQL.
     */
    public String corrigerSQL(String question, String schema, String sqlInitial, String erreurExecution) {
        String correctionPrompt = "Question : " + question
                + "\nSQL initial refuse : " + sqlInitial
                + "\nErreur MySQL : " + erreurExecution
                + "\nCorrige le SQL en respectant strictement le schema reel.";

        return callGroq(List.of(
                Map.of("role", "system", "content", buildSqlSystemPrompt(schema)),
                Map.of("role", "user", "content", correctionPrompt)
        ), true);
    }

    /**
     * Demande a Groq de reformuler les donnees en reponse metier claire.
     */
    public String reformulerReponse(String question, int nbResultats, String donneesJson) {
        String userPrompt = "Question posee : " + question
                + "\nNombre de resultats : " + nbResultats
                + "\nDonnees : " + donneesJson;
        return callGroq(List.of(
                Map.of("role", "system", "content", REFORMULATION_SYSTEM_PROMPT),
                Map.of("role", "user", "content", userPrompt)
        ), false);
    }

    /**
     * Execute un appel OpenAI-compatible vers Groq et retourne le contenu du message assistant.
     */
    private String callGroq(List<Map<String, String>> messages, boolean jsonResponse) {
        validateApiKey();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", jsonResponse ? 0 : 0.2);
        if (jsonResponse) {
            body.put("response_format", Map.of("type", "json_object"));
        }

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl,
                    new HttpEntity<>(body, headers),
                    String.class
            );
            return extractAssistantContent(response.getBody());
        } catch (RestClientException ex) {
            log.error("Erreur appel Groq", ex);
            throw new IllegalStateException("Le service IA Groq est indisponible. Reessayez plus tard.", ex);
        }
    }

    private String buildSqlSystemPrompt(String schema) {
        String schemaText = schema == null || schema.isBlank()
                ? "Schema indisponible. Utilise seulement les tables TPE connues."
                : schema;
        return SQL_SYSTEM_PROMPT_TEMPLATE.formatted(schemaText);
    }

    /**
     * Recupere choices[0].message.content dans la reponse Groq.
     */
    private String extractAssistantContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                throw new IllegalStateException("Reponse IA vide ou invalide.");
            }
            return content.asText();
        } catch (Exception ex) {
            log.error("Reponse Groq impossible a parser", ex);
            throw new IllegalStateException("Reponse IA invalide.", ex);
        }
    }

    /**
     * Verifie que la cle Groq est configuree avant tout appel externe.
     */
    private void validateApiKey() {
        if (apiKey == null || apiKey.isBlank() || "VOTRE_CLE_ICI".equals(apiKey)) {
            throw new IllegalStateException("Cle Groq manquante. Configurez groq.api.key dans application.properties.");
        }
    }
}
