package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.taux.TauxRequest;
import com.banque.abc.tpe.dto.taux.TauxResponse;
import com.banque.abc.tpe.dto.taux.ValiderTauxRequest;
import com.banque.abc.tpe.entity.Commercant;
import com.banque.abc.tpe.entity.Taux;
import com.banque.abc.tpe.entity.User;
import com.banque.abc.tpe.entity.enums.RoleType;
import com.banque.abc.tpe.entity.enums.StatutTaux;
import com.banque.abc.tpe.exception.BusinessException;
import com.banque.abc.tpe.exception.ResourceNotFoundException;
import com.banque.abc.tpe.exception.UnauthorizedException;
import com.banque.abc.tpe.repository.CommercantRepository;
import com.banque.abc.tpe.repository.TauxRepository;
import com.banque.abc.tpe.repository.UserRepository;
import com.banque.abc.tpe.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TauxService {

    private final TauxRepository tauxRepository;
    private final CommercantRepository commercantRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final AuditService auditService;

    @Transactional
    public TauxResponse createTaux(TauxRequest request) {
        // Vérifier que l'utilisateur a le rôle INPUTER
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        
        boolean isInputer = userPrincipal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(RoleType.ROLE_INPUTER.name()));
        
        if (!isInputer) {
            throw new UnauthorizedException("Seuls les Inputers peuvent saisir des taux");
        }

        Commercant commercant = commercantRepository.findById(request.getCommercantId())
                .orElseThrow(() -> new ResourceNotFoundException("Commerçant non trouvé"));

        User inputer = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Récupérer les anciens taux actifs
        Optional<Taux> tauxActifOpt = tauxRepository.findActiveTauxByCommercant(commercant.getId());
        
        Taux taux = Taux.builder()
                .commercant(commercant)
                .nouveauTauxCommission(request.getNouveauTauxCommission())
                .nouveauTauxCommissionInter(request.getNouveauTauxCommissionInter())
                .inputer(inputer)
                .dateSaisie(LocalDateTime.now())
                .statut(StatutTaux.BROUILLON)
                .commentaire(request.getCommentaire())
                .actif(false)
                .build();

        if (tauxActifOpt.isPresent()) {
            Taux tauxActif = tauxActifOpt.get();
            taux.setAncienTauxCommission(tauxActif.getNouveauTauxCommission());
            taux.setAncienTauxCommissionInter(tauxActif.getNouveauTauxCommissionInter());
        }

        Taux savedTaux = tauxRepository.save(taux);

        auditService.logAction("CREATE", "Taux", savedTaux.getId().toString(),
                "Nouveau taux créé pour " + commercant.getRaisonSociale(), "SUCCESS");

        return mapToResponse(savedTaux);
    }

    @Transactional
    public TauxResponse soumettreValidation(Long id) {
        Taux taux = tauxRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Taux non trouvé"));

        if (taux.getStatut() != StatutTaux.BROUILLON) {
            throw new BusinessException("Seuls les taux en brouillon peuvent être soumis à validation");
        }

        // Vérifier que l'utilisateur est bien l'inputer
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        
        if (!taux.getInputer().getId().equals(userPrincipal.getId())) {
            throw new UnauthorizedException("Vous ne pouvez soumettre que vos propres saisies");
        }

        taux.setStatut(StatutTaux.EN_ATTENTE_VALIDATION);
        Taux updatedTaux = tauxRepository.save(taux);

        auditService.logAction("SUBMIT", "Taux", updatedTaux.getId().toString(),
                "Taux soumis à validation", "SUCCESS");

        return mapToResponse(updatedTaux);
    }

    @Transactional
    public TauxResponse validerTaux(Long id, ValiderTauxRequest request) {
        Taux taux = tauxRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Taux non trouvé"));

        if (taux.getStatut() != StatutTaux.EN_ATTENTE_VALIDATION) {
            throw new BusinessException("Ce taux ne peut pas être validé");
        }

        // Vérifier que l'utilisateur a le rôle AUTHORIZER
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        
        boolean isAuthorizer = userPrincipal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(RoleType.ROLE_AUTHORIZER.name()));
        
        if (!isAuthorizer) {
            throw new UnauthorizedException("Seuls les Authorizers peuvent valider des taux");
        }

        // RÈGLE MÉTIER CRITIQUE: Inputer ≠ Authorizer
        if (taux.getInputer().getId().equals(userPrincipal.getId())) {
            throw new BusinessException("Vous ne pouvez pas valider vos propres saisies (Règle 4 yeux)");
        }

        User authorizer = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        taux.setAuthorizer(authorizer);
        taux.setDateValidation(LocalDateTime.now());

        if (request.getApprouver()) {
            taux.setStatut(StatutTaux.VALIDE);
            taux.setActif(true);
            taux.setDateApplication(LocalDateTime.now());

            // Désactiver les anciens taux
            List<Taux> anciensTaux = tauxRepository.findByCommercantId(taux.getCommercant().getId());
            anciensTaux.forEach(t -> {
                if (t.getActif() && !t.getId().equals(taux.getId())) {
                    t.setActif(false);
                    tauxRepository.save(t);
                }
            });

            auditService.logAction("VALIDATE", "Taux", taux.getId().toString(),
                    String.format("Taux validé: Commission %.2f%%, Commission Inter %.2f%%",
                            taux.getNouveauTauxCommission(), taux.getNouveauTauxCommissionInter()),
                    "SUCCESS");
        } else {
            taux.setStatut(StatutTaux.REJETE);
            taux.setMotifRejet(request.getMotifRejet());

            auditService.logAction("REJECT", "Taux", taux.getId().toString(),
                    "Taux rejeté: " + request.getMotifRejet(), "SUCCESS");
        }

        Taux updatedTaux = tauxRepository.save(taux);
        return mapToResponse(updatedTaux);
    }

    @Transactional(readOnly = true)
    public TauxResponse getTauxById(Long id) {
        Taux taux = tauxRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Taux non trouvé"));
        return mapToResponse(taux);
    }

    @Transactional(readOnly = true)
    public List<TauxResponse> getTauxEnAttenteValidation() {
        return tauxRepository.findTauxEnAttenteValidation().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TauxResponse> getTauxByCommercant(Long commercantId) {
        return tauxRepository.findByCommercantId(commercantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TauxResponse mapToResponse(Taux taux) {
        TauxResponse response = modelMapper.map(taux, TauxResponse.class);
        response.setCommercantId(taux.getCommercant().getId());
        response.setCommercantNom(taux.getCommercant().getRaisonSociale());
        response.setInputerNom(taux.getInputer().getUsername());
        
        if (taux.getAuthorizer() != null) {
            response.setAuthorizerNom(taux.getAuthorizer().getUsername());
        }
        
        return response;
    }
}
