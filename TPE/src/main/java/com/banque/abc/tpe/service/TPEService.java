package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.tpe.TPERequest;
import com.banque.abc.tpe.dto.tpe.TPEResponse;
import com.banque.abc.tpe.entity.Affectation;
import com.banque.abc.tpe.entity.Commercant;
import com.banque.abc.tpe.entity.Demande;
import com.banque.abc.tpe.entity.HistoriqueStatut;
import com.banque.abc.tpe.entity.Panne;
import com.banque.abc.tpe.entity.TPE;
import com.banque.abc.tpe.entity.enums.StatutTPE;
import com.banque.abc.tpe.exception.BusinessException;
import com.banque.abc.tpe.exception.DuplicateResourceException;
import com.banque.abc.tpe.exception.ResourceNotFoundException;
import com.banque.abc.tpe.repository.AffectationRepository;
import com.banque.abc.tpe.repository.CommercantRepository;
import com.banque.abc.tpe.repository.HistoriqueStatutRepository;
import com.banque.abc.tpe.repository.PanneRepository;
import com.banque.abc.tpe.repository.TPERepository;
import com.banque.abc.tpe.util.TIDGenerator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TPEService {

    private final TPERepository tpeRepository;
    private final CommercantRepository commercantRepository;
    private final HistoriqueStatutRepository historiqueStatutRepository;
    private final AffectationRepository affectationRepository;
    private final PanneRepository panneRepository;
    private final TIDGenerator tidGenerator;
    private final ModelMapper modelMapper;
    private final AuditService auditService;

    @Transactional
    public TPEResponse createTPE(TPERequest request) {
        if (tpeRepository.existsByNumeroSerie(request.getNumeroSerie())) {
            throw new DuplicateResourceException("Un TPE avec ce numéro de série existe déjà");
        }

        TPE tpe = modelMapper.map(request, TPE.class);
        tpe.setTypeTPE(resolveTypeTPE(request.getTypeTPE()));
        tpe.setStatut(StatutTPE.DISPONIBLE);

        // Le TID sera généré lors de l'affectation
        TPE savedTPE = tpeRepository.save(tpe);

        // Enregistrer l'historique du statut
        saveHistoriqueStatut(savedTPE, null, StatutTPE.DISPONIBLE, "TPE créé");

        auditService.logAction("CREATE", "TPE", savedTPE.getId().toString(),
                "TPE créé: " + savedTPE.getNumeroSerie(), "SUCCESS");

        return mapToResponse(savedTPE);
    }

    @Transactional(readOnly = true)
    public TPEResponse getTPEById(Long id) {
        TPE tpe = tpeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TPE non trouvé avec l'ID: " + id));
        return mapToResponse(tpe, true);
    }

    @Transactional(readOnly = true)
    public Page<TPEResponse> getAllTPEs(Pageable pageable) {
        return tpeRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<TPEResponse> getTPEsByStatut(StatutTPE statut) {
        return tpeRepository.findByStatut(statut).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TPEResponse> getTPEsDisponibles() {
        return tpeRepository.findByStatut(StatutTPE.DISPONIBLE).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TPEResponse> searchTPEsForPanneDeclaration(String query, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        List<String> terms = normalizeSearchTerms(query);

        Specification<TPE> specification = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("statut").in(
                    StatutTPE.AFFECTE,
                    StatutTPE.EN_PANNE,
                    StatutTPE.MAINTENANCE
            ));

            if (!terms.isEmpty()) {
                criteriaQuery.distinct(true);
                var commercant = root.join("commercant", JoinType.LEFT);
                var affectations = root.join("affectations", JoinType.LEFT);
                var affectationCommercant = affectations.join("commercant", JoinType.LEFT);

                for (String term : terms) {
                    String pattern = "%" + term + "%";
                    Predicate activeAffectationCommercantMatches = criteriaBuilder.and(
                            criteriaBuilder.equal(affectations.get("actif"), true),
                            criteriaBuilder.or(
                                    containsIgnoreCase(criteriaBuilder, affectationCommercant.get("raisonSociale"), pattern),
                                    containsIgnoreCase(criteriaBuilder, affectationCommercant.get("numeroCompte"), pattern),
                                    containsIgnoreCase(criteriaBuilder, affectationCommercant.get("codeAgence"), pattern)
                            )
                    );

                    predicates.add(criteriaBuilder.or(
                            containsIgnoreCase(criteriaBuilder, root.get("numeroSerie"), pattern),
                            containsIgnoreCase(criteriaBuilder, root.get("numeroTerminal"), pattern),
                            containsIgnoreCase(criteriaBuilder, root.get("numeroAffiliation"), pattern),
                            containsIgnoreCase(criteriaBuilder, root.get("typeTPE"), pattern),
                            containsIgnoreCase(criteriaBuilder, root.get("marque"), pattern),
                            containsIgnoreCase(criteriaBuilder, root.get("modele"), pattern),
                            containsIgnoreCase(criteriaBuilder, commercant.get("raisonSociale"), pattern),
                            containsIgnoreCase(criteriaBuilder, commercant.get("numeroCompte"), pattern),
                            containsIgnoreCase(criteriaBuilder, commercant.get("codeAgence"), pattern),
                            activeAffectationCommercantMatches
                    ));
                }
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };

        Pageable pageable = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "id"));
        return tpeRepository.findAll(specification, pageable)
                .map(this::mapToResponse)
                .getContent();
    }

    @Transactional
    public TPEResponse updateTPE(Long id, TPERequest request) {
        TPE tpe = tpeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TPE non trouvé avec l'ID: " + id));

        if (!tpe.getNumeroSerie().equals(request.getNumeroSerie()) &&
                tpeRepository.existsByNumeroSerie(request.getNumeroSerie())) {
            throw new DuplicateResourceException("Un TPE avec ce numéro de série existe déjà");
        }

        prepareNumeroTerminalForUpdate(tpe, request.getNumeroTerminal());
        modelMapper.map(request, tpe);
        tpe.setTypeTPE(resolveTypeTPE(request.getTypeTPE()));
        TPE updatedTPE = tpeRepository.save(tpe);

        auditService.logAction("UPDATE", "TPE", updatedTPE.getId().toString(),
                "TPE mis à jour: " + updatedTPE.getNumeroSerie(), "SUCCESS");

        return mapToResponse(updatedTPE);
    }

    @Transactional
    public void updateStatut(Long id, StatutTPE nouveauStatut, String commentaire) {
        TPE tpe = tpeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TPE non trouvé avec l'ID: " + id));

        StatutTPE ancienStatut = tpe.getStatut();
        tpe.setStatut(nouveauStatut);
        tpe.setCommentaire(commentaire);
        tpeRepository.save(tpe);

        saveHistoriqueStatut(tpe, ancienStatut, nouveauStatut, commentaire);

        auditService.logAction("UPDATE_STATUS", "TPE", tpe.getId().toString(),
                String.format("Statut changé de %s à %s", ancienStatut, nouveauStatut), "SUCCESS");
    }

    @Transactional
    public void deleteTPE(Long id) {
        TPE tpe = tpeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TPE non trouvé avec l'ID: " + id));

        if (tpe.getStatut() == StatutTPE.AFFECTE) {
            throw new BusinessException("Impossible de supprimer un TPE affecté");
        }

        tpeRepository.delete(tpe);

        auditService.logAction("DELETE", "TPE", id.toString(),
                "TPE supprimé: " + tpe.getNumeroSerie(), "SUCCESS");
    }

    @Transactional
    public String generateTIDForTPE(Long tpeId, String rib, String codeAgence) {
        TPE tpe = tpeRepository.findById(tpeId)
                .orElseThrow(() -> new ResourceNotFoundException("TPE non trouvé avec l'ID: " + tpeId));

        if (tpe.getNumeroTerminal() != null && !tpe.getNumeroTerminal().isEmpty()) {
            throw new BusinessException("Ce TPE a déjà un TID: " + tpe.getNumeroTerminal());
        }

        // Compter le nombre de TPE ayant déjà un TID pour le compteur
        int compteur = (int) tpeRepository.countTPEsWithNumeroTerminal() + 1;

        String tid = tidGenerator.generateTID(rib, codeAgence, compteur);

        // Vérifier l'unicité du TID
        while (tpeRepository.existsByNumeroTerminal(tid)) {
            compteur++;
            tid = tidGenerator.generateTID(rib, codeAgence, compteur);
        }

        tpe.setNumeroTerminal(tid);
        tpeRepository.save(tpe);

        auditService.logAction("GENERATE_TID", "TPE", tpe.getId().toString(),
                "TID généré: " + tid, "SUCCESS");

        return tid;
    }

    // Générer un TID sans l'associer à un TPE (pour prévisualisation)
    public String generateTID(String rib, String codeAgence) {
        if (rib == null || rib.length() < 2) {
            throw new BusinessException("RIB invalide (minimum 2 caractères requis)");
        }
        if (codeAgence == null || codeAgence.length() != 3) {
            throw new BusinessException("Code agence invalide (3 chiffres requis)");
        }

        // Compter le nombre de TPE ayant déjà un TID pour estimer le compteur
        int compteur = (int) tpeRepository.countTPEsWithNumeroTerminal() + 1;

        String tid = tidGenerator.generateTID(rib, codeAgence, compteur);

        // Vérifier l'unicité du TID
        while (tpeRepository.existsByNumeroTerminal(tid)) {
            compteur++;
            tid = tidGenerator.generateTID(rib, codeAgence, compteur);
        }

        return tid;
    }

    private void saveHistoriqueStatut(TPE tpe, StatutTPE ancienStatut, StatutTPE nouveauStatut, String commentaire) {
        HistoriqueStatut historique = HistoriqueStatut.builder()
                .tpe(tpe)
                .ancienStatut(ancienStatut)
                .nouveauStatut(nouveauStatut)
                .dateChangement(LocalDateTime.now())
                .commentaire(commentaire)
                .build();

        historiqueStatutRepository.save(historique);
    }

    private TPEResponse mapToResponse(TPE tpe) {
        return mapToResponse(tpe, false);
    }

    private TPEResponse mapToResponse(TPE tpe, boolean includeMonetiqueDetails) {
        TPEResponse response = modelMapper.map(tpe, TPEResponse.class);
        response.setTypeTPE(tpe.getTypeTPE());
        response.setSerieTpe(firstNonBlank(response.getSerieTpe(), tpe.getNumeroSerie()));

        resolveDisplayCommercant(tpe).ifPresent(commercant -> populateCommercantFields(response, commercant));

        if (includeMonetiqueDetails) {
            findDetailDemande(tpe).ifPresent(demande -> populateDemandeFields(response, demande));
        }

        return response;
    }

    private void populateCommercantFields(TPEResponse response, Commercant commercant) {
        response.setCommercantId(commercant.getId());
        response.setCommercantNom(commercant.getRaisonSociale());
        response.setRaisonSociale(firstNonBlank(response.getRaisonSociale(), commercant.getRaisonSociale()));
        response.setActivite(firstNonBlank(response.getActivite(), commercant.getActivite()));
        response.setNumeroCompte(firstNonBlank(response.getNumeroCompte(), commercant.getNumeroCompte()));
        response.setCodeAgence(firstNonBlank(response.getCodeAgence(), commercant.getCodeAgence()));
        response.setLoyer(firstNonNull(response.getLoyer(), commercant.getLoyer()));
        response.setUrlSiteMarchand(firstNonBlank(response.getUrlSiteMarchand(), commercant.getUrlSiteMarchand()));
        response.setWebhookUrl(firstNonBlank(response.getWebhookUrl(), commercant.getWebhookUrl()));
        response.setCartesAcceptees(firstNonBlank(response.getCartesAcceptees(), commercant.getTypeCartesAcceptees()));
        response.setModeTest(firstNonNull(response.getModeTest(), commercant.getModeTest()));

        if (response.getTypeCommerce() == null && commercant.getTypeCommerce() != null) {
            response.setTypeCommerce(commercant.getTypeCommerce().name());
        }
    }

    private void populateDemandeFields(TPEResponse response, Demande demande) {
        response.setRaisonSociale(firstNonBlank(demande.getRaisonSociale(), response.getRaisonSociale()));
        response.setActivite(firstNonBlank(demande.getActivite(), response.getActivite()));
        response.setMcc(firstNonBlank(demande.getMcc(), response.getMcc()));
        response.setTauxCommission(firstNonNull(demande.getTauxCommission(), response.getTauxCommission()));
        response.setTauxCommissionInter(firstNonNull(demande.getTauxCommissionInter(), response.getTauxCommissionInter()));
        response.setNumeroCompte(firstNonBlank(demande.getNumeroCompte(), demande.getRib(), response.getNumeroCompte()));
        response.setCodeAgence(firstNonBlank(demande.getCodeAgence(), response.getCodeAgence()));
        response.setSerieTpe(firstNonBlank(demande.getSerieTpe(), response.getSerieTpe()));
        response.setNumeroTerminal(firstNonBlank(demande.getNumeroTerminal(), response.getNumeroTerminal()));
        response.setLoyer(firstNonNull(demande.getLoyer(), response.getLoyer()));
        response.setUrlSiteMarchand(firstNonBlank(demande.getUrlSiteMarchand(), response.getUrlSiteMarchand()));

        response.setValueDate(demande.getValueDate());
    }

    private Optional<Demande> findDetailDemande(TPE tpe) {
        if (tpe.getId() == null) {
            return Optional.empty();
        }

        Optional<Demande> activeDemande = affectationRepository.findActiveByTpeId(tpe.getId())
                .map(Affectation::getDemande)
                .filter(demande -> demande != null);

        if (activeDemande.isPresent()) {
            return activeDemande;
        }

        Optional<Demande> historicalDemande = affectationRepository.findByTpeId(tpe.getId()).stream()
                .filter(affectation -> affectation.getDemande() != null)
                .max(Comparator.comparing(
                        Affectation::getCreatedDate,
                        Comparator.nullsFirst(Comparator.naturalOrder())
                ))
                .map(Affectation::getDemande);

        if (historicalDemande.isPresent()) {
            return historicalDemande;
        }

        return findReplacementSourceDemande(tpe);
    }

    private Optional<Demande> findReplacementSourceDemande(TPE replacementTpe) {
        if (replacementTpe.getId() == null) {
            return Optional.empty();
        }

        return panneRepository.findByTpeRemplacementId(replacementTpe.getId()).stream()
                .filter(panne -> panne.getTpe() != null)
                .flatMap(panne -> affectationRepository.findByTpeId(panne.getTpe().getId()).stream())
                .filter(affectation -> affectation.getDemande() != null)
                .max(Comparator
                        .comparing(Affectation::getDateFin, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(Affectation::getDateAffectation, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(Affectation::getCreatedDate, Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(Affectation::getDemande);
    }

    private void prepareNumeroTerminalForUpdate(TPE targetTpe, String requestedNumeroTerminal) {
        if (requestedNumeroTerminal == null || requestedNumeroTerminal.isBlank() || targetTpe.getId() == null) {
            return;
        }

        Optional<TPE> owner = tpeRepository.findByNumeroTerminal(requestedNumeroTerminal);
        if (owner.isEmpty() || owner.get().getId().equals(targetTpe.getId())) {
            return;
        }

        TPE ownerTpe = owner.get();
        boolean canTransferFromReplacedTpe = panneRepository.findByTpeRemplacementId(targetTpe.getId()).stream()
                .anyMatch(panne -> panne.getTpe() != null
                        && panne.getTpe().getId().equals(ownerTpe.getId())
                        && ownerTpe.getStatut() == StatutTPE.HORS_SERVICE);

        if (!canTransferFromReplacedTpe) {
            throw new DuplicateResourceException("Ce numero de terminal est deja utilise");
        }

        ownerTpe.setNumeroTerminal(null);
        tpeRepository.saveAndFlush(ownerTpe);
    }

    private Optional<Commercant> findActiveCommercant(TPE tpe) {
        if (tpe.getId() == null) {
            return Optional.empty();
        }

        return affectationRepository.findActiveByTpeId(tpe.getId())
                .map(Affectation::getCommercant);
    }

    private Optional<Commercant> resolveDisplayCommercant(TPE tpe) {
        if (tpe.getCommercant() != null) {
            return Optional.of(tpe.getCommercant());
        }

        Optional<Commercant> activeCommercant = findActiveCommercant(tpe);
        if (activeCommercant.isPresent()) {
            return activeCommercant;
        }

        if (tpe.getStatut() == StatutTPE.HORS_SERVICE) {
            return findLatestHistoricalCommercant(tpe);
        }

        return Optional.empty();
    }

    private Optional<Commercant> findLatestHistoricalCommercant(TPE tpe) {
        if (tpe.getId() == null) {
            return Optional.empty();
        }

        return affectationRepository.findByTpeId(tpe.getId()).stream()
                .filter(affectation -> affectation.getCommercant() != null)
                .max(Comparator
                        .comparing(Affectation::getDateFin, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(Affectation::getDateAffectation, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(Affectation::getCreatedDate, Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(Affectation::getCommercant);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }

    private String resolveTypeTPE(String value) {
        if (value == null || value.isBlank()) {
            return "TPE";
        }
        return value.trim().toUpperCase();
    }

    private Predicate containsIgnoreCase(CriteriaBuilder criteriaBuilder, Expression<String> expression, String pattern) {
        return criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(expression, "")), pattern);
    }

    private List<String> normalizeSearchTerms(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String normalized = Normalizer.normalize(query, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();

        if (normalized.isBlank()) {
            return List.of();
        }

        return List.of(normalized.split("\\s+"));
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }

        for (T value : values) {
            if (value != null) {
                return value;
            }
        }

        return null;
    }
}
