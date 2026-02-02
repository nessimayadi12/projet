package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.panne.PanneResponse;
import com.banque.abc.tpe.entity.Panne;
import com.banque.abc.tpe.entity.TPE;
import com.banque.abc.tpe.entity.User;
import com.banque.abc.tpe.entity.enums.StatutPanne;
import com.banque.abc.tpe.entity.enums.StatutTPE;
import com.banque.abc.tpe.repository.PanneRepository;
import com.banque.abc.tpe.repository.TPERepository;
import com.banque.abc.tpe.repository.UserRepository;
import com.banque.abc.tpe.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PanneService {

    private final PanneRepository panneRepository;
    private final TPERepository tpeRepository;
    private final UserRepository userRepository;

    public List<Panne> getAllPannes() {
        return panneRepository.findAll();
    }

    public Optional<Panne> getPanneById(Long id) {
        return panneRepository.findById(id);
    }

    public List<Panne> getPannesByStatut(StatutPanne statut) {
        return panneRepository.findByStatut(statut);
    }

    public List<Panne> getPannesByTPE(Long tpeId) {
        return panneRepository.findByTpeId(tpeId);
    }

    public List<Panne> getPannesByTechnicien(Long technicienId) {
        return panneRepository.findByTechnicienId(technicienId);
    }

    public Panne createPanne(Panne panne) {
        // Récupérer l'utilisateur connecté
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User declarant = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Charger le TPE complet avant toute opération
        Long tpeId = panne.getTpe() != null ? panne.getTpe().getId() : null;
        if (tpeId == null) {
            throw new RuntimeException("TPE ID est obligatoire");
        }
        
        TPE tpe = tpeRepository.findById(tpeId)
                .orElseThrow(() -> new RuntimeException("TPE non trouvé avec l'ID: " + tpeId));
        
        // Créer une nouvelle panne
        Panne nouvellePanne = new Panne();
        nouvellePanne.setReference("PAN" + System.currentTimeMillis());
        nouvellePanne.setStatut(StatutPanne.DECLAREE);
        nouvellePanne.setDateDeclaration(LocalDateTime.now());
        nouvellePanne.setDeclarant(declarant);
        nouvellePanne.setTpe(tpe);
        nouvellePanne.setDescription(panne.getDescription());
        nouvellePanne.setSousGarantie(false);
        
        // Sauvegarder la panne d'abord
        Panne panneSaved = panneRepository.save(nouvellePanne);
        
        // Ensuite mettre à jour le statut du TPE
        tpe.setStatut(StatutTPE.EN_PANNE);
        tpeRepository.save(tpe);
        
        return panneSaved;
    }

    public Panne updatePanne(Long id, Panne panneDetails) {
        Panne panne = panneRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Panne non trouvée"));
        
        panne.setDescription(panneDetails.getDescription());
        panne.setDiagnostic(panneDetails.getDiagnostic());
        panne.setActionCorrective(panneDetails.getActionCorrective());
        panne.setCommentaireTechnicien(panneDetails.getCommentaireTechnicien());
        panne.setCoutReparation(panneDetails.getCoutReparation());
        panne.setSousGarantie(panneDetails.getSousGarantie());
        
        return panneRepository.save(panne);
    }

    public Panne changeStatut(Long id, StatutPanne nouveauStatut) {
        Panne panne = panneRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Panne non trouvée"));
        
        panne.setStatut(nouveauStatut);
        
        switch (nouveauStatut) {
            case DIAGNOSTIQUEE:
                panne.setDateDiagnostic(LocalDateTime.now());
                break;
            case EN_REPARATION:
                panne.setDateReparation(LocalDateTime.now());
                break;
            case REPAREE:
            case TESTEE:
                panne.setDateResolution(LocalDateTime.now());
                // Remettre le TPE en service
                TPE tpe = panne.getTpe();
                if (tpe != null && nouveauStatut == StatutPanne.TESTEE) {
                    tpe.setStatut(StatutTPE.DISPONIBLE);
                    tpeRepository.save(tpe);
                }
                break;
            case IRRECUPERABLE:
                panne.setDateResolution(LocalDateTime.now());
                TPE tpeIrrec = panne.getTpe();
                if (tpeIrrec != null) {
                    tpeIrrec.setStatut(StatutTPE.HORS_SERVICE);
                    tpeRepository.save(tpeIrrec);
                }
                break;
        }
        
        return panneRepository.save(panne);
    }

    public Panne assignerTechnicien(Long panneId, Long technicienId) {
        Panne panne = panneRepository.findById(panneId)
            .orElseThrow(() -> new RuntimeException("Panne non trouvée"));
        
        User technicien = userRepository.findById(technicienId)
            .orElseThrow(() -> new RuntimeException("Technicien non trouvé"));
        
        panne.setTechnicien(technicien);
        return panneRepository.save(panne);
    }

    public void deletePanne(Long id) {
        Panne panne = panneRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Panne non trouvée"));
        panneRepository.delete(panne);
    }

    public Panne diagnostiquer(Long id, String diagnostic) {
        Panne panne = panneRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Panne non trouvée"));
        
        panne.setDiagnostic(diagnostic);
        panne.setStatut(StatutPanne.DIAGNOSTIQUEE);
        panne.setDateDiagnostic(LocalDateTime.now());
        
        return panneRepository.save(panne);
    }

    public Panne marquerEnReparation(Long id) {
        return changeStatut(id, StatutPanne.EN_REPARATION);
    }

    public Panne marquerReparee(Long id, String solution) {
        Panne panne = panneRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Panne non trouvée"));
        
        panne.setActionCorrective(solution);
        panne.setStatut(StatutPanne.REPAREE);
        panne.setDateResolution(LocalDateTime.now());
        
        return panneRepository.save(panne);
    }

    public Panne resoudrePanne(Long id, String solution) {
        Panne panne = panneRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Panne non trouvée"));
        
        panne.setActionCorrective(solution);
        panne.setStatut(StatutPanne.TESTEE);
        panne.setDateResolution(LocalDateTime.now());
        
        // Remettre le TPE en service
        TPE tpe = panne.getTpe();
        if (tpe != null) {
            tpe.setStatut(StatutTPE.DISPONIBLE);
            tpeRepository.save(tpe);
        }
        
        return panneRepository.save(panne);
    }

    public Panne testerPanne(Long id, boolean resultat) {
        Panne panne = panneRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Panne non trouvée"));
        
        if (resultat) {
            panne.setStatut(StatutPanne.TESTEE);
            TPE tpe = panne.getTpe();
            if (tpe != null) {
                tpe.setStatut(StatutTPE.DISPONIBLE);
                tpeRepository.save(tpe);
            }
        } else {
            panne.setStatut(StatutPanne.EN_REPARATION);
        }
        
        return panneRepository.save(panne);
    }

    public Panne affecterTPERemplacement(Long panneId, Long tpeRemplacementId) {
        Panne panne = panneRepository.findById(panneId)
            .orElseThrow(() -> new RuntimeException("Panne non trouvée"));
        
        TPE tpeRemplacement = tpeRepository.findById(tpeRemplacementId)
            .orElseThrow(() -> new RuntimeException("TPE de remplacement non trouvé"));
        
        panne.setTpeRemplacement(tpeRemplacement);
        return panneRepository.save(panne);
    }

    public List<Panne> getPannesByPeriode(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return panneRepository.findAll().stream()
            .filter(p -> p.getDateDeclaration().isAfter(dateDebut) 
                      && p.getDateDeclaration().isBefore(dateFin))
            .toList();
    }

    /**
     * Mapper une entité Panne vers un DTO PanneResponse
     */
    public PanneResponse mapToResponse(Panne panne) {
        PanneResponse response = new PanneResponse();
        response.setId(panne.getId());
        response.setReference(panne.getReference());
        response.setStatut(panne.getStatut());
        response.setDescription(panne.getDescription());
        response.setDateDeclaration(panne.getDateDeclaration());
        response.setDateDiagnostic(panne.getDateDiagnostic());
        response.setDateReparation(panne.getDateReparation());
        response.setDateResolution(panne.getDateResolution());
        response.setDiagnostic(panne.getDiagnostic());
        response.setActionCorrective(panne.getActionCorrective());
        response.setCommentaireTechnicien(panne.getCommentaireTechnicien());
        response.setCoutReparation(panne.getCoutReparation());
        response.setSousGarantie(panne.getSousGarantie());
        response.setCreatedDate(panne.getCreatedDate());
        
        if (panne.getTpe() != null) {
            response.setTpeId(panne.getTpe().getId());
            response.setTpeNumeroSerie(panne.getTpe().getNumeroSerie());
        }
        
        if (panne.getDeclarant() != null) {
            response.setDeclarantNom(panne.getDeclarant().getNom() + " " + panne.getDeclarant().getPrenom());
        }
        
        if (panne.getTechnicien() != null) {
            response.setTechnicienNom(panne.getTechnicien().getNom() + " " + panne.getTechnicien().getPrenom());
        }
        
        if (panne.getTpeRemplacement() != null) {
            response.setTpeRemplacementId(panne.getTpeRemplacement().getId());
            response.setTpeRemplacementNumero(panne.getTpeRemplacement().getNumeroTerminal());
        }
        
        return response;
    }
    
    /**
     * Méthodes avec retour DTO
     */
    public List<PanneResponse> getAllPannesDTO() {
        return panneRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public PanneResponse getPanneDTOById(Long id) {
        Panne panne = panneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Panne non trouvée"));
        return mapToResponse(panne);
    }
    
    public List<PanneResponse> getPannesDTOByStatut(StatutPanne statut) {
        return panneRepository.findByStatut(statut).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}

