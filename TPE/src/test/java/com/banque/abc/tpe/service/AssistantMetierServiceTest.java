package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.assistant.AssistantResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Assistant IA metier dynamique")
class AssistantMetierServiceTest {

    private GroqAiService groqAiService;
    private SqlExecutorService sqlExecutorService;
    private AssistantSchemaService assistantSchemaService;
    private AssistantMetierService service;
    private final String schema = "tpes(id, numero_serie, statut)\naffectations(id, tpe_id, date_affectation)";

    @BeforeEach
    void setUp() {
        groqAiService = mock(GroqAiService.class);
        sqlExecutorService = mock(SqlExecutorService.class);
        assistantSchemaService = mock(AssistantSchemaService.class);
        when(assistantSchemaService.decrireSchema()).thenReturn(schema);
        service = new AssistantMetierService(groqAiService, sqlExecutorService, assistantSchemaService, new ObjectMapper());
    }

    @Test
    @DisplayName("Execute le SELECT genere par Groq et retourne la reformulation")
    void executeSelectGenereParGroq() {
        String question = "Quels TPE sont en panne depuis plus de 7 jours ?";
        String sql = "SELECT numero_serie, statut FROM tpes WHERE statut = 'EN_PANNE'";
        List<Map<String, Object>> rows = List.of(Map.of("numero_serie", "SN-1", "statut", "EN_PANNE"));

        when(groqAiService.genererSQL(question, schema)).thenReturn("""
                {"sql":"SELECT numero_serie, statut FROM tpes WHERE statut = 'EN_PANNE'",
                 "explication":"Recherche des TPE en panne",
                 "colonnes":["numero_serie","statut"]}
                """);
        when(sqlExecutorService.executer(sql)).thenReturn(rows);
        when(groqAiService.reformulerReponse(eq(question), eq(1), anyString()))
                .thenReturn("Un TPE est actuellement en panne : SN-1.");

        AssistantResponseDTO response = service.interroger(question);

        assertFalse(response.isErreur());
        assertEquals(sql, response.getSqlGenere());
        assertEquals(1, response.getNombreResultats());
        assertTrue(response.getReponseIA().contains("SN-1"));
    }

    @Test
    @DisplayName("Retourne directement le hors perimetre detecte par Groq")
    void retourneHorsPerimetre() {
        String question = "Quel est le cours du dollar ?";
        when(groqAiService.genererSQL(question, schema))
                .thenReturn("{\"erreur\":\"Question hors perimetre de l application TPE\"}");

        AssistantResponseDTO response = service.interroger(question);

        assertTrue(response.isErreur());
        assertEquals("Question hors perimetre de l application TPE", response.getMessageErreur());
        assertEquals(0, response.getNombreResultats());
    }

    @Test
    @DisplayName("Corrige automatiquement le SQL si la premiere requete echoue")
    void corrigeSqlApresErreurExecution() {
        String question = "Quels TPE ont ete affectes ces 7 derniers jours ?";
        String sqlInitial = "SELECT type_tpe FROM tpes";
        String sqlCorrige = "SELECT numero_serie FROM tpes";
        List<Map<String, Object>> rows = List.of(Map.of("numero_serie", "SN-2"));

        when(groqAiService.genererSQL(question, schema)).thenReturn("""
                {"sql":"SELECT type_tpe FROM tpes",
                 "explication":"Recherche initiale",
                 "colonnes":["type_tpe"]}
                """);
        when(sqlExecutorService.executer(sqlInitial))
                .thenThrow(new IllegalStateException("Unknown column 'type_tpe'"));
        when(groqAiService.corrigerSQL(eq(question), eq(schema), eq(sqlInitial), anyString())).thenReturn("""
                {"sql":"SELECT numero_serie FROM tpes",
                 "explication":"Recherche corrigee",
                 "colonnes":["numero_serie"]}
                """);
        when(sqlExecutorService.executer(sqlCorrige)).thenReturn(rows);
        when(groqAiService.reformulerReponse(eq(question), eq(1), anyString()))
                .thenReturn("Un TPE trouve : SN-2.");

        AssistantResponseDTO response = service.interroger(question);

        assertFalse(response.isErreur());
        assertEquals(sqlCorrige, response.getSqlGenere());
        assertTrue(response.getReponseIA().contains("SN-2"));
    }
}
