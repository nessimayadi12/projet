package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.panne.PanneDiagnosticIaRequest;
import com.banque.abc.tpe.dto.panne.PanneDiagnosticIaResponse;
import com.banque.abc.tpe.entity.PanneDiagnosticKnowledge;
import com.banque.abc.tpe.entity.enums.TypePanne;
import com.banque.abc.tpe.exception.BusinessException;
import com.banque.abc.tpe.repository.PanneDiagnosticKnowledgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Assistant IA diagnostic pannes TPE avec RAG")
class PanneDiagnosticIaServiceTest {

    private PanneDiagnosticKnowledgeRepository repository;
    private PanneDiagnosticIaService service;

    @BeforeEach
    void setUp() {
        repository = mock(PanneDiagnosticKnowledgeRepository.class);
        service = new PanneDiagnosticIaService(repository);
        when(repository.findByActifTrueOrderByPrioriteDescLastModifiedDateDesc())
                .thenReturn(List.of(batteryKnowledge(), printerKnowledge()));
    }

    @Test
    @DisplayName("Detecte une panne batterie depuis la base RAG")
    void detecteProblemeBatterieCharge() {
        PanneDiagnosticIaResponse response = service.analyser(new PanneDiagnosticIaRequest(
                "Le TPE ne s'allume plus, ecran noir, le client dit que la batterie ne charge pas."
        ));

        assertEquals(TypePanne.PROBLEME_BATTERIE_CHARGE, response.getTypePanneSuggere());
        assertEquals("HAUTE", response.getUrgence());
        assertTrue(response.getScoreConfiance() >= 80);
        assertTrue(response.getIndicesDetectes().contains("batterie"));
        assertFalse(response.getSourcesRetenues().isEmpty());
    }

    @Test
    @DisplayName("Retourne une proposition faible quand aucun document RAG ne correspond")
    void retourneFallbackPourDescriptionVague() {
        PanneDiagnosticIaResponse response = service.analyser(new PanneDiagnosticIaRequest("Probleme terminal"));

        assertNull(response.getTypePanneSuggere());
        assertEquals("MOYENNE", response.getUrgence());
        assertTrue(response.getScoreConfiance() <= 25);
        assertTrue(response.getSourcesRetenues().isEmpty());
    }

    @Test
    @DisplayName("Refuse une description vide")
    void refuseDescriptionVide() {
        assertThrows(BusinessException.class, () -> service.analyser(new PanneDiagnosticIaRequest(" ")));
    }

    private PanneDiagnosticKnowledge batteryKnowledge() {
        PanneDiagnosticKnowledge knowledge = PanneDiagnosticKnowledge.builder()
                .titre("TPE ne s'allume plus / batterie")
                .typePanne(TypePanne.PROBLEME_BATTERIE_CHARGE)
                .motsCles("ne s allume\n ecran noir\n batterie\n charge\n chargeur")
                .symptomes("Terminal eteint, ecran noir, batterie faible ou charge impossible.")
                .diagnostic("Batterie dechargee, chargeur defectueux ou circuit de charge a controler.")
                .actionCorrective("Verifier le chargeur et tester la batterie.")
                .urgence("HAUTE")
                .recommandations("Controler la tension batterie\nTester avec un chargeur fonctionnel")
                .remplacementRecommande(false)
                .actif(true)
                .priorite(90)
                .build();
        knowledge.setId(1L);
        return knowledge;
    }

    private PanneDiagnosticKnowledge printerKnowledge() {
        PanneDiagnosticKnowledge knowledge = PanneDiagnosticKnowledge.builder()
                .titre("Imprimante bloquee")
                .typePanne(TypePanne.IMPRIMANTE_BLOQUEE)
                .motsCles("imprimante\npapier\nticket\nbourrage")
                .symptomes("Ticket non imprime, papier bloque ou compartiment imprimante coince.")
                .diagnostic("Blocage imprimante ou probleme de papier.")
                .actionCorrective("Verifier le rouleau et nettoyer le compartiment papier.")
                .urgence("MOYENNE")
                .recommandations("Verifier le sens du papier")
                .remplacementRecommande(false)
                .actif(true)
                .priorite(80)
                .build();
        knowledge.setId(2L);
        return knowledge;
    }
}
