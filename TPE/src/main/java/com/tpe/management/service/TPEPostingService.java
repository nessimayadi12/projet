package com.tpe.management.service;

import com.tpe.management.dto.EcritureComptableDTO;
import com.tpe.management.dto.PorteurInfo;
import com.tpe.management.dto.TPEInfo;
import com.tpe.management.entity.TPEPosting;
import com.tpe.management.repository.PorteurRepository;
import com.tpe.management.repository.TPEPostingRepository;
import com.tpe.management.repository.TPERepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class TPEPostingService {

    @Autowired
    private TPERepository tpeRepository;

    @Autowired
    private PorteurRepository porteurRepository;

    @Autowired
    private TPEPostingRepository tpePostingRepository;

    /**
     * Vérifie si un TPE existe dans la base
     */
    public TPEInfo verifyTPE(String nAffiliation) {
        Map<String, Object> result = tpeRepository.findByAffiliation(nAffiliation);
        
        if (result != null && !result.isEmpty()) {
            String nCompte = (String) result.get("N_compte");
            
            // Parser le compte pour extraire branch, profitCentre, clientId
            // Format attendu: "branch-profitCentre-clientId-accountNo"
            String[] parts = nCompte != null ? nCompte.split("-") : new String[0];
            
            return new TPEInfo(
                nAffiliation,
                nCompte,
                true,
                parts.length > 0 ? parts[0] : "999",
                parts.length > 1 ? parts[1] : "TR",
                parts.length > 2 ? parts[2] : "910234"
            );
        }
        
        return new TPEInfo(nAffiliation, null, false, null, null, null);
    }

    /**
     * Vérifie si un porteur existe et récupère ses informations de devise
     */
    public PorteurInfo verifyPorteur(String ncarte) {
        Map<String, Object> result = porteurRepository.findByNumeroCarteWithCurrency(ncarte);
        
        if (result != null && !result.isEmpty()) {
            String compte = (String) result.get("compte");
            String devise = (String) result.get("devise");
            String ccyId = (String) result.get("ccy_id");
            Double ccyRate = result.get("ccy_rate") != null ? ((Number) result.get("ccy_rate")).doubleValue() : 1.0;
            Integer deciPlaces = result.get("deci_places") != null ? ((Number) result.get("deci_places")).intValue() : 3;
            
            // Parser le compte pour extraire branch, profitCentre, clientId
            String[] parts = compte != null ? compte.split("-") : new String[0];
            
            return new PorteurInfo(
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
        
        return new PorteurInfo(ncarte, null, "TND", "TND", 1.0, 3, false, null, null, null);
    }

    /**
     * Insère les écritures comptables dans TPE_POSTING_comp
     */
    @Transactional
    public int insertPostings(List<EcritureComptableDTO> ecritures, String sessionUser) {
        int count = 0;
        
        for (EcritureComptableDTO dto : ecritures) {
            TPEPosting posting = new TPEPosting();
            posting.setBranch(dto.getBranch());
            posting.setProfitCentre(dto.getProfitCentre());
            posting.setClientId(dto.getClientId());
            posting.setAccountNo(dto.getAccountNo());
            posting.setAccountName(dto.getAccountName());
            posting.setAccountType(dto.getAccountType());
            posting.setCcy(dto.getCcy());
            posting.setSeqNo(dto.getSeqNo());
            posting.setReferenceNo(dto.getReferenceNo());
            posting.setRbTranType(dto.getRbTranType());
            posting.setValueDate(dto.getValueDate());
            posting.setAmount(new BigDecimal(dto.getAmount()));
            posting.setDc(dto.getDc());
            posting.setNarrative(dto.getNarrative());
            posting.setTranType(dto.getTranType());
            posting.setRbGl(dto.getRbGl());
            posting.setSessionDate(dto.getSessionDate());
            posting.setSessionUser(sessionUser);
            posting.setCreatedAt(LocalDateTime.now());
            
            tpePostingRepository.save(posting);
            count++;
        }
        
        return count;
    }
}
