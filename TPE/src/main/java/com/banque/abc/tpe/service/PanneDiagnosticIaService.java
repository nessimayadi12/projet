package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.panne.PanneDiagnosticIaRequest;
import com.banque.abc.tpe.dto.panne.PanneDiagnosticIaResponse;
import com.banque.abc.tpe.dto.panne.PanneDiagnosticIaSource;
import com.banque.abc.tpe.entity.PanneDiagnosticKnowledge;
import com.banque.abc.tpe.exception.BusinessException;
import com.banque.abc.tpe.repository.PanneDiagnosticKnowledgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PanneDiagnosticIaService {

    private final PanneDiagnosticKnowledgeRepository knowledgeRepository;

    public PanneDiagnosticIaResponse analyser(PanneDiagnosticIaRequest request) {
        String description = request != null ? request.getDescription() : null;
        if (description == null || description.isBlank()) {
            throw new BusinessException("La description est obligatoire pour lancer le diagnostic IA");
        }

        List<PanneDiagnosticKnowledge> documents = knowledgeRepository.findByActifTrueOrderByPrioriteDescLastModifiedDateDesc();
        if (documents.isEmpty()) {
            return emptyKnowledgeResponse();
        }

        String normalizedDescription = normalize(description);
        List<RagMatch> matches = documents.stream()
                .map(document -> score(document, normalizedDescription))
                .filter(match -> match.score() > 0)
                .sorted(Comparator.comparingInt(RagMatch::score).reversed())
                .limit(3)
                .toList();

        if (matches.isEmpty()) {
            return noMatchResponse();
        }

        RagMatch bestMatch = matches.get(0);
        PanneDiagnosticKnowledge bestDocument = bestMatch.document();
        List<PanneDiagnosticIaSource> sources = matches.stream()
                .map(this::toSource)
                .toList();

        return PanneDiagnosticIaResponse.builder()
                .typePanneSuggere(bestDocument.getTypePanne())
                .diagnosticPropose(generateDiagnostic(bestDocument, matches))
                .actionCorrectiveProposee(bestDocument.getActionCorrective())
                .urgence(bestDocument.getUrgence())
                .scoreConfiance(confidence(bestMatch.score()))
                .indicesDetectes(bestMatch.indices())
                .recommandations(split(bestDocument.getRecommandations()))
                .remplacementRecommande(Boolean.TRUE.equals(bestDocument.getRemplacementRecommande()))
                .contexteRag(generateContext(matches))
                .sourcesRetenues(sources)
                .build();
    }

    private RagMatch score(PanneDiagnosticKnowledge document, String normalizedDescription) {
        Set<String> indices = new LinkedHashSet<>();
        int score = 0;

        for (String keyword : split(document.getMotsCles())) {
            String normalizedKeyword = normalize(keyword);
            if (!normalizedKeyword.isBlank() && normalizedDescription.contains(normalizedKeyword)) {
                indices.add(keyword);
                score += 10;
            }
        }

        String searchableText = normalize(String.join(" ",
                safe(document.getTitre()),
                document.getTypePanne() != null ? document.getTypePanne().name() : "",
                safe(document.getMotsCles()),
                safe(document.getSymptomes()),
                safe(document.getDiagnostic())
        ));

        for (String token : tokens(normalizedDescription)) {
            if (searchableText.contains(token)) {
                indices.add(token);
                score += 2;
            }
        }

        if (score > 0) {
            int priority = document.getPriorite() != null ? document.getPriorite() : 0;
            score += Math.max(0, priority / 20);
        }

        return new RagMatch(document, score, List.copyOf(indices));
    }

    private PanneDiagnosticIaSource toSource(RagMatch match) {
        PanneDiagnosticKnowledge document = match.document();
        return PanneDiagnosticIaSource.builder()
                .id(document.getId())
                .titre(document.getTitre())
                .typePanne(document.getTypePanne())
                .score(match.score())
                .extrait(extract(document))
                .indices(match.indices())
                .build();
    }

    private String generateDiagnostic(PanneDiagnosticKnowledge bestDocument, List<RagMatch> matches) {
        return safe(bestDocument.getDiagnostic());
    }

    private String generateContext(List<RagMatch> matches) {
        return matches.stream()
                .map(match -> {
                    PanneDiagnosticKnowledge document = match.document();
                    return "- " + safe(document.getTitre())
                            + " | score " + match.score()
                            + " | symptomes: " + safe(document.getSymptomes())
                            + " | diagnostic: " + safe(document.getDiagnostic());
                })
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
    }

    private PanneDiagnosticIaResponse noMatchResponse() {
        return PanneDiagnosticIaResponse.builder()
                .typePanneSuggere(null)
                .diagnosticPropose("Aucun document RAG suffisamment proche n'a ete trouve.")
                .actionCorrectiveProposee("Completer la description avec les symptomes, le message affiche et le comportement du TPE.")
                .urgence("MOYENNE")
                .scoreConfiance(25)
                .indicesDetectes(List.of())
                .recommandations(List.of(
                        "Ajouter le message d'erreur exact",
                        "Preciser si le TPE s'allume",
                        "Indiquer le contexte: transaction, charge, impression ou reseau"
                ))
                .remplacementRecommande(false)
                .contexteRag("")
                .sourcesRetenues(List.of())
                .build();
    }

    private PanneDiagnosticIaResponse emptyKnowledgeResponse() {
        return PanneDiagnosticIaResponse.builder()
                .typePanneSuggere(null)
                .diagnosticPropose("La base de connaissances RAG est vide.")
                .actionCorrectiveProposee("Ajouter des documents de diagnostic avant de lancer l'analyse.")
                .urgence("MOYENNE")
                .scoreConfiance(0)
                .indicesDetectes(List.of())
                .recommandations(List.of("Ajouter au moins une connaissance diagnostic active"))
                .remplacementRecommande(false)
                .contexteRag("")
                .sourcesRetenues(List.of())
                .build();
    }

    private int confidence(int score) {
        return Math.min(95, Math.max(30, 30 + score * 3));
    }

    private String extract(PanneDiagnosticKnowledge document) {
        String text = safe(document.getSymptomes());
        if (text.isBlank()) {
            text = safe(document.getDiagnostic());
        }
        return text.length() > 180 ? text.substring(0, 177) + "..." : text;
    }

    private List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return Arrays.stream(value.split("\\R|;|,"))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private List<String> tokens(String normalizedDescription) {
        return Arrays.stream(normalizedDescription.split(" "))
                .map(String::trim)
                .filter(token -> token.length() >= 4)
                .filter(token -> !stopWords().contains(token))
                .distinct()
                .toList();
    }

    private Set<String> stopWords() {
        return Set.of("avec", "dans", "pour", "plus", "client", "terminal", "probleme", "panne", "tpe");
    }

    private String normalize(String value) {
        String normalized = Normalizer.normalize(safe(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
        return normalized.replaceAll("\\s+", " ");
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    private record RagMatch(PanneDiagnosticKnowledge document, int score, List<String> indices) {
    }
}
