package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.panne.PanneDiagnosticKnowledgeRequest;
import com.banque.abc.tpe.dto.panne.PanneDiagnosticKnowledgeResponse;
import com.banque.abc.tpe.entity.Panne;
import com.banque.abc.tpe.entity.PanneDiagnosticKnowledge;
import com.banque.abc.tpe.entity.enums.TypePanne;
import com.banque.abc.tpe.exception.BusinessException;
import com.banque.abc.tpe.exception.ResourceNotFoundException;
import com.banque.abc.tpe.repository.PanneDiagnosticKnowledgeRepository;
import com.banque.abc.tpe.repository.PanneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PanneDiagnosticKnowledgeService {

    private final PanneDiagnosticKnowledgeRepository repository;
    private final PanneRepository panneRepository;

    public List<PanneDiagnosticKnowledgeResponse> getAll() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PanneDiagnosticKnowledgeResponse create(PanneDiagnosticKnowledgeRequest request) {
        PanneDiagnosticKnowledge knowledge = new PanneDiagnosticKnowledge();
        applyRequest(knowledge, request);
        return mapToResponse(repository.save(knowledge));
    }

    public PanneDiagnosticKnowledgeResponse update(Long id, PanneDiagnosticKnowledgeRequest request) {
        PanneDiagnosticKnowledge knowledge = getOrThrow(id);
        applyRequest(knowledge, request);
        return mapToResponse(repository.save(knowledge));
    }

    public PanneDiagnosticKnowledgeResponse desactivate(Long id) {
        PanneDiagnosticKnowledge knowledge = getOrThrow(id);
        knowledge.setActif(false);
        return mapToResponse(repository.save(knowledge));
    }

    public List<PanneDiagnosticKnowledgeResponse> generateFromHistoriquePannes() {
        List<Panne> pannes = panneRepository.findAll().stream()
                .filter(this::canFeedRag)
                .toList();

        if (pannes.isEmpty()) {
            throw new BusinessException("Aucune panne historique exploitable pour generer la base RAG");
        }

        Map<TypePanne, List<Panne>> groupedByType = pannes.stream()
                .filter(panne -> panne.getTypePanne() != null)
                .collect(Collectors.groupingBy(
                        Panne::getTypePanne,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        if (groupedByType.isEmpty()) {
            throw new BusinessException("Les pannes historiques doivent avoir un type pour generer la base RAG");
        }

        return groupedByType.entrySet().stream()
                .map(entry -> saveGeneratedKnowledge(entry.getKey(), entry.getValue()))
                .map(this::mapToResponse)
                .toList();
    }

    private PanneDiagnosticKnowledge saveGeneratedKnowledge(TypePanne typePanne, List<Panne> pannes) {
        String titre = "Historique " + typePanne.name();
        PanneDiagnosticKnowledge knowledge = repository.findByTitre(titre)
                .orElseGet(PanneDiagnosticKnowledge::new);

        knowledge.setTitre(titre);
        knowledge.setTypePanne(typePanne);
        knowledge.setMotsCles(extractKeywords(pannes));
        knowledge.setSymptomes(extractSamples(pannes, Panne::getDescription));
        knowledge.setDiagnostic(firstNonBlank(pannes, Panne::getDiagnostic,
                "Diagnostic genere depuis l'historique des pannes " + typePanne.name()));
        knowledge.setActionCorrective(firstNonBlank(pannes, Panne::getActionCorrective,
                "Verifier les symptomes similaires dans l'historique et appliquer la procedure adaptee."));
        knowledge.setUrgence(inferUrgence(typePanne));
        knowledge.setRecommandations(generateRecommendations(pannes));
        knowledge.setRemplacementRecommande(shouldRecommendReplacement(typePanne, pannes));
        knowledge.setActif(true);
        knowledge.setPriorite(Math.min(100, 50 + pannes.size() * 5));

        return repository.save(knowledge);
    }

    private boolean canFeedRag(Panne panne) {
        return panne != null
                && (hasText(panne.getDescription())
                || hasText(panne.getDiagnostic())
                || hasText(panne.getActionCorrective()));
    }

    private String extractKeywords(List<Panne> pannes) {
        return pannes.stream()
                .flatMap(panne -> tokens(String.join(" ",
                        clean(panne.getDescription()),
                        clean(panne.getDiagnostic()),
                        clean(panne.getActionCorrective())
                )).stream())
                .filter(token -> !stopWords().contains(token))
                .collect(Collectors.groupingBy(token -> token, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(20)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining("\n"));
    }

    private String extractSamples(List<Panne> pannes, java.util.function.Function<Panne, String> extractor) {
        return pannes.stream()
                .map(extractor)
                .filter(this::hasText)
                .map(String::trim)
                .distinct()
                .limit(8)
                .collect(Collectors.joining("\n"));
    }

    private String generateRecommendations(List<Panne> pannes) {
        String correctiveSamples = extractSamples(pannes, Panne::getActionCorrective);
        if (correctiveSamples.isBlank()) {
            return "Verifier les symptomes declares\nComparer avec les pannes similaires\nDocumenter le diagnostic final";
        }
        return correctiveSamples;
    }

    private String inferUrgence(TypePanne typePanne) {
        return switch (typePanne) {
            case ALERTE_IRRUPTION, COURT_CIRCUIT -> "CRITIQUE";
            case PROBLEME_BATTERIE_CHARGE, HARDWARE, INCIDENT_0060_CENTRE_BANCAIRE_NON_ATTEINT -> "HAUTE";
            default -> "MOYENNE";
        };
    }

    private boolean shouldRecommendReplacement(TypePanne typePanne, List<Panne> pannes) {
        return typePanne == TypePanne.ALERTE_IRRUPTION
                || typePanne == TypePanne.COURT_CIRCUIT
                || pannes.stream().anyMatch(panne -> hasText(panne.getActionCorrective())
                && normalize(panne.getActionCorrective()).contains("remplacement"));
    }

    private String firstNonBlank(List<Panne> pannes,
                                 java.util.function.Function<Panne, String> extractor,
                                 String fallback) {
        return pannes.stream()
                .map(extractor)
                .filter(this::hasText)
                .map(String::trim)
                .findFirst()
                .orElse(fallback);
    }

    private void applyRequest(PanneDiagnosticKnowledge knowledge, PanneDiagnosticKnowledgeRequest request) {
        if (request == null) {
            throw new BusinessException("La connaissance diagnostic est obligatoire");
        }

        knowledge.setTitre(request.getTitre().trim());
        knowledge.setTypePanne(request.getTypePanne());
        knowledge.setMotsCles(join(request.getMotsCles()));
        knowledge.setSymptomes(clean(request.getSymptomes()));
        knowledge.setDiagnostic(request.getDiagnostic().trim());
        knowledge.setActionCorrective(request.getActionCorrective().trim());
        knowledge.setUrgence(request.getUrgence().trim().toUpperCase());
        knowledge.setRecommandations(join(request.getRecommandations()));
        knowledge.setRemplacementRecommande(Boolean.TRUE.equals(request.getRemplacementRecommande()));
        knowledge.setActif(request.getActif() == null || request.getActif());
        knowledge.setPriorite(request.getPriorite() != null ? request.getPriorite() : 0);
    }

    private PanneDiagnosticKnowledge getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Connaissance diagnostic introuvable"));
    }

    private PanneDiagnosticKnowledgeResponse mapToResponse(PanneDiagnosticKnowledge knowledge) {
        return PanneDiagnosticKnowledgeResponse.builder()
                .id(knowledge.getId())
                .titre(knowledge.getTitre())
                .typePanne(knowledge.getTypePanne())
                .motsCles(split(knowledge.getMotsCles()))
                .symptomes(knowledge.getSymptomes())
                .diagnostic(knowledge.getDiagnostic())
                .actionCorrective(knowledge.getActionCorrective())
                .urgence(knowledge.getUrgence())
                .recommandations(split(knowledge.getRecommandations()))
                .remplacementRecommande(Boolean.TRUE.equals(knowledge.getRemplacementRecommande()))
                .actif(Boolean.TRUE.equals(knowledge.getActif()))
                .priorite(knowledge.getPriorite())
                .lastModifiedDate(knowledge.getLastModifiedDate())
                .build();
    }

    private String join(List<String> values) {
        if (values == null) {
            return "";
        }

        return values.stream()
                .map(this::clean)
                .filter(this::hasText)
                .collect(Collectors.joining("\n"));
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

    private List<String> tokens(String value) {
        return Arrays.stream(normalize(value).split(" "))
                .map(String::trim)
                .filter(token -> token.length() >= 4)
                .distinct()
                .toList();
    }

    private List<String> stopWords() {
        return List.of("avec", "dans", "pour", "plus", "client", "terminal", "probleme", "panne", "tpe");
    }

    private String normalize(String value) {
        String normalized = Normalizer.normalize(clean(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
        return normalized.replaceAll("\\s+", " ");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String clean(String value) {
        return Objects.toString(value, "").trim();
    }
}
