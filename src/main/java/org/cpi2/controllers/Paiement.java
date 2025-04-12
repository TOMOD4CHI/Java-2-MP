package org.cpi2.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.cpi2.entities.*;
import org.cpi2.service.CandidatService;
import org.cpi2.service.ExamenService;
import org.cpi2.service.InscriptionService;
import org.cpi2.service.PaiementService;
import org.cpi2.utils.PaymentReceiptGenerator;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class Paiement implements Initializable {
    private final PaiementService paiementService = new PaiementService();
    private final InscriptionService inscriptionService = new InscriptionService();
    private final CandidatService candidatService = new CandidatService();
    private final ExamenService examenService = new ExamenService();

    // Fields from FXML
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> candidatComboBox;
    @FXML private TextField montantField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> modeComboBox;
    @FXML private TextArea descriptionArea;

    // Search fields
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> searchCandidatComboBox;

    // Table
    @FXML private TableView<PaiementData> paiementsTable;
    @FXML private TableColumn<PaiementData, LocalDate> dateColumn;
    @FXML private TableColumn<PaiementData, String> candidatColumn;
    @FXML private TableColumn<PaiementData, String> typeColumn;
    @FXML private TableColumn<PaiementData, Double> montantColumn;
    @FXML private TableColumn<PaiementData, String> methodeColumn;
    @FXML private TableColumn<PaiementData, String> descriptionColumn;
    @FXML private TableColumn<PaiementData, Void> actionsColumn;

    // Labels and buttons
    @FXML private Label totalLabel;
    @FXML private Button printButton;
    @FXML private Button exportButton;

    // Mock data class for table
    public static class PaiementData {
        private long id;
        private LocalDate date;
        private String candidat;
        private String cin;
        private String type;
        private Double montant;
        private String methode;
        private String description;

        public PaiementData(LocalDate date, String candidat, String type, Double montant, String methode, String description) {
            this.date = date;
            this.cin=cin;
            this.candidat = candidat;
            this.type = type;
            this.montant = montant;
            this.methode = methode;
            this.description = description;
        }
        public PaiementData(long id, LocalDate date,String cin, String candidat, String type, Double montant, String methode, String description) {
            this.id = id;
            this.date = date;
            this.cin=cin;
            this.candidat = candidat;
            this.type = type;
            this.montant = montant;
            this.methode = methode;
            this.description = description;
        }

        public long getId() {
            return id;
        }
        public void setId(long id) {
            this.id = id;
        }
        public String getCin() { return cin; }
        public LocalDate getDate() { return date; }
        public String getCandidat() { return candidat; }
        public String getType() { return type; }
        public Double getMontant() { return montant; }
        public String getMethode() { return methode; }
        public String getDescription() { return description; }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupFormControls();
        setupTable();
        setupListeners();
        loadMockData();
    }

    private void setupFormControls() {
        // Initialize payment type options
        typeComboBox.setItems(FXCollections.observableArrayList(
                "Inscription", "Examen", "Tranche"
        ));

        // Initialize mock candidates
        candidatComboBox.setItems(FXCollections.observableArrayList(
                candidatService.getAllCandidats().stream()
                        .map(c -> "("+c.getCin()+") : "+c.getNom()+" "+c.getPrenom())
                        .toList()
        ));

        searchCandidatComboBox.setItems(candidatComboBox.getItems());

        // Initialize payment modes
        modeComboBox.setItems(FXCollections.observableArrayList(
                Arrays.stream(ModePaiement.values())
                        .map(Enum::name)
                        .toList()
        ));

        // Set today's date as default
        datePicker.setValue(LocalDate.now());

        // Add listener for amount field validation
        montantField.textProperty().addListener((obs, old, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                montantField.setText(old);
            }
        });
    }

    private void setupTable() {
        // Configure table columns
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        candidatColumn.setCellValueFactory(new PropertyValueFactory<>("candidat"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        montantColumn.setCellValueFactory(new PropertyValueFactory<>("montant"));
        methodeColumn.setCellValueFactory(new PropertyValueFactory<>("methode"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Add action buttons to the actions column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");

            {
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");

                editButton.setOnAction(event -> {
                    PaiementData data = getTableView().getItems().get(getIndex());
                    
                    try {
                        // Create the payment edit view
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/PaymentEdit.fxml"));
                        Parent root = loader.load();
                        
                        // Get controller and pass payment details
                        PaymentEditController controller = loader.getController();
                        controller.initData(data);
                        
                        // Show in new window
                        Stage stage = new Stage();
                        stage.setTitle("Modifier le Paiement #" + data.getId());
                        stage.setScene(new Scene(root));
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.showAndWait();
                        
                        // Refresh data after edit window is closed
                        loadMockData();
                        updateTotalLabel();
                    } catch (IOException e) {
                        e.printStackTrace();
                        showErrorDialog("Impossible d'ouvrir la page de modification: " + e.getMessage());
                    }
                });

                deleteButton.setOnAction(event -> {
                    PaiementData data = getTableView().getItems().get(getIndex());
                    boolean deleted = paiementService.deletePaiement(data.getId());
                    
                    if (deleted) {
                        showSuccessDialog("Paiement supprimé avec succès");
                        getTableView().getItems().remove(getIndex());
                        updateTotalLabel();
                    } else {
                        showErrorDialog("Impossible de supprimer le paiement");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void setupListeners() {
        // Set up button action handlers
        printButton.setOnAction(event -> handlePrintReceipt());
        exportButton.setOnAction(event -> handleExportPdf());
    }

    private void loadMockData() {
        ObservableList<PaiementData> paiements = FXCollections.observableArrayList(
                paiementService.getAllPaiements().stream()
                        .map(p -> new PaiementData(
                                p.getId(),
                                p.getDatePaiement(),
                                p.getCandidat().getCin(),
                                p.getCandidat().getNom() + " " + p.getCandidat().getPrenom(),
                                p instanceof PaiementInscription ? ((p.getTypePaiement() == null) ? "Inscription" : p.getTypePaiement()): "Examen",
                                p.getMontant(),
                                p.getModePaiement().toString(),
                                p.getDescription()
                        )).toList()
        );
        paiementsTable.setItems(paiements);
        updateTotalLabel();
    }

    @FXML
    private void handleSavePayment() {
        if (!validatePaymentForm()) {
            return;
        }

        try {
            double montant = Double.parseDouble(montantField.getText());
            String type = typeComboBox.getValue();
            String candidat = candidatComboBox.getValue();
            String mode = modeComboBox.getValue();
            LocalDate date = datePicker.getValue();
            String description = descriptionArea.getText();

            String cin = candidat.substring(1, candidat.indexOf(")"));
            
            try {
                if (type.equals("Examen")) {
                    if(!examenService.hasPendingExamens(cin)){
                        showErrorDialog("Le candidat n'a pas d'examen actif");
                        return;
                    }
                    
                    Examen examen = examenService.getPendingExamen(cin);
                    paiementService.enregistrerPaiement(new PaiementExamen(
                            StatutPaiement.COMPLETE,
                            null,
                            candidatService.getCandidatByCin(cin),
                            montant, 
                            date, 
                            ModePaiement.valueOf(mode), 
                            examen, 
                            "Payement d'examen du "+examen.getType()
                    ));
                    
                    showSuccessDialog("Paiement d'examen enregistré avec succès");
                } else {
                    // For Inscription or Tranche
                    if(!inscriptionService.haveActifInscription(cin)){
                        showErrorDialog("Le candidat n'a pas d'inscription active");
                        return;
                    }
                    
                    Inscription inscription = inscriptionService.getActifInscirptionBycin(cin).get(0);
                    double reste = paiementService.calculerMontantRestant(inscription.getId());
                    
                    String paymentType;
                    if (type.equals("Inscription")) {
                        paymentType = "Totale";
                    } else if (type.equals("Tranche")) {
                        paymentType = inscription.getPaymentCycle();
                    } else {
                        paymentType = type;
                    }
                    
                    paiementService.enregistrerPaiement(new PaiementInscription(
                            StatutPaiement.COMPLETE,
                            null,
                            candidatService.getCandidatByCin(cin),
                            montant, 
                            date, 
                            ModePaiement.valueOf(mode), 
                            inscription, 
                            paymentType, 
                            description
                    ));
                    
                    if(montant >= reste){
                        inscriptionService.updatePaymentStatus(inscription.getId(), true);
                        showSuccessDialog("Paiement enregistré avec succès. L'inscription est maintenant payée en totalité.");
                    } else {
                        showSuccessDialog("Paiement enregistré avec succès. Il reste " + (reste - montant) + " DT à payer.");
                    }
                }
                
                loadMockData();
                updateTotalLabel();
                clearPaymentForm();
                
            } catch(Exception e) {
                e.printStackTrace();
                showErrorDialog("Erreur lors de l'enregistrement du paiement: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorDialog("Montant invalide");
        }
    }

    @FXML
    private void handleSearch() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String candidat = searchCandidatComboBox.getValue();
        if (startDate == null && (endDate == null || candidat == null)) {
            paiementsTable.getItems().clear();
            paiementService.getAllPaiements()
                    .forEach(p -> paiementsTable.getItems().add(new PaiementData(
                            p.getDatePaiement(),
                            p.getCandidat().getNom()+" "+p.getCandidat().getPrenom(),
                            p instanceof PaiementInscription ? ((p.getTypePaiement() == null) ? "Inscription" : p.getTypePaiement()): "Examen",
                            p.getMontant(),
                            p.getModePaiement().toString(),
                            p.getDescription()
                    )));
            return;
        }
        if (startDate.isAfter(endDate)) {
            showErrorDialog("La date de début ne peut pas être après la date de fin");
            return;
        }
        System.out.println(searchCandidatComboBox.getValue());
        if(searchCandidatComboBox.getValue() == null || searchCandidatComboBox.getValue().isEmpty()){
            paiementsTable.getItems().clear();
            paiementService.getAllPaiements().stream().filter(p -> !p.getDatePaiement().isBefore(startDate) && !p.getDatePaiement().isAfter(endDate))
                    .forEach(p -> paiementsTable.getItems().add(new PaiementData(
                            p.getDatePaiement(),
                            p.getCandidat().getNom()+" "+p.getCandidat().getPrenom(),
                            p instanceof PaiementInscription ? ((p.getTypePaiement() == null) ? "Inscription" : p.getTypePaiement()): "Examen",
                            p.getMontant(),
                            p.getModePaiement().toString(),
                            p.getDescription()
                    )));
        }
        else {
            paiementsTable.getItems().clear();
            String cin = candidat.substring(1, candidat.indexOf(")"));
            paiementService.getAllPaiements().stream().filter(p -> p.getCandidat().getCin().equals(cin) && !p.getDatePaiement().isBefore(startDate) && !p.getDatePaiement().isAfter(endDate))
                    .forEach(p -> paiementsTable.getItems().add(new PaiementData(
                            p.getDatePaiement(),
                            p.getCandidat().getNom()+" "+p.getCandidat().getPrenom(),
                            p instanceof PaiementInscription ? ((p.getTypePaiement() == null) ? "Inscription" : p.getTypePaiement()): "Examen",
                            p.getMontant(),
                            p.getModePaiement().toString(),
                            p.getDescription()
                    )));
        }
    }

    private boolean validatePaymentForm() {
        if (typeComboBox.getValue() == null) {
            showErrorDialog("Veuillez sélectionner le type de paiement");
            return false;
        }
        if (candidatComboBox.getValue() == null) {
            showErrorDialog("Veuillez sélectionner un candidat");
            return false;
        }
        if (montantField.getText().isEmpty()) {
            showErrorDialog("Veuillez saisir le montant");
            return false;
        }
        if (datePicker.getValue() == null) {
            showErrorDialog("Veuillez sélectionner une date");
            return false;
        }
        if (modeComboBox.getValue() == null) {
            showErrorDialog("Veuillez sélectionner le mode de paiement");
            return false;
        }
        return true;
    }

    private void clearPaymentForm() {
        typeComboBox.setValue(null);
        candidatComboBox.setValue(null);
        montantField.clear();
        datePicker.setValue(LocalDate.now());
        modeComboBox.setValue(null);
        descriptionArea.clear();
    }

    private void updateTotalLabel() {
        double total = paiementsTable.getItems().stream()
                .mapToDouble(PaiementData::getMontant)
                .sum();
        totalLabel.setText(String.format("Total: %.2f DT", total));
    }

    // Dialog methods
    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Handle printing a receipt for the selected payment
     */
    @FXML
    private void handlePrintReceipt() {
        // Get selected payment from table
        PaiementData selectedPayment = paiementsTable.getSelectionModel().getSelectedItem();
        
        if (selectedPayment == null) {
            showErrorDialog("Veuillez sélectionner un paiement à imprimer");
            return;
        }
        
        try {
            // Convert PaiementData to Map for the receipt generator
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("date", selectedPayment.getDate());
            paymentData.put("candidat", selectedPayment.getCandidat());
            paymentData.put("cin", selectedPayment.getCin());
            paymentData.put("type", selectedPayment.getType());
            paymentData.put("montant", selectedPayment.getMontant());
            paymentData.put("methode", selectedPayment.getMethode());
            paymentData.put("description", selectedPayment.getDescription());
            
            // Generate receipt
            String pdfPath = PaymentReceiptGenerator.generateSinglePaymentReceipt(paymentData);
            
            if (pdfPath != null) {
                // Show success alert with option to open file
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText("Reçu généré");
                alert.setContentText("Le reçu a été enregistré sous:\n" + pdfPath);
                
                // Add buttons to open file or directory
                ButtonType openFileButton = new ButtonType("Ouvrir le fichier");
                ButtonType openDirButton = new ButtonType("Ouvrir le dossier");
                ButtonType closeButton = ButtonType.CLOSE;
                
                alert.getButtonTypes().setAll(openFileButton, openDirButton, closeButton);
                
                // Handle user choice
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent()) {
                    if (result.get() == openFileButton) {
                        // Open the PDF file
                        File pdfFile = new File(pdfPath);
                        if (pdfFile.exists()) {
                            Desktop.getDesktop().open(pdfFile);
                        }
                    } else if (result.get() == openDirButton) {
                        // Open the directory containing the PDF
                        File pdfDirectory = new File(pdfPath).getParentFile();
                        if (pdfDirectory.exists()) {
                            Desktop.getDesktop().open(pdfDirectory);
                        }
                    }
                }
            } else {
                showErrorDialog("Une erreur est survenue lors de la génération du reçu");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Une erreur est survenue: " + e.getMessage());
        }
    }
    
    /**
     * Handle exporting a summary of payments to PDF
     */
    @FXML
    private void handleExportPdf() {
        // Check if there are payments to export
        if (paiementsTable.getItems().isEmpty()) {
            showErrorDialog("Aucun paiement à exporter");
            return;
        }
        
        try {
            // Get current search parameters
            LocalDate startDate = startDatePicker.getValue() != null ? startDatePicker.getValue() : LocalDate.now().minusMonths(1);
            LocalDate endDate = endDatePicker.getValue() != null ? endDatePicker.getValue() : LocalDate.now();
            String candidat = searchCandidatComboBox.getValue();
            
            // Convert table items to list of maps for the report generator
            List<Map<String, Object>> paymentsData = new ArrayList<>();
            
            for (PaiementData payment : paiementsTable.getItems()) {
                Map<String, Object> paymentMap = new HashMap<>();
                paymentMap.put("date", payment.getDate());
                paymentMap.put("candidat", payment.getCandidat());
                paymentMap.put("cin", payment.getCin());
                paymentMap.put("type", payment.getType());
                paymentMap.put("montant", payment.getMontant());
                paymentMap.put("methode", payment.getMethode());
                paymentMap.put("description", payment.getDescription());
                
                paymentsData.add(paymentMap);
            }
            
            // Generate summary report
            String pdfPath = PaymentReceiptGenerator.generatePaymentSummaryReport(paymentsData, startDate, endDate, candidat);
            
            if (pdfPath != null) {
                // Show success alert with option to open file
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText("Rapport généré");
                alert.setContentText("Le rapport a été enregistré sous:\n" + pdfPath);
                
                // Add buttons to open file or directory
                ButtonType openFileButton = new ButtonType("Ouvrir le fichier");
                ButtonType openDirButton = new ButtonType("Ouvrir le dossier");
                ButtonType closeButton = ButtonType.CLOSE;
                
                alert.getButtonTypes().setAll(openFileButton, openDirButton, closeButton);
                
                // Handle user choice
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent()) {
                    if (result.get() == openFileButton) {
                        // Open the PDF file
                        File pdfFile = new File(pdfPath);
                        if (pdfFile.exists()) {
                            Desktop.getDesktop().open(pdfFile);
                        }
                    } else if (result.get() == openDirButton) {
                        // Open the directory containing the PDF
                        File pdfDirectory = new File(pdfPath).getParentFile();
                        if (pdfDirectory.exists()) {
                            Desktop.getDesktop().open(pdfDirectory);
                        }
                    }
                }
            } else {
                showErrorDialog("Une erreur est survenue lors de la génération du rapport");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Une erreur est survenue: " + e.getMessage());
        }
    }
}