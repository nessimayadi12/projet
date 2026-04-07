package com.banque.abc.tpe.service;

import com.banque.abc.tpe.entity.Commercant;
import com.banque.abc.tpe.entity.TPE;
import com.banque.abc.tpe.entity.TPEPostingComp;
import com.banque.abc.tpe.repository.CommercantRepository;
import com.banque.abc.tpe.repository.TPEPostingCompRepository;
import com.banque.abc.tpe.repository.TPERepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FichierBancaireService {

    private final TPERepository tpeRepository;
    private final CommercantRepository commercantRepository;
    private final TPEPostingCompRepository tpePostingCompRepository;

    /**
     * Traite un fichier bancaire complet
     * @param fileContent Contenu du fichier (chaque ligne)
     * @param sessionDate Date de session au format yyyyMMdd
     * @return Nombre d'écritures créées
     */
    @Transactional
    public int traiterFichierBancaire(List<String> fileContent, String sessionDate) {
        int compteurEcritures = 0;
        LocalDate sessionLocalDate = parseSessionDate(sessionDate);
        
        for (String line : fileContent) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            
            // Type 10 nécessite au moins 250 caractères, Type 20 environ 230
            if (line.length() < 200) {
                log.warn("Ligne ignorée (trop courte): {}", line);
                continue;
            }
            
            try {
                // Extraire le numéro de terminal de la ligne (positions 16-26, longueur 10)
                String numeroTerminal = extractSubstring(line, 16, 10).trim();
                
                // Vérifier si le TPE existe et récupérer les informations du commerçant
                Optional<TPE> tpeOpt = tpeRepository.findByNumeroTerminal(numeroTerminal);
                
                if (!tpeOpt.isPresent()) {
                    log.warn("TPE non trouvé pour numeroTerminal: {}", numeroTerminal);
                    continue;
                }
                
                TPE tpe = tpeOpt.get();
                Commercant commercant = tpe.getCommercant();
                
                if (commercant == null) {
                    log.warn("Aucun commerçant affecté au TPE: {}", numeroTerminal);
                    continue;
                }
                
                String numeroCompte = commercant.getNumeroCompte();
                String narrative = commercant.getRaisonSociale() != null && !commercant.getRaisonSociale().isBlank()
                    ? commercant.getRaisonSociale().trim()
                    : numeroTerminal;
                String numeroAffiliation = tpe.getNumeroAffiliation() != null ? tpe.getNumeroAffiliation() : numeroTerminal;
                
                // Type de transaction: "10" ou "20"
                String typeTransaction = extractSubstring(line, 0, 2);
                
                if ("10".equals(typeTransaction)) {
                    // Traitement des transactions de type 10 (Commissions)
                    compteurEcritures += traiterType10(line, numeroCompte, numeroAffiliation, numeroTerminal, narrative, sessionLocalDate, sessionDate);
                    
                } else if ("20".equals(typeTransaction)) {
                    // Traitement des transactions de type 20 (Paiements)
                    // Note: Sans les tables PORTEUR, FM_CURRENCY, RATES, on traite uniquement en TND
                    compteurEcritures += traiterType20(line, numeroCompte, numeroTerminal, narrative, sessionLocalDate, sessionDate);
                }
                
            } catch (Exception e) {
                log.error("Erreur lors du traitement de la ligne: {}", line, e);
            }
        }
        
        return compteurEcritures;
    }
    
    /**
     * Traite les transactions de type 10 (Commissions) - Génère écritures GL
     * Chaque montant non nul génère 2 écritures: 1 DR (principal) + 1 CR (commission)
     */
    private int traiterType10(String line, String numeroCompte, String numeroAffiliation,
                              String numeroTerminal, String narrative, LocalDate sessionLocalDate, String sessionDate) {
        int count = 0;
        
        try {
            // Extraire éléments du fichier
            String sessionDateFile = extractSubstring(line, 10, 6); // Position 10, 6 chars: "180226"
            String processingDate = sessionDate; // Date de traitement actuelle
            
            // Extraire BRANCH du compte (position 2, 3 chars) - ex: "28000000501" → "000"
            String branch = numeroCompte.length() >= 5 ? numeroCompte.substring(2, 5) : "000";
            
            // Extraire CLIENT du compte (position 5, 6 chars) - ex: "28000000501100000181" → "000501"
            String client = numeroCompte.length() >= 11 ? numeroCompte.substring(5, 11) : "000000";
            
            // NARRATIVE = raison sociale du commerçant
            
            // Extraire les 3 paires (montant, commission) possibles
            // Position 219: premier montant/commission
            // Position 231: deuxième montant/commission  
            // Position 243: troisième montant/commission (si présent)
            
            double[] montants = new double[3];
            double[] commissions = new double[3];
            String[] accountSuffixes = {"0000", "0001", "0002"};
            
            // Montant 1: position 219, taille 12
            montants[0] = parseMontant(extractSubstring(line, 219, 12)) / 1000.0;
            // Commission 1: position 248, taille 12
            commissions[0] = parseMontant(extractSubstring(line, 248, 12)) / 10000.0;
            
            // Montant 2: position 231, taille 12  
            montants[1] = parseMontant(extractSubstring(line, 231, 12)) / 1000.0;
            // Commission 2: position 260, taille 12
            commissions[1] = parseMontant(extractSubstring(line, 260, 12)) / 10000.0;
            
            // Montant 3: position 272, taille 12 (si ligne assez longue)
            if (line.length() >= 284) {
                montants[2] = parseMontant(extractSubstring(line, 272, 12)) / 1000.0;
                // Commission 3: position 284, taille 12
                if (line.length() >= 296) {
                    commissions[2] = parseMontant(extractSubstring(line, 284, 12)) / 10000.0;
                }
            }
            
            // Créer écritures pour chaque paire non nulle
            for (int i = 0; i < 3; i++) {
                if (montants[i] > 0) {
                    // Date de valeur = sessionLocalDate + 3 jours (approximation)
                    LocalDate dateValeur = sessionLocalDate.plusDays(3);
                    
                    // Écriture 1: DR sur compte 151.1105.xxxx
                    TPEPostingComp ecriture1 = TPEPostingComp.builder()
                            .branch(branch)
                            .profitCenter("TR")
                            .client(client)
                            .account("151.1105." + accountSuffixes[i])
                            .rbGl("G")
                            .ccy("TND")
                            .seqNo("1")
                            .ref(numeroTerminal)
                            .tranType("")
                            .date(dateValeur)
                            .amount(BigDecimal.valueOf(montants[i]).setScale(3, RoundingMode.HALF_UP))
                            .crDr("DR")
                            .narrative(narrative)
                            .sessionDate(sessionDate)
                            .build();
                    tpePostingCompRepository.save(ecriture1);
                    count++;
                    
                    // Écriture 2: CR commission sur 707.9102.1000
                    if (commissions[i] > 0) {
                        TPEPostingComp ecriture2 = TPEPostingComp.builder()
                                .branch(branch)
                                .profitCenter("TR")
                                .client(client)
                                .account("707.9102.1000")
                                .rbGl("G")
                                .ccy("TND")
                                .seqNo("1")
                                .ref(numeroTerminal)
                                .tranType("")
                                .date(sessionLocalDate)
                                .amount(BigDecimal.valueOf(commissions[i]).setScale(3, RoundingMode.HALF_UP))
                                .crDr("CR")
                                .narrative(narrative)
                                .sessionDate(sessionDate)
                                .build();
                        tpePostingCompRepository.save(ecriture2);
                        count++;
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Erreur traitement Type 10", e);
        }
        
        return count;
    }
    
    /**
     * Traite les transactions de type 20 (Paiements) - Génère écritures sur compte client
     * Crée 2 écritures: 1 CADV (crédit avance) + 1 CTPE (débit commission)
     */
    private int traiterType20(String line, String numeroCompte, String numeroTerminal,
                              String narrative, LocalDate sessionLocalDate, String sessionDate) {
        int count = 0;
        
        try {
            // Extraire éléments du fichier
            String sessionDateFile = extractSubstring(line, 10, 6); // Position 10, 6 chars: "180226"
            String processingDate = sessionDate; // Date de traitement actuelle
            
            // Extraire BRANCH du compte (position 2, 3 chars)
            String branch = numeroCompte.length() >= 5 ? numeroCompte.substring(2, 5) : "000";
            
            // Extraire CLIENT du compte (position 5, 6 chars)
            String client = numeroCompte.length() >= 11 ? numeroCompte.substring(5, 11) : "000000";
            
            // NARRATIVE = raison sociale du commerçant
            
            // Montant: position 215, longueur 12
            double montant = parseMontant(extractSubstring(line, 215, 12)) / 1000.0;
            
            // Référence: position 209, longueur 6
            String reference = extractSubstring(line, 209, 6).trim();
            
            // Date de transaction: positions 203-208 (format AAMMJJ → YYYYMMDD)
            String dateTransStr = extractSubstring(line, 203, 6);
            String dateTransFormatted = "20" + dateTransStr.substring(0, 2) + 
                                        dateTransStr.substring(2, 4) + 
                                        dateTransStr.substring(4, 6);
            LocalDate dateTransaction = parseDate(dateTransFormatted);
            
            // Vérifier type de carte: position 99, 1 char ('T' = TND, 'I' = International)
            String typeIndicator = extractSubstring(line, 99, 1);
            
            if (montant > 0) {
                // Écriture 1: CADV - Crédit avance sur compte client (date = date transaction)
                TPEPostingComp ecriture1 = TPEPostingComp.builder()
                        .branch(branch)
                        .profitCenter("TR")
                        .client(client)
                        .account(numeroCompte.trim())
                        .rbGl("C")
                        .ccy("TND")
                        .seqNo("1")
                        .ref(numeroTerminal)
                        .tranType("CADV")
                        .date(dateTransaction)
                        .amount(BigDecimal.valueOf(montant).setScale(3, RoundingMode.HALF_UP))
                        .crDr("CR")
                        .narrative(narrative)
                        .sessionDate(sessionDate)
                        .build();
                tpePostingCompRepository.save(ecriture1);
                count++;
                
                // Calculer commission (approximation - devrait venir du type 10 correspondant)
                // Pour l'instant, on utilise un taux forfaitaire de 0.8%
                double commission = montant * 0.008;
                
                // Écriture 2: CTPE - Débit commission sur compte client (date = date session)
                TPEPostingComp ecriture2 = TPEPostingComp.builder()
                        .branch(branch)
                        .profitCenter("TR")
                        .client(client)
                        .account(numeroCompte.trim())
                        .rbGl("C")
                        .ccy("TND")
                        .seqNo("1")
                        .ref(numeroTerminal)
                        .tranType("CTPE")
                        .date(sessionLocalDate)
                        .amount(BigDecimal.valueOf(commission).setScale(3, RoundingMode.HALF_UP))
                        .crDr("DR")
                        .narrative(narrative)
                        .sessionDate(sessionDate)
                        .build();
                tpePostingCompRepository.save(ecriture2);
                count++;
            }
            
        } catch (Exception e) {
            log.error("Erreur traitement Type 20", e);
        }
        
        return count;
    }
    
    /**
     * Extrait une sous-chaîne de la ligne
     */
    private String extractSubstring(String line, int start, int length) {
        if (line == null || start + length > line.length()) {
            return "";
        }
        return line.substring(start, start + length);
    }
    
    /**
     * Parse un montant depuis une chaîne
     */
    private double parseMontant(String montantStr) {
        try {
            return Double.parseDouble(montantStr.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    /**
     * Parse une date de session
     */
    private LocalDate parseSessionDate(String sessionDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            return LocalDate.parse(sessionDate, formatter);
        } catch (Exception e) {
            log.warn("Impossible de parser la date de session: {}, utilisation de la date actuelle", sessionDate);
            return LocalDate.now();
        }
    }
    
    /**
     * Parse une date
     */
    private LocalDate parseDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }
    
    /**
     * Obtient les statistiques de traitement
     */
    public int getTransactionCount(String sessionDate) {
        return tpePostingCompRepository.findBySessionDate(sessionDate).size();
    }
    
    /**
     * Récupère toutes les transactions pour une session donnée
     * @param sessionDate Date de session au format yyyyMMdd
     * @return Liste des transactions avec les colonnes dans l'ordre de l'affichage
     */
    public List<Map<String, Object>> getTransactions(String sessionDate) {
        List<TPEPostingComp> postings = tpePostingCompRepository.findBySessionDate(sessionDate);
        List<Map<String, Object>> transactions = new ArrayList<>();
        
        for (TPEPostingComp posting : postings) {
            Map<String, Object> transaction = new HashMap<>();
            transaction.put("branch", posting.getBranch());
            transaction.put("profitCenter", posting.getProfitCenter());
            transaction.put("client", posting.getClient());
            transaction.put("account", posting.getAccount());
            transaction.put("rbGl", posting.getRbGl());
            transaction.put("ccy", posting.getCcy());
            transaction.put("seqNo", posting.getSeqNo());
            transaction.put("ref", posting.getRef());
            transaction.put("tranType", posting.getTranType());
            transaction.put("date", posting.getDate());
            transaction.put("amount", posting.getAmount());
            transaction.put("crDr", posting.getCrDr());
            transaction.put("narrative", posting.getNarrative());
            
            transactions.add(transaction);
        }
        
        return transactions;
    }
}
