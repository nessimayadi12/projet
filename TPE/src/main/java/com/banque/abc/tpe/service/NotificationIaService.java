package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.notification.NotificationIaEventType;
import com.banque.abc.tpe.dto.notification.NotificationIaRequest;
import com.banque.abc.tpe.dto.notification.NotificationIaResponse;
import com.banque.abc.tpe.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationIaService {

    public NotificationIaResponse generer(NotificationIaRequest request) {
        if (request == null || request.getType() == null) {
            throw new BusinessException("Le type d'evenement est obligatoire pour generer la notification IA");
        }

        return generer(request.getType(), request.getContexte(), request.getDestinataire());
    }

    public NotificationIaResponse generer(NotificationIaEventType type, Map<String, Object> contexte) {
        return generer(type, contexte, null);
    }

    public NotificationIaResponse generer(NotificationIaEventType type,
                                          Map<String, Object> contexte,
                                          String destinataire) {
        if (type == null) {
            throw new BusinessException("Le type d'evenement est obligatoire pour generer la notification IA");
        }

        Map<String, Object> safeContext = contexte != null ? new LinkedHashMap<>(contexte) : Map.of();
        NotificationDraft draft = switch (type) {
            case DEMANDE_TPE_CREEE -> demandeCreee(safeContext);
            case DEMANDE_TPE_VALIDEE -> demandeValidee(safeContext);
            case DEMANDE_TPE_REFUSEE -> demandeRefusee(safeContext);
            case TPE_AFFECTE_COMMERCANT -> tpeAffecte(safeContext);
            case PANNE_TPE_DECLAREE -> panneDeclaree(safeContext);
            case PANNE_TPE_DIAGNOSTIQUEE -> panneDiagnostiquee(safeContext);
            case TPE_REPARE -> tpeRepare(safeContext);
            case TPE_REMPLACE -> tpeRemplace(safeContext);
            case DEMANDE_ATTENTE_COMPLEMENT_INFORMATION -> demandeAttenteComplement(safeContext);
        };

        return NotificationIaResponse.builder()
                .type(type)
                .titre(draft.titre())
                .message(draft.message())
                .priorite(draft.priorite())
                .destinataire(firstNonBlank(destinataire, draft.destinataire()))
                .actionsRecommandees(draft.actionsRecommandees())
                .contexte(safeContext)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    public String message(NotificationIaEventType type, Map<String, Object> contexte) {
        return generer(type, contexte).getMessage();
    }

    private NotificationDraft demandeCreee(Map<String, Object> contexte) {
        String reference = value(contexte, "reference", "la nouvelle demande");
        String commercant = value(contexte, "commercantNom", "le commercant");
        String typeDemande = value(contexte, "typeDemande", "TPE");
        String urgence = value(contexte, "urgence", "normale");

        return new NotificationDraft(
                "Nouvelle demande TPE",
                "La demande " + reference + " a ete creee pour " + commercant
                        + ". Elle concerne un TPE de type " + typeDemande
                        + " avec une urgence " + urgence
                        + ". Le dossier doit etre verifie afin de lancer le traitement monetique.",
                priorityFromUrgence(urgence),
                "Service Monetique",
                List.of("Verifier les informations commercant", "Controler les pieces jointes", "Planifier le traitement")
        );
    }

    private NotificationDraft demandeValidee(Map<String, Object> contexte) {
        String reference = value(contexte, "reference", "la demande");
        String commercant = value(contexte, "commercantNom", "le commercant");
        String statut = value(contexte, "statut", "");
        String tpe = firstContextValue(contexte, "numeroTerminal", "serieTpe", "tpeNumeroSerie");
        String affectation = hasText(tpe)
                ? " Le terminal " + tpe + " est rattache au dossier."
                : "";

        return new NotificationDraft(
                "Demande TPE validee",
                "La demande " + reference + " de " + commercant
                        + " a ete validee par le service Monetique."
                        + affectation
                        + statusSentence(statut)
                        + " Le suivi peut continuer vers l'affectation, la mise en service ou la cloture operationnelle.",
                "HAUTE",
                "Agence",
                List.of("Informer l'agence", "Verifier l'affectation du TPE", "Suivre la mise en service")
        );
    }

    private NotificationDraft demandeRefusee(Map<String, Object> contexte) {
        String reference = value(contexte, "reference", "la demande");
        String commercant = value(contexte, "commercantNom", "le commercant");
        String motif = firstContextValue(contexte, "motif", "commentaire", "commentaireValidation");
        String motifSentence = hasText(motif) ? " Motif indique: " + motif + "." : "";

        return new NotificationDraft(
                "Demande TPE refusee",
                "La demande " + reference + " concernant " + commercant
                        + " a ete refusee par le service Monetique."
                        + motifSentence
                        + " Le demandeur doit etre informe pour corriger le dossier ou soumettre une nouvelle demande si necessaire.",
                "HAUTE",
                "Agence",
                List.of("Notifier le demandeur", "Archiver le motif de refus", "Preparer une correction si besoin")
        );
    }

    private NotificationDraft tpeAffecte(Map<String, Object> contexte) {
        String tpe = firstContextValue(contexte, "tpeNumeroTerminal", "numeroTerminal", "tpeNumeroSerie", "numeroSerie");
        String commercant = value(contexte, "commercantNom", "le commercant");
        String reference = value(contexte, "demandeReference", "la demande associee");

        return new NotificationDraft(
                "TPE affecte",
                "Le TPE " + defaultText(tpe, "selectionne") + " a ete affecte a " + commercant
                        + " dans le cadre de " + reference
                        + ". La disponibilite du terminal est mise a jour et la mise en service peut etre planifiee.",
                "HAUTE",
                "Agence",
                List.of("Confirmer la remise du TPE", "Planifier la mise en service", "Mettre a jour le dossier commercant")
        );
    }

    private NotificationDraft panneDeclaree(Map<String, Object> contexte) {
        String reference = value(contexte, "reference", "la panne");
        String tpe = firstContextValue(contexte, "tpeNumeroTerminal", "tpeNumeroSerie", "numeroTerminal", "numeroSerie");
        String commercant = value(contexte, "commercantNom", "le commercant");
        String description = value(contexte, "description", "aucune description detaillee");

        return new NotificationDraft(
                "Panne TPE declaree",
                "Une panne " + reference + " a ete declaree sur le TPE "
                        + defaultText(tpe, "concerne") + " de " + commercant
                        + ". Symptome signale: " + description
                        + ". Le terminal doit etre controle rapidement afin de limiter l'impact sur l'activite du commercant.",
                "HAUTE",
                "Service Monetique",
                List.of("Prioriser le diagnostic", "Verifier l'etat du TPE", "Informer le commercant du suivi")
        );
    }

    private NotificationDraft panneDiagnostiquee(Map<String, Object> contexte) {
        String reference = value(contexte, "reference", "la panne");
        String tpe = firstContextValue(contexte, "tpeNumeroTerminal", "tpeNumeroSerie", "numeroTerminal", "numeroSerie");
        String diagnostic = value(contexte, "diagnostic", "diagnostic non precise");

        return new NotificationDraft(
                "Panne diagnostiquee",
                "La panne " + reference + " du TPE " + defaultText(tpe, "concerne")
                        + " a ete diagnostiquee. Conclusion technique: " + diagnostic
                        + ". La prochaine etape consiste a engager la reparation ou a evaluer un remplacement si le terminal est irrecuperable.",
                "HAUTE",
                "Service Technique",
                List.of("Valider l'action corrective", "Planifier la reparation", "Preparer un remplacement si necessaire")
        );
    }

    private NotificationDraft tpeRepare(Map<String, Object> contexte) {
        String reference = value(contexte, "reference", "la panne");
        String tpe = firstContextValue(contexte, "tpeNumeroTerminal", "tpeNumeroSerie", "numeroTerminal", "numeroSerie");
        String solution = firstContextValue(contexte, "solution", "actionCorrective");
        String solutionSentence = hasText(solution) ? " Solution appliquee: " + solution + "." : "";

        return new NotificationDraft(
                "TPE repare",
                "Le TPE " + defaultText(tpe, "concerne") + " a ete repare suite a " + reference + "."
                        + solutionSentence
                        + " Une verification fonctionnelle est recommandee avant remise en service complete.",
                "MOYENNE",
                "Agence",
                List.of("Effectuer un test de fonctionnement", "Confirmer la remise en service", "Cloturer le suivi de panne")
        );
    }

    private NotificationDraft tpeRemplace(Map<String, Object> contexte) {
        String ancienTpe = firstContextValue(contexte, "ancienTpeNumeroSerie", "ancienNumeroSerie", "tpeNumeroSerie");
        String nouveauTpe = firstContextValue(contexte, "tpeRemplacementNumeroSerie", "nouveauNumeroSerie", "numeroSerieRemplacement");
        String commercant = value(contexte, "commercantNom", "le commercant");

        return new NotificationDraft(
                "TPE remplace",
                "Le TPE " + defaultText(ancienTpe, "initial") + " a ete remplace par le TPE "
                        + defaultText(nouveauTpe, "de remplacement") + " pour " + commercant
                        + ". L'ancien terminal doit rester hors service et la nouvelle affectation doit etre controlee avant reprise d'exploitation.",
                "CRITIQUE",
                "Service Monetique",
                List.of("Verifier la nouvelle affectation", "Confirmer la mise hors service de l'ancien TPE", "Informer le commercant")
        );
    }

    private NotificationDraft demandeAttenteComplement(Map<String, Object> contexte) {
        String reference = value(contexte, "reference", "la demande");
        String commercant = value(contexte, "commercantNom", "le commercant");
        String motif = firstContextValue(contexte, "motif", "commentaire", "commentaireValidation");
        String motifSentence = hasText(motif)
                ? " Complement attendu: " + motif + "."
                : " Des informations complementaires sont attendues avant poursuite.";

        return new NotificationDraft(
                "Complement d'information demande",
                "La demande " + reference + " de " + commercant
                        + " est mise en attente de complement d'information."
                        + motifSentence
                        + " Le traitement reprendra des reception des elements manquants.",
                "MOYENNE",
                "Agence",
                List.of("Informer le demandeur", "Completer les informations manquantes", "Relancer le traitement apres correction")
        );
    }

    private String priorityFromUrgence(String urgence) {
        if (urgence == null) {
            return "MOYENNE";
        }
        String normalized = urgence.trim().toUpperCase();
        if (normalized.contains("CRITIQUE")) {
            return "CRITIQUE";
        }
        if (normalized.contains("HAUTE") || normalized.contains("URGENT")) {
            return "HAUTE";
        }
        return "MOYENNE";
    }

    private String statusSentence(String statut) {
        if (!hasText(statut)) {
            return "";
        }
        return " Statut actuel: " + statut + ".";
    }

    private String firstContextValue(Map<String, Object> contexte, String... keys) {
        for (String key : keys) {
            String value = value(contexte, key, null);
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private String value(Map<String, Object> contexte, String key, String fallback) {
        if (contexte == null || key == null || !contexte.containsKey(key)) {
            return fallback;
        }
        Object value = contexte.get(key);
        if (value == null) {
            return fallback;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? fallback : text;
    }

    private String defaultText(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private String firstNonBlank(String primary, String fallback) {
        return hasText(primary) ? primary : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record NotificationDraft(String titre,
                                     String message,
                                     String priorite,
                                     String destinataire,
                                     List<String> actionsRecommandees) {
    }
}
