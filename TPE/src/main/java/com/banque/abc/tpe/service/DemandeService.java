package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.affectation.AffectationRequest;
import com.banque.abc.tpe.dto.demande.DemandeRequest;
import com.banque.abc.tpe.dto.demande.DemandeResponse;
import com.banque.abc.tpe.dto.demande.ValiderDemandeRequest;
import com.banque.abc.tpe.entity.Affectation;
import com.banque.abc.tpe.entity.Commercant;
import com.banque.abc.tpe.entity.Demande;
import com.banque.abc.tpe.entity.Panne;
import com.banque.abc.tpe.entity.TPE;
import com.banque.abc.tpe.entity.User;
import com.banque.abc.tpe.entity.PieceJointe;
import com.banque.abc.tpe.entity.enums.StatutDemande;
import com.banque.abc.tpe.entity.enums.StatutTPE;
import com.banque.abc.tpe.exception.BusinessException;
import com.banque.abc.tpe.exception.ResourceNotFoundException;
import com.banque.abc.tpe.entity.enums.RoleType;
import com.banque.abc.tpe.repository.AffectationRepository;
import com.banque.abc.tpe.repository.CommercantRepository;
import com.banque.abc.tpe.repository.DemandeRepository;
import com.banque.abc.tpe.repository.PanneRepository;
import com.banque.abc.tpe.repository.PieceJointeRepository;
import com.banque.abc.tpe.repository.UserRepository;
import com.banque.abc.tpe.security.UserPrincipal;
import com.banque.abc.tpe.util.ReferenceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DemandeService {

    private final DemandeRepository demandeRepository;
    private final CommercantRepository commercantRepository;
    private final UserRepository userRepository;
    private final ReferenceGenerator referenceGenerator;
    private final ModelMapper modelMapper;
    private final AuditService auditService;
    private final AffectationService affectationService;
    private final AffectationRepository affectationRepository;
    private final PanneRepository panneRepository;
    private final PieceJointeRepository pieceJointeRepository;

    @Transactional
    public DemandeResponse createDemande(DemandeRequest request) {
        // Chercher ou créer le commerçant
        Commercant commercant = findOrCreateCommercant(request);

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User demandeur = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Générer une référence unique
        int compteur = (int) demandeRepository.count() + 1;
        String reference = referenceGenerator.generateDemandeReference(compteur);
        while (demandeRepository.existsByReference(reference)) {
            compteur++;
            reference = referenceGenerator.generateDemandeReference(compteur);
        }

        Demande demande = Demande.builder()
                .reference(reference)
                .typeDemande(request.getTypeDemande())
                .commercant(commercant)
                .demandeur(demandeur)
                .description(request.getDescription())
                .urgence(request.getUrgence())
                .statut(StatutDemande.NOUVELLE)
                // Champs agence (TPE)
                .raisonSociale(request.getRaisonSociale())
                .activite(request.getActivite())
                .numeroCompte(request.getNumeroCompte())
                .adresse(request.getAdresse())
                .codePostal(request.getCodePostal())
                .codeAgence(request.getCodeAgence())
                .telephone(request.getTelephone())
                // Champs Mobile
                .localite(request.getLocalite())
                .rib(request.getRib())
                .webmaster(request.getWebmaster())
                .contactTechnique(request.getContactTechnique())
                .urlSiteMarchand(request.getUrlSiteMarchand())
                .build();

        Demande savedDemande = demandeRepository.save(demande);

        auditService.logAction("CREATE", "Demande", savedDemande.getId().toString(),
                "Demande créée: " + savedDemande.getReference(), "SUCCESS");

        return mapToResponse(savedDemande);
    }

    @Transactional(readOnly = true)
    public DemandeResponse getDemandeById(Long id) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée avec l'ID: " + id));
        return mapToResponse(demande);
    }

    @Transactional(readOnly = true)
    public Page<DemandeResponse> getAllDemandes(Pageable pageable) {
        return demandeRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional
    public DemandeResponse updateDemande(Long id, DemandeRequest request) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée avec l'ID: " + id));

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        boolean isMonetique = hasAnyAuthority(userPrincipal,
                RoleType.ROLE_MONETIQUE, RoleType.ROLE_ADMIN);

        if (demande.getStatut() == StatutDemande.AFFECTEE) {
            if (!isMonetique) {
                throw new BusinessException("Seul le service Monétique peut modifier après affectation");
            }

            if (hasWorkflowImpactingChanges(demande, request)) {
                resetWorkflowForModification(demande);
            }
        } else if (demande.getStatut() != StatutDemande.NOUVELLE
                && demande.getStatut() != StatutDemande.EN_COURS) {
            throw new BusinessException("Cette demande ne peut pas être modifiée à ce statut");
        }

        Commercant commercant = findOrCreateCommercant(request);
        demande.setCommercant(commercant);

        demande.setTypeDemande(request.getTypeDemande());
        demande.setDescription(request.getDescription());
        demande.setUrgence(request.getUrgence());

        demande.setRaisonSociale(request.getRaisonSociale());
        demande.setActivite(request.getActivite());
        demande.setNumeroCompte(request.getNumeroCompte());
        demande.setAdresse(request.getAdresse());
        demande.setCodePostal(request.getCodePostal());
        demande.setCodeAgence(request.getCodeAgence());
        demande.setTelephone(request.getTelephone());

        demande.setLocalite(request.getLocalite());
        demande.setRib(request.getRib());
        demande.setWebmaster(request.getWebmaster());
        demande.setContactTechnique(request.getContactTechnique());
        demande.setUrlSiteMarchand(request.getUrlSiteMarchand());
        applyMonetiqueFieldsForUpdate(demande, request);

        Demande updatedDemande = demandeRepository.save(demande);

        auditService.logAction("UPDATE", "Demande", updatedDemande.getId().toString(),
                "Demande modifiée", "SUCCESS");

        return mapToResponse(updatedDemande);
    }

    @Transactional
    public DemandeResponse validerDemande(Long id, ValiderDemandeRequest request) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée avec l'ID: " + id));

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User valideur = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!hasAnyAuthority(userPrincipal, RoleType.ROLE_MONETIQUE, RoleType.ROLE_ADMIN)) {
            throw new BusinessException("Seuls le service Monetique ou un administrateur peuvent valider ou rejeter une demande");
        }

        if (demande.getStatut() != StatutDemande.NOUVELLE
                && demande.getStatut() != StatutDemande.EN_COURS) {
            throw new BusinessException("Seules les demandes nouvelles ou en cours peuvent etre validees");
        }

        LocalDateTime decisionDate = LocalDateTime.now();
        demande.setValideur(valideur);
        demande.setDateValidation(decisionDate);
        demande.setCommentaireValidation(request.getCommentaire());

        // Autoriser la modification des taux avant validation finale
        demande.setMcc(request.getMcc());
        demande.setTauxCommission(request.getTauxCommission());
        demande.setTauxCommissionInter(request.getTauxCommissionInter());
        demande.setLoyer(request.getLoyer());
        demande.setSerieTpe(request.getSerieTpe());
        demande.setNumeroTerminal(request.getNumeroTerminal());
        demande.setValueDate(resolveValueDate(request.getValueDate()));

        if (Boolean.TRUE.equals(request.getApprouver())) {
            demande.setDateSaisieTaux(decisionDate);
            demande.setStatut(StatutDemande.VALIDEE_MONETIQUE);
        } else {
            demande.setStatut(StatutDemande.REJETEE);
            demande.setDateCloture(LocalDateTime.now());
        }

        Demande updatedDemande = demandeRepository.save(demande);

        if (Boolean.TRUE.equals(request.getApprouver())) {
            if (affectationRepository.existsByDemandeIdAndActifTrue(updatedDemande.getId())) {
                updatedDemande.setStatut(StatutDemande.AFFECTEE);
                updatedDemande.setDateCloture(LocalDateTime.now());
                updatedDemande = demandeRepository.save(updatedDemande);
            } else {
                AffectationRequest affectationRequest = new AffectationRequest();
                affectationRequest.setDemandeId(updatedDemande.getId());
                affectationRequest.setCommentaire("Affectation automatique suite a validation monetique");

                affectationService.affecterTPE(affectationRequest);
                updatedDemande = demandeRepository.findById(updatedDemande.getId())
                        .orElse(updatedDemande);
            }
        }

        auditService.logAction("VALIDATE", "Demande", updatedDemande.getId().toString(),
                "Demande " + (request.getApprouver() ? "validee" : "rejetee") + " par Monetique", "SUCCESS");

        return mapToResponse(updatedDemande);
    }

    @Transactional
    public DemandeResponse rejeterDemande(Long id, String commentaire) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée avec l'ID: " + id));

        if (demande.getStatut() != StatutDemande.NOUVELLE
                && demande.getStatut() != StatutDemande.EN_COURS) {
            throw new BusinessException("Seules les demandes nouvelles ou en cours peuvent etre rejetees");
        }

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User valideur = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!hasAnyAuthority(userPrincipal, RoleType.ROLE_MONETIQUE, RoleType.ROLE_ADMIN)) {
            throw new BusinessException("Seuls le service Monetique ou un administrateur peuvent rejeter une demande");
        }

        demande.setValideur(valideur);
        demande.setDateValidation(LocalDateTime.now());
        demande.setCommentaireValidation(commentaire);
        demande.setStatut(StatutDemande.REJETEE);
        demande.setDateCloture(LocalDateTime.now());

        Demande updatedDemande = demandeRepository.save(demande);


        auditService.logAction("REJECT", "Demande", updatedDemande.getId().toString(),
                "Demande rejetée: " + demande.getReference(), "SUCCESS");

        return mapToResponse(updatedDemande);
    }

    @Transactional
    public void cloturerDemande(Long id) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée avec l'ID: " + id));

        demande.setStatut(StatutDemande.CLOTUREE);
        demande.setDateCloture(LocalDateTime.now());
        demandeRepository.save(demande);

        auditService.logAction("CLOSE", "Demande", demande.getId().toString(),
                "Demande clôturée: " + demande.getReference(), "SUCCESS");
    }

    private DemandeResponse mapToResponse(Demande demande) {
        DemandeResponse response = modelMapper.map(demande, DemandeResponse.class);
        
        // Commerçant devrait toujours exister maintenant
        if (demande.getCommercant() != null) {
            response.setCommercantId(demande.getCommercant().getId());
            response.setCommercantNom(demande.getCommercant().getRaisonSociale());
        } else {
            // Fallback : utiliser la raison sociale de la demande
            response.setCommercantNom(demande.getRaisonSociale());
        }
        
        if (demande.getDemandeur() != null) {
            response.setDemandeurId(demande.getDemandeur().getId());
            response.setDemandeurNom(demande.getDemandeur().getNom() + " " + demande.getDemandeur().getPrenom());
        }

        if (demande.getInputer() != null) {
            response.setInputerId(demande.getInputer().getId());
            response.setInputerNom(demande.getInputer().getNom() + " " + demande.getInputer().getPrenom());
        }
        response.setDateSaisieTaux(demande.getDateSaisieTaux());
        
        if (demande.getValideur() != null) {
            response.setValideurId(demande.getValideur().getId());
            response.setValideurNom(demande.getValideur().getNom() + " " + demande.getValideur().getPrenom());
        }
        
        // Mapper les pièces jointes
        if (demande.getPiecesJointes() != null && !demande.getPiecesJointes().isEmpty()) {
            response.setPiecesJointes(
                demande.getPiecesJointes().stream()
                    .filter(pj -> pj != null && pj.getCheminFichier() != null)
                    .map(PieceJointe::getCheminFichier)
                    .toList()
            );
        }

        populateTpeAffectationFields(response, demande);
        
        return response;
    }

    private void populateTpeAffectationFields(DemandeResponse response, Demande demande) {
        findPrimaryAffectation(demande).ifPresent(affectation -> {
            response.setDateAffectation(affectation.getDateAffectation());

            TPE tpe = affectation.getTpe();
            if (tpe != null) {
                response.setTpeAffecteId(tpe.getId());
                response.setTpeAffecteNumeroSerie(tpe.getNumeroSerie());
                response.setTpeAffecteStatut(toTpeDisplayStatut(tpe.getStatut()));

                findLatestReplacement(tpe).ifPresent(replacement -> {
                    response.setTpeRemplacementId(replacement.getId());
                    response.setTpeRemplacementNumeroSerie(replacement.getNumeroSerie());
                    response.setNouvelleSerieTpe(replacement.getNumeroSerie());
                });
            }
        });
    }

    private Optional<Affectation> findPrimaryAffectation(Demande demande) {
        if (demande.getId() == null) {
            return Optional.empty();
        }
        return affectationRepository.findByDemandeIdOrderByActifDescDateAffectationDescIdDesc(demande.getId())
                .stream()
                .findFirst();
    }

    private Optional<TPE> findLatestReplacement(TPE oldTpe) {
        if (oldTpe.getId() == null) {
            return Optional.empty();
        }

        return panneRepository.findByTpeId(oldTpe.getId()).stream()
                .filter(panne -> panne.getTpeRemplacement() != null)
                .max(Comparator
                        .comparing(Panne::getDateResolution, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(Panne::getCreatedDate, Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(Panne::getTpeRemplacement);
    }

    private String toTpeDisplayStatut(StatutTPE statut) {
        if (statut == null) {
            return null;
        }
        if (statut == StatutTPE.HORS_SERVICE) {
            return "Cloture";
        }
        return statut.name();
    }

    private boolean hasWorkflowImpactingChanges(Demande demande, DemandeRequest request) {
        if (!Objects.equals(demande.getTypeDemande(), request.getTypeDemande())) {
            return true;
        }
        if (!Objects.equals(demande.getUrgence(), request.getUrgence())) {
            return true;
        }
        if (request.getCommercantId() != null
                && (demande.getCommercant() == null
                || !Objects.equals(demande.getCommercant().getId(), request.getCommercantId()))) {
            return true;
        }

        return !sameText(demande.getDescription(), request.getDescription())
                || !sameText(demande.getRaisonSociale(), request.getRaisonSociale())
                || !sameText(demande.getActivite(), request.getActivite())
                || !sameText(demande.getNumeroCompte(), request.getNumeroCompte())
                || !sameText(demande.getAdresse(), request.getAdresse())
                || !sameText(demande.getCodePostal(), request.getCodePostal())
                || !sameText(demande.getCodeAgence(), request.getCodeAgence())
                || !sameText(demande.getTelephone(), request.getTelephone())
                || !sameText(demande.getLocalite(), request.getLocalite())
                || !sameText(demande.getRib(), request.getRib())
                || !sameText(demande.getWebmaster(), request.getWebmaster())
                || !sameText(demande.getContactTechnique(), request.getContactTechnique())
                || !sameText(demande.getUrlSiteMarchand(), request.getUrlSiteMarchand());
    }

    private void applyMonetiqueFieldsForUpdate(Demande demande, DemandeRequest request) {
        if (!hasMonetiqueFields(request)) {
            return;
        }

        demande.setMcc(trimToNull(request.getMcc()));
        demande.setTauxCommission(request.getTauxCommission());
        demande.setTauxCommissionInter(request.getTauxCommissionInter());
        demande.setLoyer(request.getLoyer());
        demande.setSerieTpe(trimToNull(request.getSerieTpe()));
        demande.setNumeroTerminal(trimToNull(request.getNumeroTerminal()));
        demande.setValueDate(resolveValueDate(request.getValueDate()));
    }

    private boolean hasMonetiqueFields(DemandeRequest request) {
        return hasText(request.getMcc())
                || request.getTauxCommission() != null
                || request.getTauxCommissionInter() != null
                || request.getLoyer() != null
                || hasText(request.getSerieTpe())
                || hasText(request.getNumeroTerminal())
                || request.getValueDate() != null;
    }

    private boolean sameText(String currentValue, String requestedValue) {
        return Objects.equals(trimToNull(currentValue), trimToNull(requestedValue));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void resetWorkflowForModification(Demande demande) {
        demande.setStatut(StatutDemande.NOUVELLE);

        demande.setInputer(null);
        demande.setDateSaisieTaux(null);
        demande.setValideur(null);
        demande.setDateValidation(null);
        demande.setDateCloture(null);
        demande.setCommentaireValidation(null);

        demande.setMcc(null);
        demande.setTauxCommission(null);
        demande.setTauxCommissionInter(null);
        demande.setLoyer(null);
        demande.setSerieTpe(null);
        demande.setNumeroTerminal(null);
        demande.setValueDate(1);
    }

    private Integer resolveValueDate(Integer valueDate) {
        if (valueDate == null) {
            return 1;
        }
        if (valueDate != 1 && valueDate != 2) {
            throw new BusinessException("La value date doit etre 1 ou 2");
        }
        return valueDate;
    }

    private boolean hasAnyAuthority(UserPrincipal userPrincipal, RoleType... roles) {
        return userPrincipal.getAuthorities().stream()
                .anyMatch(auth -> {
                    for (RoleType role : roles) {
                        if (auth.getAuthority().equals(role.name())) {
                            return true;
                        }
                    }
                    return false;
                });
    }

    /**
     * Chercher un commerçant existant ou en créer un nouveau
     */
    private Commercant findOrCreateCommercant(DemandeRequest request) {
        // Si commercantId fourni, utiliser le commerçant existant
        if (request.getCommercantId() != null) {
            return commercantRepository.findById(request.getCommercantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Commerçant non trouvé"));
        }
        
        // Chercher un commerçant existant par raison sociale
        Optional<Commercant> existingCommercant = commercantRepository
                .findByRaisonSociale(request.getRaisonSociale());
        
        if (existingCommercant.isPresent()) {
            auditService.logAction("INFO", "Demande", null,
                    "Commerçant existant trouvé: " + request.getRaisonSociale(), "SUCCESS");
            return existingCommercant.get();
        }
        
        // Créer un nouveau commerçant
        Commercant nouveauCommercant = Commercant.builder()
                .raisonSociale(request.getRaisonSociale())
                .activite(request.getActivite())
                .numeroCompte(request.getNumeroCompte() != null ? request.getNumeroCompte() : "TEMP_" + System.currentTimeMillis())
                .adresse(request.getAdresse())
                .localite(request.getLocalite())
                .codePostal(request.getCodePostal())
                .codeAgence(request.getCodeAgence())
                .telephone(request.getTelephone())
                .statut(com.banque.abc.tpe.entity.enums.StatutCommercant.ACTIF)
                .typeCommerce(request.getTypeDemande())
                // Champs Mobile
                .urlSiteMarchand(request.getUrlSiteMarchand())
                .webmaster(request.getWebmaster())
                .contactTechnique(request.getContactTechnique())
                .build();
        
        Commercant savedCommercant = commercantRepository.save(nouveauCommercant);
        
        auditService.logAction("CREATE", "Commercant", savedCommercant.getId().toString(),
                "Commerçant créé automatiquement depuis demande: " + savedCommercant.getRaisonSociale(), 
                "SUCCESS");
        
        return savedCommercant;
    }

    @Transactional
    public void uploadPieceJointe(Long demandeId, MultipartFile file) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée avec l'ID: " + demandeId));

        if (file.isEmpty()) {
            throw new BusinessException("Le fichier est vide");
        }

        // Créer le répertoire si nécessaire
        String uploadDir = "uploads/demandes/" + demandeId;
        Path uploadPath = Paths.get(uploadDir);
        
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Générer un nom de fichier unique
            String originalFilename = file.getOriginalFilename();
            String filename = System.currentTimeMillis() + "_" + originalFilename;
            Path filePath = uploadPath.resolve(filename);

            // Copier le fichier
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Créer l'entité PieceJointe
            PieceJointe pieceJointe = PieceJointe.builder()
                    .demande(demande)
                    .nomFichier(originalFilename)
                    .cheminFichier(filePath.toString())
                    .typeMime(file.getContentType())
                    .tailleFichier(file.getSize())
                    .description("Document RNE")
                    .build();

            // Ajouter à la liste des pièces jointes de la demande
            demande.getPiecesJointes().add(pieceJointe);
            demandeRepository.save(demande);

            auditService.logAction("UPLOAD", "Demande", demande.getId().toString(),
                    "Pièce jointe uploadée: " + filename, "SUCCESS");

            log.info("Fichier uploadé avec succès: {} pour la demande {}", filename, demandeId);
        } catch (IOException e) {
            log.error("Erreur lors de l'upload du fichier pour la demande {}", demandeId, e);
            throw new BusinessException("Erreur lors de l'upload du fichier: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public byte[] downloadPieceJointe(Long demandeId, String fileName) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée avec l'ID: " + demandeId));

        Path filePath = resolvePieceJointePath(demandeId, fileName);

        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("Fichier non trouvé: " + fileName);
        }

        try {
            byte[] fileContent = Files.readAllBytes(filePath);
            
            auditService.logAction("DOWNLOAD", "Demande", demande.getId().toString(),
                    "Pièce jointe téléchargée: " + fileName, "SUCCESS");
            
            log.info("Fichier téléchargé: {} pour la demande {}", fileName, demandeId);
            return fileContent;
        } catch (IOException e) {
            log.error("Erreur lors de la lecture du fichier {} pour la demande {}", fileName, demandeId, e);
            throw new BusinessException("Erreur lors du téléchargement du fichier: " + e.getMessage());
        }
    }

    private Path resolvePieceJointePath(Long demandeId, String fileName) {
        return pieceJointeRepository.findByDemandeId(demandeId).stream()
                .filter(pieceJointe -> isRequestedPieceJointe(pieceJointe, fileName))
                .map(PieceJointe::getCheminFichier)
                .filter(Objects::nonNull)
                .map(Paths::get)
                .findFirst()
                .orElseGet(() -> resolveLegacyUploadPath(demandeId, fileName));
    }

    private boolean isRequestedPieceJointe(PieceJointe pieceJointe, String fileName) {
        if (pieceJointe == null || fileName == null) {
            return false;
        }

        return fileName.equals(pieceJointe.getNomFichier())
                || fileName.equals(pieceJointe.getCheminFichier())
                || fileName.equals(extractFileName(pieceJointe.getCheminFichier()));
    }

    private String extractFileName(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }

        String normalizedPath = path.replace('\\', '/');
        int lastSeparator = normalizedPath.lastIndexOf('/');
        return lastSeparator >= 0 ? normalizedPath.substring(lastSeparator + 1) : normalizedPath;
    }

    private Path resolveLegacyUploadPath(Long demandeId, String fileName) {
        Path uploadPath = Paths.get("uploads", "demandes", String.valueOf(demandeId)).normalize();
        Path filePath = uploadPath.resolve(fileName).normalize();

        if (!filePath.startsWith(uploadPath)) {
            throw new BusinessException("Nom de fichier invalide");
        }

        return filePath;
    }
}
