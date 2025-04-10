package org.cpi2.utils;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.cpi2.entities.Candidat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InvoiceGenerator {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceGenerator.class);
    private static final String FONT_PATH = "src/main/resources/fonts/arial.ttf";
    private static final float MARGIN = 50f;
    private static final float LINE_SPACING = 1.5f;
    private static final float TITLE_FONT_SIZE = 24f;
    private static final float HEADER_FONT_SIZE = 14f;
    private static final float BODY_FONT_SIZE = 12f;
    private static final float FOOTER_FONT_SIZE = 10f;
    
    // Custom colors
    private static final DeviceRgb DARK_BLUE = new DeviceRgb(0, 51, 102);
    private static final DeviceRgb LIGHT_BLUE = new DeviceRgb(173, 216, 230);
    private static final DeviceRgb GRAY = new DeviceRgb(128, 128, 128);

    public static void generatePDF(Candidat candidat, String typeFacture, LocalDate dateDebut, 
                                 LocalDate dateFin, double montant, String note) {
        try {
            logger.info("Début de la génération du PDF pour le candidat: {}", candidat.getNom());
            
            String documentsPath = System.getProperty("user.home") + "/Documents";
            File documentsDir = new File(documentsPath);
            if (!documentsDir.exists()) {
                logger.info("Création du dossier Documents");
                documentsDir.mkdirs();
            }

            String invoicesPath = documentsPath + "/Factures";
            File invoicesDir = new File(invoicesPath);
            if (!invoicesDir.exists()) {
                logger.info("Création du dossier Factures");
                invoicesDir.mkdirs();
            }

            String fileName = invoicesPath + "/Facture_" + candidat.getNom().replace(" ", "_") + "_" + 
                            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            
            logger.info("Création du fichier PDF: {}", fileName);
            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Set margins
            document.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
            
            // Add header with logo and company info
            addHeader(document, candidat);
            
            // Add client information
            addClientInfo(document, candidat);
            
            // Add invoice details
            addInvoiceDetails(document, typeFacture, dateDebut, dateFin, montant);
            
            // Add payment information
            addPaymentInfo(document, montant);
            
            // Add note if exists
            if (note != null && !note.trim().isEmpty()) {
                addNote(document, note);
            }
            
            // Add footer
            addFooter(document);
            
            document.close();
            
            logger.info("PDF généré avec succès: {}", fileName);
            
            AlertUtil.showInfo("Succès", "La facture a été générée avec succès dans le dossier Documents/Factures");
        } catch (FileNotFoundException e) {
            logger.error("Erreur lors de la création du fichier PDF", e);
            AlertUtil.showError("Erreur", "Impossible de générer le PDF : " + e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de la génération du PDF", e);
            AlertUtil.showError("Erreur", "Une erreur est survenue: " + e.getMessage());
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
        
        // Logo and company info
        Paragraph header = new Paragraph()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(TITLE_FONT_SIZE)
                .setFontColor(DARK_BLUE)
                .add("Auto-École Excellence")
                .setMarginBottom(20);
        
        document.add(header);
        
        // Company details
        Paragraph companyDetails = new Paragraph()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(BODY_FONT_SIZE)
                .add("123 Avenue Habib Bourguiba, Tunis\n")
                .add("Tél: +216 71 123 456\n")
                .add("Email: contact@autoecole-excellence.tn\n")
                .add("R.C.: 12345678/A/M/000\n")
                .add("Matricule Fiscale: 12345678\n")
                .setMarginBottom(20);
        
        document.add(companyDetails);
        
        // Separator
        document.add(new LineSeparator(new SolidLine())
                .setStrokeColor(DARK_BLUE)
                .setMarginBottom(20));
    }

    private static void addClientInfo(Document document, Candidat candidat) {
        Paragraph clientInfo = new Paragraph()
                .setFontSize(HEADER_FONT_SIZE)
                .setFontColor(DARK_BLUE)
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
        Paragraph details = new Paragraph()
                .setFontSize(HEADER_FONT_SIZE)
                .setFontColor(DARK_BLUE)
                .add("Détails de la facture :\n")
                .setMarginBottom(10);
        
        details.add(new Text("Type : " + typeFacture + "\n")
                .setFontSize(BODY_FONT_SIZE));
        details.add(new Text("Période : " + dateDebut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                          " - " + dateFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n")
                .setFontSize(BODY_FONT_SIZE));
        details.add(new Text("Date d'émission : " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n")
                .setFontSize(BODY_FONT_SIZE));
        details.add(new Text("Date limite de paiement : " + 
                          LocalDate.now().plusDays(30).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n")
                .setFontSize(BODY_FONT_SIZE));
        
        document.add(details);
        document.add(new Paragraph().setMarginBottom(20));
    }

    private static void addPaymentInfo(Document document, double montant) {
        Table paymentTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
        
        // Net amount
        paymentTable.addCell(new Cell().add(new Paragraph("Montant net :"))
                .setBackgroundColor(LIGHT_BLUE));
        paymentTable.addCell(new Cell().add(new Paragraph(String.format("%.2f DT", montant)))
                .setTextAlignment(TextAlignment.RIGHT));
        
        // VAT
        double tva = montant * 0.19; // 19% VAT
        paymentTable.addCell(new Cell().add(new Paragraph("TVA (19%) :"))
                .setBackgroundColor(LIGHT_BLUE));
        paymentTable.addCell(new Cell().add(new Paragraph(String.format("%.2f DT", tva)))
                .setTextAlignment(TextAlignment.RIGHT));
        
        // Total amount
        paymentTable.addCell(new Cell().add(new Paragraph("Montant total TTC :"))
                .setBackgroundColor(LIGHT_BLUE)
                .setBold());
        paymentTable.addCell(new Cell().add(new Paragraph(String.format("%.2f DT", montant + tva)))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBold());
        
        document.add(paymentTable);
        document.add(new Paragraph().setMarginBottom(20));
    }

    private static void addNote(Document document, String note) {
        Paragraph noteParagraph = new Paragraph()
                .setFontSize(HEADER_FONT_SIZE)
                .setFontColor(DARK_BLUE)
                .add("Note :\n")
                .setMarginBottom(10);
        
        noteParagraph.add(new Text(note)
                .setFontSize(BODY_FONT_SIZE));
        
        document.add(noteParagraph);
        document.add(new Paragraph().setMarginBottom(20));
    }

    private static void addFooter(Document document) {
        // Payment conditions
        Paragraph conditions = new Paragraph()
                .setFontSize(BODY_FONT_SIZE)
                .setFontColor(DARK_BLUE)
                .add("Conditions de paiement :\n")
                .add("Le paiement doit être effectué dans les 30 jours suivant la date d'émission de la facture.\n")
                .add("En cas de retard, des frais de retard de 1% par mois seront appliqués.\n")
                .setMarginBottom(20);
        
        document.add(conditions);
        
        // Separator
        document.add(new LineSeparator(new SolidLine())
                .setStrokeColor(DARK_BLUE)
                .setMarginTop(20)
                .setMarginBottom(10));
        
        Paragraph footer = new Paragraph()
                .setFontSize(FOOTER_FONT_SIZE)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(GRAY)
                .add("Auto-École Excellence\n")
                .add("123 Avenue Habib Bourguiba, Tunis\n")
                .add("Tél: +216 71 123 456\n")
                .add("Email: contact@autoecole-excellence.tn\n")
                .add("R.C.: 12345678/A/M/000\n")
                .add("Matricule Fiscale: 12345678\n")
                .add("Date d'émission: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        document.add(footer);
    }

    public static void previewInvoice(Candidat candidat, String typeFacture, LocalDate dateDebut, 
                                    LocalDate dateFin, double montant, String note) {
        VBox previewBox = new VBox(10);
        previewBox.setStyle("-fx-padding: 20; -fx-background-color: white;");
        
        // Header
        Label header = new Label("Auto-École Excellence");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        header.setTextFill(javafx.scene.paint.Color.rgb(0, 51, 102));
        header.setStyle("-fx-alignment: center;");
        
        // Company details
        Label companyDetails = new Label(
            "123 Avenue Habib Bourguiba, Tunis\n" +
            "Tél: +216 71 123 456\n" +
            "Email: contact@autoecole-excellence.tn\n" +
            "R.C.: 12345678/A/M/000\n" +
            "Matricule Fiscale: 12345678"
        );
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