package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.commercant.CommercantRequest;
import com.banque.abc.tpe.dto.commercant.CommercantResponse;
import com.banque.abc.tpe.entity.Commercant;
import com.banque.abc.tpe.entity.enums.StatutCommercant;
import com.banque.abc.tpe.exception.DuplicateResourceException;
import com.banque.abc.tpe.exception.ResourceNotFoundException;
import com.banque.abc.tpe.repository.CommercantRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommercantService {

    private final CommercantRepository commercantRepository;
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

        Commercant commercant = modelMapper.map(request, Commercant.class);
        commercant.setStatut(StatutCommercant.ACTIF);

        Commercant savedCommercant = commercantRepository.save(commercant);

        auditService.logAction("CREATE", "Commercant", savedCommercant.getId().toString(),
                "Commerçant créé: " + savedCommercant.getRaisonSociale(), "SUCCESS");

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

        if (request.getEmail() != null && !commercant.getEmail().equals(request.getEmail()) &&
                commercantRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Un commerçant avec cet email existe déjà");
        }

        if (!commercant.getNumeroCompte().equals(request.getNumeroCompte()) &&
                commercantRepository.existsByNumeroCompte(request.getNumeroCompte())) {
            throw new DuplicateResourceException("Un commerçant avec ce numéro de compte existe déjà");
        }

        modelMapper.map(request, commercant);
        Commercant updatedCommercant = commercantRepository.save(commercant);

        auditService.logAction("UPDATE", "Commercant", updatedCommercant.getId().toString(),
                "Commerçant mis à jour: " + updatedCommercant.getRaisonSociale(), "SUCCESS");

        return mapToResponse(updatedCommercant);
    }

    @Transactional
    public void updateStatut(Long id, StatutCommercant statut) {
        Commercant commercant = commercantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commerçant non trouvé avec l'ID: " + id));

        StatutCommercant ancienStatut = commercant.getStatut();
        commercant.setStatut(statut);
        commercantRepository.save(commercant);

        auditService.logAction("UPDATE_STATUS", "Commercant", commercant.getId().toString(),
                String.format("Statut changé de %s à %s", ancienStatut, statut), "SUCCESS");
    }

    @Transactional
    public void deleteCommercant(Long id) {
        Commercant commercant = commercantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commerçant non trouvé avec l'ID: " + id));

        commercantRepository.delete(commercant);

        auditService.logAction("DELETE", "Commercant", id.toString(),
                "Commerçant supprimé: " + commercant.getRaisonSociale(), "SUCCESS");
    }

    private CommercantResponse mapToResponse(Commercant commercant) {
        CommercantResponse response = modelMapper.map(commercant, CommercantResponse.class);
        response.setNombreTPEs(commercant.getTpes().size());
        return response;
    }
}
