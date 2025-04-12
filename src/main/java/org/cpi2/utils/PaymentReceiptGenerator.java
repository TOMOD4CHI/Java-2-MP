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
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Utility class for generating PDF payment receipts
 */
public class PaymentReceiptGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PaymentReceiptGenerator.class);
    
    // PDF styling constants
    private static final float MARGIN = 50f;
    private static final float TITLE_FONT_SIZE = 20f;
    private static final float HEADER_FONT_SIZE = 16f;
    private static final float SUBHEADER_FONT_SIZE = 14f;
    private static final float BODY_FONT_SIZE = 12f;
    private static final float FOOTER_FONT_SIZE = 10f;
    
    // Custom colors
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(31, 78, 121); // Dark blue
    private static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(46, 134, 193); // Lighter blue
    private static final DeviceRgb HIGHLIGHT_COLOR = new DeviceRgb(241, 196, 15); // Gold
    private static final DeviceRgb LIGHT_COLOR = new DeviceRgb(245, 245, 245); // Light gray
    private static final DeviceRgb TEXT_COLOR = new DeviceRgb(50, 50, 50); // Dark gray
    private static final DeviceRgb SUCCESS_COLOR = new DeviceRgb(46, 204, 113); // Green
    
    /**
     * Generate a payment receipt PDF for a single payment
     */
    public static String generateSinglePaymentReceipt(Map<String, Object> paymentData) {
        try {
            // Extract payment data safely handling different types
            LocalDate paymentDate;
            Object dateObj = paymentData.get("date");
            if (dateObj instanceof LocalDate) {
                paymentDate = (LocalDate) dateObj;
            } else if (dateObj instanceof String) {
                paymentDate = LocalDate.parse((String) dateObj);
            } else {
                // Default to today if date can't be parsed
                paymentDate = LocalDate.now();
                logger.warn("Payment date couldn't be parsed, using today's date");
            }
            
            String candidatName = (String) paymentData.get("candidat");
            String candidatCin = (String) paymentData.get("cin");
            String paymentType = (String) paymentData.get("type");
            Double amount = (Double) paymentData.get("montant");
            String paymentMethod = (String) paymentData.get("methode");
            String description = (String) paymentData.get("description");
            
            logger.info("Generating receipt for payment from: {}", candidatName);
            
            // Create directory if it doesn't exist
            String documentsPath = System.getProperty("user.home") + "/Documents";
            File documentsDir = new File(documentsPath);
            if (!documentsDir.exists()) {
                documentsDir.mkdirs();
            }

            String receiptsPath = documentsPath + "/Recus_Paiements";
            File receiptsDir = new File(receiptsPath);
            if (!receiptsDir.exists()) {
                receiptsDir.mkdirs();
            }

            // Create filename with date and candidate name
            String fileName = receiptsPath + "/Recu_" + candidatName.replace(" ", "_") + "_" + 
                              LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            
            // Create PDF document
            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Set margins
            document.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
            
            // Add receipt components
            addHeader(document, "Reçu de Paiement");
            addCompanyInfo(document);
            addPaymentInfo(document, paymentData);
            addAmountSection(document, amount);
            if(description != null && !description.isEmpty()) {
                addDescription(document, description);
            }
            addFooter(document);
            
            document.close();
            
            logger.info("Receipt generated successfully: {}", fileName);
            
            return fileName;
            
        } catch (Exception e) {
            logger.error("Error generating payment receipt: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Generate a payment summary report for multiple payments
     */
    public static String generatePaymentSummaryReport(List<Map<String, Object>> paymentsData, LocalDate startDate, LocalDate endDate, String candidat) {
        try {
            logger.info("Generating payment summary report");
            
            // Create directory if it doesn't exist
            String documentsPath = System.getProperty("user.home") + "/Documents";
            File documentsDir = new File(documentsPath);
            if (!documentsDir.exists()) {
                documentsDir.mkdirs();
            }

            String reportsPath = documentsPath + "/Rapports_Paiements";
            File reportsDir = new File(reportsPath);
            if (!reportsDir.exists()) {
                reportsDir.mkdirs();
            }

            // Create filename
            String fileNamePrefix = candidat != null ? "Paiements_" + candidat.split(" ")[0].replace(" ", "_") : "Tous_Paiements";
            String fileName = reportsPath + "/" + fileNamePrefix + "_" + 
                              LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            
            // Create PDF document
            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Set margins
            document.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
            
            // Add report components
            addHeader(document, "Rapport des Paiements");
            addCompanyInfo(document);
            addReportPeriod(document, startDate, endDate, candidat);
            addPaymentsList(document, paymentsData);
            addSummarySection(document, paymentsData);
            addFooter(document);
            
            document.close();
            
            logger.info("Payment report generated successfully: {}", fileName);
            
            return fileName;
            
        } catch (Exception e) {
            logger.error("Error generating payment report: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Add header with logo and title
     */
    private static void addHeader(Document document, String title) {
        // Create a table for header layout
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{100}));
        headerTable.setWidth(UnitValue.createPercentValue(100));
        
        Cell headerCell = new Cell();
        headerCell.setBorder(null);
        headerCell.setPadding(10);
        headerCell.setBackgroundColor(PRIMARY_COLOR);
        
        // Title
        Paragraph titleParagraph = new Paragraph(title)
                .setFontSize(TITLE_FONT_SIZE)
                .setBold()
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER);
        
        headerCell.add(titleParagraph);
        headerTable.addCell(headerCell);
        
        document.add(headerTable);
        document.add(new Paragraph("\n"));
    }
    
    /**
     * Add company information section
     */
    private static void addCompanyInfo(Document document) {
        // Create table for company info
        Table companyTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        companyTable.setWidth(UnitValue.createPercentValue(100));
        
        // Company name cell
        Cell nameCell = new Cell();
        nameCell.setBorder(null);
        nameCell.setPadding(5);
        
        Paragraph companyName = new Paragraph("Auto-École Excellence")
                .setFontSize(HEADER_FONT_SIZE)
                .setBold()
                .setFontColor(PRIMARY_COLOR);
        nameCell.add(companyName);
        
        // Company details cell
        Cell detailsCell = new Cell();
        detailsCell.setBorder(null);
        detailsCell.setPadding(5);
        detailsCell.setTextAlignment(TextAlignment.RIGHT);
        
        Paragraph companyDetails = new Paragraph(
                "123 Avenue Habib Bourguiba, Tunis\n" +
                "Tél: +216 71 123 456\n" +
                "Email: contact@autoecole-excellence.tn\n" +
                "Matricule Fiscale: 12345678")
                .setFontSize(BODY_FONT_SIZE)
                .setFontColor(TEXT_COLOR);
        detailsCell.add(companyDetails);
        
        companyTable.addCell(nameCell);
        companyTable.addCell(detailsCell);
        
        document.add(companyTable);
        
        // Add separator
        LineSeparator separator = new LineSeparator(new SolidLine(1f));
        separator.setFontColor(SECONDARY_COLOR);
        separator.setMarginTop(5);
        separator.setMarginBottom(15);
        document.add(separator);
    }
    
    /**
     * Add payment information section
     */
    private static void addPaymentInfo(Document document, Map<String, Object> paymentData) {
        // Extract data
        LocalDate paymentDate = (LocalDate) paymentData.get("date");
        String candidatName = (String) paymentData.get("candidat");
        String candidatCin = (String) paymentData.get("cin");
        String paymentType = (String) paymentData.get("type");
        String paymentMethod = (String) paymentData.get("methode");
        
        // Section title
        Paragraph sectionTitle = new Paragraph("Détails du Paiement")
                .setFontSize(SUBHEADER_FONT_SIZE)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(10);
        document.add(sectionTitle);
        
        // Payment info table
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        infoTable.setWidth(UnitValue.createPercentValue(100));
        infoTable.setBackgroundColor(LIGHT_COLOR, 0.3f);
        infoTable.setBorder(new SolidBorder(SECONDARY_COLOR, 1));
        
        // Add info rows
        addInfoRow(infoTable, "Numéro de Reçu", generateReceiptNumber(paymentDate, candidatCin));
        addInfoRow(infoTable, "Date", paymentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        addInfoRow(infoTable, "Candidat", candidatName);
        addInfoRow(infoTable, "CIN", candidatCin);
        addInfoRow(infoTable, "Type de Paiement", paymentType);
        addInfoRow(infoTable, "Mode de Paiement", paymentMethod);
        
        document.add(infoTable);
        document.add(new Paragraph("\n"));
    }
    
    /**
     * Add period information for summary reports
     */
    private static void addReportPeriod(Document document, LocalDate startDate, LocalDate endDate, String candidat) {
        // Section title
        Paragraph sectionTitle = new Paragraph("Informations du Rapport")
                .setFontSize(SUBHEADER_FONT_SIZE)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(10);
        document.add(sectionTitle);
        
        // Report info table
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        infoTable.setWidth(UnitValue.createPercentValue(100));
        infoTable.setBackgroundColor(LIGHT_COLOR, 0.3f);
        infoTable.setBorder(new SolidBorder(SECONDARY_COLOR, 1));
        
        // Add info rows
        addInfoRow(infoTable, "Période", startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                " - " + endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        if (candidat != null && !candidat.isEmpty()) {
            addInfoRow(infoTable, "Candidat", candidat);
        } else {
            addInfoRow(infoTable, "Candidat", "Tous les candidats");
        }
        
        addInfoRow(infoTable, "Date de génération", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        document.add(infoTable);
        document.add(new Paragraph("\n"));
    }
    
    /**
     * Add list of payments for summary report
     */
    private static void addPaymentsList(Document document, List<Map<String, Object>> paymentsData) {
        // Section title
        Paragraph sectionTitle = new Paragraph("Liste des Paiements")
                .setFontSize(SUBHEADER_FONT_SIZE)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(10);
        document.add(sectionTitle);
        
        // Create table for payments
        Table paymentsTable = new Table(UnitValue.createPercentArray(new float[]{15, 25, 20, 15, 25}));
        paymentsTable.setWidth(UnitValue.createPercentValue(100));
        
        // Add header row
        Cell dateHeader = new Cell().add(new Paragraph("Date").setBold().setFontColor(ColorConstants.WHITE));
        Cell candidatHeader = new Cell().add(new Paragraph("Candidat").setBold().setFontColor(ColorConstants.WHITE));
        Cell typeHeader = new Cell().add(new Paragraph("Type").setBold().setFontColor(ColorConstants.WHITE));
        Cell amountHeader = new Cell().add(new Paragraph("Montant").setBold().setFontColor(ColorConstants.WHITE));
        Cell methodHeader = new Cell().add(new Paragraph("Méthode").setBold().setFontColor(ColorConstants.WHITE));
        
        dateHeader.setBackgroundColor(PRIMARY_COLOR);
        candidatHeader.setBackgroundColor(PRIMARY_COLOR);
        typeHeader.setBackgroundColor(PRIMARY_COLOR);
        amountHeader.setBackgroundColor(PRIMARY_COLOR);
        methodHeader.setBackgroundColor(PRIMARY_COLOR);
        
        paymentsTable.addHeaderCell(dateHeader);
        paymentsTable.addHeaderCell(candidatHeader);
        paymentsTable.addHeaderCell(typeHeader);
        paymentsTable.addHeaderCell(amountHeader);
        paymentsTable.addHeaderCell(methodHeader);
        
        // Add payment rows
        boolean alternate = false;
        for (Map<String, Object> payment : paymentsData) {
            LocalDate date = (LocalDate) payment.get("date");
            String candidat = (String) payment.get("candidat");
            String type = (String) payment.get("type");
            Double amount = (Double) payment.get("montant");
            String method = (String) payment.get("methode");
            
            Cell dateCell = new Cell().add(new Paragraph(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            Cell candidatCell = new Cell().add(new Paragraph(candidat));
            Cell typeCell = new Cell().add(new Paragraph(type));
            Cell amountCell = new Cell().add(new Paragraph(String.format("%.2f DT", amount)).setTextAlignment(TextAlignment.RIGHT));
            Cell methodCell = new Cell().add(new Paragraph(method));
            
            if (alternate) {
                dateCell.setBackgroundColor(LIGHT_COLOR, 0.3f);
                candidatCell.setBackgroundColor(LIGHT_COLOR, 0.3f);
                typeCell.setBackgroundColor(LIGHT_COLOR, 0.3f);
                amountCell.setBackgroundColor(LIGHT_COLOR, 0.3f);
                methodCell.setBackgroundColor(LIGHT_COLOR, 0.3f);
            }
            
            paymentsTable.addCell(dateCell);
            paymentsTable.addCell(candidatCell);
            paymentsTable.addCell(typeCell);
            paymentsTable.addCell(amountCell);
            paymentsTable.addCell(methodCell);
            
            alternate = !alternate;
        }
        
        document.add(paymentsTable);
        document.add(new Paragraph("\n"));
    }
    
    /**
     * Add amount section for individual payment receipt
     */
    private static void addAmountSection(Document document, Double amount) {
        // Create amount box
        Table amountTable = new Table(UnitValue.createPercentArray(new float[]{100}));
        amountTable.setWidth(UnitValue.createPercentValue(100));
        
        Cell amountCell = new Cell();
        amountCell.setBorder(new SolidBorder(HIGHLIGHT_COLOR, 2));
        amountCell.setBackgroundColor(SECONDARY_COLOR, 0.1f);
        amountCell.setPadding(15);
        
        Paragraph amountTitle = new Paragraph("Montant Payé")
                .setFontSize(SUBHEADER_FONT_SIZE)
                .setBold()
                .setFontColor(SECONDARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER);
        
        Paragraph amountValue = new Paragraph(String.format("%.2f DT", amount))
                .setFontSize(TITLE_FONT_SIZE)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER);
        
        amountCell.add(amountTitle);
        amountCell.add(amountValue);
        
        amountTable.addCell(amountCell);
        
        document.add(amountTable);
        document.add(new Paragraph("\n"));
        
        // Payment confirmation
        Paragraph confirmation = new Paragraph("Ce reçu confirme que le paiement a été effectué à la date indiquée ci-dessus.")
                .setFontSize(BODY_FONT_SIZE)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER);
        
        document.add(confirmation);
        document.add(new Paragraph("\n"));
    }
    
    /**
     * Add summary section for payment reports
     */
    private static void addSummarySection(Document document, List<Map<String, Object>> paymentsData) {
        // Calculate totals
        double total = 0;
        int countInscription = 0;
        int countExamen = 0;
        int countEspeces = 0;
        int countCarte = 0;
        int countCheque = 0;
        
        for (Map<String, Object> payment : paymentsData) {
            Double amount = (Double) payment.get("montant");
            String type = (String) payment.get("type");
            String method = (String) payment.get("methode");
            
            total += amount;
            
            if (type.contains("Inscription")) countInscription++;
            else if (type.contains("Examen")) countExamen++;
            
            if (method.contains("Espèces")) countEspeces++;
            else if (method.contains("Carte")) countCarte++;
            else if (method.contains("Chèque")) countCheque++;
        }
        
        // Section title
        Paragraph sectionTitle = new Paragraph("Résumé des Paiements")
                .setFontSize(SUBHEADER_FONT_SIZE)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(10);
        document.add(sectionTitle);
        
        // Total amount box
        Table totalTable = new Table(UnitValue.createPercentArray(new float[]{100}));
        totalTable.setWidth(UnitValue.createPercentValue(100));
        
        Cell totalCell = new Cell();
        totalCell.setBorder(new SolidBorder(HIGHLIGHT_COLOR, 2));
        totalCell.setBackgroundColor(SECONDARY_COLOR, 0.1f);
        totalCell.setPadding(15);
        
        Paragraph totalTitle = new Paragraph("Montant Total")
                .setFontSize(SUBHEADER_FONT_SIZE)
                .setBold()
                .setFontColor(SECONDARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER);
        
        Paragraph totalValue = new Paragraph(String.format("%.2f DT", total))
                .setFontSize(TITLE_FONT_SIZE)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER);
        
        totalCell.add(totalTitle);
        totalCell.add(totalValue);
        
        totalTable.addCell(totalCell);
        
        document.add(totalTable);
        document.add(new Paragraph("\n"));
        
        // Statistics tables
        Table statsTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        statsTable.setWidth(UnitValue.createPercentValue(100));
        
        // Payment types
        Cell typesCell = new Cell();
        typesCell.setBorder(new SolidBorder(SECONDARY_COLOR, 1));
        typesCell.setPadding(10);
        
        Paragraph typesTitle = new Paragraph("Par Type de Paiement")
                .setFontSize(SUBHEADER_FONT_SIZE)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER);
        
        Table typesTable = new Table(2);
        typesTable.setWidth(UnitValue.createPercentValue(100));
        
        typesTable.addCell(new Cell().add(new Paragraph("Inscription")).setBold());
        typesTable.addCell(new Cell().add(new Paragraph(String.valueOf(countInscription))));
        
        typesTable.addCell(new Cell().add(new Paragraph("Examen")).setBold());
        typesTable.addCell(new Cell().add(new Paragraph(String.valueOf(countExamen))));
        
        typesCell.add(typesTitle);
        typesCell.add(typesTable);
        
        // Payment methods
        Cell methodsCell = new Cell();
        methodsCell.setBorder(new SolidBorder(SECONDARY_COLOR, 1));
        methodsCell.setPadding(10);
        
        Paragraph methodsTitle = new Paragraph("Par Méthode de Paiement")
                .setFontSize(SUBHEADER_FONT_SIZE)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER);
        
        Table methodsTable = new Table(2);
        methodsTable.setWidth(UnitValue.createPercentValue(100));
        
        methodsTable.addCell(new Cell().add(new Paragraph("Espèces")).setBold());
        methodsTable.addCell(new Cell().add(new Paragraph(String.valueOf(countEspeces))));
        
        methodsTable.addCell(new Cell().add(new Paragraph("Carte")).setBold());
        methodsTable.addCell(new Cell().add(new Paragraph(String.valueOf(countCarte))));
        
        methodsTable.addCell(new Cell().add(new Paragraph("Chèque")).setBold());
        methodsTable.addCell(new Cell().add(new Paragraph(String.valueOf(countCheque))));
        
        methodsCell.add(methodsTitle);
        methodsCell.add(methodsTable);
        
        statsTable.addCell(typesCell);
        statsTable.addCell(methodsCell);
        
        document.add(statsTable);
        document.add(new Paragraph("\n"));
    }
    
    /**
     * Add description for individual payment receipt
     */
    private static void addDescription(Document document, String description) {
        // Section title
        Paragraph sectionTitle = new Paragraph("Description")
                .setFontSize(SUBHEADER_FONT_SIZE)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(10);
        document.add(sectionTitle);
        
        // Description box
        Table descTable = new Table(UnitValue.createPercentArray(new float[]{100}));
        descTable.setWidth(UnitValue.createPercentValue(100));
        
        Cell descCell = new Cell();
        descCell.setBorder(new SolidBorder(SECONDARY_COLOR, 1));
        descCell.setBackgroundColor(LIGHT_COLOR, 0.3f);
        descCell.setPadding(10);
        
        Paragraph descText = new Paragraph(description)
                .setFontSize(BODY_FONT_SIZE)
                .setFontColor(TEXT_COLOR);
        
        descCell.add(descText);
        descTable.addCell(descCell);
        
        document.add(descTable);
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
     * Generate a unique receipt number
     */
    private static String generateReceiptNumber(LocalDate date, String cin) {
        return "REC-" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + cin.substring(0, Math.min(cin.length(), 4));
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
        
        // Footer information
        Paragraph footer = new Paragraph("Auto-École Excellence • 123 Avenue Habib Bourguiba, Tunis • Tél: +216 71 123 456")
                .setFontSize(FOOTER_FONT_SIZE)
                .setFontColor(TEXT_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic();
        
        document.add(footer);
        
        // Legal note
        Paragraph legalNote = new Paragraph("Ce document est généré automatiquement et ne nécessite pas de signature.")
                .setFontSize(FOOTER_FONT_SIZE)
                .setFontColor(TEXT_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic();
        
        document.add(legalNote);
    }
}
