package org.cpi2.utils;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.kernel.geom.Rectangle;
import org.cpi2.entities.Candidat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Utility class for generating PDF progression reports
 */
public class ProgressionReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ProgressionReportGenerator.class);
    
    // PDF styling constants
    private static final float MARGIN = 50f;
    private static final float TITLE_FONT_SIZE = 20f;
    private static final float HEADER_FONT_SIZE = 16f;
    private static final float SUBHEADER_FONT_SIZE = 14f;
    private static final float BODY_FONT_SIZE = 12f;
    private static final float FOOTER_FONT_SIZE = 10f;
    
    // Custom colors
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(38, 70, 83); // Dark blue
    private static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(42, 157, 143); // Teal
    private static final DeviceRgb ACCENT_COLOR = new DeviceRgb(233, 196, 106); // Yellow
    private static final DeviceRgb LIGHT_COLOR = new DeviceRgb(244, 241, 222); // Light cream
    private static final DeviceRgb TEXT_COLOR = new DeviceRgb(55, 55, 55); // Dark gray
    private static final DeviceRgb SUCCESS_COLOR = new DeviceRgb(42, 157, 143); // Teal
    
    /**
     * Generate a progression report PDF for a candidate
     */
    public static String generateProgressionReport(Map<String, Object> progressionData) {
        try {
            // Extract data from the progression data map
            String nom = (String) progressionData.get("nom");
            String prenom = (String) progressionData.get("prenom");
            String typePermis = (String) progressionData.get("typePermis");
            String dateInscription = (String) progressionData.get("dateInscription");
            String statut = (String) progressionData.get("statut");
            int totalSeancesCode = (int) progressionData.get("totalSeancesCode");
            int totalSeancesConduite = (int) progressionData.get("totalSeancesConduite");
            int seancesCodeCompletes = (int) progressionData.get("seancesCodeCompletes");
            int seancesConduiteCompletes = (int) progressionData.get("seancesConduiteCompletes");
            double progressionTotale = (double) progressionData.get("progressionTotale");
            
            logger.info("Début de la génération du rapport de progression pour: {} {}", nom, prenom);
            
            // Create directory if it doesn't exist
            String documentsPath = System.getProperty("user.home") + "/Documents";
            File documentsDir = new File(documentsPath);
            if (!documentsDir.exists()) {
                documentsDir.mkdirs();
            }

            String reportsPath = documentsPath + "/Rapports_Progression";
            File reportsDir = new File(reportsPath);
            if (!reportsDir.exists()) {
                reportsDir.mkdirs();
            }

            // Create filename with date and candidate name
            String fileName = reportsPath + "/Progression_" + nom.replace(" ", "_") + "_" + prenom.replace(" ", "_") + "_" + 
                              LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            
            // Create PDF document
            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Set margins
            document.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
            
            // Add report components
            addHeader(document);
            addCandidateInfo(document, progressionData);
            addProgressionSummary(document, progressionData);
            addProgressionDetails(document, progressionData);
            addFooter(document);
            
            document.close();
            
            logger.info("Rapport de progression généré avec succès: {}", fileName);
            
            return fileName;
            
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du rapport de progression: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Add header with logo and title
     */
    private static void addHeader(Document document) {
        // Create a table for header layout
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        headerTable.setWidth(UnitValue.createPercentValue(100));
        
        // Auto-École Title
        Paragraph title = new Paragraph("Auto-École Excellence")
                .setFontSize(TITLE_FONT_SIZE)
                .setBold()
                .setFontColor(PRIMARY_COLOR);
                
        Paragraph subtitle = new Paragraph("Rapport de Progression")
                .setFontSize(SUBHEADER_FONT_SIZE)
                .setFontColor(SECONDARY_COLOR);
        
        Paragraph date = new Paragraph("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFontSize(BODY_FONT_SIZE)
                .setFontColor(TEXT_COLOR);
        
        // Create title cell
        Cell titleCell = new Cell();
        titleCell.add(title);
        titleCell.add(subtitle);
        titleCell.add(date);
        titleCell.setBorder(null);
        titleCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        
        // Auto-École Info
        Paragraph address = new Paragraph("123 Avenue Habib Bourguiba, Tunis\nTél: +216 71 123 456\nEmail: contact@autoecole-excellence.tn")
                .setFontSize(BODY_FONT_SIZE)
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontColor(TEXT_COLOR);
        
        // Create info cell
        Cell infoCell = new Cell();
        infoCell.add(address);
        infoCell.setBorder(null);
        infoCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        infoCell.setTextAlignment(TextAlignment.RIGHT);
        
        // Add cells to table
        headerTable.addCell(titleCell);
        headerTable.addCell(infoCell);
        
        // Add table to document
        document.add(headerTable);
        
        // Add separator
        LineSeparator separator = new LineSeparator(new SolidLine(1f));
        separator.setFontColor(SECONDARY_COLOR);
        separator.setMarginTop(5);
        separator.setMarginBottom(15);
        document.add(separator);
    }
    
    /**
     * Add candidate information section
     */
    private static void addCandidateInfo(Document document, Map<String, Object> progressionData) {
        // Extract data
        String nom = (String) progressionData.get("nom");
        String prenom = (String) progressionData.get("prenom");
        String typePermis = (String) progressionData.get("typePermis");
        String dateInscription = (String) progressionData.get("dateInscription");
        String statut = (String) progressionData.get("statut");
        
        // Section title
        Paragraph sectionTitle = new Paragraph("Informations du Candidat")
                .setFontSize(HEADER_FONT_SIZE)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(10);
        document.add(sectionTitle);
        
        // Create info table
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        infoTable.setWidth(UnitValue.createPercentValue(100));
        infoTable.setBackgroundColor(LIGHT_COLOR, 0.3f);
        infoTable.setBorder(new SolidBorder(SECONDARY_COLOR, 1));
        
        // Add rows
        addInfoRow(infoTable, "Nom", nom + " " + prenom);
        addInfoRow(infoTable, "Type de Permis", "Permis " + typePermis);
        addInfoRow(infoTable, "Date d'Inscription", dateInscription);
        addInfoRow(infoTable, "Statut", statut);
        
        document.add(infoTable);
        document.add(new Paragraph("\n"));
    }
    
    /**
     * Helper method to add info rows to a table
     */
    private static void addInfoRow(Table table, String label, String value) {
        Cell labelCell = new Cell();
        labelCell.add(new Paragraph(label)
                .setFontSize(BODY_FONT_SIZE)
                .setBold()
                .setFontColor(PRIMARY_COLOR));
        labelCell.setBorder(null);
        labelCell.setPadding(5);
        
        Cell valueCell = new Cell();
        valueCell.add(new Paragraph(value)
                .setFontSize(BODY_FONT_SIZE)
                .setFontColor(TEXT_COLOR));
        valueCell.setBorder(null);
        valueCell.setPadding(5);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    /**
     * Add progression summary section with progress bar
     */
    private static void addProgressionSummary(Document document, Map<String, Object> progressionData) {
        // Extract data
        int totalSeancesCode = (int) progressionData.get("totalSeancesCode");
        int totalSeancesConduite = (int) progressionData.get("totalSeancesConduite");
        int seancesCodeCompletes = (int) progressionData.get("seancesCodeCompletes");
        int seancesConduiteCompletes = (int) progressionData.get("seancesConduiteCompletes");
        double progressionTotale = (double) progressionData.get("progressionTotale");
        
        // Section title
        Paragraph sectionTitle = new Paragraph("Résumé de Progression")
                .setFontSize(HEADER_FONT_SIZE)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(10);
        document.add(sectionTitle);
        
        // Create main table for summary
        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{100}));
        summaryTable.setWidth(UnitValue.createPercentValue(100));
        
        // Overall progression
        Cell progressCell = new Cell();
        progressCell.setBorder(null);
        progressCell.setPadding(10);
        progressCell.setBackgroundColor(LIGHT_COLOR, 0.3f);
        
        Paragraph progressTitle = new Paragraph("Progression Globale: " + String.format("%.1f%%", progressionTotale * 100))
                .setFontSize(SUBHEADER_FONT_SIZE)
                .setBold()
                .setFontColor(SECONDARY_COLOR)
                .setMarginBottom(5);
        progressCell.add(progressTitle);
        
        // Progress bar
        Table progressBar = createProgressBar(progressionTotale);
        progressCell.add(progressBar);
        
        summaryTable.addCell(progressCell);
        
        // Code sessions
        Cell codeCell = new Cell();
        codeCell.setBorder(null);
        codeCell.setPadding(10);
        
        Paragraph codeTitle = new Paragraph("Séances de Code")
                .setFontSize(SUBHEADER_FONT_SIZE)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(5);
        codeCell.add(codeTitle);
        
        Paragraph codeProgress = new Paragraph(
                String.format("Complétées: %d / %d séances (%.1f%%)", 
                seancesCodeCompletes, totalSeancesCode, 
                totalSeancesCode > 0 ? (seancesCodeCompletes * 100.0 / totalSeancesCode) : 0))
                .setFontSize(BODY_FONT_SIZE)
                .setFontColor(TEXT_COLOR);
        codeCell.add(codeProgress);
        
        // Code progress bar
        double codeProgressRatio = totalSeancesCode > 0 ? (double) seancesCodeCompletes / totalSeancesCode : 0;
        Table codeProgressBar = createProgressBar(codeProgressRatio);
        codeCell.add(codeProgressBar);
        
        summaryTable.addCell(codeCell);
        
        // Driving sessions
        Cell driveCell = new Cell();
        driveCell.setBorder(null);
        driveCell.setPadding(10);
        
        Paragraph driveTitle = new Paragraph("Séances de Conduite")
                .setFontSize(SUBHEADER_FONT_SIZE)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(5);
        driveCell.add(driveTitle);
        
        Paragraph driveProgress = new Paragraph(
                String.format("Complétées: %d / %d séances (%.1f%%)", 
                seancesConduiteCompletes, totalSeancesConduite, 
                totalSeancesConduite > 0 ? (seancesConduiteCompletes * 100.0 / totalSeancesConduite) : 0))
                .setFontSize(BODY_FONT_SIZE)
                .setFontColor(TEXT_COLOR);
        driveCell.add(driveProgress);
        
        // Driving progress bar
        double driveProgressRatio = totalSeancesConduite > 0 ? (double) seancesConduiteCompletes / totalSeancesConduite : 0;
        Table driveProgressBar = createProgressBar(driveProgressRatio);
        driveCell.add(driveProgressBar);
        
        summaryTable.addCell(driveCell);
        
        document.add(summaryTable);
        document.add(new Paragraph("\n"));
    }
    
    /**
     * Create a progress bar
     */
    private static Table createProgressBar(double progress) {
        // Ensure progress is between 0 and 1
        progress = Math.max(0, Math.min(1, progress));
        
        // Create table for progress bar (100 columns to represent percentage)
        Table progressBar = new Table(UnitValue.createPercentArray(new float[]{100}));
        progressBar.setWidth(UnitValue.createPercentValue(100));
        progressBar.setMarginTop(5);
        progressBar.setMarginBottom(10);
        
        // Calculate filled portion
        int filledCells = (int) Math.floor(progress * 100);
        
        // Create cell
        Cell barCell = new Cell();
        barCell.setBorder(new SolidBorder(SECONDARY_COLOR, 1));
        barCell.setPadding(0);
        barCell.setHeight(20);
        
        // Create nested table for inner bar
        Table innerBar = new Table(UnitValue.createPercentArray(new float[]{filledCells, 100 - filledCells}));
        innerBar.setWidth(UnitValue.createPercentValue(100));
        
        // Filled portion
        Cell filledCell = new Cell();
        filledCell.setBackgroundColor(SUCCESS_COLOR);
        filledCell.setBorder(null);
        filledCell.setHeight(18);
        
        // Empty portion
        Cell emptyCell = new Cell();
        emptyCell.setBackgroundColor(LIGHT_COLOR, 0.3f);
        emptyCell.setBorder(null);
        emptyCell.setHeight(18);
        
        // Only add cells if there is content to show
        if (filledCells > 0) {
            innerBar.addCell(filledCell);
        }
        
        if (filledCells < 100) {
            innerBar.addCell(emptyCell);
        }
        
        barCell.add(innerBar);
        progressBar.addCell(barCell);
        
        return progressBar;
    }
    
    /**
     * Add detailed progression information
     */
    private static void addProgressionDetails(Document document, Map<String, Object> progressionData) {
        // Section title
        Paragraph sectionTitle = new Paragraph("Détails de Progression")
                .setFontSize(HEADER_FONT_SIZE)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(10);
        document.add(sectionTitle);
        
        // Create details table
        Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        detailsTable.setWidth(UnitValue.createPercentValue(100));
        
        // Code Sessions Details
        Cell codeDetailsCell = new Cell();
        codeDetailsCell.setBorder(new SolidBorder(SECONDARY_COLOR, 1));
        codeDetailsCell.setPadding(10);
        codeDetailsCell.setBackgroundColor(LIGHT_COLOR, 0.1f);
        
        Paragraph codeTitle = new Paragraph("Séances de Code")
                .setFontSize(SUBHEADER_FONT_SIZE)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(5);
        codeDetailsCell.add(codeTitle);
        
        // Extract data
        int totalSeancesCode = (int) progressionData.get("totalSeancesCode");
        int seancesCodeCompletes = (int) progressionData.get("seancesCodeCompletes");
        
        Paragraph codeDetails = new Paragraph(
                "Total des séances planifiées: " + totalSeancesCode + "\n" +
                "Séances complétées: " + seancesCodeCompletes + "\n" +
                "Séances restantes: " + (totalSeancesCode - seancesCodeCompletes) + "\n\n" +
                "Recommandations:\n" +
                "• Maintenir une fréquence régulière de participation aux séances\n" +
                "• Réviser le code de la route entre les séances\n" +
                "• Poser des questions lors des séances en cas de doute"
        )
        .setFontSize(BODY_FONT_SIZE)
        .setFontColor(TEXT_COLOR);
        
        codeDetailsCell.add(codeDetails);
        detailsTable.addCell(codeDetailsCell);
        
        // Driving Sessions Details
        Cell driveDetailsCell = new Cell();
        driveDetailsCell.setBorder(new SolidBorder(SECONDARY_COLOR, 1));
        driveDetailsCell.setPadding(10);
        driveDetailsCell.setBackgroundColor(LIGHT_COLOR, 0.1f);
        
        Paragraph driveTitle = new Paragraph("Séances de Conduite")
                .setFontSize(SUBHEADER_FONT_SIZE)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(5);
        driveDetailsCell.add(driveTitle);
        
        // Extract data
        int totalSeancesConduite = (int) progressionData.get("totalSeancesConduite");
        int seancesConduiteCompletes = (int) progressionData.get("seancesConduiteCompletes");
        
        Paragraph driveDetails = new Paragraph(
                "Total des séances planifiées: " + totalSeancesConduite + "\n" +
                "Séances complétées: " + seancesConduiteCompletes + "\n" +
                "Séances restantes: " + (totalSeancesConduite - seancesConduiteCompletes) + "\n\n" +
                "Recommandations:\n" +
                "• Pratiquer les manœuvres difficiles régulièrement\n" +
                "• Être ponctuel aux séances de conduite\n" +
                "• Suivre attentivement les instructions du moniteur"
        )
        .setFontSize(BODY_FONT_SIZE)
        .setFontColor(TEXT_COLOR);
        
        driveDetailsCell.add(driveDetails);
        detailsTable.addCell(driveDetailsCell);
        
        document.add(detailsTable);
        document.add(new Paragraph("\n"));
    }
    
    /**
     * Add footer with page number and company info
     */
    private static void addFooter(Document document) {
        // Separator line
        LineSeparator separator = new LineSeparator(new SolidLine(1f));
        separator.setFontColor(SECONDARY_COLOR);
        separator.setMarginTop(10);
        separator.setMarginBottom(10);
        document.add(separator);
        
        // Footer table
        Table footerTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}));
        footerTable.setWidth(UnitValue.createPercentValue(100));
        
        // Company info
        Cell infoCell = new Cell();
        infoCell.setBorder(null);
        infoCell.setPadding(0);
        
        Paragraph info = new Paragraph("Auto-École Excellence • 123 Avenue Habib Bourguiba, Tunis • Tél: +216 71 123 456")
                .setFontSize(FOOTER_FONT_SIZE)
                .setFontColor(TEXT_COLOR)
                .setItalic();
        infoCell.add(info);
        footerTable.addCell(infoCell);
        
        // Date and page
        Cell dateCell = new Cell();
        dateCell.setBorder(null);
        dateCell.setPadding(0);
        dateCell.setTextAlignment(TextAlignment.RIGHT);
        
        Paragraph date = new Paragraph("Généré le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFontSize(FOOTER_FONT_SIZE)
                .setFontColor(TEXT_COLOR)
                .setItalic();
        dateCell.add(date);
        footerTable.addCell(dateCell);
        
        document.add(footerTable);
    }
}
