package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.affectation.AffectationRequest;
import com.banque.abc.tpe.dto.affectation.AffectationResponse;
import com.banque.abc.tpe.entity.*;
import com.banque.abc.tpe.entity.enums.StatutDemande;
import com.banque.abc.tpe.entity.enums.StatutTPE;
import com.banque.abc.tpe.exception.BusinessException;
import com.banque.abc.tpe.exception.ResourceNotFoundException;
import com.banque.abc.tpe.repository.*;
import com.banque.abc.tpe.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AffectationService {

    private final AffectationRepository affectationRepository;
    private final TPERepository tpeRepository;
    private final DemandeRepository demandeRepository;
    private final CommercantRepository commercantRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final ModelMapper modelMapper;

    @Transactional
    public AffectationResponse affecterTPE(AffectationRequest request) {
        // Récupérer la demande
        Demande demande = demandeRepository.findById(request.getDemandeId())
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée"));

        // Vérifier que la demande est validée
        if (demande.getStatut() != StatutDemande.VALIDEE_MONETIQUE) {
            throw new BusinessException("La demande doit être validée par le monétique avant l'affectation");
        }

        // Vérifier qu'il n'y a pas déjà une affectation active
        if (affectationRepository.existsByDemandeIdAndActifTrue(demande.getId())) {
            throw new BusinessException("Cette demande a déjà une affectation active");
        }

        TPE tpe;
        
        // Cas 1: TPE spécifique fourni
        if (request.getTpeId() != null) {
            tpe = tpeRepository.findById(request.getTpeId())
                    .orElseThrow(() -> new ResourceNotFoundException("TPE non trouvé"));
            
            if (tpe.getStatut() != StatutTPE.DISPONIBLE) {
                throw new BusinessException("Ce TPE n'est pas disponible");
            }
        }
        // Cas 2: Créer un nouveau TPE avec les infos de la demande
        else if (demande.getNumeroTerminal() != null) {
            // Vérifier si ce TPE existe déjà
            tpe = tpeRepository.findByNumeroTerminal(demande.getNumeroTerminal())
                    .orElseGet(() -> {
                        String numeroSerie = demande.getSerieTpe() != null && !demande.getSerieTpe().isBlank()
                                ? demande.getSerieTpe()
                                : "AUTO-" + demande.getNumeroTerminal();
                        TPE nouveauTPE = TPE.builder()
                                .numeroTerminal(demande.getNumeroTerminal())
                                .numeroSerie(numeroSerie)
                                .marque(request.getMarque() != null ? request.getMarque() : "Generic")
                                .modele(request.getModele() != null ? request.getModele() : "Standard")
                                .typeTPE(demande.getTypeDemande().name())
                                .statut(StatutTPE.DISPONIBLE)
                                .dateAcquisition(LocalDate.now())
                                .build();
                        return tpeRepository.save(nouveauTPE);
                    });
        }
        // Cas 3: Chercher un TPE disponible automatiquement
        else {
            List<TPE> tpesDisponibles = tpeRepository.findByStatutAndTypeTPE(
                    StatutTPE.DISPONIBLE, 
                    demande.getTypeDemande().name()
            );
            
            if (tpesDisponibles.isEmpty()) {
                throw new BusinessException("Aucun TPE disponible pour ce type de demande");
            }
            
            tpe = tpesDisponibles.get(0);
        }

        // Mettre à jour le statut du TPE
        tpe.setCommercant(demande.getCommercant());
        tpe.setStatut(StatutTPE.AFFECTE);
        tpeRepository.save(tpe);

        // Récupérer l'utilisateur connecté
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User affectePar = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Créer l'affectation
        Affectation affectation = Affectation.builder()
                .tpe(tpe)
                .commercant(demande.getCommercant())
                .demande(demande)
                .dateAffectation(LocalDate.now())
                .actif(true)
                .affectePar(affectePar)
                .commentaire(request.getCommentaire())
                .build();

        Affectation savedAffectation = affectationRepository.save(affectation);

        // Mettre à jour le statut de la demande
        demande.setStatut(StatutDemande.AFFECTEE);
        demande.setDateCloture(LocalDateTime.now());
        demandeRepository.save(demande);

        // Audit
        auditService.logAction("CREATE", "Affectation", savedAffectation.getId().toString(),
                "TPE " + tpe.getNumeroTerminal() + " affecté au commerçant " + demande.getCommercant().getRaisonSociale(),
                "SUCCESS");

        return mapToResponse(savedAffectation);
    }

    @Transactional(readOnly = true)
    public Page<AffectationResponse> getAllAffectations(Pageable pageable) {
        return affectationRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<AffectationResponse> getAffectationsByCommercant(Long commercantId) {
        return affectationRepository.findByCommercantIdOrderByDateAffectationDesc(commercantId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AffectationResponse> getAffectationsActives() {
        return affectationRepository.findByActifTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AffectationResponse getAffectationById(Long id) {
        Affectation affectation = affectationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation non trouvée"));
        return mapToResponse(affectation);
    }

    @Transactional
    public AffectationResponse mettreEnService(Long id, LocalDate dateMiseEnService) {
        Affectation affectation = affectationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation non trouvée"));

        affectation.setDateMiseEnService(dateMiseEnService);
        Affectation updated = affectationRepository.save(affectation);

        auditService.logAction("UPDATE", "Affectation", id.toString(),
                "TPE mis en service", "SUCCESS");

        return mapToResponse(updated);
    }

    @Transactional
    public void desaffecterTPE(Long id, String motif) {
        Affectation affectation = affectationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation non trouvée"));

        affectation.setActif(false);
        affectation.setDateFin(LocalDate.now());
        affectation.setCommentaire(affectation.getCommentaire() + "\nMotif de désaffectation: " + motif);
        affectationRepository.save(affectation);

        // Remettre le TPE en disponible (ou en maintenance selon le motif)
        TPE tpe = affectation.getTpe();
        tpe.setCommercant(null);
        tpe.setStatut(StatutTPE.DISPONIBLE);
        tpeRepository.save(tpe);

        auditService.logAction("UPDATE", "Affectation", id.toString(),
                "TPE désaffecté: " + motif, "SUCCESS");
    }

    private AffectationResponse mapToResponse(Affectation affectation) {
        AffectationResponse response = modelMapper.map(affectation, AffectationResponse.class);
        
        if (affectation.getTpe() != null) {
            response.setTpeId(affectation.getTpe().getId());
            response.setNumeroTerminal(affectation.getTpe().getNumeroTerminal());
            response.setNumeroSerie(affectation.getTpe().getNumeroSerie());
        }
        
        if (affectation.getCommercant() != null) {
            response.setCommercantId(affectation.getCommercant().getId());
            response.setCommercantNom(affectation.getCommercant().getRaisonSociale());
        }
        
        if (affectation.getDemande() != null) {
            response.setDemandeId(affectation.getDemande().getId());
            response.setDemandeReference(affectation.getDemande().getReference());
        }
        
        if (affectation.getAffectePar() != null) {
            response.setAffecteParId(affectation.getAffectePar().getId());
            response.setAffecteParNom(affectation.getAffectePar().getNom() + " " + affectation.getAffectePar().getPrenom());
        }
        
        return response;
    }
}
