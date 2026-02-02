package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.affectation.AffectationRequest;
import com.banque.abc.tpe.dto.demande.DemandeRequest;
import com.banque.abc.tpe.dto.demande.DemandeResponse;
import com.banque.abc.tpe.dto.demande.ValiderDemandeRequest;
import com.banque.abc.tpe.entity.Commercant;
import com.banque.abc.tpe.entity.Demande;
import com.banque.abc.tpe.entity.User;
import com.banque.abc.tpe.entity.enums.StatutDemande;
import com.banque.abc.tpe.exception.BusinessException;
import com.banque.abc.tpe.exception.ResourceNotFoundException;
import com.banque.abc.tpe.repository.CommercantRepository;
import com.banque.abc.tpe.repository.DemandeRepository;
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

import java.time.LocalDateTime;
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
    private final NotificationService notificationService;
    private final AffectationService affectationService;

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
                // Champs agence (TPE Physique)
                .raisonSociale(request.getRaisonSociale())
                .activite(request.getActivite())
                .numeroCompte(request.getNumeroCompte())
                .adresse(request.getAdresse())
                .codePostal(request.getCodePostal())
                .codeAgence(request.getCodeAgence())
                .telephone(request.getTelephone())
                .emailNotification(request.getEmailNotification())
                // Champs E-commerce
                .localite(request.getLocalite())
                .rib(request.getRib())
                .webmaster(request.getWebmaster())
                .contactTechnique(request.getContactTechnique())
                .urlSiteMarchand(request.getUrlSiteMarchand())
                .build();

        Demande savedDemande = demandeRepository.save(demande);

        auditService.logAction("CREATE", "Demande", savedDemande.getId().toString(),
                "Demande créée: " + savedDemande.getReference(), "SUCCESS");

        // Notifier la Monétique
        notificationService.notifierNouvelleDemande(savedDemande);

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
    public DemandeResponse validerDemande(Long id, ValiderDemandeRequest request) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée avec l'ID: " + id));

        if (demande.getStatut() != StatutDemande.NOUVELLE && demande.getStatut() != StatutDemande.EN_COURS) {
            throw new BusinessException("Cette demande ne peut plus être validée");
        }

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User valideur = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        demande.setValideur(valideur);
        demande.setDateValidation(LocalDateTime.now());
        demande.setCommentaireValidation(request.getCommentaire());
        
        // Champs de validation Monétique
        demande.setMcc(request.getMcc());
        demande.setTauxCommission(request.getTauxCommission());
        demande.setTauxCommissionInter(request.getTauxCommissionInter());
        demande.setLoyer(request.getLoyer());
        demande.setSerieTpe(request.getSerieTpe());
        demande.setNumeroTerminal(request.getNumeroTerminal());
        demande.setValueDate(request.getValueDate());

        if (request.getApprouver()) {
            demande.setStatut(StatutDemande.VALIDEE_MONETIQUE);
            notificationService.notifierDemandeValidee(demande);
        } else {
            demande.setStatut(StatutDemande.REJETEE);
            demande.setDateCloture(LocalDateTime.now());
            notificationService.notifierDemandeRejetee(demande);
        }

        Demande updatedDemande = demandeRepository.save(demande);

        // Affectation automatique si approuvée
        if (request.getApprouver()) {
            try {
                AffectationRequest affectationRequest = new AffectationRequest();
                affectationRequest.setDemandeId(updatedDemande.getId());
                affectationRequest.setCommentaire("Affectation automatique suite à validation monétique");
                
                affectationService.affecterTPE(affectationRequest);
                log.info("TPE affecté automatiquement pour la demande {}", updatedDemande.getReference());
            } catch (Exception e) {
                log.error("Erreur lors de l'affectation automatique du TPE pour la demande {}", 
                        updatedDemande.getReference(), e);
                // Ne pas bloquer la validation si l'affectation échoue
                // L'affectation pourra être faite manuellement plus tard
            }
        }

        auditService.logAction("VALIDATE", "Demande", updatedDemande.getId().toString(),
                "Demande " + (request.getApprouver() ? "validée" : "rejetée"), "SUCCESS");

        return mapToResponse(updatedDemande);
    }

    @Transactional
    public DemandeResponse rejeterDemande(Long id, String commentaire) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée avec l'ID: " + id));

        if (demande.getStatut() != StatutDemande.NOUVELLE && demande.getStatut() != StatutDemande.EN_COURS) {
            throw new BusinessException("Cette demande ne peut plus être rejetée");
        }

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User valideur = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        demande.setValideur(valideur);
        demande.setDateValidation(LocalDateTime.now());
        demande.setCommentaireValidation(commentaire);
        demande.setStatut(StatutDemande.REJETEE);
        demande.setDateCloture(LocalDateTime.now());

        Demande updatedDemande = demandeRepository.save(demande);

        notificationService.notifierDemandeRejetee(demande);

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
        
        response.setDemandeurId(demande.getDemandeur().getId());
        response.setDemandeurNom(demande.getDemandeur().getNom() + " " + demande.getDemandeur().getPrenom());
        
        if (demande.getValideur() != null) {
            response.setValideurId(demande.getValideur().getId());
            response.setValideurNom(demande.getValideur().getNom() + " " + demande.getValideur().getPrenom());
        }
        
        return response;
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
                .email(request.getEmailNotification())
                .statut(com.banque.abc.tpe.entity.enums.StatutCommercant.ACTIF)
                .typeCommerce(request.getTypeDemande())
                // Champs E-commerce
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
}
