package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.tpe.TPERequest;
import com.banque.abc.tpe.dto.tpe.TPEResponse;
import com.banque.abc.tpe.entity.Commercant;
import com.banque.abc.tpe.entity.HistoriqueStatut;
import com.banque.abc.tpe.entity.TPE;
import com.banque.abc.tpe.entity.enums.StatutTPE;
import com.banque.abc.tpe.exception.BusinessException;
import com.banque.abc.tpe.exception.DuplicateResourceException;
import com.banque.abc.tpe.exception.ResourceNotFoundException;
import com.banque.abc.tpe.repository.CommercantRepository;
import com.banque.abc.tpe.repository.HistoriqueStatutRepository;
import com.banque.abc.tpe.repository.TPERepository;
import com.banque.abc.tpe.util.TIDGenerator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TPEService {

    private final TPERepository tpeRepository;
    private final CommercantRepository commercantRepository;
    private final HistoriqueStatutRepository historiqueStatutRepository;
    private final TIDGenerator tidGenerator;
    private final ModelMapper modelMapper;
    private final AuditService auditService;

    @Transactional
    public TPEResponse createTPE(TPERequest request) {
        if (tpeRepository.existsByNumeroSerie(request.getNumeroSerie())) {
            throw new DuplicateResourceException("Un TPE avec ce numéro de série existe déjà");
        }

        TPE tpe = modelMapper.map(request, TPE.class);
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
        return mapToResponse(tpe);
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

    @Transactional
    public TPEResponse updateTPE(Long id, TPERequest request) {
        TPE tpe = tpeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TPE non trouvé avec l'ID: " + id));

        if (!tpe.getNumeroSerie().equals(request.getNumeroSerie()) &&
                tpeRepository.existsByNumeroSerie(request.getNumeroSerie())) {
            throw new DuplicateResourceException("Un TPE avec ce numéro de série existe déjà");
        }

        modelMapper.map(request, tpe);
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
        TPEResponse response = modelMapper.map(tpe, TPEResponse.class);
        if (tpe.getCommercant() != null) {
            response.setCommercantId(tpe.getCommercant().getId());
            response.setCommercantNom(tpe.getCommercant().getRaisonSociale());
        }
        return response;
    }
}
