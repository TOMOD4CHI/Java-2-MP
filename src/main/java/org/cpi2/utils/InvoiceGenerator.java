package org.cpi2.utils;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.cpi2.entities.AutoEcole;
import org.cpi2.entities.Candidat;
import org.cpi2.service.AutoEcoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.nio.file.Paths;

public class InvoiceGenerator {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceGenerator.class);
    private static final String FONT_PATH = "src/main/resources/fonts/arial.ttf";
    private static final float MARGIN = 40f;
    private static final float LINE_SPACING = 1.5f;
    private static final float TITLE_FONT_SIZE = 20f;
    private static final float HEADER_FONT_SIZE = 14f;
    private static final float BODY_FONT_SIZE = 12f;
    private static final float FOOTER_FONT_SIZE = 10f;
    
    // Custom colors
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(0, 51, 102);
    private static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(0, 102, 204);
    private static final DeviceRgb LIGHT_BLUE = new DeviceRgb(204, 229, 255);
    private static final DeviceRgb GRAY = new DeviceRgb(128, 128, 128);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(240, 240, 240);
    private static final DeviceRgb TEXT_COLOR = new DeviceRgb(51, 51, 51);

    private static AutoEcole autoEcole;
    private static AutoEcoleService autoEcoleService;
    
    static {
        autoEcoleService = new AutoEcoleService();
        autoEcole = autoEcoleService.getAutoEcoleData();
    }

    public static void generatePDF(Candidat candidat, String typeFacture, LocalDate dateDebut, 
                                  LocalDate dateFin, double montant, String note) {
        try {
            logger.info("Début de la génération du PDF pour le candidat: {}", candidat.getNom());
            
            // Create PDF document
            PdfDocument pdf = new PdfDocument(new PdfWriter("facture_" + candidat.getCin() + 
                    "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + 
                    ".pdf"));
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
            
            // Add content
            addHeader(document, candidat);
            addClientInfo(document, candidat);
            addInvoiceDetails(document, typeFacture, dateDebut, dateFin, montant);
            addPaymentInfo(document, montant);
            addNote(document, note);
            addFooter(document);
            
            // Close document
            document.close();
            
            System.out.println("Invoice generated successfully at: " + 
                Paths.get("facture_" + candidat.getCin() + "_" + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + 
                ".pdf").toAbsolutePath());
            
        } catch (Exception e) {
            logger.error("Error generating invoice: " + e.getMessage(), e);
            throw new RuntimeException("Error generating invoice", e);
        }
    }

    private static void addHeader(Document document, Candidat candidat) {
        // Invoice number and date
        Table headerTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
        headerTable.addCell(new Cell().add(new Paragraph("Facture N°: " + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + candidat.getCin()))
                .setBorder(null));
        headerTable.addCell(new Cell().add(new Paragraph("Date: " + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setTextAlignment(TextAlignment.RIGHT))
                .setBorder(null));
        
        document.add(headerTable);
        
        // Company header
        Paragraph header = new Paragraph()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(TITLE_FONT_SIZE)
                .setFontColor(PRIMARY_COLOR)
                .add(autoEcole.getNom())
                .setMarginBottom(10);
        
        document.add(header);
        
        // Company details
        Paragraph companyDetails = new Paragraph()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(BODY_FONT_SIZE)
                .setFontColor(SECONDARY_COLOR)
                .add(autoEcole.getAdresse() + "\n")
                .add("Tél: " + autoEcole.getTelephone() + "\n")
                .add("Email: " + autoEcole.getEmail() + "\n")
                .setMarginBottom(20);
        
        document.add(companyDetails);
        
        // Separator
        document.add(new LineSeparator(new SolidLine(1f))
                .setStrokeColor(SECONDARY_COLOR)
                .setMarginBottom(20));
    }

    private static void addClientInfo(Document document, Candidat candidat) {
        Paragraph clientInfo = new Paragraph()
                .setFontSize(HEADER_FONT_SIZE)
                .setFontColor(PRIMARY_COLOR)
                .add("Informations du client :\n")
                .setMarginBottom(10);
        
        clientInfo.add(new Text("Nom : " + candidat.getNom() + " " + candidat.getPrenom() + "\n")
                .setFontSize(BODY_FONT_SIZE));
        clientInfo.add(new Text("CIN : " + candidat.getCin() + "\n")
                .setFontSize(BODY_FONT_SIZE));
        clientInfo.add(new Text("Téléphone : " + candidat.getTelephone() + "\n")
                .setFontSize(BODY_FONT_SIZE));
        clientInfo.add(new Text("Email : " + candidat.getEmail() + "\n")
                .setFontSize(BODY_FONT_SIZE));
        clientInfo.add(new Text("Adresse : " + candidat.getAdresse() + "\n")
                .setFontSize(BODY_FONT_SIZE));
        clientInfo.add(new Text("Date de naissance : " + candidat.getDateNaissance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n")
                .setFontSize(BODY_FONT_SIZE));
        
        document.add(clientInfo);
        document.add(new Paragraph().setMarginBottom(20));
    }

    private static void addInvoiceDetails(Document document, String typeFacture, 
                                    LocalDate dateDebut, LocalDate dateFin, double montant) {
        Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        detailsTable.setWidth(UnitValue.createPercentValue(100));
        
        // Add header row
        Cell headerCell = new Cell()
                .add(new Paragraph("Détails de la facture")
                        .setFontSize(HEADER_FONT_SIZE)
                        .setFontColor(PRIMARY_COLOR)
                        .setBold())
                .setBackgroundColor(LIGHT_BLUE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        
        detailsTable.addCell(headerCell);
        detailsTable.addCell(headerCell);
        
        // Add content rows
        Cell labelCell = new Cell().add(new Paragraph("Type :")
                .setFontSize(BODY_FONT_SIZE))
                .setBorder(new SolidBorder(LIGHT_GRAY, 0.5f));
        Cell valueCell = new Cell().add(new Paragraph(typeFacture)
                .setFontSize(BODY_FONT_SIZE))
                .setBorder(new SolidBorder(LIGHT_GRAY, 0.5f));
        detailsTable.addCell(labelCell);
        detailsTable.addCell(valueCell);
        
        labelCell = new Cell().add(new Paragraph("Période :")
                .setFontSize(BODY_FONT_SIZE))
                .setBorder(new SolidBorder(LIGHT_GRAY, 0.5f));
        valueCell = new Cell().add(new Paragraph(dateDebut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                      " - " + dateFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))))
                .setFontSize(BODY_FONT_SIZE)
                .setBorder(new SolidBorder(LIGHT_GRAY, 0.5f));
        detailsTable.addCell(labelCell);
        detailsTable.addCell(valueCell);
        
        labelCell = new Cell().add(new Paragraph("Date d'émission :")
                .setFontSize(BODY_FONT_SIZE))
                .setBorder(new SolidBorder(LIGHT_GRAY, 0.5f));
        valueCell = new Cell().add(new Paragraph(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))))
                .setFontSize(BODY_FONT_SIZE)
                .setBorder(new SolidBorder(LIGHT_GRAY, 0.5f));
        detailsTable.addCell(labelCell);
        detailsTable.addCell(valueCell);
        
        document.add(detailsTable);
        document.add(new Paragraph().setMarginBottom(20));
    }

    private static void addPaymentInfo(Document document, double montant) {
        Table paymentTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        paymentTable.setWidth(UnitValue.createPercentValue(100));
        
        // Add header row
        Cell headerCell = new Cell()
                .add(new Paragraph("Détails du paiement")
                        .setFontSize(HEADER_FONT_SIZE)
                        .setFontColor(PRIMARY_COLOR)
                        .setBold())
                .setBackgroundColor(LIGHT_BLUE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        
        paymentTable.addCell(headerCell);
        paymentTable.addCell(headerCell);
        
        // Add content rows
        Cell labelCell = new Cell().add(new Paragraph("Montant total :")
                .setFontSize(BODY_FONT_SIZE))
                .setBorder(new SolidBorder(LIGHT_GRAY, 0.5f));
        Cell valueCell = new Cell().add(new Paragraph(String.format("%.2f DT", montant))
                .setFontSize(BODY_FONT_SIZE))
                .setBorder(new SolidBorder(LIGHT_GRAY, 0.5f));
        paymentTable.addCell(labelCell);
        paymentTable.addCell(valueCell);
        
        document.add(paymentTable);
        document.add(new Paragraph().setMarginBottom(20));
    }

    private static void addNote(Document document, String note) {
        if (note != null && !note.trim().isEmpty()) {
            Table noteTable = new Table(UnitValue.createPercentArray(1));
            noteTable.setWidth(UnitValue.createPercentValue(100));
            
            Cell headerCell = new Cell()
                    .add(new Paragraph("Note")
                            .setFontSize(HEADER_FONT_SIZE)
                            .setFontColor(PRIMARY_COLOR)
                            .setBold())
                    .setBackgroundColor(LIGHT_BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5);
            
            noteTable.addCell(headerCell);
            
            Cell noteCell = new Cell()
                    .add(new Paragraph(note)
                            .setFontSize(BODY_FONT_SIZE))
                    .setBorder(new SolidBorder(LIGHT_GRAY, 0.5f));
            
            noteTable.addCell(noteCell);
            document.add(noteTable);
            document.add(new Paragraph().setMarginBottom(20));
        }
    }

    private static void addFooter(Document document) {
        // Payment conditions
        Table conditionsTable = new Table(UnitValue.createPercentArray(1));
        conditionsTable.setWidth(UnitValue.createPercentValue(100));
        
        Cell conditionsCell = new Cell()
                .add(new Paragraph("Conditions de paiement")
                        .setFontSize(HEADER_FONT_SIZE)
                        .setFontColor(PRIMARY_COLOR)
                        .setBold())
                .setBackgroundColor(LIGHT_BLUE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        
        conditionsTable.addCell(conditionsCell);
        
        Cell conditionsContent = new Cell()
                .add(new Paragraph("• Le paiement doit être effectué dans les 30 jours suivant la date d'émission de la facture.\n" +
                                  "• En cas de retard, des frais de retard de 1% par mois seront appliqués.")
                        .setFontSize(BODY_FONT_SIZE))
                .setBorder(new SolidBorder(LIGHT_GRAY, 0.5f));
        
        conditionsTable.addCell(conditionsContent);
        document.add(conditionsTable);
        
        // Separator
        document.add(new LineSeparator(new SolidLine(1f))
                .setStrokeColor(SECONDARY_COLOR)
                .setMarginTop(20)
                .setMarginBottom(10));
        
        // Footer information
        Paragraph footer = new Paragraph()
                .setFontSize(FOOTER_FONT_SIZE)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(GRAY)
                .add(autoEcole.getNom() + "\n")
                .add(autoEcole.getAdresse() + "\n")
                .add("Tél: " + autoEcole.getTelephone() + "\n")
                .add("Email: " + autoEcole.getEmail() + "\n")
                .add("Date d'émission: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        document.add(footer);
    }

    public static void previewInvoice(Candidat candidat, String typeFacture, LocalDate dateDebut, 
                                    LocalDate dateFin, double montant, String note) {
        VBox previewBox = new VBox(10);
        previewBox.setStyle("-fx-padding: 20; -fx-background-color: white;");
        
        // Header
        Label header = new Label(autoEcole.getNom());
        header.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        header.setTextFill(javafx.scene.paint.Color.rgb(0, 51, 102));
        header.setStyle("-fx-alignment: center;");
        
        // Company details
        Label companyDetails = new Label(
            autoEcole.getAdresse() + "\n" +
            "Tél: " + autoEcole.getTelephone() + "\n" +
            "Email: " + autoEcole.getEmail() + "\n" );
        companyDetails.setFont(Font.font("Arial", 12));
        companyDetails.setStyle("-fx-alignment: center;");
        
        // Invoice details
        Label invoiceDetails = new Label(
            "Facture N°: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + candidat.getCin() + "\n" +
            "Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );
        invoiceDetails.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        // Client info
        Label clientInfo = new Label(
            "Informations du client :\n" +
            "Nom : " + candidat.getNom() + " " + candidat.getPrenom() + "\n" +
            "CIN : " + candidat.getCin() + "\n" +
            "Téléphone : " + candidat.getTelephone() + "\n" +
            "Email : " + candidat.getEmail() + "\n" +
            "Adresse : " + candidat.getAdresse() + "\n" +
            "Date de naissance : " + candidat.getDateNaissance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );
        clientInfo.setFont(Font.font("Arial", 12));
        
        // Invoice type and dates
        Label invoiceType = new Label(
            "Type de facture : " + typeFacture + "\n" +
            "Période : " + dateDebut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
            " - " + dateFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" +
            "Date limite de paiement : " + LocalDate.now().plusDays(30).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );
        invoiceType.setFont(Font.font("Arial", 12));
        
        // Payment details
        double tva = montant * 0.19;
        Label paymentDetails = new Label(
            "Montant net : " + String.format("%.2f DT", montant) + "\n" +
            "TVA (19%) : " + String.format("%.2f DT", tva) + "\n" +
            "Montant total TTC : " + String.format("%.2f DT", montant + tva)
        );
        paymentDetails.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        paymentDetails.setStyle("-fx-background-color: #ADD8E6; -fx-padding: 10;");
        
        // Payment conditions
        Label conditions = new Label(
            "Conditions de paiement :\n" +
            "Le paiement doit être effectué dans les 30 jours suivant la date d'émission de la facture.\n" +
            "En cas de retard, des frais de retard de 1% par mois seront appliqués."
        );
        conditions.setFont(Font.font("Arial", 12));
        
        // Note if exists
        if (note != null && !note.trim().isEmpty()) {
            Label noteLabel = new Label("Note :\n" + note);
            noteLabel.setFont(Font.font("Arial", 12));
            previewBox.getChildren().add(noteLabel);
        }
        
        // Add all elements to the preview box
        previewBox.getChildren().addAll(
            header,
            companyDetails,
            new javafx.scene.control.Separator(),
            invoiceDetails,
            new javafx.scene.control.Separator(),
            clientInfo,
            new javafx.scene.control.Separator(),
            invoiceType,
            new javafx.scene.control.Separator(),
            paymentDetails,
            new javafx.scene.control.Separator(),
            conditions
        );
        
        // Create and show the alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Prévisualisation de la facture");
        alert.setHeaderText("Aperçu de la facture");
        alert.getDialogPane().setContent(previewBox);
        alert.getDialogPane().setPrefWidth(600);
        alert.showAndWait();
    }
} 