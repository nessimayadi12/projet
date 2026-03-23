package com.banque.abc.tpe.service;

import com.banque.abc.tpe.entity.TPEPostingComp;
import com.banque.abc.tpe.repository.TPEPostingCompRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RapportFichierBancaireService {

    private final TPEPostingCompRepository tpePostingCompRepository;

    /**
     * Génère un rapport PDF des écritures comptables pour une session
     */
    public byte[] genererRapportPDF(String sessionDate) throws DocumentException {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // En-tête
            ajouterEntetePDF(document, sessionDate);

            // Récupérer les écritures
            List<TPEPostingComp> ecritures = tpePostingCompRepository.findBySessionDate(sessionDate);

            if (ecritures.isEmpty()) {
                document.add(new Paragraph("Aucune écriture trouvée pour cette session."));
            } else {
                // Statistiques
                ajouterStatistiquesPDF(document, ecritures);

                // Tableau des écritures
                ajouterTableauEcrituresPDF(document, ecritures);

                // Totaux
                ajouterTotauxPDF(document, ecritures);
            }

            // Pied de page
            ajouterPiedDePagePDF(document);

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF", e);
            throw e;
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    /**
     * Génère un fichier texte des écritures comptables
     */
    public String genererRapportTexte(String sessionDate) {
        StringBuilder sb = new StringBuilder();

        // Récupérer les écritures
        List<TPEPostingComp> ecritures = tpePostingCompRepository.findBySessionDate(sessionDate);

        if (ecritures.isEmpty()) {
            sb.append("Aucune écriture trouvée pour cette session.\n");
        } else {
            // En-tête du tableau  
            sb.append(String.format("%-10s %-40s %-15s %-30s %-5s %-5s %-8s %-20s %-12s %-15s %-10s %-40s\n",
                    "BRANCH", "PROFIT CENTER", "CLIENT", "ACCOUNT RB OR GL", "CCY", "SEQ NO", "REF",
                    "TRAN TYPE", "date", "Montant CR/DR", "NARRATIVE", ""));
            sb.append("-".repeat(220)).append("\n");

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

            for (TPEPostingComp ecriture : ecritures) {
                // Combiner ACCOUNT et RB_GL
                String accountRbGl = nvl(ecriture.getAccount());
                if (ecriture.getRbGl() != null && !ecriture.getRbGl().isEmpty()) {
                    accountRbGl = accountRbGl + (accountRbGl.isEmpty() ? "" : " ") + ecriture.getRbGl();
                }
                
                // Combiner Amount et CR/DR
                String montantCrDr = "";
                if (ecriture.getAmount() != null) {
                    montantCrDr = String.format("%.1f", ecriture.getAmount());
                }
                
                sb.append(String.format("%-10s %-40s %-15s %-30s %-5s %-5s %-8s %-20s %-12s %15s %-10s %-40s\n",
                        nvl(ecriture.getBranch()),
                        nvl(ecriture.getProfitCenter()),
                        nvl(ecriture.getClient()),
                        accountRbGl,
                        nvl(ecriture.getCcy()),
                        nvl(ecriture.getSeqNo()),
                        nvl(ecriture.getRef()),
                        nvl(ecriture.getTranType()),
                        ecriture.getDate() != null ? ecriture.getDate().format(dateFormatter) : "",
                        montantCrDr,
                        nvl(ecriture.getCrDr()),
                        nvl(ecriture.getNarrative())));
            }

            sb.append("-".repeat(220)).append("\n");
        }

        return sb.toString();
    }

    /**
     * Retourne une chaîne vide si la valeur est null
     */
    private String nvl(String value) {
        return value != null ? value : "";
    }

    /**
     * Ajoute l'en-tête au PDF
     */
    private void ajouterEntetePDF(Document document, String sessionDate) throws DocumentException {
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
        Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.GRAY);

        Paragraph title = new Paragraph("RAPPORT DE TRAITEMENT FICHIER BANCAIRE TPE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph(" "));

        Paragraph sessionInfo = new Paragraph("Date de session : " + formatSessionDate(sessionDate), subtitleFont);
        sessionInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(sessionInfo);

        Paragraph dateGeneration = new Paragraph("Généré le : " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")), subtitleFont);
        dateGeneration.setAlignment(Element.ALIGN_CENTER);
        document.add(dateGeneration);

        document.add(new Paragraph(" "));
        document.add(new LineSeparator());
        document.add(new Paragraph(" "));
    }

    /**
     * Ajoute les statistiques au PDF
     */
    private void ajouterStatistiquesPDF(Document document, List<TPEPostingComp> ecritures) throws DocumentException {
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        document.add(new Paragraph("STATISTIQUES", headerFont));
        document.add(new Paragraph(" "));

        PdfPTable statsTable = new PdfPTable(2);
        statsTable.setWidthPercentage(50);

        ajouterCellule(statsTable, "Nombre total d'écritures", true);
        ajouterCellule(statsTable, String.valueOf(ecritures.size()), false);

        ajouterCellule(statsTable, "Débits", true);
        ajouterCellule(statsTable, String.valueOf(ecritures.stream().filter(e -> "DR".equals(e.getCrDr())).count()), false);

        ajouterCellule(statsTable, "Crédits", true);
        ajouterCellule(statsTable, String.valueOf(ecritures.stream().filter(e -> "CR".equals(e.getCrDr())).count()), false);

        document.add(statsTable);
        document.add(new Paragraph(" "));
    }

    /**
     * Ajoute le tableau des écritures au PDF
     */
    private void ajouterTableauEcrituresPDF(Document document, List<TPEPostingComp> ecritures) throws DocumentException {
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        document.add(new Paragraph("DÉTAIL DES ÉCRITURES", headerFont));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{5, 8, 8, 15, 10, 8, 8, 20});

        // En-têtes
        Font headerCellFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
        ajouterCelluleEnTete(table, "ID", headerCellFont);
        ajouterCelluleEnTete(table, "BRANCH", headerCellFont);
        ajouterCelluleEnTete(table, "CLIENT", headerCellFont);
        ajouterCelluleEnTete(table, "ACCOUNT", headerCellFont);
        ajouterCelluleEnTete(table, "AMOUNT", headerCellFont);
        ajouterCelluleEnTete(table, "CR/DR", headerCellFont);
        ajouterCelluleEnTete(table, "REF", headerCellFont);
        ajouterCelluleEnTete(table, "NARRATIVE", headerCellFont);

        // Données
        Font dataFont = new Font(Font.FontFamily.HELVETICA, 8);
        for (TPEPostingComp ecriture : ecritures) {
            ajouterCelluleDonnee(table, String.valueOf(ecriture.getId()), dataFont);
            ajouterCelluleDonnee(table, ecriture.getBranch(), dataFont);
            ajouterCelluleDonnee(table, ecriture.getClient(), dataFont);
            ajouterCelluleDonnee(table, ecriture.getAccount(), dataFont);
            ajouterCelluleDonnee(table, ecriture.getAmount().toString(), dataFont);
            
            Font crDrFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD,
                    "DR".equals(ecriture.getCrDr()) ? BaseColor.RED : BaseColor.GREEN);
            ajouterCelluleDonnee(table, ecriture.getCrDr(), crDrFont);
            
            ajouterCelluleDonnee(table, ecriture.getRef() != null ? ecriture.getRef() : "", dataFont);
            ajouterCelluleDonnee(table, ecriture.getNarrative() != null ? 
                    tronquer(ecriture.getNarrative(), 40) : "", dataFont);
        }

        document.add(table);
        document.add(new Paragraph(" "));
    }

    /**
     * Ajoute les totaux au PDF
     */
    private void ajouterTotauxPDF(Document document, List<TPEPostingComp> ecritures) throws DocumentException {
        BigDecimal totalDebits = ecritures.stream()
                .filter(e -> "DR".equals(e.getCrDr()))
                .map(TPEPostingComp::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredits = ecritures.stream()
                .filter(e -> "CR".equals(e.getCrDr()))
                .map(TPEPostingComp::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        document.add(new Paragraph("TOTAUX", headerFont));
        document.add(new Paragraph(" "));

        PdfPTable totauxTable = new PdfPTable(2);
        totauxTable.setWidthPercentage(40);

        ajouterCellule(totauxTable, "Total Débits", true);
        PdfPCell debitCell = new PdfPCell(new Phrase(String.format("%.3f", totalDebits)));
        debitCell.setBackgroundColor(new BaseColor(255, 200, 200));
        totauxTable.addCell(debitCell);

        ajouterCellule(totauxTable, "Total Crédits", true);
        PdfPCell creditCell = new PdfPCell(new Phrase(String.format("%.3f", totalCredits)));
        creditCell.setBackgroundColor(new BaseColor(200, 255, 200));
        totauxTable.addCell(creditCell);

        ajouterCellule(totauxTable, "Solde", true);
        ajouterCellule(totauxTable, String.format("%.3f", totalDebits.subtract(totalCredits)), false);

        document.add(totauxTable);
    }

    /**
     * Ajoute le pied de page au PDF
     */
    private void ajouterPiedDePagePDF(Document document) throws DocumentException {
        document.add(new Paragraph(" "));
        document.add(new LineSeparator());
        
        Font footerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY);
        Paragraph footer = new Paragraph("Système de Gestion TPE - Document généré automatiquement", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    /**
     * Ajoute une cellule au tableau
     */
    private void ajouterCellule(PdfPTable table, String text, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        if (isHeader) {
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        }
        cell.setPadding(5);
        table.addCell(cell);
    }

    /**
     * Ajoute une cellule d'en-tête
     */
    private void ajouterCelluleEnTete(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new BaseColor(51, 122, 183));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    /**
     * Ajoute une cellule de données
     */
    private void ajouterCelluleDonnee(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(3);
        table.addCell(cell);
    }

    /**
     * Formate la date de session
     */
    private String formatSessionDate(String sessionDate) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date = LocalDate.parse(sessionDate, inputFormatter);
            return date.format(outputFormatter);
        } catch (Exception e) {
            return sessionDate;
        }
    }

    /**
     * Tronque un texte
     */
    private String tronquer(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
