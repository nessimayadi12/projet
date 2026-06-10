package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.tpe.TPEImportResult;
import com.banque.abc.tpe.dto.tpe.TPEImportRecordDTO;
import com.banque.abc.tpe.entity.Affectation;
import com.banque.abc.tpe.entity.Commercant;
import com.banque.abc.tpe.entity.Demande;
import com.banque.abc.tpe.entity.TPEImportRecord;
import com.banque.abc.tpe.entity.TPE;
import com.banque.abc.tpe.entity.User;
import com.banque.abc.tpe.entity.enums.StatutCommercant;
import com.banque.abc.tpe.entity.enums.StatutDemande;
import com.banque.abc.tpe.entity.enums.StatutTPE;
import com.banque.abc.tpe.entity.enums.TypeTPE;
import com.banque.abc.tpe.exception.BusinessException;
import com.banque.abc.tpe.repository.AffectationRepository;
import com.banque.abc.tpe.repository.CommercantRepository;
import com.banque.abc.tpe.repository.DemandeRepository;
import com.banque.abc.tpe.repository.TPEImportRecordRepository;
import com.banque.abc.tpe.repository.TPERepository;
import com.banque.abc.tpe.repository.UserRepository;
import com.banque.abc.tpe.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
@Slf4j
public class TPEExcelImportService {

