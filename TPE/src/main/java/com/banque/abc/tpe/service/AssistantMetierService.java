package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.assistant.AssistantResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AssistantMetierService {

    private final GroqAiService groqAiService;
    private final SqlExecutorService sqlExecutorService;
    private final AssistantSchemaService assistantSchemaService;
    private final ObjectMapper objectMapper;

    /**
     * Orchestre le dialogue IA : question libre, generation SQL, execution, reformulation.
     */
    public AssistantResponseDTO interroger(String question) {
        try {
            validateQuestion(question);

            String schema = assistantSchemaService.decrireSchema();
            String jsonGroq = groqAiService.genererSQL(question, schema);
            SqlPlan sqlPlan = parseSqlPlan(jsonGroq);
            if (sqlPlan.erreur() != null && !sqlPlan.erreur().isBlank()) {
                return AssistantResponseDTO.builder()
                        .question(question)
                        .erreur(true)
                        .messageErreur(sqlPlan.erreur())
                        .nombreResultats(0)
                        .build();
            }

            ExecutionResult executionResult = executerAvecCorrection(question, schema, sqlPlan);
            List<Map<String, Object>> donnees = executionResult.donnees();
            String donneesJson = objectMapper.writeValueAsString(donnees);
            String reponseIA = groqAiService.reformulerReponse(question, donnees.size(), donneesJson);

            return AssistantResponseDTO.builder()
                    .question(question)
                    .reponseIA(reponseIA)
                    .sqlGenere(executionResult.sqlPlan().sql())
                    .explication(executionResult.sqlPlan().explication())
                    .donnees(donnees)
                    .nombreResultats(donnees.size())
                    .erreur(false)
                    .build();
        } catch (Exception ex) {
            log.error("Erreur assistant IA metier", ex);
            return AssistantResponseDTO.builder()
                    .question(question)
                    .erreur(true)
                    .messageErreur(resolveMessageErreur(ex))
                    .nombreResultats(0)
                    .build();
        }
    }

    private ExecutionResult executerAvecCorrection(String question, String schema, SqlPlan sqlPlan) {
        try {
            return new ExecutionResult(sqlPlan, sqlExecutorService.executer(sqlPlan.sql()));
        } catch (RuntimeException initialError) {
            log.warn("Premiere requete SQL IA refusee, tentative de correction automatique");
            String jsonCorrection = groqAiService.corrigerSQL(
                    question,
                    schema,
                    sqlPlan.sql(),
                    rootCauseMessage(initialError)
            );
            SqlPlan correctedPlan = parseSqlPlan(jsonCorrection);
            if (correctedPlan.erreur() != null && !correctedPlan.erreur().isBlank()) {
                throw new IllegalStateException(correctedPlan.erreur(), initialError);
            }
            try {
                return new ExecutionResult(correctedPlan, sqlExecutorService.executer(correctedPlan.sql()));
            } catch (RuntimeException correctionError) {
                correctionError.addSuppressed(initialError);
                throw correctionError;
            }
        }
    }

    /**
     * Verifie que la question libre n'est pas vide.
     */
    private void validateQuestion(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("La question est obligatoire.");
        }
    }

    /**
     * Parse le JSON retourne par Groq pour extraire le SQL ou le message hors perimetre.
     */
    private SqlPlan parseSqlPlan(String jsonGroq) {
        try {
            JsonNode root = objectMapper.readTree(cleanJson(jsonGroq));
            String erreur = textOrNull(root, "erreur");
            if (erreur != null) {
                return new SqlPlan(null, null, List.of(), erreur);
            }

            String sql = textOrNull(root, "sql");
            if (sql == null || sql.isBlank()) {
                throw new IllegalStateException("L'IA n'a pas genere de requete SQL.");
            }

            String explication = textOrNull(root, "explication");
            List<String> colonnes = root.has("colonnes") && root.get("colonnes").isArray()
                    ? objectMapper.convertValue(
                    root.get("colonnes"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            )
                    : List.of();
            return new SqlPlan(sql, explication, colonnes, null);
        } catch (Exception ex) {
            throw new IllegalStateException("Le JSON SQL genere par l'IA est invalide.", ex);
        }
    }

    /**
     * Nettoie les balises markdown si le modele en ajoute malgre la consigne.
     */
    private String cleanJson(String value) {
        if (value == null) {
            return "";
        }
        String cleaned = value.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?", "").trim();
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
            }
        }
        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            cleaned = cleaned.substring(firstBrace, lastBrace + 1);
        }
        return cleaned;
    }

    /**
     * Lit une propriete texte optionnelle dans le JSON.
     */
    private String textOrNull(JsonNode root, String field) {
        JsonNode node = root.path(field);
        if (node.isMissingNode() || node.isNull() || node.asText().isBlank()) {
            return null;
        }
        return node.asText();
    }

    /**
     * Transforme une exception technique en message clair pour l'agent bancaire.
     */
    private String resolveMessageErreur(Exception ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof SecurityException) {
                return current.getMessage();
            }
            current = current.getCause();
        }
        String message = ex.getMessage() != null ? ex.getMessage() : "Erreur inattendue de l'assistant IA.";
        String rootCause = rootCauseMessage(ex);
        if (!rootCause.equals(message)) {
            return message + " Detail: " + rootCause;
        }
        return message;
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        Throwable last = throwable;
        while (current != null) {
            last = current;
            current = current.getCause();
        }
        return last.getMessage() != null ? last.getMessage() : last.getClass().getSimpleName();
    }

    private record SqlPlan(String sql, String explication, List<String> colonnes, String erreur) {
    }

    private record ExecutionResult(SqlPlan sqlPlan, List<Map<String, Object>> donnees) {
    }
}
