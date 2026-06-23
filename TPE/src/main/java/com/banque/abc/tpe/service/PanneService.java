package com.banque.abc.tpe.service;

import com.banque.abc.tpe.dto.panne.PanneRequest;
import com.banque.abc.tpe.dto.panne.PanneResponse;
import com.banque.abc.tpe.dto.audit.AuditEvent;
import com.banque.abc.tpe.dto.notification.NotificationIaEventType;
import com.banque.abc.tpe.entity.Affectation;
import com.banque.abc.tpe.entity.Commercant;
import com.banque.abc.tpe.entity.Demande;
import com.banque.abc.tpe.entity.Panne;
import com.banque.abc.tpe.entity.TPE;
import com.banque.abc.tpe.entity.User;
import com.banque.abc.tpe.entity.enums.StatutDemande;
import com.banque.abc.tpe.entity.enums.StatutPanne;
import com.banque.abc.tpe.entity.enums.StatutTPE;
import com.banque.abc.tpe.exception.BusinessException;
import com.banque.abc.tpe.exception.ResourceNotFoundException;
import com.banque.abc.tpe.repository.AffectationRepository;
import com.banque.abc.tpe.repository.DemandeRepository;
import com.banque.abc.tpe.repository.PanneRepository;
import com.banque.abc.tpe.repository.TPERepository;
import com.banque.abc.tpe.repository.UserRepository;
import com.banque.abc.tpe.security.UserPrincipal;
import com.banque.abc.tpe.util.ReferenceGenerator;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PanneService {

    private final PanneRepository panneRepository;
    private final TPERepository tpeRepository;
    private final UserRepository userRepository;
    private final AffectationRepository affectationRepository;
    private final DemandeRepository demandeRepository;
    private final ReferenceGenerator referenceGenerator;
    private final AuditService auditService;
    private final BusinessNotificationService businessNotificationService;
    private static final DateTimeFormatter EXPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final List<StatutTPE> STATUTS_TPE_DECLARATION = List.of(
            StatutTPE.AFFECTE,
            StatutTPE.EN_PANNE,
            StatutTPE.MAINTENANCE
    );

    public List<Panne> getAllPannes() {
        return panneRepository.findAll();
    }

    public Optional<Panne> getPanneById(Long id) {
        return panneRepository.findById(id);
    }

    public List<Panne> getPannesByStatut(StatutPanne statut) {
        return panneRepository.findByStatut(statut);
    }

    public List<Panne> getPannesByTPE(Long tpeId) {
        return panneRepository.findByTpeId(tpeId);
    }

    public List<Panne> getPannesByTechnicien(Long technicienId) {
        return panneRepository.findByTechnicienId(technicienId);
    }

    public Panne createPanne(PanneRequest request) {
        // Récupérer l'utilisateur connecté
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User declarant = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));
        
        // Charger le TPE complet avant toute opération
        Long tpeId = request.getTpeId();
        if (tpeId == null) {
            throw new BusinessException("TPE ID est obligatoire");
        }

        if ((request.getTypePanne() == null) && (request.getDescription() == null || request.getDescription().isBlank())) {
            throw new BusinessException("La description ou le type de panne est obligatoire");
        }
        
        TPE tpe = getTpeOrThrow(tpeId);
        validateTpeEligibleForDeclaration(tpe);
        
        // Créer une nouvelle panne
        Panne nouvellePanne = new Panne();
        nouvellePanne.setReference("PAN" + System.currentTimeMillis());
        nouvellePanne.setStatut(StatutPanne.DECLAREE);
        nouvellePanne.setDateDeclaration(LocalDateTime.now());
        nouvellePanne.setDeclarant(declarant);
        nouvellePanne.setTpe(tpe);
        String description = request.getDescription();
        if ((description == null || description.isBlank()) && request.getTypePanne() != null) {
            description = request.getTypePanne().name();
        }
        nouvellePanne.setDescription(description);
        nouvellePanne.setTypePanne(request.getTypePanne());
        nouvellePanne.setSousGarantie(false);
        
        // Sauvegarder la panne d'abord
        Panne panneSaved = panneRepository.save(nouvellePanne);
        
        // Ensuite mettre a jour le statut du TPE uniquement si la panne vient d'un TPE affecte.
        if (tpe.getStatut() == StatutTPE.AFFECTE) {
            tpe.setStatut(StatutTPE.EN_PANNE);
            tpeRepository.save(tpe);
        }

        String notification = businessNotificationService.publish(
                NotificationIaEventType.PANNE_TPE_DECLAREE,
                panneNotificationContext(panneSaved)
        );
        auditService.logCreation("Panne", panneSaved.getId().toString(), panneSaved.getReference(),
                snapshot(panneSaved), notification);
        
        return panneSaved;
    }

    public Panne updatePanne(Long id, Panne panneDetails) {
        Panne panne = getPanneOrThrow(id);
        Map<String, Object> oldValues = snapshot(panne);
        
        if (panneDetails.getDescription() != null && !panneDetails.getDescription().isBlank()) {
            panne.setDescription(panneDetails.getDescription());
        }
        if (panneDetails.getTypePanne() != null) {
            panne.setTypePanne(panneDetails.getTypePanne());
        }
        panne.setDiagnostic(panneDetails.getDiagnostic());
        panne.setActionCorrective(panneDetails.getActionCorrective());
        panne.setCommentaireTechnicien(panneDetails.getCommentaireTechnicien());
        panne.setCoutReparation(panneDetails.getCoutReparation());
        panne.setSousGarantie(panneDetails.getSousGarantie());
        
        Panne updated = panneRepository.save(panne);
        auditService.logUpdate("Panne", updated.getId().toString(), updated.getReference(),
                oldValues, snapshot(updated), "Panne mise a jour: " + updated.getReference());
        return updated;
    }

    public Panne changeStatut(Long id, StatutPanne nouveauStatut) {
        if (nouveauStatut == StatutPanne.IRRECUPERABLE) {
            throw new BusinessException("Le passage a IRRECUPERABLE exige un nouveau numero de serie");
        }

        Panne panne = getPanneOrThrow(id);
        Map<String, Object> oldValues = snapshot(panne);
        StatutPanne ancienStatut = panne.getStatut();
        validateTransition(panne, nouveauStatut);
        
        panne.setStatut(nouveauStatut);
        
        switch (nouveauStatut) {
            case DIAGNOSTIQUEE:
                panne.setDateDiagnostic(LocalDateTime.now());
                break;
            case EN_REPARATION:
                panne.setDateReparation(LocalDateTime.now());
                if (panne.getTpe() != null) {
                    panne.getTpe().setStatut(StatutTPE.MAINTENANCE);
                    tpeRepository.save(panne.getTpe());
                }
                break;
            case REPAREE:
                panne.setDateResolution(LocalDateTime.now());
                TPE tpe = panne.getTpe();
                if (tpe != null) {
                    updateTpeAfterResolution(tpe);
                }
                break;
        }
        
        Panne updated = panneRepository.save(panne);
        String details = "Statut panne change de " + ancienStatut + " a " + updated.getStatut();
        if (nouveauStatut == StatutPanne.DIAGNOSTIQUEE) {
            details = businessNotificationService.publish(
                    NotificationIaEventType.PANNE_TPE_DIAGNOSTIQUEE,
                    panneNotificationContext(updated)
            );
        } else if (nouveauStatut == StatutPanne.REPAREE) {
            details = businessNotificationService.publish(
                    NotificationIaEventType.TPE_REPARE,
                    panneNotificationContext(updated)
            );
        }
        auditService.logUpdate("Panne", updated.getId().toString(), updated.getReference(),
                oldValues, snapshot(updated),
                details);
        return updated;
    }

    public Panne assignerTechnicien(Long panneId, Long technicienId) {
        Panne panne = getPanneOrThrow(panneId);
        Map<String, Object> oldValues = snapshot(panne);
        
        User technicien = userRepository.findById(technicienId)
            .orElseThrow(() -> new ResourceNotFoundException("Technicien non trouve"));
        
        panne.setTechnicien(technicien);
        Panne updated = panneRepository.save(panne);
        auditService.logUpdate("Panne", updated.getId().toString(), updated.getReference(),
                oldValues, snapshot(updated), "Technicien assigne a la panne " + updated.getReference());
        return updated;
    }

    public void deletePanne(Long id) {
        Panne panne = getPanneOrThrow(id);
        panneRepository.delete(panne);
        auditService.logBusinessEvent(AuditEvent.builder()
                .action("DELETE")
                .actionLabel("Suppression")
                .moduleName("Panne")
                .entityType("Panne")
                .entityId(id.toString())
                .entityReference(panne.getReference())
                .details("Panne supprimee: " + panne.getReference())
                .oldValues(snapshot(panne))
                .statut("SUCCESS")
                .riskLevel("CRITICAL")
                .build());
    }

    public Panne diagnostiquer(Long id, String diagnostic) {
        if (diagnostic == null || diagnostic.isBlank()) {
            throw new BusinessException("Le diagnostic est obligatoire");
        }

        Panne panne = getPanneOrThrow(id);
        Map<String, Object> oldValues = snapshot(panne);
        StatutPanne ancienStatut = panne.getStatut();
        validateTransition(panne, StatutPanne.DIAGNOSTIQUEE);
        
        panne.setDiagnostic(diagnostic.trim());
        panne.setStatut(StatutPanne.DIAGNOSTIQUEE);
        panne.setDateDiagnostic(LocalDateTime.now());
        
        Panne updated = panneRepository.save(panne);
        String notification = businessNotificationService.publish(
                NotificationIaEventType.PANNE_TPE_DIAGNOSTIQUEE,
                panneNotificationContext(updated)
        );
        auditService.logUpdate("Panne", updated.getId().toString(), updated.getReference(),
                oldValues, snapshot(updated),
                notification);
        return updated;
    }

    public Panne marquerEnReparation(Long id) {
        Panne panne = changeStatut(id, StatutPanne.EN_REPARATION);
        if (panne.getTpe() != null) {
            panne.getTpe().setStatut(StatutTPE.MAINTENANCE);
            tpeRepository.save(panne.getTpe());
        }
        return panne;
    }

    public Panne marquerReparee(Long id, String solution) {
        Panne panne = getPanneOrThrow(id);
        Map<String, Object> oldValues = snapshot(panne);
        StatutPanne ancienStatut = panne.getStatut();
        validateTransition(panne, StatutPanne.REPAREE);
        
        if (solution != null && !solution.isBlank()) {
            panne.setActionCorrective(solution.trim());
        }
        panne.setStatut(StatutPanne.REPAREE);
        panne.setDateResolution(LocalDateTime.now());

        TPE tpe = panne.getTpe();
        if (tpe != null) {
            updateTpeAfterResolution(tpe);
        }
        
        Panne updated = panneRepository.save(panne);
        String notification = businessNotificationService.publish(
                NotificationIaEventType.TPE_REPARE,
                panneNotificationContext(updated)
        );
        auditService.logUpdate("Panne", updated.getId().toString(), updated.getReference(),
                oldValues, snapshot(updated),
                notification);
        return updated;
    }

    public Panne resoudrePanne(Long id, String solution) {
        return marquerReparee(id, solution);
    }

    public Panne testerPanne(Long id, boolean resultat) {
        Panne panne = getPanneOrThrow(id);
        
        if (resultat) {
            return marquerReparee(id, panne.getActionCorrective());
        }

        Map<String, Object> oldValues = snapshot(panne);
        if (panne.getStatut() == StatutPanne.REPAREE) {
            panne.setStatut(StatutPanne.EN_REPARATION);
            TPE tpe = panne.getTpe();
            if (tpe != null) {
                tpe.setStatut(StatutTPE.MAINTENANCE);
                tpeRepository.save(tpe);
            }
        } else if (panne.getStatut() != StatutPanne.EN_REPARATION) {
            throw new BusinessException("Le test ne peut etre relance que depuis REPAREE ou EN_REPARATION");
        }
        
        Panne updated = panneRepository.save(panne);
        auditService.logUpdate("Panne", updated.getId().toString(), updated.getReference(),
                oldValues, snapshot(updated), "Test de reparation non concluant");
        return updated;
    }

    public Panne affecterTPERemplacement(Long panneId, Long tpeRemplacementId) {
        Panne panne = getPanneOrThrow(panneId);
        Map<String, Object> oldValues = snapshot(panne);
        
        TPE tpeRemplacement = getTpeOrThrow(tpeRemplacementId);
        
        panne.setTpeRemplacement(tpeRemplacement);
        Panne updated = panneRepository.save(panne);
        String notification = businessNotificationService.publish(
                NotificationIaEventType.TPE_REMPLACE,
                panneNotificationContext(updated)
        );
        auditService.logUpdate("Panne", updated.getId().toString(), updated.getReference(),
                oldValues, snapshot(updated),
                notification);
        return updated;
    }

    public Panne marquerIrrecuperableAvecRemplacement(Long panneId,
                                                      String nouveauNumeroSerie,
                                                      String nouveauTypeTPE,
                                                      String nouvelleMarque,
                                                      String nouveauModele,
                                                      String commentaire) {
        if (nouveauNumeroSerie == null || nouveauNumeroSerie.isBlank()) {
            throw new BusinessException("Le nouveau numero de serie est obligatoire");
        }
        if (nouveauTypeTPE == null || nouveauTypeTPE.isBlank()) {
            throw new BusinessException("Le type du nouveau TPE est obligatoire");
        }
        if (nouvelleMarque == null || nouvelleMarque.isBlank()) {
            throw new BusinessException("La marque du nouveau TPE est obligatoire");
        }
        if (nouveauModele == null || nouveauModele.isBlank()) {
            throw new BusinessException("Le modele du nouveau TPE est obligatoire");
        }

        String numeroSerieRemplacement = nouveauNumeroSerie.trim();
        String typeRemplacement = nouveauTypeTPE.trim().toUpperCase();
        String marqueRemplacement = nouvelleMarque.trim();
        String modeleRemplacement = nouveauModele.trim();
        if (tpeRepository.existsByNumeroSerie(numeroSerieRemplacement)) {
            throw new BusinessException("Ce numero de serie est deja utilise");
        }

        Panne panne = getPanneOrThrow(panneId);
        Map<String, Object> oldValues = snapshot(panne);
        StatutPanne ancienStatut = panne.getStatut();
        validateTransition(panne, StatutPanne.IRRECUPERABLE);

        TPE tpe = panne.getTpe();
        if (tpe == null) {
            throw new BusinessException("TPE introuvable pour cette panne");
        }

        Optional<Affectation> affectationActive = affectationRepository.findActiveByTpeId(tpe.getId());
        Demande demandeSource = affectationActive.map(Affectation::getDemande).orElse(null);
        String numeroTerminalTransfere = firstNonBlank(
                tpe.getNumeroTerminal(),
                demandeSource != null ? demandeSource.getNumeroTerminal() : null
        );
        numeroTerminalTransfere = resolveTransferableNumeroTerminal(tpe, numeroTerminalTransfere);

        if (numeroTerminalTransfere != null && numeroTerminalTransfere.equals(tpe.getNumeroTerminal())) {
            tpe.setNumeroTerminal(null);
            tpeRepository.saveAndFlush(tpe);
        }

        TPE tpeRemplacement = TPE.builder()
            .typeTPE(typeRemplacement)
            .numeroSerie(numeroSerieRemplacement)
            .numeroTerminal(numeroTerminalTransfere)
            .marque(marqueRemplacement)
            .modele(modeleRemplacement)
            .dateAcquisition(LocalDate.now())
            .dateMiseEnService(tpe.getDateMiseEnService())
            .mcc(firstNonBlank(tpe.getMcc(), demandeSource != null ? demandeSource.getMcc() : null))
            .numeroAffiliation(tpe.getNumeroAffiliation())
            .cleApi(tpe.getCleApi())
            .statut(affectationActive.isPresent() ? StatutTPE.AFFECTE : StatutTPE.DISPONIBLE)
            .commercant(affectationActive.map(Affectation::getCommercant).orElse(null))
            .commentaire(firstNonBlank(commentaire, tpe.getCommentaire()))
            .build();

        tpeRemplacement = tpeRepository.save(tpeRemplacement);

        if (affectationActive.isPresent()) {
            Affectation active = affectationActive.get();
            active.setActif(false);
            active.setDateFin(LocalDate.now());
            String commentaireActuel = active.getCommentaire() != null ? active.getCommentaire() : "";
            active.setCommentaire(commentaireActuel + "\nRemplacement suite a panne irrecuperable");
            affectationRepository.save(active);

            UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            User affectePar = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));

            Demande demandeRemplacement = null;
            if (demandeSource != null) {
                cloturerDemandeSource(demandeSource);
                demandeRemplacement = creerDemandeRemplacement(
                        demandeSource,
                        tpeRemplacement,
                        active.getCommercant(),
                        affectePar,
                        numeroTerminalTransfere
                );
            }

            Affectation nouvelleAffectation = Affectation.builder()
                .tpe(tpeRemplacement)
                .commercant(active.getCommercant())
                .demande(demandeRemplacement)
                .dateAffectation(LocalDate.now())
                .actif(true)
                .affectePar(affectePar)
                .commentaire("Remplacement automatique suite a panne irrecuperable")
                .build();

            affectationRepository.save(nouvelleAffectation);
        }

        tpe.setCommercant(null);
        tpe.setStatut(StatutTPE.HORS_SERVICE);
        tpeRepository.save(tpe);

        panne.setStatut(StatutPanne.IRRECUPERABLE);
        panne.setDateResolution(LocalDateTime.now());
        panne.setTpeRemplacement(tpeRemplacement);
        if (commentaire != null && !commentaire.isBlank()) {
            panne.setActionCorrective(commentaire.trim());
        } else {
            panne.setActionCorrective("Remplacement par TPE "
                    + numeroSerieRemplacement + " - " + marqueRemplacement + " " + modeleRemplacement);
        }

        Panne updated = panneRepository.save(panne);
        Map<String, Object> newValues = snapshot(updated);
        newValues.put("ancienTpeId", tpe.getId());
        newValues.put("ancienTpeNumeroSerie", tpe.getNumeroSerie());
        newValues.put("tpeRemplacementId", tpeRemplacement.getId());
        newValues.put("tpeRemplacementNumeroSerie", tpeRemplacement.getNumeroSerie());
        Map<String, Object> notificationContext = panneNotificationContext(updated);
        notificationContext.put("ancienTpeId", tpe.getId());
        notificationContext.put("ancienTpeNumeroSerie", tpe.getNumeroSerie());
        notificationContext.put("tpeRemplacementId", tpeRemplacement.getId());
        notificationContext.put("tpeRemplacementNumeroSerie", tpeRemplacement.getNumeroSerie());
        String notification = businessNotificationService.publish(
                NotificationIaEventType.TPE_REMPLACE,
                notificationContext
        );
        auditService.logUpdate("Panne", updated.getId().toString(), updated.getReference(),
                oldValues, newValues,
                notification);
        return updated;
    }

    private void cloturerDemandeSource(Demande demandeSource) {
        demandeSource.setStatut(StatutDemande.CLOTUREE);
        demandeSource.setDateCloture(LocalDateTime.now());
        demandeRepository.save(demandeSource);
    }

    private Demande creerDemandeRemplacement(Demande source,
                                             TPE tpeRemplacement,
                                             Commercant commercant,
                                             User affectePar,
                                             String numeroTerminalTransfere) {
        Demande demande = Demande.builder()
                .reference(generateUniqueDemandeReference())
                .typeDemande(source.getTypeDemande())
                .statut(StatutDemande.AFFECTEE)
                .commercant(commercant != null ? commercant : source.getCommercant())
                .demandeur(source.getDemandeur() != null ? source.getDemandeur() : affectePar)
                .inputer(source.getInputer())
                .valideur(source.getValideur())
                .dateSaisieTaux(source.getDateSaisieTaux())
                .dateValidation(source.getDateValidation())
                .dateCloture(LocalDateTime.now())
                .description(source.getDescription())
                .commentaireValidation(source.getCommentaireValidation())
                .urgence(source.getUrgence())
                .raisonSociale(source.getRaisonSociale())
                .activite(source.getActivite())
                .numeroCompte(source.getNumeroCompte())
                .adresse(source.getAdresse())
                .codePostal(source.getCodePostal())
                .codeAgence(source.getCodeAgence())
                .telephone(source.getTelephone())
                .mcc(source.getMcc())
                .tauxCommission(source.getTauxCommission())
                .tauxCommissionInter(source.getTauxCommissionInter())
                .loyer(source.getLoyer())
                .serieTpe(tpeRemplacement.getNumeroSerie())
                .numeroTerminal(firstNonBlank(numeroTerminalTransfere, tpeRemplacement.getNumeroTerminal(), source.getNumeroTerminal()))
                .valueDate(source.getValueDate())
                .localite(source.getLocalite())
                .rib(source.getRib())
                .webmaster(source.getWebmaster())
                .contactTechnique(source.getContactTechnique())
                .urlSiteMarchand(source.getUrlSiteMarchand())
                .rneFilePath(source.getRneFilePath())
                .build();

        return demandeRepository.save(demande);
    }

    private String generateUniqueDemandeReference() {
        int compteur = (int) demandeRepository.count() + 1;
        String reference = referenceGenerator.generateDemandeReference(compteur);
        while (demandeRepository.existsByReference(reference)) {
            compteur++;
            reference = referenceGenerator.generateDemandeReference(compteur);
        }
        return reference;
    }

    public List<Panne> getPannesByPeriode(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return panneRepository.findAll().stream()
            .filter(p -> p.getDateDeclaration().isAfter(dateDebut) 
                      && p.getDateDeclaration().isBefore(dateFin))
            .toList();
    }

    public byte[] exportPannesExcel(LocalDateTime dateDebut, LocalDateTime dateFin) throws IOException {
        List<Panne> pannes = getPannesForExport(dateDebut, dateFin);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Pannes");
            String[] headers = {
                    "Reference", "TPE", "Commercant", "Type panne", "Description", "Statut",
                    "Declarant", "Technicien", "Diagnostic", "Solution", "Remplacement",
                    "Date declaration", "Date diagnostic", "Date reparation", "Date resolution"
            };

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (Panne panne : pannes) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(nvl(panne.getReference()));
                row.createCell(1).setCellValue(panne.getTpe() != null ? nvl(panne.getTpe().getNumeroSerie()) : "");
                row.createCell(2).setCellValue(getCommercantNom(panne));
                row.createCell(3).setCellValue(panne.getTypePanne() != null ? panne.getTypePanne().name() : "");
                row.createCell(4).setCellValue(nvl(panne.getDescription()));
                row.createCell(5).setCellValue(panne.getStatut() != null ? panne.getStatut().name() : "");
                row.createCell(6).setCellValue(getUserNom(panne.getDeclarant()));
                row.createCell(7).setCellValue(getUserNom(panne.getTechnicien()));
                row.createCell(8).setCellValue(nvl(panne.getDiagnostic()));
                row.createCell(9).setCellValue(nvl(panne.getActionCorrective()));
                row.createCell(10).setCellValue(panne.getTpeRemplacement() != null ? nvl(panne.getTpeRemplacement().getNumeroSerie()) : "");
                row.createCell(11).setCellValue(formatDate(panne.getDateDeclaration()));
                row.createCell(12).setCellValue(formatDate(panne.getDateDiagnostic()));
                row.createCell(13).setCellValue(formatDate(panne.getDateReparation()));
                row.createCell(14).setCellValue(formatDate(panne.getDateResolution()));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportPannesPdf(LocalDateTime dateDebut, LocalDateTime dateFin) throws DocumentException {
        List<Panne> pannes = getPannesForExport(dateDebut, dateFin);
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("Suivi des pannes TPE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("Genere le " + LocalDateTime.now().format(EXPORT_DATE_FORMAT)));
        document.add(new Paragraph("Nombre de pannes: " + pannes.size()));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{9, 10, 12, 13, 20, 10, 12, 12, 16, 13});

        Font headerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.WHITE);
        addPdfHeader(table, "Ref", headerFont);
        addPdfHeader(table, "TPE", headerFont);
        addPdfHeader(table, "Commercant", headerFont);
        addPdfHeader(table, "Type", headerFont);
        addPdfHeader(table, "Description", headerFont);
        addPdfHeader(table, "Statut", headerFont);
        addPdfHeader(table, "Declarant", headerFont);
        addPdfHeader(table, "Technicien", headerFont);
        addPdfHeader(table, "Solution", headerFont);
        addPdfHeader(table, "Resolution", headerFont);

        Font dataFont = new Font(Font.FontFamily.HELVETICA, 7);
        for (Panne panne : pannes) {
            addPdfCell(table, nvl(panne.getReference()), dataFont);
            addPdfCell(table, panne.getTpe() != null ? nvl(panne.getTpe().getNumeroSerie()) : "", dataFont);
            addPdfCell(table, getCommercantNom(panne), dataFont);
            addPdfCell(table, panne.getTypePanne() != null ? panne.getTypePanne().name() : "", dataFont);
            addPdfCell(table, truncate(nvl(panne.getDescription()), 80), dataFont);
            addPdfCell(table, panne.getStatut() != null ? panne.getStatut().name() : "", dataFont);
            addPdfCell(table, getUserNom(panne.getDeclarant()), dataFont);
            addPdfCell(table, getUserNom(panne.getTechnicien()), dataFont);
            addPdfCell(table, truncate(nvl(panne.getActionCorrective()), 70), dataFont);
            addPdfCell(table, formatDate(panne.getDateResolution()), dataFont);
        }

        document.add(table);
        document.close();
        return out.toByteArray();
    }

    /**
     * Mapper une entité Panne vers un DTO PanneResponse
     */
    public PanneResponse mapToResponse(Panne panne) {
        PanneResponse response = new PanneResponse();
        response.setId(panne.getId());
        response.setReference(panne.getReference());
        response.setStatut(panne.getStatut());
        response.setDescription(panne.getDescription());
        response.setTypePanne(panne.getTypePanne());
        response.setDateDeclaration(panne.getDateDeclaration());
        response.setDateDiagnostic(panne.getDateDiagnostic());
        response.setDateReparation(panne.getDateReparation());
        response.setDateResolution(panne.getDateResolution());
        response.setDiagnostic(panne.getDiagnostic());
        response.setActionCorrective(panne.getActionCorrective());
        response.setCommentaireTechnicien(panne.getCommentaireTechnicien());
        response.setCoutReparation(panne.getCoutReparation());
        response.setSousGarantie(panne.getSousGarantie());
        response.setCreatedDate(panne.getCreatedDate());
        
        if (panne.getTpe() != null) {
            response.setTpeId(panne.getTpe().getId());
            response.setTpeNumeroSerie(panne.getTpe().getNumeroSerie());
        }

        resolveCommercant(panne).ifPresent(commercant -> {
            response.setCommercantId(commercant.getId());
            response.setCommercantNom(commercant.getRaisonSociale());
        });
        
        if (panne.getDeclarant() != null) {
            response.setDeclarantNom(panne.getDeclarant().getNom() + " " + panne.getDeclarant().getPrenom());
        }
        
        if (panne.getTechnicien() != null) {
            response.setTechnicienNom(panne.getTechnicien().getNom() + " " + panne.getTechnicien().getPrenom());
        }
        
        if (panne.getTpeRemplacement() != null) {
            response.setTpeRemplacementId(panne.getTpeRemplacement().getId());
            response.setTpeRemplacementNumero(panne.getTpeRemplacement().getNumeroSerie());
        }
        
        return response;
    }

    private void updateTpeAfterResolution(TPE tpe) {
        Optional<Affectation> active = affectationRepository.findActiveByTpeId(tpe.getId());
        tpe.setStatut(active.isPresent() ? StatutTPE.AFFECTE : StatutTPE.DISPONIBLE);
        tpeRepository.save(tpe);
    }

    private Panne getPanneOrThrow(Long id) {
        return panneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Panne non trouvee"));
    }

    private TPE getTpeOrThrow(Long id) {
        return tpeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TPE non trouve avec l'ID: " + id));
    }

    private void validateTpeEligibleForDeclaration(TPE tpe) {
        if (!STATUTS_TPE_DECLARATION.contains(tpe.getStatut())) {
            throw new BusinessException("Le TPE doit etre affecte, en panne ou en maintenance pour declarer une panne");
        }
    }

    private void validateTransition(Panne panne, StatutPanne nouveauStatut) {
        StatutPanne statutActuel = panne.getStatut();
        boolean allowed = switch (statutActuel) {
            case DECLAREE -> nouveauStatut == StatutPanne.DIAGNOSTIQUEE;
            case DIAGNOSTIQUEE -> nouveauStatut == StatutPanne.EN_REPARATION;
            case EN_REPARATION -> nouveauStatut == StatutPanne.REPAREE || nouveauStatut == StatutPanne.IRRECUPERABLE;
            default -> false;
        };

        if (!allowed) {
            throw new BusinessException("Transition panne non autorisee: " + statutActuel + " -> " + nouveauStatut);
        }
    }

    private List<Panne> getPannesForExport(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return panneRepository.findAll().stream()
                .filter(panne -> {
                    if (dateDebut == null || dateFin == null) {
                        return true;
                    }
                    LocalDateTime dateDeclaration = panne.getDateDeclaration();
                    return dateDeclaration != null
                            && !dateDeclaration.isBefore(dateDebut)
                            && !dateDeclaration.isAfter(dateFin);
                })
                .toList();
    }

    private String getCommercantNom(Panne panne) {
        return resolveCommercant(panne)
                .map(Commercant::getRaisonSociale)
                .map(this::nvl)
                .orElse("");
    }

    private Optional<Commercant> resolveCommercant(Panne panne) {
        if (panne.getTpe() == null) {
            return Optional.empty();
        }

        TPE tpe = panne.getTpe();
        if (tpe.getCommercant() != null) {
            return Optional.of(tpe.getCommercant());
        }

        if (tpe.getId() == null) {
            return Optional.empty();
        }

        Optional<Commercant> activeCommercant = affectationRepository.findActiveByTpeId(tpe.getId())
                .map(Affectation::getCommercant);

        if (activeCommercant.isPresent()) {
            return activeCommercant;
        }

        return affectationRepository.findByTpeId(tpe.getId()).stream()
                .filter(affectation -> affectation.getCommercant() != null)
                .max(Comparator
                        .comparing(Affectation::getDateFin, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(Affectation::getDateAffectation, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(Affectation::getCreatedDate, Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(Affectation::getCommercant);
    }

    private String getUserNom(User user) {
        if (user == null) {
            return "";
        }
        return (nvl(user.getNom()) + " " + nvl(user.getPrenom())).trim();
    }

    private String formatDate(LocalDateTime value) {
        return value != null ? value.format(EXPORT_DATE_FORMAT) : "";
    }

    private String nvl(String value) {
        return value != null ? value : "";
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }

    private String resolveTransferableNumeroTerminal(TPE oldTpe, String numeroTerminal) {
        if (numeroTerminal == null || numeroTerminal.isBlank()) {
            return null;
        }

        Optional<TPE> owner = tpeRepository.findByNumeroTerminal(numeroTerminal);
        if (owner.isEmpty() || owner.get().getId().equals(oldTpe.getId())) {
            return numeroTerminal;
        }

        return null;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return nvl(value);
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    private void addPdfHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new BaseColor(51, 122, 183));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private void addPdfCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(nvl(text), font));
        cell.setPadding(3);
        table.addCell(cell);
    }

    private Map<String, Object> snapshot(Panne panne) {
        TPE tpe = panne.getTpe();
        return auditService.values(
                "reference", panne.getReference(),
                "statut", panne.getStatut(),
                "description", panne.getDescription(),
                "typePanne", panne.getTypePanne(),
                "tpeId", tpe != null ? tpe.getId() : null,
                "tpeNumeroSerie", tpe != null ? tpe.getNumeroSerie() : null,
                "tpeNumeroTerminal", tpe != null ? tpe.getNumeroTerminal() : null,
                "tpeStatut", tpe != null ? tpe.getStatut() : null,
                "dateDeclaration", panne.getDateDeclaration(),
                "dateDiagnostic", panne.getDateDiagnostic(),
                "dateReparation", panne.getDateReparation(),
                "dateResolution", panne.getDateResolution(),
                "declarantId", panne.getDeclarant() != null ? panne.getDeclarant().getId() : null,
                "technicienId", panne.getTechnicien() != null ? panne.getTechnicien().getId() : null,
                "diagnostic", panne.getDiagnostic(),
                "actionCorrective", panne.getActionCorrective(),
                "commentaireTechnicien", panne.getCommentaireTechnicien(),
                "tpeRemplacementId", panne.getTpeRemplacement() != null ? panne.getTpeRemplacement().getId() : null,
                "tpeRemplacementNumeroSerie", panne.getTpeRemplacement() != null ? panne.getTpeRemplacement().getNumeroSerie() : null,
                "coutReparation", panne.getCoutReparation(),
                "sousGarantie", panne.getSousGarantie()
        );
    }

    private Map<String, Object> panneNotificationContext(Panne panne) {
        Map<String, Object> context = snapshot(panne);
        TPE tpe = panne.getTpe();
        context.put("commercantNom", getCommercantNom(panne));
        context.put("numeroSerie", tpe != null ? tpe.getNumeroSerie() : null);
        context.put("numeroTerminal", tpe != null ? tpe.getNumeroTerminal() : null);
        context.put("codeAgence", tpe != null && tpe.getCommercant() != null
                ? tpe.getCommercant().getCodeAgence()
                : panne.getDeclarant() != null ? panne.getDeclarant().getCodeAgence() : null);
        context.put("solution", panne.getActionCorrective());
        return context;
    }
    
    /**
     * Méthodes avec retour DTO
     */
    public List<PanneResponse> getAllPannesDTO() {
        return panneRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public PanneResponse getPanneDTOById(Long id) {
        Panne panne = getPanneOrThrow(id);
        return mapToResponse(panne);
    }
    
    public List<PanneResponse> getPannesDTOByStatut(StatutPanne statut) {
        return panneRepository.findByStatut(statut).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}

