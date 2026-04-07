package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.EcritureComptableDTO;
import com.banque.abc.tpe.dto.PorteurInfoDTO;
import com.banque.abc.tpe.dto.TPEInfoDTO;
import com.banque.abc.tpe.entity.TPE;
import com.banque.abc.tpe.entity.TPEPostingComp;
import com.banque.abc.tpe.repository.PorteurRepository;
import com.banque.abc.tpe.repository.TPEPostingCompRepository;
import com.banque.abc.tpe.repository.TPERepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TPEPostingService {

    private final TPERepository tpeRepository;
    private final PorteurRepository porteurRepository;
    private final TPEPostingCompRepository tpePostingCompRepository;

    /**
     * Vérifie si un TPE existe dans la base par son numéro d'affiliation
     */
    public TPEInfoDTO verifyTPE(String nAffiliation) {
        // Recherche du TPE par numéro d'affiliation
        Optional<TPE> tpeOpt = tpeRepository.findAll().stream()
                .filter(tpe -> nAffiliation.equals(tpe.getNumeroAffiliation()))
                .findFirst();
        
        if (tpeOpt.isPresent()) {
            TPE tpe = tpeOpt.get();
            String raisonSociale = tpe.getCommercant() != null ? tpe.getCommercant().getRaisonSociale() : null;
            // Format du compte simulé : "branch-profitCentre-clientId-accountNo"
            String nCompte = "999-TR-910234-150.1103.0000"; // En production, vient de la base
            
            return new TPEInfoDTO(
                nAffiliation,
                nCompte,
                true,
                "999",
                "TR",
                "910234",
                raisonSociale
            );
        }
        
        // TPE non trouvé - en mode simulation, on accepte tous les TPE
        return new TPEInfoDTO(
            nAffiliation,
            "999-TR-910234-150.1103.0000",
            true, // Mettre à false en production pour rejeter
            "999",
            "TR",
            "910234",
            null
        );
    }

    /**
     * Vérifie si un porteur existe et récupère ses informations de devise
     */
    public PorteurInfoDTO verifyPorteur(String ncarte) {
        Map<String, Object> result = porteurRepository.findByNumeroCarteWithCurrency(ncarte);
        
        if (result != null && !result.isEmpty()) {
            String compte = (String) result.get("compte");
            String devise = (String) result.get("devise");
            String ccyId = (String) result.get("ccy_id");
            Double ccyRate = result.get("ccy_rate") != null ? ((Number) result.get("ccy_rate")).doubleValue() : 1.0;
            Integer deciPlaces = result.get("deci_places") != null ? ((Number) result.get("deci_places")).intValue() : 3;
            
            // Parser le compte pour extraire branch, profitCentre, clientId
            String[] parts = compte != null ? compte.split("-") : new String[0];
            
            return new PorteurInfoDTO(
                ncarte,
                compte,
                devise != null ? devise : "TND",
                ccyId != null ? ccyId : "TND",
                ccyRate,
                deciPlaces,
                true,
                parts.length > 0 ? parts[0] : "IK",
                parts.length > 1 ? parts[1] : "IK",
                parts.length > 2 ? parts[2] : "918671"
            );
        }
        
        // Carte non trouvée - en mode simulation, on accepte toutes les cartes en TND
        return new PorteurInfoDTO(
            ncarte,
            "IK-IK-918671-150.1206.0000",
            "TND",
            "TND",
            1.0,
            3,
            true, // Mettre à false en production pour rejeter
            "IK",
            "IK",
            "918671"
        );
    }

    /**
     * Insère les écritures comptables dans TPE_POSTING_comp
     */
    @Transactional
    public int insertPostings(List<EcritureComptableDTO> ecritures, String sessionUser) {
        int count = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        
        for (EcritureComptableDTO dto : ecritures) {
            TPEPostingComp posting = TPEPostingComp.builder()
                    .branch(dto.getBranch())
                    .client(dto.getClientId())
                    .account(dto.getAccountNo())
                    .ref(dto.getReferenceNo())
                    .date(parseDate(dto.getValueDate(), formatter))
                    .amount(new BigDecimal(dto.getAmount()))
                    .crDr(dto.getDc())
                    .narrative(dto.getNarrative())
                    .tranType(dto.getTranType())
                    .rbGl(dto.getRbGl())
                    .sessionDate(dto.getSessionDate())
                    .ccy(dto.getCcy())
                    .build();
            
            tpePostingCompRepository.save(posting);
            count++;
        }
        
        return count;
    }
    
    private LocalDate parseDate(String dateStr, DateTimeFormatter formatter) {
        try {
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }
}
