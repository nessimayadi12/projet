package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.commercant.CommercantRequest;
import com.banque.abc.tpe.dto.commercant.CommercantResponse;
import com.banque.abc.tpe.dto.audit.AuditEvent;
import com.banque.abc.tpe.entity.Commercant;
import com.banque.abc.tpe.entity.enums.StatutCommercant;
import com.banque.abc.tpe.exception.DuplicateResourceException;
import com.banque.abc.tpe.exception.ResourceNotFoundException;
import com.banque.abc.tpe.repository.AffectationRepository;
import com.banque.abc.tpe.repository.CommercantRepository;
import com.banque.abc.tpe.repository.DemandeRepository;
import com.banque.abc.tpe.repository.TPERepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommercantService {

    private final CommercantRepository commercantRepository;
    private final TPERepository tpeRepository;
    private final AffectationRepository affectationRepository;
    private final DemandeRepository demandeRepository;
    private final ModelMapper modelMapper;
    private final AuditService auditService;

    @Transactional
    public CommercantResponse createCommercant(CommercantRequest request) {
        if (request.getEmail() != null && commercantRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Un commerçant avec cet email existe déjà");
        }

        if (commercantRepository.existsByNumeroCompte(request.getNumeroCompte())) {
            throw new DuplicateResourceException("Un commerçant avec ce numéro de compte existe déjà");
        }

        Commercant commercant = new Commercant();
        applyMerchantInformation(request, commercant);
        commercant.setStatut(StatutCommercant.ACTIF);

        Commercant savedCommercant = commercantRepository.save(commercant);

        auditService.logCreation("Commercant", savedCommercant.getId().toString(), savedCommercant.getRaisonSociale(),
                snapshot(savedCommercant), "Commercant cree: " + savedCommercant.getRaisonSociale());

        return mapToResponse(savedCommercant);
    }

    @Transactional(readOnly = true)
    public CommercantResponse getCommercantById(Long id) {
        Commercant commercant = commercantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commerçant non trouvé avec l'ID: " + id));
        return mapToResponse(commercant);
    }

    @Transactional(readOnly = true)
    public Page<CommercantResponse> getAllCommercants(Pageable pageable) {
        return commercantRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<CommercantResponse> getAllCommercantsList() {
        return commercantRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommercantResponse> searchCommercants(String search) {
        return commercantRepository.searchByRaisonSocialeOrEmail(search).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommercantResponse> getCommercantsByCodeAgence(String codeAgence) {
        return commercantRepository.findByCodeAgence(codeAgence).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommercantResponse updateCommercant(Long id, CommercantRequest request) {
        Commercant commercant = commercantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commerçant non trouvé avec l'ID: " + id));
        Map<String, Object> oldValues = snapshot(commercant);

        if (request.getEmail() != null && !Objects.equals(commercant.getEmail(), request.getEmail()) &&
                commercantRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Un commerçant avec cet email existe déjà");
        }

        if (!commercant.getNumeroCompte().equals(request.getNumeroCompte()) &&
                commercantRepository.existsByNumeroCompte(request.getNumeroCompte())) {
            throw new DuplicateResourceException("Un commerçant avec ce numéro de compte existe déjà");
        }

        applyMerchantInformation(request, commercant);
        Commercant updatedCommercant = commercantRepository.save(commercant);

        auditService.logUpdate("Commercant", updatedCommercant.getId().toString(), updatedCommercant.getRaisonSociale(),
                oldValues, snapshot(updatedCommercant),
                "Commercant mis a jour: " + updatedCommercant.getRaisonSociale());

        return mapToResponse(updatedCommercant);
    }

    @Transactional
    public void updateStatut(Long id, StatutCommercant statut) {
        Commercant commercant = commercantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commerçant non trouvé avec l'ID: " + id));

        StatutCommercant ancienStatut = commercant.getStatut();
        commercant.setStatut(statut);
        commercantRepository.save(commercant);

        auditService.logStatusChange("Commercant", commercant.getId().toString(), commercant.getRaisonSociale(),
                ancienStatut, statut, String.format("Statut change de %s a %s", ancienStatut, statut));
    }

    @Transactional
    public void deleteCommercant(Long id) {
        Commercant commercant = commercantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commerçant non trouvé avec l'ID: " + id));

        commercantRepository.delete(commercant);

        auditService.logBusinessEvent(AuditEvent.builder()
                .action("DELETE")
                .actionLabel("Suppression")
                .moduleName("Commercant")
                .entityType("Commercant")
                .entityId(id.toString())
                .entityReference(commercant.getRaisonSociale())
                .details("Commercant supprime: " + commercant.getRaisonSociale())
                .oldValues(snapshot(commercant))
                .statut("SUCCESS")
                .riskLevel("CRITICAL")
                .build());
    }

    private CommercantResponse mapToResponse(Commercant commercant) {
        CommercantResponse response = modelMapper.map(commercant, CommercantResponse.class);
        response.setNombreTPEs(resolveNombreTpes(commercant.getId()));
        return response;
    }

    private Integer resolveNombreTpes(Long commercantId) {
        if (commercantId == null) {
            return 0;
        }

        Long directCount = tpeRepository.countByCommercantId(commercantId);
        Long activeAffectationCount = affectationRepository.countActiveAffectationsByCommercant(commercantId);
        Long demandeTpeCount = demandeRepository.countDistinctTpeReferencesByCommercantId(commercantId);
        long count = Math.max(directCount != null ? directCount : 0L, activeAffectationCount != null ? activeAffectationCount : 0L);
        count = Math.max(count, demandeTpeCount != null ? demandeTpeCount : 0L);

        return Math.toIntExact(count);
    }

    private void applyMerchantInformation(CommercantRequest request, Commercant commercant) {
        commercant.setRaisonSociale(request.getRaisonSociale());
        commercant.setActivite(request.getActivite());
        commercant.setNumeroCompte(request.getNumeroCompte());
        commercant.setAdresse(request.getAdresse());
        commercant.setLocalite(request.getLocalite());
        commercant.setCodePostal(request.getCodePostal());
        commercant.setCodeAgence(request.getCodeAgence());
        commercant.setTelephone(request.getTelephone());
        commercant.setEmail(request.getEmail());
    }

    private Map<String, Object> snapshot(Commercant commercant) {
        return auditService.values(
                "raisonSociale", commercant.getRaisonSociale(),
                "activite", commercant.getActivite(),
                "numeroCompte", commercant.getNumeroCompte(),
                "adresse", commercant.getAdresse(),
                "localite", commercant.getLocalite(),
                "codePostal", commercant.getCodePostal(),
                "codeAgence", commercant.getCodeAgence(),
                "telephone", commercant.getTelephone(),
                "email", commercant.getEmail(),
                "statut", commercant.getStatut(),
                "loyer", commercant.getLoyer(),
                "typeCommerce", commercant.getTypeCommerce(),
                "urlSiteMarchand", commercant.getUrlSiteMarchand(),
                "webhookUrl", commercant.getWebhookUrl(),
                "webmaster", commercant.getWebmaster(),
                "contactTechnique", commercant.getContactTechnique(),
                "typeCartesAcceptees", commercant.getTypeCartesAcceptees(),
                "modeTest", commercant.getModeTest()
        );
    }
}
