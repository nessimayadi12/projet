package com.banque.abc.tpe.service;

import com.banque.abc.tpe.entity.TPEPostingComp;
import com.banque.abc.tpe.repository.TPEPostingCompRepository;
import com.banque.abc.tpe.repository.TPERepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final TPEPostingCompRepository tpePostingCompRepository;
    private final TPERepository tpeRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void uploadAndProcessFile(MultipartFile file) throws Exception {
        LocalDate today = LocalDate.now();
        String sessionDate = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sessionUser = System.getProperty("user.name");

        List<TPEPostingComp> postingsToSave = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.length() < 100) {
                    continue; // Ligne trop courte
                }

                String prefix = line.substring(0, 2);
                
                // Traitement des lignes commençant par "10"
                if ("10".equals(prefix)) {
                    processType10Line(line, sessionDate, postingsToSave);
                }
                // Traitement des lignes commençant par "20"
                else if ("20".equals(prefix) && line.length() > 99) {
                    String indicator = line.substring(99, 100);
                    if ("T".equals(indicator) || "I".equals(indicator)) {
                        processType20Line(line, sessionDate, postingsToSave);
                    }
                }
            }
        }

        // Sauvegarder tous les enregistrements en une seule fois
        if (!postingsToSave.isEmpty()) {
            tpePostingCompRepository.saveAll(postingsToSave);
            log.info("Fichier traité avec succès. {} enregistrements insérés.", postingsToSave.size());
        }
    }

    private void processType10Line(String line, String sessionDate, List<TPEPostingComp> postingsToSave) {
        try {
            String nAffiliation = line.substring(16, 26).trim();

            // Vérifier l'existence du TPE
            String sqlCheckTPE = "SELECT N_compte FROM TPE WHERE N_AFFILIATION = ?";
            List<Map<String, Object>> tpeResults = jdbcTemplate.queryForList(sqlCheckTPE, nAffiliation);

            if (!tpeResults.isEmpty()) {
                for (Map<String, Object> tpeRow : tpeResults) {
                    String nCompte = (String) tpeRow.get("N_compte");
                    
                    if (nCompte != null && nCompte.length() >= 11) {
                        String client = nCompte.substring(5, 11);
                        
                        // Extraction des montants
                        BigDecimal amount1 = extractAmount(line, 242, 12, 1000);
                        BigDecimal amount2 = extractAmount(line, 219, 12, 10000);
                        String narrative = line.substring(50, 75).trim();

                        // Insertion 1
                        postingsToSave.add(createPosting("999", client, "150.1103.0000", nAffiliation, 
                            sessionDate, amount1, "DR", narrative, null, null, sessionDate, null));

                        // Insertion 2
                        postingsToSave.add(createPosting("999", client, "151.1105.0000", nAffiliation, 
                            sessionDate, amount1, "CR", narrative, null, null, sessionDate, null));

                        // Insertion 3
                        postingsToSave.add(createPosting("999", client, "601.9106.0000", nAffiliation, 
                            sessionDate, amount2, "DR", narrative, null, null, sessionDate, null));

                        // Insertion 4
                        postingsToSave.add(createPosting("999", client, "150.1103.0000", nAffiliation, 
                            sessionDate, amount2, "CR", narrative, null, null, sessionDate, null));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur lors du traitement de la ligne de type 10: {}", e.getMessage(), e);
        }
    }

    private void processType20Line(String line, String sessionDate, List<TPEPostingComp> postingsToSave) {
        try {
            if (line.length() < 227) {
                return;
            }

            String nAffiliation = line.substring(15, 25).trim();
            String nCarte = line.substring(113, 129).trim();

            // Vérifier l'existence du TPE
            String sqlCheckTPE = "SELECT N_AFFILIATION FROM TPE WHERE N_AFFILIATION = ?";
            List<Map<String, Object>> tpeResults = jdbcTemplate.queryForList(sqlCheckTPE, nAffiliation);

            if (tpeResults.isEmpty()) {
                return; // TPE n'existe pas
            }

            // Vérifier l'existence de la carte
            String sqlCheckCarte = "SELECT ncarte, compte, devise, ccy_id, ccy_rate, deci_places " +
                "FROM PORTEUR a, FM_CURRENCY b, RATES c " +
                "WHERE typecarte NOT IN ('AANP','ADNC','ADNG','ADNP','IANG','IANI','IDNC','IDNE','IDNG','IDNI','IANC','IDNT','ADIP','AAIP','IDII','IDIG','IAIG','IAII') " +
                "AND DEVISE = c.ccy AND b.ccy = c.ccy " +
                "AND effective_date = (SELECT MAX(EFFECTIVE_DATE) FROM RATES) " +
                "AND a.ncarte = ?";

            List<Map<String, Object>> carteResults = jdbcTemplate.queryForList(sqlCheckCarte, nCarte);

            if (carteResults.isEmpty()) {
                return; // Carte n'existe pas
            }

            for (Map<String, Object> carteRow : carteResults) {
                String compte = (String) carteRow.get("compte");
                String devise = (String) carteRow.get("devise");
                String ccyId = String.valueOf(carteRow.get("ccy_id"));
                BigDecimal ccyRate = new BigDecimal(String.valueOf(carteRow.get("ccy_rate")));
                int deciPlaces = Integer.parseInt(String.valueOf(carteRow.get("deci_places")));

                if (compte == null || compte.length() < 11) {
                    continue;
                }

                String branch = compte.substring(2, 5);
                String client = compte.substring(5, 11);
                
                // Extraction des données de la transaction
                String ref = line.substring(209, 215).trim();
                String tranDate = "20" + line.substring(207, 209) + line.substring(205, 207) + line.substring(203, 205);
                BigDecimal amount = extractAmount(line, 215, 12, 1000);
                String narrative = "PAYMENT -" + line.substring(50, 75).trim();

                if ("TNC".equals(devise) || "TND".equals(devise)) {
                    // Devise locale (TND)
                    
                    // Insertion 1 - Débit compte client
                    postingsToSave.add(createPosting(branch, client, compte, ref, 
                        tranDate, amount, "DR", narrative, "CMS2", "C", sessionDate, null));

                    // Insertion 2 - Crédit compte de compensation
                    postingsToSave.add(createPosting(branch, client, "150.1103.0000", ref, 
                        sessionDate, amount, "CR", narrative, null, null, sessionDate, null));

                } else {
                    // Devise étrangère
                    
                    // Calcul du montant en devise
                    BigDecimal amountInCcy = amount.divide(ccyRate, deciPlaces, RoundingMode.HALF_UP);

                    // Insertion 1 - Crédit compte de compensation devise
                    postingsToSave.add(createPosting("999", "000234", "151.1103.0000", ref, 
                        sessionDate, amount, "CR", narrative, null, null, sessionDate, null));

                    // Insertion 2 - Débit compte position de change
                    postingsToSave.add(createPosting("999", "000234", "342.1101.0" + ccyId, ref, 
                        sessionDate, amount, "DR", narrative, null, null, sessionDate, null));

                    // Insertion 3 - Débit compte client en devise
                    postingsToSave.add(createPosting(branch, client, compte, ref, 
                        tranDate, amountInCcy, "DR", narrative, "CMS2", "C", sessionDate, devise));

                    // Insertion 4 - Crédit contrepartie
                    postingsToSave.add(createPosting("999", "000234", "341.1101.0000", ref, 
                        sessionDate, amountInCcy, "CR", line.substring(50, 75).trim(), null, null, sessionDate, null));
                }
            }
        } catch (Exception e) {
            log.error("Erreur lors du traitement de la ligne de type 20: {}", e.getMessage(), e);
        }
    }

    private BigDecimal extractAmount(String line, int start, int length, int divisor) {
        try {
            String amountStr = line.substring(start, start + length).trim();
            double amount = Double.parseDouble(amountStr);
            return BigDecimal.valueOf(Math.round(amount) / (double) divisor);
        } catch (Exception e) {
            log.error("Erreur lors de l'extraction du montant à la position {}: {}", start, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private TPEPostingComp createPosting(String branch, String client, String account, String ref,
                                          String dateStr, BigDecimal amount, String crDr, String narrative,
                                          String tranType, String rbGl, String sessionDate, String ccy) {
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            date = LocalDate.now();
        }

        return TPEPostingComp.builder()
            .branch(branch)
            .client(client)
            .account(account)
            .ref(ref)
            .date(date)
            .amount(amount)
            .crDr(crDr)
            .narrative(narrative)
            .tranType(tranType)
            .rbGl(rbGl)
            .sessionDate(sessionDate)
            .ccy(ccy)
            .build();
    }
}