    private final TPERepository tpeRepository;
    private final CommercantRepository commercantRepository;
    private final DemandeRepository demandeRepository;
    private final AffectationRepository affectationRepository;
    private final TPEImportRecordRepository tpeImportRecordRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    public TPEImportResult importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Le fichier Excel est vide");
        }

        TPEImportResult result = new TPEImportResult();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() < 2) {
                throw new BusinessException("Le fichier Excel ne contient aucune ligne de données");
            }

            int headerRowIndex = findHeaderRowIndex(sheet);
            Map<String, Integer> headers = readHeaders(sheet.getRow(headerRowIndex));
            DataFormatter formatter = new DataFormatter(Locale.FRANCE);

            for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowEmpty(row, formatter)) {
                    continue;
                }

                result.setTotalRows(result.getTotalRows() + 1);

                try {
                    String rawKey = persistStagingRecord(row, headers, formatter, file.getOriginalFilename(), rowIndex + 1, result);
                    importRow(row, headers, formatter, result, rawKey);
                } catch (Exception ex) {
                    result.setSkippedRows(result.getSkippedRows() + 1);
                    String message = "Ligne " + (rowIndex + 1) + " ignorée: " + ex.getMessage();
                    result.getErrors().add(message);
                    log.warn(message, ex);
                }
            }

            reconcileImportedValidatedDemandes(result);

            auditService.logAction("IMPORT", "TPE", null,
                    String.format("Import Excel terminé: %d lignes stockées, %d créées, %d mises à jour, %d affectations, %d ignorées",
                        result.getStoredRows(), result.getImportedRows(), result.getUpdatedRows(), result.getAffectedRows(), result.getSkippedRows()),
                    result.getErrors().isEmpty() ? "SUCCESS" : "PARTIAL_SUCCESS");

            return result;
        } catch (IOException ex) {
            throw new BusinessException("Erreur lors de la lecture du fichier Excel: " + ex.getMessage());
        }
    }

    public Page<TPEImportRecordDTO> getImportRecords(Pageable pageable) {
        return tpeImportRecordRepository.findAll(pageable).map(this::toDto);
    }

    private String persistStagingRecord(Row row,
                                        Map<String, Integer> headers,
                                        DataFormatter formatter,
                                        String sourceFileName,
                                        int sourceRowNumber,
                                        TPEImportResult result) throws IOException {
        String numeroAffiliation = firstNonBlank(readString(row, headers, formatter, "N_AFFILIATION"), "ROW_" + sourceRowNumber);
        String tauxCommission = readAnyString(row, headers, formatter, "TAUX_COMMISSION", "TAUX_COMMISION");
        String tauxCommissionInter = readAnyString(row, headers, formatter, "TAUX_COMMISSION_INTER", "TAUX_COMMISION_INTER");
        String numeroTerminal = readNumeroTerminal(row, headers, formatter);
        String codeAgence = resolveCodeAgence(readString(row, headers, formatter, "CODE_AGENCE"), numeroTerminal);
        boolean parcUpdate = "PARC_TPE_MAJ".equalsIgnoreCase(firstNonBlank(readString(row, headers, formatter, "IMPORT_MODE"), ""));
        Map<String, Object> rawData = new LinkedHashMap<>();
        rawData.put("IMPORT_MODE", readString(row, headers, formatter, "IMPORT_MODE"));
        rawData.put("TYPE_TPE", readString(row, headers, formatter, "TYPE_TPE"));
        rawData.put("MARQUE", readString(row, headers, formatter, "MARQUE"));
        rawData.put("N_AFFILIATION", readString(row, headers, formatter, "N_AFFILIATION"));
        rawData.put("N_TERMINAL", numeroTerminal);
        rawData.put("RAISON_SOCIALE", readString(row, headers, formatter, "RAISON_SOCIALE"));
        rawData.put("ACTIVITE", readString(row, headers, formatter, "ACTIVITE"));
        rawData.put("MCC", readString(row, headers, formatter, "MCC"));
        rawData.put("N_COMPTE", readString(row, headers, formatter, "N_COMPTE"));
        rawData.put("CODE_AGENCE", codeAgence);
        rawData.put("ADRESSE", readString(row, headers, formatter, "ADRESSE"));
        rawData.put("CODE_POSTAL", readString(row, headers, formatter, "CODE_POSTAL"));
        rawData.put("TELEPHONE", readString(row, headers, formatter, "TELEPHONE"));
        rawData.put("EMAIL", firstNonBlank(readString(row, headers, formatter, "EMAIL"), readString(row, headers, formatter, "MAIL")));
        rawData.put("OPERATEUR", readString(row, headers, formatter, "OPERATEUR"));
        rawData.put("PREVILEGE_SECTEUR", readString(row, headers, formatter, "PREVILEGE_SECTEUR"));
        rawData.put("TAUX_COMMISSION", tauxCommission);
        rawData.put("TAUX_COMMISSION_INTER", tauxCommissionInter);
        rawData.put("LOYER", readString(row, headers, formatter, "LOYER"));
        rawData.put("N_COMPTE_INTERN", readString(row, headers, formatter, "N_COMPTE_INTERN"));
        rawData.put("GROUP", readString(row, headers, formatter, "GROUP"));
        rawData.put("NUM_SEQ", readString(row, headers, formatter, "NUM_SEQ"));
        Boolean active = resolveActiveStatus(row, headers, formatter,
            readValueDate(row, headers, formatter),
            readLocalDate(row, headers, formatter, "DATE_AFFILIATION"),
            numeroTerminal);
        rawData.put("ACTIVE", active);
        rawData.put("VALUE_DATE", readString(row, headers, formatter, "VALUE_DATE"));
        rawData.put("DATE_AFFILIATION", readString(row, headers, formatter, "DATE_AFFILIATION"));

        TPEImportRecord record = tpeImportRecordRepository
                .findLatestByNAffiliation(numeroAffiliation)
                .stream()
                .findFirst()
                .orElseGet(TPEImportRecord::new);
        record.setNAffiliation(numeroAffiliation);
        record.setSourceRowNumber(sourceRowNumber);
        record.setSourceFileName(sourceFileName);
        record.setTypeTPE(firstNonBlank(readString(row, headers, formatter, "TYPE_TPE"), null));
        String resolvedNumeroSerie = resolveNumeroSerie(row, headers, formatter);
        String normalizedTerminal = normalizeTerminal(numeroTerminal);
        record.setNumeroSerie(firstNonBlank(resolvedNumeroSerie, normalizedTerminal, numeroAffiliation));
        record.setNumeroTerminal(normalizedTerminal);
        record.setRaisonSociale(firstNonBlank(readString(row, headers, formatter, "RAISON_SOCIALE"), numeroAffiliation));
        record.setActivite(readString(row, headers, formatter, "ACTIVITE"));
        record.setMcc(readString(row, headers, formatter, "MCC"));
        String recordNumeroCompte = readString(row, headers, formatter, "N_COMPTE");
        record.setNumeroCompte(parcUpdate ? recordNumeroCompte : firstNonBlank(recordNumeroCompte, numeroAffiliation));
        record.setCodeAgence(codeAgence);
        record.setAdresse(readString(row, headers, formatter, "ADRESSE"));
        record.setCodePostal(readString(row, headers, formatter, "CODE_POSTAL"));
        record.setTelephone(readString(row, headers, formatter, "TELEPHONE"));
        record.setEmail(firstNonBlank(readString(row, headers, formatter, "EMAIL"), readString(row, headers, formatter, "MAIL")));
        record.setPrivilegeSecteur(readString(row, headers, formatter, "PREVILEGE_SECTEUR"));
        record.setTauxCommission(tauxCommission);
        record.setTauxCommissionInter(tauxCommissionInter);
        record.setLoyer(readString(row, headers, formatter, "LOYER"));
        record.setNCompteIntern(readString(row, headers, formatter, "N_COMPTE_INTERN"));
        record.setGroupe(readString(row, headers, formatter, "GROUP"));
        record.setNumSeq(readString(row, headers, formatter, "NUM_SEQ"));
        record.setActive(active);
        record.setValueDate(null);
        record.setDateAffiliation(readLocalDate(row, headers, formatter, "DATE_AFFILIATION"));
        record.setRawDataJson(objectMapper.writeValueAsString(rawData));

        tpeImportRecordRepository.save(record);
        result.setStoredRows(result.getStoredRows() + 1);
        return numeroAffiliation;
    }

    private void importRow(Row row, Map<String, Integer> headers, DataFormatter formatter, TPEImportResult result, String rawKey) {
        boolean parcUpdate = "PARC_TPE_MAJ".equalsIgnoreCase(firstNonBlank(readString(row, headers, formatter, "IMPORT_MODE"), ""));
        String typeValue = readString(row, headers, formatter, "TYPE_TPE");
        String marque = readString(row, headers, formatter, "MARQUE");
        String numeroAffiliation = firstNonBlank(readString(row, headers, formatter, "N_AFFILIATION"), rawKey);
        String numeroSerie = resolveNumeroSerie(row, headers, formatter);
        String numeroTerminal = normalizeTerminal(readNumeroTerminal(row, headers, formatter));
        String raisonSociale = readString(row, headers, formatter, "RAISON_SOCIALE");
        String activite = readString(row, headers, formatter, "ACTIVITE");
        String mcc = readString(row, headers, formatter, "MCC");
        String numeroCompte = readString(row, headers, formatter, "N_COMPTE");
        String codeAgence = resolveCodeAgence(readString(row, headers, formatter, "CODE_AGENCE"), numeroTerminal);
        String adresse = readString(row, headers, formatter, "ADRESSE");
        String codePostal = readString(row, headers, formatter, "CODE_POSTAL");
        String telephone = readString(row, headers, formatter, "TELEPHONE");
        String email = firstNonBlank(
                readString(row, headers, formatter, "EMAIL"),
                readString(row, headers, formatter, "MAIL")
        );
        String previlegeSecteur = readString(row, headers, formatter, "PREVILEGE_SECTEUR");
        String operateur = readString(row, headers, formatter, "OPERATEUR");
        String tauxCommission = readAnyString(row, headers, formatter, "TAUX_COMMISSION", "TAUX_COMMISION");
        String tauxCommissionInter = readAnyString(row, headers, formatter, "TAUX_COMMISSION_INTER", "TAUX_COMMISION_INTER");
        String loyer = readString(row, headers, formatter, "LOYER");
        String nCompteIntern = readString(row, headers, formatter, "N_COMPTE_INTERN");
        String groupe = readString(row, headers, formatter, "GROUP");
        String numSeq = readString(row, headers, formatter, "NUM_SEQ");
        Integer valueDate = readValueDate(row, headers, formatter);
        LocalDate dateAffiliation = readLocalDate(row, headers, formatter, "DATE_AFFILIATION");
        LocalDate effectiveTpeDate = dateAffiliation;
        Boolean active = resolveActiveStatus(row, headers, formatter, valueDate, dateAffiliation, numeroTerminal);
        boolean terminated = isTerminatedActiveStatus(row, headers, formatter);

        if ((numeroSerie == null || numeroSerie.isBlank()) && (numeroTerminal == null || numeroTerminal.isBlank())) {
            throw new BusinessException("numéro de série et numéro terminal manquants");
        }

        TPE tpe = findOrCreateTPE(numeroSerie, numeroTerminal, numeroAffiliation);
        boolean isNew = tpe.getId() == null;

        if (shouldSkipStaleTerminatedRow(tpe, terminated, active, effectiveTpeDate)) {
            result.setSkippedRows(result.getSkippedRows() + 1);
            log.info("Ligne TERMINATED ignoree pour la serie {} car une affectation plus recente existe",
                    firstNonBlank(numeroSerie, tpe.getNumeroSerie(), numeroTerminal, numeroAffiliation));
            return;
        }

        String safeRaisonSociale = firstNonBlank(raisonSociale, numeroAffiliation, numeroSerie);
        String safeActivite = blankToNull(activite);
        String normalizedNumeroCompte = normalizeNumeroCompte(numeroCompte);
        String safeNumeroCompte = parcUpdate
                ? blankToNull(normalizedNumeroCompte)
                : firstNonBlank(normalizedNumeroCompte, numeroAffiliation, numeroSerie);
        String safeCodeAgence = firstNonBlank(codeAgence, "000");

        Commercant commercant = findOrCreateCommercant(safeRaisonSociale, safeActivite, safeNumeroCompte, numeroAffiliation, numeroTerminal, adresse, codePostal, safeCodeAgence, telephone, email, loyer, typeValue, !parcUpdate);

        if (blankToNull(typeValue) != null || tpe.getTypeTPE() == null) {
            tpe.setTypeTPE(resolveTypeTPEValue(typeValue));
        }

        if (blankToNull(numeroSerie) != null || isNew) {
            tpe.setNumeroSerie(resolveSafeNumeroSerie(numeroSerie, numeroTerminal, numeroAffiliation, tpe.getId()));
        }
        assignNumeroTerminal(tpe, numeroTerminal, numeroSerie, result);
        if (!parcUpdate && blankToNull(numeroAffiliation) != null) {
            tpe.setNumeroAffiliation(blankToNull(numeroAffiliation));
        }
        if (terminated) {
            tpe.setStatut(StatutTPE.HORS_SERVICE);
        } else if (Boolean.TRUE.equals(active)) {
            tpe.setStatut(StatutTPE.AFFECTE);
        } else if (!parcUpdate) {
            tpe.setStatut(StatutTPE.DISPONIBLE);
        }
        tpe.setMarque(firstNonBlank(marque, tpe.getMarque()));
        tpe.setModele(firstNonBlank(readAnyString(row, headers, formatter, "CODE_TPE", "MODELE", "MODELE_TPE", "MODEL"), tpe.getModele()));
        if (effectiveTpeDate != null) {
            tpe.setDateAcquisition(effectiveTpeDate);
            tpe.setDateMiseEnService(effectiveTpeDate);
        }
        if (blankToNull(mcc) != null) {
            tpe.setMcc(blankToNull(mcc));
        }
        if (Boolean.TRUE.equals(active)) {
            tpe.setCommercant(commercant);
        }
        String commentaire = buildCommentaire(previlegeSecteur, operateur, tauxCommission, tauxCommissionInter, nCompteIntern, groupe, numSeq);
        if (blankToNull(commentaire) != null) {
            tpe.setCommentaire(commentaire);
        }

        tpeRepository.save(tpe);
        if (isNew) {
            result.setImportedRows(result.getImportedRows() + 1);
        } else {
            result.setUpdatedRows(result.getUpdatedRows() + 1);
        }

        Demande demande = upsertDemande(row, headers, formatter, result, commercant, tpe, active, terminated, numeroAffiliation, valueDate, dateAffiliation, typeValue, safeRaisonSociale, safeActivite, safeNumeroCompte, addressOrNull(adresse), codePostal, safeCodeAgence, telephone, email, loyer, mcc, tauxCommission, tauxCommissionInter);

        if (Boolean.TRUE.equals(active)) {
            upsertActiveAffectation(tpe, commercant, demande, dateAffiliation, result);
        }

        if (terminated || (!parcUpdate && !Boolean.TRUE.equals(active))) {
            deactivateActiveAffectationIfNeeded(tpe);
        }
    }

    private void reconcileImportedValidatedDemandes(TPEImportResult result) {
        List<Demande> pendingDemandes = demandeRepository.findByReferenceStartingWithAndStatut("IMP-", StatutDemande.VALIDEE_MONETIQUE);
        for (Demande demande : pendingDemandes) {
            if (demande.getCommercant() == null) {
                continue;
            }

            TPE tpe = resolveTpeForDemande(demande);
            if (tpe == null) {
                continue;
            }

            tpe.setCommercant(demande.getCommercant());
            tpe.setStatut(StatutTPE.AFFECTE);
            tpeRepository.save(tpe);

            upsertActiveAffectation(tpe, demande.getCommercant(), demande, LocalDate.now(), result);

            demande.setStatut(StatutDemande.AFFECTEE);
            demande.setDateCloture(LocalDateTime.now());
            demandeRepository.save(demande);
        }
    }

    private TPE resolveTpeForDemande(Demande demande) {
        String numeroTerminal = normalizeTerminal(demande.getNumeroTerminal());
        if (numeroTerminal != null) {
            Optional<TPE> byTerminal = findTpeByNumeroTerminal(numeroTerminal);
            if (byTerminal.isPresent()) {
                return byTerminal.get();
            }

            Optional<TPE> byCanonicalTerminal = findByCanonicalTerminal(numeroTerminal);
            if (byCanonicalTerminal.isPresent()) {
                return byCanonicalTerminal.get();
            }
        }

        String serie = blankToNull(demande.getSerieTpe());
        if (serie != null) {
            Optional<TPE> bySerie = findTpeByNumeroSerie(serie);
            if (bySerie.isPresent()) {
                return bySerie.get();
            }

            Optional<TPE> byCanonicalSerie = findByCanonicalSerie(serie);
            if (byCanonicalSerie.isPresent()) {
                return byCanonicalSerie.get();
            }
        }

        return null;
    }

    private Demande upsertDemande(Row row,
                                  Map<String, Integer> headers,
                                  DataFormatter formatter,
                                  TPEImportResult result,
                                  Commercant commercant,
                                  TPE tpe,
                                  Boolean active,
                                  boolean terminated,
                                  String numeroAffiliation,
                                  Integer valueDate,
                                  LocalDate dateAffiliation,
                                  String typeValue,
                                  String raisonSociale,
                                  String activite,
                                  String numeroCompte,
                                  String adresse,
                                  String codePostal,
                                  String codeAgence,
                                  String telephone,
                                  String email,
                                  String loyer,
                                  String mcc,
                                  String tauxCommission,
                                  String tauxCommissionInter) {
                    String uniqueTpeKey = firstNonBlank(
                        blankToNull(tpe.getNumeroSerie()),
                        blankToNull(tpe.getNumeroTerminal()),
                        blankToNull(numeroAffiliation),
                        "SANS_CLE"
                    ).replaceAll("[^A-Za-z0-9_-]", "_");
                    String reference = "IMP-" + numeroAffiliation + "-" + uniqueTpeKey;
        Demande demande = demandeRepository.findFirstByReferenceOrderByLastModifiedDateDescIdDesc(reference).orElseGet(Demande::new);

        User importUser = resolveImportUser();

        demande.setReference(reference);
        demande.setTypeDemande(parseDemandeType(typeValue));
        demande.setCommercant(commercant);
        demande.setDemandeur(importUser);
        demande.setInputer(importUser);
        demande.setStatut(terminated ? StatutDemande.CLOTUREE : (Boolean.TRUE.equals(active) ? StatutDemande.AFFECTEE : StatutDemande.VALIDEE_MONETIQUE));
        demande.setDateSaisieTaux(LocalDateTime.now());
        demande.setDateValidation(LocalDateTime.now());
        demande.setDateCloture((terminated || Boolean.TRUE.equals(active)) ? LocalDateTime.now() : null);
        demande.setDescription("Import automatique depuis fichier Excel");
        demande.setCommentaireValidation(Boolean.TRUE.equals(active) ? "Affectation automatique après import" : "Demande préparée automatiquement depuis import");
        if (terminated) {
            demande.setCommentaireValidation("TPE cloture selon le statut ACTIVE du fichier import");
        }
        demande.setUrgence(com.banque.abc.tpe.entity.enums.Urgence.NORMALE);

        demande.setRaisonSociale(raisonSociale);
        demande.setActivite(activite);
        demande.setNumeroCompte(numeroCompte);
        demande.setAdresse(adresse);
        demande.setCodePostal(codePostal);
        demande.setCodeAgence(codeAgence);
        demande.setTelephone(telephone);
        demande.setMcc(mcc);
        demande.setTauxCommission(parseDouble(tauxCommission));
        demande.setTauxCommissionInter(parseDouble(tauxCommissionInter));
        demande.setLoyer(parseDouble(loyer));
        demande.setSerieTpe(tpe.getNumeroSerie());
        demande.setNumeroTerminal(tpe.getNumeroTerminal());
        demande.setValueDate(resolveValueDate(valueDate));

        if (parseDemandeType(typeValue) == TypeTPE.MOBILE) {
            demande.setRib(numeroCompte);
        }

        return demandeRepository.save(demande);
    }

    private Commercant findOrCreateCommercant(String raisonSociale,
                                              String activite,
                                              String numeroCompte,
                                              String numeroAffiliation,
                                              String numeroTerminal,
                                              String adresse,
                                              String codePostal,
                                              String codeAgence,
                                              String telephone,
                                              String email,
                                              String loyer,
                                              String typeValue,
                                              boolean allowNumeroCompteFallback) {
        String normalizedCodeAgence = firstNonBlank(blankToNull(codeAgence), "000");
        String normalizedRaisonSociale = firstNonBlank(blankToNull(raisonSociale), "COMMERCANT_SANS_NOM");
        String normalizedAdresse = blankToNull(adresse);
        String normalizedNumeroCompte = firstNonBlank(
                normalizeNumeroCompte(numeroCompte),
                allowNumeroCompteFallback ? blankToNull(numeroAffiliation) : null
        );
        String creationNumeroCompte = firstNonBlank(
                normalizedNumeroCompte,
                buildGeneratedNumeroCompte(normalizedRaisonSociale, numeroAffiliation)
        );

        Commercant commercant = commercantRepository
            .findForImportExact(
                normalizedRaisonSociale,
                normalizedNumeroCompte,
                normalizedCodeAgence,
                normalizedAdresse
            )
            .stream()
            .findFirst()
            .orElse(null);

        if (commercant == null && !"COMMERCANT_SANS_NOM".equals(normalizedRaisonSociale)) {
            commercant = commercantRepository.findFirstByRaisonSocialeOrderByLastModifiedDateDescIdDesc(normalizedRaisonSociale).orElse(null);
        }

        if (commercant == null) {
            commercant = new Commercant();
            commercant.setNumeroCompte(creationNumeroCompte);
            commercant.setCodeAgence(normalizedCodeAgence);
            commercant.setRaisonSociale(normalizedRaisonSociale);
            commercant.setStatut(StatutCommercant.ACTIF);
        }

        String incomingRaisonSociale = "COMMERCANT_SANS_NOM".equals(normalizedRaisonSociale) ? null : normalizedRaisonSociale;
        commercant.setRaisonSociale(firstNonBlank(incomingRaisonSociale, blankToNull(commercant.getRaisonSociale()), normalizedRaisonSociale));
        commercant.setActivite(firstNonBlank(blankToNull(activite), blankToNull(commercant.getActivite()), "INCONNUE"));
        String existingNumeroCompte = blankToNull(commercant.getNumeroCompte());
        if (isTechnicalNumeroCompte(existingNumeroCompte, numeroTerminal, numeroAffiliation)) {
            commercant.setNumeroCompte(firstNonBlank(normalizedNumeroCompte, creationNumeroCompte));
        } else {
            commercant.setNumeroCompte(firstNonBlank(existingNumeroCompte, normalizedNumeroCompte, creationNumeroCompte));
        }
        commercant.setCodeAgence(resolveStoredCodeAgence(commercant.getCodeAgence(), normalizedCodeAgence));

        commercant.setAdresse(firstNonBlank(normalizedAdresse, blankToNull(commercant.getAdresse())));
        commercant.setCodePostal(firstNonBlank(blankToNull(codePostal), blankToNull(commercant.getCodePostal())));
        commercant.setTelephone(firstNonBlank(blankToNull(telephone), blankToNull(commercant.getTelephone())));

        String safeEmail = resolveSafeMerchantEmail(commercant.getId(), blankToNull(email));
        if (safeEmail != null || commercant.getEmail() == null) {
            commercant.setEmail(safeEmail);
        }

        Double parsedLoyer = parseDouble(loyer);
        if (parsedLoyer != null) {
            commercant.setLoyer(parsedLoyer);
        }
        if (blankToNull(typeValue) != null || commercant.getTypeCommerce() == null) {
            commercant.setTypeCommerce(parseDemandeType(typeValue));
        }
        commercant.setStatut(StatutCommercant.ACTIF);

        return commercantRepository.save(commercant);
    }

    private TPE findOrCreateTPE(String numeroSerie, String numeroTerminal, String numeroAffiliation) {
        TPE tpe = null;

        if (numeroSerie != null && !numeroSerie.isBlank()) {
            tpe = findTpeByNumeroSerie(numeroSerie).orElse(null);
            if (tpe == null) {
                tpe = findByCanonicalSerie(numeroSerie).orElse(null);
            }
        }

        if (tpe == null && numeroTerminal != null && !numeroTerminal.isBlank()) {
            TPE terminalMatch = findTpeByNumeroTerminal(numeroTerminal).orElse(null);
            if (terminalMatch == null) {
                terminalMatch = findByCanonicalTerminal(numeroTerminal).orElse(null);
            }
            if (terminalMatch != null && canReuseTerminalMatch(terminalMatch, numeroSerie)) {
                tpe = terminalMatch;
            }
        }

        if (tpe == null && numeroAffiliation != null && !numeroAffiliation.isBlank()) {
            tpe = findTpeByNumeroAffiliation(numeroAffiliation).orElse(null);
        }

        return tpe != null ? tpe : new TPE();
    }

    private boolean shouldSkipStaleTerminatedRow(TPE tpe, boolean terminated, Boolean active, LocalDate incomingDate) {
        if (!terminated || Boolean.TRUE.equals(active) || tpe == null || tpe.getId() == null) {
            return false;
        }

        boolean currentIsActive = tpe.getStatut() == StatutTPE.AFFECTE || tpe.getCommercant() != null;
        if (!currentIsActive) {
            return false;
        }

        LocalDate currentDate = firstNonNull(tpe.getDateMiseEnService(), tpe.getDateAcquisition());
        return incomingDate != null && currentDate != null && incomingDate.isBefore(currentDate);
    }

    private boolean canReuseTerminalMatch(TPE terminalMatch, String incomingNumeroSerie) {
        String incomingSerie = blankToNull(incomingNumeroSerie);
        if (incomingSerie == null) {
            return true;
        }

        String existingSerie = blankToNull(terminalMatch.getNumeroSerie());
        if (existingSerie == null || existingSerie.startsWith("SERIE-")) {
            return true;
        }

        return Objects.equals(canonicalizeKey(existingSerie), canonicalizeKey(incomingSerie));
    }

    private void assignNumeroTerminal(TPE tpe, String numeroTerminal, String incomingNumeroSerie, TPEImportResult result) {
        String normalizedTerminal = blankToNull(numeroTerminal);
        if (normalizedTerminal == null) {
            return;
        }

        Optional<TPE> existing = findTpeByNumeroTerminal(normalizedTerminal);
        if (existing.isEmpty()) {
            existing = findByCanonicalTerminal(normalizedTerminal);
        }

        if (existing.isEmpty() || (tpe.getId() != null && existing.get().getId().equals(tpe.getId()))) {
            tpe.setNumeroTerminal(normalizedTerminal);
            return;
        }

        TPE terminalOwner = existing.get();
        if (canReleaseTerminalFromOwner(terminalOwner, incomingNumeroSerie)) {
            terminalOwner.setNumeroTerminal(null);
            tpeRepository.save(terminalOwner);
            tpe.setNumeroTerminal(normalizedTerminal);
            log.info("TID {} réaffecté à la série {}", normalizedTerminal, firstNonBlank(incomingNumeroSerie, tpe.getNumeroSerie()));
            return;
        }

        result.getErrors().add("TID " + normalizedTerminal + " ignoré pour la série "
                + firstNonBlank(incomingNumeroSerie, tpe.getNumeroSerie())
                + " car il est déjà utilisé par la série " + terminalOwner.getNumeroSerie());
    }

    private boolean canReleaseTerminalFromOwner(TPE terminalOwner, String incomingNumeroSerie) {
        String ownerSerie = blankToNull(terminalOwner.getNumeroSerie());
        if (ownerSerie == null || !ownerSerie.startsWith("SERIE-")) {
            return false;
        }

        if (terminalOwner.getCommercant() != null) {
            return false;
        }

        if (terminalOwner.getId() != null && findLatestActiveAffectationByTpeId(terminalOwner.getId()).isPresent()) {
            return false;
        }

        String incomingSerie = canonicalizeKey(incomingNumeroSerie);
        return incomingSerie == null || !Objects.equals(canonicalizeKey(ownerSerie), incomingSerie);
    }

    private String resolveSafeNumeroSerie(String numeroSerie,
                                          String numeroTerminal,
                                          String numeroAffiliation,
                                          Long currentTpeId) {
        String baseSerie = firstNonBlank(numeroSerie, numeroTerminal != null ? "SERIE-" + numeroTerminal : null);
        if (baseSerie == null) {
            throw new BusinessException("numéro de série introuvable");
        }

        Optional<TPE> existingBySerie = findTpeByNumeroSerie(baseSerie);
        if (existingBySerie.isEmpty()) {
            return baseSerie;
        }

        if (currentTpeId != null && existingBySerie.get().getId().equals(currentTpeId)) {
            return baseSerie;
        }

        String candidate = firstNonBlank(
                numeroTerminal != null ? "SERIE-" + numeroTerminal : null,
                numeroAffiliation != null ? "SERIE-" + numeroAffiliation : null,
                baseSerie + "-IMP"
        );

        Optional<TPE> existingByCandidate = findTpeByNumeroSerie(candidate);
        if (existingByCandidate.isEmpty()) {
            return candidate;
        }

        if (currentTpeId != null && existingByCandidate.get().getId().equals(currentTpeId)) {
            return candidate;
        }

        return candidate + "-" + Math.abs(candidate.hashCode());
    }

    private String resolveNumeroSerie(Row row, Map<String, Integer> headers, DataFormatter formatter) {
        return firstNonBlank(
                readString(row, headers, formatter, "SERIE_TPE"),
                readString(row, headers, formatter, "NUMERO_SERIE"),
                readString(row, headers, formatter, "N_SERIE"),
                readString(row, headers, formatter, "N_SERIE_TPE"),
                readString(row, headers, formatter, "SERIAL_NUMBER"),
                readString(row, headers, formatter, "SERIE")
        );
    }

    private void upsertActiveAffectation(TPE tpe, Commercant commercant, Demande demande, LocalDate dateAffectation, TPEImportResult result) {
        Affectation affectation = demande != null && demande.getId() != null
                ? affectationRepository.findByDemandeIdOrderByActifDescDateAffectationDescIdDesc(demande.getId())
                    .stream()
                    .findFirst()
                    .orElse(null)
                : null;

        if (affectation == null) {
            affectation = findLatestActiveAffectationByTpeId(tpe.getId()).orElse(null);
        }

        if (affectation == null) {
            affectation = new Affectation();
            affectation.setTpe(tpe);
            result.setAffectedRows(result.getAffectedRows() + 1);
        }

        affectation.setCommercant(commercant);
        affectation.setDemande(demande);
        affectation.setDateAffectation(dateAffectation != null ? dateAffectation : LocalDate.now());
        affectation.setDateMiseEnService(dateAffectation);
        affectation.setDateFin(null);
        affectation.setActif(true);
        affectation.setCommentaire("Import Excel");

        affectationRepository.save(affectation);
    }

    private void deactivateActiveAffectationIfNeeded(TPE tpe) {
        findLatestActiveAffectationByTpeId(tpe.getId()).ifPresent(affectation -> {
            affectation.setActif(false);
            affectation.setDateFin(LocalDate.now());
            affectationRepository.save(affectation);
        });
    }

    private TPEImportRecordDTO toDto(TPEImportRecord record) {
        return TPEImportRecordDTO.builder()
                .id(record.getId())
                .nAffiliation(record.getNAffiliation())
                .sourceRowNumber(record.getSourceRowNumber())
                .sourceFileName(record.getSourceFileName())
                .typeTPE(record.getTypeTPE())
                .numeroSerie(record.getNumeroSerie())
                .numeroTerminal(record.getNumeroTerminal())
                .raisonSociale(record.getRaisonSociale())
                .activite(record.getActivite())
                .mcc(record.getMcc())
                .numeroCompte(record.getNumeroCompte())
                .codeAgence(record.getCodeAgence())
                .adresse(record.getAdresse())
                .codePostal(record.getCodePostal())
                .telephone(record.getTelephone())
                .email(record.getEmail())
                .privilegeSecteur(record.getPrivilegeSecteur())
                .tauxCommission(record.getTauxCommission())
                .tauxCommissionInter(record.getTauxCommissionInter())
                .loyer(record.getLoyer())
                .nCompteIntern(record.getNCompteIntern())
                .groupe(record.getGroupe())
                .numSeq(record.getNumSeq())
                .active(record.getActive())
                .valueDate(record.getValueDate())
                .dateAffiliation(record.getDateAffiliation())
                .createdDate(record.getCreatedDate())
                .lastModifiedDate(record.getLastModifiedDate())
                .build();
    }

    private Map<String, Integer> readHeaders(Row headerRow) {
        if (headerRow == null) {
            throw new BusinessException("La première ligne du fichier doit contenir les en-têtes");
        }

        Map<String, Integer> headers = new HashMap<>();
        DataFormatter formatter = new DataFormatter(Locale.FRANCE);
        for (Cell cell : headerRow) {
            String header = normalizeHeader(formatter.formatCellValue(cell));
            if (!header.isBlank()) {
                headers.putIfAbsent(header, cell.getColumnIndex());
            }
        }
        return headers;
    }

    private int findHeaderRowIndex(Sheet sheet) {
        int firstRow = sheet.getFirstRowNum();
        int maxProbeRow = Math.min(sheet.getLastRowNum(), firstRow + 20);
        for (int rowIndex = firstRow; rowIndex <= maxProbeRow; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            Map<String, Integer> headers = readHeaders(row);
            if (looksLikeTpeImportHeader(headers)) {
                return rowIndex;
            }
        }

        throw new BusinessException("Impossible de trouver les en-tetes TPE dans le fichier Excel");
    }

    private boolean looksLikeTpeImportHeader(Map<String, Integer> headers) {
        return headers.containsKey("N_AFFILIATION")
                || headers.containsKey("N_TERMINAL")
                || headers.containsKey("SERIE_TPE")
                || headers.containsKey("NUMERO_SERIE")
                || headers.containsKey("TYPE_TPE");
    }

    private String readString(Row row, Map<String, Integer> headers, DataFormatter formatter, String headerName) {
        Integer index = headers.get(normalizeHeader(headerName));
        if (index == null) {
            return null;
        }
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }
        String value = formatter.formatCellValue(cell);
        return blankToNull(value);
    }

    private String readAnyString(Row row, Map<String, Integer> headers, DataFormatter formatter, String... headerNames) {
        if (headerNames == null) {
            return null;
        }

        for (String headerName : headerNames) {
            String value = readString(row, headers, formatter, headerName);
            if (blankToNull(value) != null) {
                return value;
            }
        }

        return null;
    }

    private String readNumeroTerminal(Row row, Map<String, Integer> headers, DataFormatter formatter) {
        return readAnyString(
                row,
                headers,
                formatter,
                "N_TERMINAL",
                "ID_TERMINAL",
                "ID TERMINAL",
                "NUMERO_TERMINAL",
                "NUMERO TERMINAL",
                "TID"
        );
    }

    private String resolveCodeAgence(String explicitCodeAgence, String numeroTerminal) {
        return firstNonBlank(
                explicitCodeAgence,
                deriveCodeAgenceFromTerminal(numeroTerminal),
                "000"
        );
    }

    private String deriveCodeAgenceFromTerminal(String numeroTerminal) {
        String terminal = normalizeTerminal(numeroTerminal);
        if (terminal == null || !terminal.matches("\\d{5,}") || !isValidLuhn(terminal)) {
            return null;
        }

        return terminal.substring(2, 5);
    }

    private boolean isValidLuhn(String value) {
        int sum = 0;
        boolean doubleDigit = false;

        for (int index = value.length() - 1; index >= 0; index--) {
            int digit = value.charAt(index) - '0';
            if (digit < 0 || digit > 9) {
                return false;
            }

            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            doubleDigit = !doubleDigit;
        }

        return sum % 10 == 0;
    }

    private Boolean readBoolean(Row row, Map<String, Integer> headers, DataFormatter formatter, String headerName) {
        String value = readString(row, headers, formatter, headerName);
        if (value == null) {
            return false;
        }

        String normalized = normalizeHeader(value);
        return "1".equals(normalized)
                || "TRUE".equals(normalized)
                || "OUI".equals(normalized)
                || "YES".equals(normalized)
                || "Y".equals(normalized)
                || "ACTIF".equals(normalized);
    }

    private boolean isTerminatedActiveStatus(Row row, Map<String, Integer> headers, DataFormatter formatter) {
        String rawActive = readString(row, headers, formatter, "ACTIVE");
        String normalized = rawActive == null ? "" : normalizeHeader(rawActive);
        return "TERMINATED".equals(normalized)
                || "TERMINETED".equals(normalized)
                || "TERMIANTED".equals(normalized)
                || "TERMINE".equals(normalized)
                || "CLOTURE".equals(normalized)
                || "CLOTUREE".equals(normalized)
                || "RESILIE".equals(normalized);
    }

    private Boolean resolveActiveStatus(Row row,
                                        Map<String, Integer> headers,
                                        DataFormatter formatter,
                                        Integer valueDate,
                                        LocalDate dateAffiliation,
                                        String numeroTerminal) {
        String rawActive = readString(row, headers, formatter, "ACTIVE");
        String normalized = rawActive == null ? null : normalizeHeader(rawActive);

        if (normalized != null && !normalized.isBlank()) {
            if ("1".equals(normalized) || "TRUE".equals(normalized) || "OUI".equals(normalized)
                    || "YES".equals(normalized) || "Y".equals(normalized) || "ACTIF".equals(normalized)
                    || "A".equals(normalized) || "ACTIVE".equals(normalized)) {
                return true;
            }
            if ("0".equals(normalized) || "FALSE".equals(normalized) || "NON".equals(normalized)
                    || "NO".equals(normalized) || "N".equals(normalized) || "INACTIF".equals(normalized)
                    || "I".equals(normalized) || "INACTIVE".equals(normalized)
                    || "TERMINATED".equals(normalized) || "TERMINETED".equals(normalized)
                    || "TERMIANTED".equals(normalized) || "TERMINE".equals(normalized)
                    || "CLOTURE".equals(normalized) || "CLOTUREE".equals(normalized)
                    || "RESILIE".equals(normalized)) {
                return false;
            }
        }

        // Fallback for source files where ACTIVE is missing/unreliable.
        if (dateAffiliation != null) {
            return true;
        }
        if (valueDate != null && blankToNull(numeroTerminal) != null) {
            return true;
        }
        return false;
    }

    private Integer readValueDate(Row row, Map<String, Integer> headers, DataFormatter formatter) {
        String rawValueDate = readString(row, headers, formatter, "VALUE_DATE");
        if (rawValueDate == null) {
            return null;
        }

        try {
            double parsed = Double.parseDouble(rawValueDate.replace(',', '.').trim());
            return resolveValueDate((int) parsed);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer resolveValueDate(Integer valueDate) {
        if (valueDate == null) {
            return 1;
        }
        return valueDate == 2 ? 2 : 1;
    }

    private LocalDate readLocalDate(Row row, Map<String, Integer> headers, DataFormatter formatter, String headerName) {
        Integer index = headers.get(normalizeHeader(headerName));
        if (index == null) {
            return null;
        }

        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }

            double numericValue = cell.getNumericCellValue();
            if (numericValue >= 20000 && numericValue <= 60000) {
                return DateUtil.getJavaDate(numericValue).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
        }

        String value = formatter.formatCellValue(cell);
        if (value == null || value.isBlank()) {
            return null;
        }

        value = value.trim();
        try {
            if (value.contains("T")) {
                return LocalDateTime.parse(value).toLocalDate();
            }
            if (value.matches("\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?")) {
                return LocalDate.parse(value.substring(0, 10));
            }
            if (value.length() == 8 && value.chars().allMatch(Character::isDigit)) {
                return LocalDate.parse(value, java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
            }
            return LocalDate.parse(value);
        } catch (Exception ex) {
            log.debug("Impossible de parser la date '{}' pour la colonne {}", value, headerName);
            return null;
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(value.replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String resolveTypeTPEValue(String value) {
        String type = blankToNull(value);
        return type != null ? type.trim().toUpperCase() : "TPE";
    }

    private TypeTPE parseDemandeType(String value) {
        if (value == null) {
            return TypeTPE.TPE;
        }

        String normalized = normalizeHeader(value);
        if (normalized.contains("MOBILE") || normalized.contains("ECOM") || normalized.contains("COMMERCE") || normalized.contains("ONLINE")) {
            return TypeTPE.MOBILE;
        }
        return TypeTPE.TPE;
    }

    private String buildCommentaire(String previlegeSecteur,
                                    String operateur,
                                    String tauxCommission,
                                    String tauxCommissionInter,
                                    String nCompteIntern,
                                    String groupe,
                                    String numSeq) {
        StringBuilder builder = new StringBuilder();
        appendPart(builder, "Privilege secteur", previlegeSecteur);
        appendPart(builder, "Operateur", operateur);
        appendPart(builder, "Taux commission", tauxCommission);
        appendPart(builder, "Taux commission inter", tauxCommissionInter);
        appendPart(builder, "Compte interne", nCompteIntern);
        appendPart(builder, "Groupe", groupe);
        appendPart(builder, "Num seq", numSeq);
        return builder.length() == 0 ? null : builder.toString();
    }

    private void appendPart(StringBuilder builder, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(" | ");
        }
        builder.append(label).append(": ").append(value.trim());
    }

    private String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }
        String normalized = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_");
        return normalized.replaceAll("^_+|_+$", "");
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value
                .replaceAll("[\\u0000\\u200B-\\u200D\\uFEFF]", "")
                .replace('\u00A0', ' ')
                .trim();
        return cleaned.isBlank() ? null : cleaned;
    }

    private String normalizeNumeroCompte(String value) {
        String raw = blankToNull(value);
        if (raw == null) {
            return null;
        }

        String normalized = raw.replaceAll("\\s+", "").replace(',', '.');
        try {
            BigDecimal bigDecimal = new BigDecimal(normalized);
            String plain = bigDecimal.toPlainString();
            if (plain.contains(".")) {
                plain = plain.substring(0, plain.indexOf('.'));
            }
            String digitsOnly = plain.replaceAll("\\D", "");
            return digitsOnly.isBlank() ? plain : digitsOnly;
        } catch (NumberFormatException ex) {
            String digitsOnly = normalized.replaceAll("\\D", "");
            return digitsOnly.isBlank() ? normalized : digitsOnly;
        }
    }

    private String buildGeneratedNumeroCompte(String raisonSociale, String reference) {
        String base = firstNonBlank(raisonSociale, reference, "IMPORT");
        return "SANS_RIB_" + Integer.toUnsignedString(base.hashCode());
    }

    private boolean isTechnicalNumeroCompte(String numeroCompte, String numeroTerminal, String numeroAffiliation) {
        String normalizedCompte = canonicalizeKey(numeroCompte);
        if (normalizedCompte == null) {
            return true;
        }

        if (normalizedCompte.startsWith("SANSRIB")) {
            return true;
        }

        return Objects.equals(normalizedCompte, canonicalizeKey(numeroTerminal))
                || Objects.equals(normalizedCompte, canonicalizeKey(numeroAffiliation));
    }

    private String resolveStoredCodeAgence(String existingCodeAgence, String incomingCodeAgence) {
        String existing = blankToNull(existingCodeAgence);
        String incoming = firstNonBlank(incomingCodeAgence, "000");

        if (existing == null) {
            return incoming;
        }
        if ("000".equals(existing) && !"000".equals(incoming)) {
            return incoming;
        }
        return existing;
    }

    private String normalizeTerminal(String value) {
        String raw = blankToNull(value);
        if (raw == null) {
            return null;
        }

        String normalized = raw.replaceAll("\\s+", "").replace(',', '.');
        try {
            BigDecimal bigDecimal = new BigDecimal(normalized);
            String plain = bigDecimal.toPlainString();
            if (plain.contains(".")) {
                plain = plain.substring(0, plain.indexOf('.'));
            }
            String digitsOnly = plain.replaceAll("\\D", "");
            return digitsOnly.isBlank() ? plain : digitsOnly;
        } catch (NumberFormatException ex) {
            return normalized;
        }
    }

    private Optional<TPE> findByCanonicalTerminal(String terminal) {
        String canonicalTerminal = canonicalizeKey(terminal);
        if (canonicalTerminal == null) {
            return Optional.empty();
        }

        return tpeRepository.findAll().stream()
                .filter(tpe -> canonicalTerminal.equals(canonicalizeKey(tpe.getNumeroTerminal())))
                .findFirst();
    }

    private Optional<TPE> findTpeByNumeroTerminal(String terminal) {
        return Optional.ofNullable(blankToNull(terminal))
                .flatMap(tpeRepository::findFirstByNumeroTerminalOrderByLastModifiedDateDescIdDesc);
    }

    private Optional<TPE> findByCanonicalSerie(String serie) {
        String canonicalSerie = canonicalizeKey(serie);
        if (canonicalSerie == null) {
            return Optional.empty();
        }

        return tpeRepository.findAll().stream()
                .filter(tpe -> canonicalSerie.equals(canonicalizeKey(tpe.getNumeroSerie())))
                .findFirst();
    }

    private Optional<TPE> findTpeByNumeroSerie(String serie) {
        return Optional.ofNullable(blankToNull(serie))
                .flatMap(tpeRepository::findFirstByNumeroSerieOrderByLastModifiedDateDescIdDesc);
    }

    private Optional<TPE> findTpeByNumeroAffiliation(String affiliation) {
        return Optional.ofNullable(blankToNull(affiliation))
                .flatMap(tpeRepository::findFirstByNumeroAffiliationOrderByLastModifiedDateDescIdDesc);
    }

    private Optional<Affectation> findLatestActiveAffectationByTpeId(Long tpeId) {
        if (tpeId == null) {
            return Optional.empty();
        }

        return affectationRepository.findActiveByTpeIdOrderByDateAffectationDescIdDesc(tpeId)
                .stream()
                .findFirst();
    }

    private String canonicalizeKey(String value) {
        String raw = blankToNull(value);
        if (raw == null) {
            return null;
        }
        String canonical = raw.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        return canonical.isBlank() ? null : canonical;
    }

    private String resolveSafeMerchantEmail(Long commercantId, String email) {
        if (email == null) {
            return null;
        }

        Optional<Commercant> existing = commercantRepository.findFirstByEmailOrderByLastModifiedDateDescIdDesc(email);
        if (existing.isEmpty()) {
            return email;
        }

        if (commercantId != null && existing.get().getId().equals(commercantId)) {
            return email;
        }

        return null;
    }

    private String addressOrNull(String value) {
        return blankToNull(value);
    }

    private com.banque.abc.tpe.entity.User resolveImportUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return userRepository.findById(principal.getId())
                    .orElseGet(this::fallbackImportUser);
        }
        return fallbackImportUser();
    }

    private com.banque.abc.tpe.entity.User fallbackImportUser() {
        return userRepository.findByUsername("admin")
                .orElseThrow(() -> new BusinessException("Utilisateur d'import introuvable"));
    }

    private boolean isRowEmpty(Row row, DataFormatter formatter) {
        for (Cell cell : row) {
            if (!formatter.formatCellValue(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }
}
