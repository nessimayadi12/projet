package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.tpe.TPEImportResult;
import com.banque.abc.tpe.dto.tpe.TPEImportRecordDTO;
import com.banque.abc.tpe.entity.Affectation;
import com.banque.abc.tpe.entity.Commercant;
import com.banque.abc.tpe.entity.Demande;
import com.banque.abc.tpe.entity.TPEImportRecord;
import com.banque.abc.tpe.entity.TPE;
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
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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

            Map<String, Integer> headers = readHeaders(sheet.getRow(sheet.getFirstRowNum()));
            DataFormatter formatter = new DataFormatter(Locale.FRANCE);

            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
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
        Map<String, Object> rawData = new LinkedHashMap<>();
        rawData.put("TYPE_TPE", readString(row, headers, formatter, "TYPE_TPE"));
        rawData.put("N_AFFILIATION", readString(row, headers, formatter, "N_AFFILIATION"));
        rawData.put("N_TERMINAL", readString(row, headers, formatter, "N_TERMINAL"));
        rawData.put("RAISON_SOCIALE", readString(row, headers, formatter, "RAISON_SOCIALE"));
        rawData.put("ACTIVITE", readString(row, headers, formatter, "ACTIVITE"));
        rawData.put("MCC", readString(row, headers, formatter, "MCC"));
        rawData.put("N_COMPTE", readString(row, headers, formatter, "N_COMPTE"));
        rawData.put("CODE_AGENCE", readString(row, headers, formatter, "CODE_AGENCE"));
        rawData.put("ADRESSE", readString(row, headers, formatter, "ADRESSE"));
        rawData.put("CODE_POSTAL", readString(row, headers, formatter, "CODE_POSTAL"));
        rawData.put("TELEPHONE", readString(row, headers, formatter, "TELEPHONE"));
        rawData.put("EMAIL", firstNonBlank(readString(row, headers, formatter, "EMAIL"), readString(row, headers, formatter, "MAIL"), readString(row, headers, formatter, "EMAIL_NOTIFICATION")));
        rawData.put("PREVILEGE_SECTEUR", readString(row, headers, formatter, "PREVILEGE_SECTEUR"));
        rawData.put("TAUX_COMMISSION", readString(row, headers, formatter, "TAUX_COMMISSION"));
        rawData.put("TAUX_COMMISSION_INTER", readString(row, headers, formatter, "TAUX_COMMISSION_INTER"));
        rawData.put("LOYER", readString(row, headers, formatter, "LOYER"));
        rawData.put("N_COMPTE_INTERN", readString(row, headers, formatter, "N_COMPTE_INTERN"));
        rawData.put("GROUP", readString(row, headers, formatter, "GROUP"));
        rawData.put("NUM_SEQ", readString(row, headers, formatter, "NUM_SEQ"));
        Boolean active = resolveActiveStatus(row, headers, formatter,
            readLocalDate(row, headers, formatter, "VALUE_DATE"),
            readLocalDate(row, headers, formatter, "DATE_AFFILIATION"),
            readString(row, headers, formatter, "N_TERMINAL"));
        rawData.put("ACTIVE", active);
        rawData.put("VALUE_DATE", readString(row, headers, formatter, "VALUE_DATE"));
        rawData.put("DATE_AFFILIATION", readString(row, headers, formatter, "DATE_AFFILIATION"));

        TPEImportRecord record = tpeImportRecordRepository.findByNAffiliation(numeroAffiliation)
                .orElseGet(TPEImportRecord::new);
        record.setNAffiliation(numeroAffiliation);
        record.setSourceRowNumber(sourceRowNumber);
        record.setSourceFileName(sourceFileName);
        record.setTypeTPE(firstNonBlank(readString(row, headers, formatter, "TYPE_TPE"), null));
        String resolvedNumeroSerie = resolveNumeroSerie(row, headers, formatter);
        record.setNumeroSerie(firstNonBlank(resolvedNumeroSerie, numeroAffiliation));
        record.setNumeroTerminal(readString(row, headers, formatter, "N_TERMINAL"));
        record.setRaisonSociale(firstNonBlank(readString(row, headers, formatter, "RAISON_SOCIALE"), numeroAffiliation));
        record.setActivite(readString(row, headers, formatter, "ACTIVITE"));
        record.setMcc(readString(row, headers, formatter, "MCC"));
        record.setNumeroCompte(firstNonBlank(readString(row, headers, formatter, "N_COMPTE"), numeroAffiliation));
        record.setCodeAgence(firstNonBlank(readString(row, headers, formatter, "CODE_AGENCE"), "000"));
        record.setAdresse(readString(row, headers, formatter, "ADRESSE"));
        record.setCodePostal(readString(row, headers, formatter, "CODE_POSTAL"));
        record.setTelephone(readString(row, headers, formatter, "TELEPHONE"));
        record.setEmail(firstNonBlank(readString(row, headers, formatter, "EMAIL"), readString(row, headers, formatter, "MAIL"), readString(row, headers, formatter, "EMAIL_NOTIFICATION")));
        record.setPrivilegeSecteur(readString(row, headers, formatter, "PREVILEGE_SECTEUR"));
        record.setTauxCommission(readString(row, headers, formatter, "TAUX_COMMISSION"));
        record.setTauxCommissionInter(readString(row, headers, formatter, "TAUX_COMMISSION_INTER"));
        record.setLoyer(readString(row, headers, formatter, "LOYER"));
        record.setNCompteIntern(readString(row, headers, formatter, "N_COMPTE_INTERN"));
        record.setGroupe(readString(row, headers, formatter, "GROUP"));
        record.setNumSeq(readString(row, headers, formatter, "NUM_SEQ"));
        record.setActive(active);
        record.setValueDate(readLocalDate(row, headers, formatter, "VALUE_DATE"));
        record.setDateAffiliation(readLocalDate(row, headers, formatter, "DATE_AFFILIATION"));
        record.setRawDataJson(objectMapper.writeValueAsString(rawData));

        tpeImportRecordRepository.save(record);
        result.setStoredRows(result.getStoredRows() + 1);
        return numeroAffiliation;
    }

    private void importRow(Row row, Map<String, Integer> headers, DataFormatter formatter, TPEImportResult result, String rawKey) {
        String typeValue = readString(row, headers, formatter, "TYPE_TPE");
        String numeroAffiliation = firstNonBlank(readString(row, headers, formatter, "N_AFFILIATION"), rawKey);
        String numeroSerie = resolveNumeroSerie(row, headers, formatter);
        String numeroTerminal = readString(row, headers, formatter, "N_TERMINAL");
        String raisonSociale = readString(row, headers, formatter, "RAISON_SOCIALE");
        String activite = readString(row, headers, formatter, "ACTIVITE");
        String mcc = readString(row, headers, formatter, "MCC");
        String numeroCompte = readString(row, headers, formatter, "N_COMPTE");
        String codeAgence = readString(row, headers, formatter, "CODE_AGENCE");
        String adresse = readString(row, headers, formatter, "ADRESSE");
        String codePostal = readString(row, headers, formatter, "CODE_POSTAL");
        String telephone = readString(row, headers, formatter, "TELEPHONE");
        String email = firstNonBlank(
                readString(row, headers, formatter, "EMAIL"),
                readString(row, headers, formatter, "MAIL"),
                readString(row, headers, formatter, "EMAIL_NOTIFICATION")
        );
        String previlegeSecteur = readString(row, headers, formatter, "PREVILEGE_SECTEUR");
        String tauxCommission = readString(row, headers, formatter, "TAUX_COMMISSION");
        String tauxCommissionInter = readString(row, headers, formatter, "TAUX_COMMISSION_INTER");
        String loyer = readString(row, headers, formatter, "LOYER");
        String nCompteIntern = readString(row, headers, formatter, "N_COMPTE_INTERN");
        String groupe = readString(row, headers, formatter, "GROUP");
        String numSeq = readString(row, headers, formatter, "NUM_SEQ");
        LocalDate valueDate = readLocalDate(row, headers, formatter, "VALUE_DATE");
        LocalDate dateAffiliation = readLocalDate(row, headers, formatter, "DATE_AFFILIATION");
        Boolean active = resolveActiveStatus(row, headers, formatter, valueDate, dateAffiliation, numeroTerminal);

        if (numeroSerie == null || numeroSerie.isBlank()) {
            throw new BusinessException("numéro de série manquant (colonnes série introuvables)");
        }

        String safeRaisonSociale = firstNonBlank(raisonSociale, numeroAffiliation, numeroSerie);
        String safeActivite = blankToNull(activite);
        String safeNumeroCompte = firstNonBlank(normalizeNumeroCompte(numeroCompte), numeroAffiliation, numeroSerie);
        String safeCodeAgence = firstNonBlank(codeAgence, "000");

        Commercant commercant = findOrCreateCommercant(safeRaisonSociale, safeActivite, safeNumeroCompte, numeroAffiliation, adresse, codePostal, safeCodeAgence, telephone, email, loyer, typeValue);

        TPE tpe = findOrCreateTPE(numeroSerie, numeroAffiliation, numeroTerminal);
        boolean isNew = tpe.getId() == null;

        tpe.setTypeTPE(parseTypeTPE(typeValue));
        tpe.setNumeroSerie(numeroSerie);
        tpe.setNumeroTerminal(blankToNull(numeroTerminal));
        tpe.setNumeroAffiliation(blankToNull(numeroAffiliation != null ? numeroAffiliation : numeroSerie));
        tpe.setStatut(Boolean.TRUE.equals(active) ? StatutTPE.AFFECTE : StatutTPE.DISPONIBLE);
        tpe.setMarque(firstNonBlank(tpe.getMarque(), typeValue));
        tpe.setModele(firstNonBlank(readString(row, headers, formatter, "CODE_TPE"), readString(row, headers, formatter, "SERIE_PUCE")));
        tpe.setDateAcquisition(valueDate);
        tpe.setDateMiseEnService(valueDate);
        tpe.setMcc(blankToNull(mcc));
        tpe.setCommercant(Boolean.TRUE.equals(active) ? commercant : null);
        tpe.setCommentaire(buildCommentaire(previlegeSecteur, tauxCommission, tauxCommissionInter, nCompteIntern, groupe, numSeq));

        tpeRepository.save(tpe);
        if (isNew) {
            result.setImportedRows(result.getImportedRows() + 1);
        } else {
            result.setUpdatedRows(result.getUpdatedRows() + 1);
        }

        Demande demande = upsertDemande(row, headers, formatter, result, commercant, tpe, active, numeroAffiliation, valueDate, dateAffiliation, typeValue, safeRaisonSociale, safeActivite, safeNumeroCompte, addressOrNull(adresse), codePostal, safeCodeAgence, telephone, email, loyer, mcc, tauxCommission, tauxCommissionInter);

        if (Boolean.TRUE.equals(active)) {
            upsertActiveAffectation(tpe, commercant, demande, dateAffiliation != null ? dateAffiliation : valueDate, result);
        }

        if (!Boolean.TRUE.equals(active)) {
            deactivateActiveAffectationIfNeeded(tpe);
        }
    }

    private Demande upsertDemande(Row row,
                                  Map<String, Integer> headers,
                                  DataFormatter formatter,
                                  TPEImportResult result,
                                  Commercant commercant,
                                  TPE tpe,
                                  Boolean active,
                                  String numeroAffiliation,
                                  LocalDate valueDate,
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
        Demande demande = demandeRepository.findByReference(reference).orElseGet(Demande::new);

        demande.setReference(reference);
        demande.setTypeDemande(parseTypeTPE(typeValue));
        demande.setCommercant(commercant);
        demande.setDemandeur(resolveImportUser());
        demande.setStatut(Boolean.TRUE.equals(active) ? StatutDemande.AFFECTEE : StatutDemande.VALIDEE_MONETIQUE);
        demande.setDateValidation(LocalDateTime.now());
        demande.setDateCloture(Boolean.TRUE.equals(active) ? LocalDateTime.now() : null);
        demande.setDescription("Import automatique depuis fichier Excel");
        demande.setCommentaireValidation(Boolean.TRUE.equals(active) ? "Affectation automatique après import" : "Demande préparée automatiquement depuis import");
        demande.setUrgence(com.banque.abc.tpe.entity.enums.Urgence.NORMALE);

        demande.setRaisonSociale(raisonSociale);
        demande.setActivite(activite);
        demande.setNumeroCompte(numeroCompte);
        demande.setAdresse(adresse);
        demande.setCodePostal(codePostal);
        demande.setCodeAgence(codeAgence);
        demande.setTelephone(telephone);
        demande.setEmailNotification(email);
        demande.setMcc(mcc);
        demande.setTauxCommission(parseDouble(tauxCommission));
        demande.setTauxCommissionInter(parseDouble(tauxCommissionInter));
        demande.setLoyer(parseDouble(loyer));
        demande.setSerieTpe(tpe.getNumeroSerie());
        demande.setNumeroTerminal(tpe.getNumeroTerminal());
        demande.setValueDate(valueDate != null ? valueDate.atStartOfDay() : null);

        if (parseTypeTPE(typeValue) == TypeTPE.ECOMMERCE) {
            demande.setRib(numeroCompte);
        }

        return demandeRepository.save(demande);
    }

    private Commercant findOrCreateCommercant(String raisonSociale,
                                              String activite,
                                              String numeroCompte,
                                              String numeroAffiliation,
                                              String adresse,
                                              String codePostal,
                                              String codeAgence,
                                              String telephone,
                                              String email,
                                              String loyer,
                                              String typeValue) {
        String normalizedCodeAgence = firstNonBlank(blankToNull(codeAgence), "000");
        String normalizedNumeroCompte = firstNonBlank(normalizeNumeroCompte(numeroCompte), blankToNull(numeroAffiliation));
        String normalizedRaisonSociale = firstNonBlank(blankToNull(raisonSociale), "COMMERCANT_SANS_NOM");
        String normalizedAdresse = blankToNull(adresse);

        Commercant commercant = commercantRepository
            .findForImportExact(
                normalizedRaisonSociale,
                normalizedNumeroCompte,
                normalizedCodeAgence,
                normalizedAdresse
            )
            .orElse(null);

        if (commercant == null) {
            commercant = new Commercant();
            commercant.setNumeroCompte(normalizedNumeroCompte);
            commercant.setCodeAgence(normalizedCodeAgence);
            commercant.setRaisonSociale(normalizedRaisonSociale);
            commercant.setStatut(StatutCommercant.ACTIF);
        }

        commercant.setRaisonSociale(firstNonBlank(blankToNull(commercant.getRaisonSociale()), normalizedRaisonSociale));
        commercant.setActivite(firstNonBlank(blankToNull(activite), blankToNull(commercant.getActivite()), "INCONNUE"));
        commercant.setNumeroCompte(firstNonBlank(blankToNull(commercant.getNumeroCompte()), normalizedNumeroCompte));
        commercant.setCodeAgence(firstNonBlank(blankToNull(commercant.getCodeAgence()), normalizedCodeAgence));

        if (blankToNull(commercant.getAdresse()) == null) {
            commercant.setAdresse(normalizedAdresse);
        }
        if (blankToNull(commercant.getCodePostal()) == null) {
            commercant.setCodePostal(blankToNull(codePostal));
        }
        if (blankToNull(commercant.getTelephone()) == null) {
            commercant.setTelephone(blankToNull(telephone));
        }

        String safeEmail = resolveSafeMerchantEmail(commercant.getId(), blankToNull(email));
        if (safeEmail != null || commercant.getEmail() == null) {
            commercant.setEmail(safeEmail);
            commercant.setEmailNotification(safeEmail);
        }

        commercant.setLoyer(parseDouble(loyer));
        commercant.setTypeCommerce(parseTypeTPE(typeValue));
        commercant.setStatut(StatutCommercant.ACTIF);

        return commercantRepository.save(commercant);
    }

    private TPE findOrCreateTPE(String numeroSerie, String numeroAffiliation, String numeroTerminal) {
        TPE tpe = tpeRepository.findByNumeroAffiliation(numeroAffiliation)
                .orElseGet(() -> tpeRepository.findByNumeroSerie(numeroSerie).orElseGet(() -> {
                    if (numeroTerminal != null && !numeroTerminal.isBlank()) {
                        return tpeRepository.findByNumeroTerminal(numeroTerminal).orElse(null);
                    }
                    return null;
                }));

        return tpe != null ? tpe : new TPE();
    }

    private String resolveNumeroSerie(Row row, Map<String, Integer> headers, DataFormatter formatter) {
        return firstNonBlank(
                readString(row, headers, formatter, "SERIE_TPE"),
                readString(row, headers, formatter, "NUMERO_SERIE"),
                readString(row, headers, formatter, "N_SERIE"),
                readString(row, headers, formatter, "N_SERIE_TPE"),
                readString(row, headers, formatter, "SERIE_PUCE"),
                readString(row, headers, formatter, "SERIAL_NUMBER"),
                readString(row, headers, formatter, "SERIE")
        );
    }

    private void upsertActiveAffectation(TPE tpe, Commercant commercant, Demande demande, LocalDate dateAffectation, TPEImportResult result) {
        Affectation affectation = affectationRepository.findActiveByTpeId(tpe.getId()).orElse(null);
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
        affectationRepository.findActiveByTpeId(tpe.getId()).ifPresent(affectation -> {
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
                headers.put(header, cell.getColumnIndex());
            }
        }
        return headers;
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

    private Boolean resolveActiveStatus(Row row,
                                        Map<String, Integer> headers,
                                        DataFormatter formatter,
                                        LocalDate valueDate,
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
                    || "I".equals(normalized) || "INACTIVE".equals(normalized)) {
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

    private LocalDate readLocalDate(Row row, Map<String, Integer> headers, DataFormatter formatter, String headerName) {
        Integer index = headers.get(normalizeHeader(headerName));
        if (index == null) {
            return null;
        }

        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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

    private TypeTPE parseTypeTPE(String value) {
        if (value == null) {
            return TypeTPE.PHYSIQUE;
        }

        String normalized = normalizeHeader(value);
        if (normalized.contains("ECOM") || normalized.contains("COMMERCE") || normalized.contains("ONLINE")) {
            return TypeTPE.ECOMMERCE;
        }
        return TypeTPE.PHYSIQUE;
    }

    private String buildCommentaire(String previlegeSecteur,
                                    String tauxCommission,
                                    String tauxCommissionInter,
                                    String nCompteIntern,
                                    String groupe,
                                    String numSeq) {
        StringBuilder builder = new StringBuilder();
        appendPart(builder, "Privilege secteur", previlegeSecteur);
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

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeNumeroCompte(String value) {
        String raw = blankToNull(value);
        if (raw == null) {
            return null;
        }

        String normalized = raw.replace(" ", "").replace(',', '.');
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

    private String resolveSafeMerchantEmail(Long commercantId, String email) {
        if (email == null) {
            return null;
        }

        Optional<Commercant> existing = commercantRepository.findByEmail(email);
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