package org.cpi2.controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class Paiement implements Initializable {

    // Form controls
    @FXML
    private ToggleGroup pageTabs;
    @FXML private ToggleButton ajouterTab;
    @FXML private ToggleButton afficherTab;
    @FXML private VBox ajouterView;
    @FXML private VBox afficherView;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> candidatComboBox;
    @FXML private ComboBox<String> modeComboBox;

    @FXML private TextField montantField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> methodeComboBox;
    @FXML private TextArea descriptionArea;
    
    // Search controls
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> searchCandidatComboBox;
    
    // Table controls
    @FXML private TableView<PaiementEntry> paiementsTable;
    @FXML private TableColumn<PaiementEntry, String> dateColumn;
    @FXML private TableColumn<PaiementEntry, String> candidatColumn;
    @FXML private TableColumn<PaiementEntry, String> typeColumn;
    @FXML private TableColumn<PaiementEntry, Double> montantColumn;
    @FXML private TableColumn<PaiementEntry, String> methodeColumn;
    @FXML private TableColumn<PaiementEntry, String> descriptionColumn;
    @FXML private TableColumn<PaiementEntry, Void> actionsColumn;
    
    @FXML private Label totalLabel;
    
    private ObservableList<PaiementEntry> paiementData = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private void switchView(boolean isAjouter) {
        ajouterView.setVisible(isAjouter);
        ajouterView.setManaged(isAjouter);
        afficherView.setVisible(!isAjouter);
        afficherView.setManaged(!isAjouter);
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pageTabs = new ToggleGroup();

        // Assign ToggleGroup to buttons
        ajouterTab.setToggleGroup(pageTabs);
        afficherTab.setToggleGroup(pageTabs);

        // Set initial selection
        ajouterTab.setSelected(true);

        // Rest of your existing initialization code...
        ajouterTab.setOnAction(event -> switchView(true));
        afficherTab.setOnAction(event -> switchView(false));
        modeComboBox.getItems().addAll( "paiement mensuel",
                 "paiement hebdomadaire",
        "paiement quotidien");
        // Set up the ComboBox items
        typeComboBox.getItems().addAll(
            "Paiement leçon",
            "Formation de code",
            "Examen pratique",
            "Examen de code",
            "Autre"
        );

        methodeComboBox.getItems().addAll(
            "Espèces",
            "Carte Bancaire",
            "Chèque",
            "Virement"
        );
        
        // Set default date to today
        datePicker.setValue(LocalDate.now());
        
        // Set up the table columns
        setupTableColumns();
        setupActionsColumn();
        
        // Load sample data
        loadSampleData();
        
        // Set up date range picker default values
        LocalDate now = LocalDate.now();
        LocalDate oneMonthAgo = now.minusMonths(1);
        startDatePicker.setValue(oneMonthAgo);
        endDatePicker.setValue(now);
        
        // Sample candidates for the combo boxes
        ObservableList<String> candidats = FXCollections.observableArrayList(
            "Tous les candidats",
            "Ahmed Salah",
            "Nadia Mejri",
            "Mohamed Karim",
            "Fatma Ben Salem",
            "Sami Ferchichi"
        );
        
        candidatComboBox.setItems(candidats);
        searchCandidatComboBox.setItems(candidats);
        searchCandidatComboBox.setValue("Tous les candidats");
        
        // Update total
        updateTotal();
    }
    
    private void setupTableColumns() {
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDate()));
        
        candidatColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCandidat()));
        
        typeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getType()));
        
        montantColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getMontant()).asObject());
        
        // Format amount to show DT currency
        montantColumn.setCellFactory(tc -> new TableCell<PaiementEntry, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", amount));
                }
            }
        });
        
        methodeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getMethode()));
        
        descriptionColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDescription()));
        
        // Setup actions column with edit and delete buttons
        setupActionsColumn();
        
        // Set the table items
        paiementsTable.setItems(paiementData);
    }
    
    private void setupActionsColumn() {
        Callback<TableColumn<PaiementEntry, Void>, TableCell<PaiementEntry, Void>> cellFactory = 
            new Callback<>() {
                @Override
                public TableCell<PaiementEntry, Void> call(final TableColumn<PaiementEntry, Void> param) {
                    return new TableCell<>() {
                        private final Button editBtn = new Button("Éditer");
                        private final Button deleteBtn = new Button("Supprimer");
                        private final HBox pane = new HBox(5, editBtn, deleteBtn);
                        
                        {
                            editBtn.getStyleClass().addAll("button", "edit-button", "small-button");
                            deleteBtn.getStyleClass().addAll("button", "delete-button", "small-button");
                            pane.setAlignment(Pos.CENTER);
                            
                            editBtn.setOnAction(event -> {
                                PaiementEntry paiement = getTableView().getItems().get(getIndex());
                                handleEdit(paiement);
                            });
                            
                            deleteBtn.setOnAction(event -> {
                                PaiementEntry paiement = getTableView().getItems().get(getIndex());
                                handleDelete(paiement);
                            });
                        }
                        
                        @Override
                        public void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            setGraphic(empty ? null : pane);
                        }
                    };
                }
            };
        
        actionsColumn.setCellFactory(cellFactory);
    }
    
    private void loadSampleData() {
        // Add sample payment data
        paiementData.addAll(
            new PaiementEntry("25/01/2024", "Ahmed Salah", "Paiement leçon", 120.00, "Espèces", "Leçon de conduite #3"),
            new PaiementEntry("24/01/2024", "Nadia Mejri", "Paiement leçon", 120.00, "Carte Bancaire", "Leçon de conduite #2"),
            new PaiementEntry("22/01/2024", "Lina Trabelsi", "Formation de code", 250.00, "Virement", "Formation de code - 10 séances"),
            new PaiementEntry("20/01/2024", "Mohamed Karim", "Paiement leçon", 120.00, "Espèces", "Leçon de conduite #4"),
            new PaiementEntry("19/01/2024", "Fatma Ben Salem", "Examen pratique", 150.00, "Chèque", "Frais d'examen pratique"),
            new PaiementEntry("16/01/2024", "Sami Ferchichi", "Paiement leçon", 120.00, "Espèces", "Leçon de conduite #1")
        );
    }
    
    @FXML
    private void handleSave() {
        // Validate input
        if (!validateInput()) {
            return;
        }
        
        try {
            // Parse amount
            double montant = Double.parseDouble(montantField.getText().trim());
            
            // Create new payment entry
            PaiementEntry newPaiement = new PaiementEntry(
                datePicker.getValue().format(dateFormatter),
                candidatComboBox.getValue(),
                typeComboBox.getValue(),
                montant,
                methodeComboBox.getValue(),
                descriptionArea.getText()
            );
            
            // Add to data
            paiementData.add(0, newPaiement);
            
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Le paiement a été enregistré avec succès");
            
            // Clear form
            clearForm();
            
            // Update total
            updateTotal();
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le montant doit être un nombre valide");
        }
    }
    
    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (typeComboBox.getValue() == null) {
            errorMessage.append("- Veuillez sélectionner un type de paiement\n");
        }
        
        if (candidatComboBox.getValue() == null) {
            errorMessage.append("- Veuillez sélectionner un candidat\n");
        }
        
        if (montantField.getText().trim().isEmpty()) {
            errorMessage.append("- Veuillez saisir un montant\n");
        } else {
            try {
                double montant = Double.parseDouble(montantField.getText().trim());
                if (montant <= 0) {
                    errorMessage.append("- Le montant doit être supérieur à 0\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("- Le montant doit être un nombre valide\n");
            }
        }
        
        if (datePicker.getValue() == null) {
            errorMessage.append("- Veuillez sélectionner une date\n");
        }
        
        if (methodeComboBox.getValue() == null) {
            errorMessage.append("- Veuillez sélectionner une méthode de paiement\n");
        }
        
        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", errorMessage.toString());
            return false;
        }
        
        return true;
    }
    
    @FXML
    private void handleCancel() {
        clearForm();
    }
    
    private void clearForm() {
        typeComboBox.setValue(null);
        candidatComboBox.setValue(null);
        montantField.clear();
        datePicker.setValue(LocalDate.now());
        methodeComboBox.setValue(null);
        descriptionArea.clear();
    }
    
    @FXML
    private void handleSearch() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String candidat = searchCandidatComboBox.getValue();
        
        // Validate date range
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            showAlert(Alert.AlertType.ERROR, "Erreur de recherche", 
                    "La date de début doit être antérieure à la date de fin");
            return;
        }
        
        // For demo purposes, just show all data and display a message
        showAlert(Alert.AlertType.INFORMATION, "Recherche", 
                "Recherche effectuée pour la période du " 
                + (startDate != null ? startDate.format(dateFormatter) : "début") 
                + " au " 
                + (endDate != null ? endDate.format(dateFormatter) : "aujourd'hui")
                + (candidat != null ? " pour le candidat " + candidat : ""));
    }
    
    private void handleEdit(PaiementEntry paiement) {
        // In a real application, this would populate the form with the payment details
        typeComboBox.setValue(paiement.getType());
        candidatComboBox.setValue(paiement.getCandidat());
        montantField.setText(String.valueOf(paiement.getMontant()));
        datePicker.setValue(LocalDate.parse(paiement.getDate(), dateFormatter));
        methodeComboBox.setValue(paiement.getMethode());
        descriptionArea.setText(paiement.getDescription());
        
        // Remove the payment from the table (it will be re-added when saved)
        paiementData.remove(paiement);
        
        // Update total
        updateTotal();
    }
    
    private void handleDelete(PaiementEntry paiement) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                "Êtes-vous sûr de vouloir supprimer ce paiement ?",
                ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer le paiement");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // Remove the payment from the table
                paiementData.remove(paiement);
                
                // Update total
                updateTotal();
                
                showAlert(Alert.AlertType.INFORMATION, "Suppression réussie", 
                        "Le paiement a été supprimé avec succès");
            }
        });
    }
    
    @FXML
    private void handlePrint() {
        showAlert(Alert.AlertType.INFORMATION, "Impression", 
                "Impression de l'historique des paiements...");
    }
    
    @FXML
    private void handleExport() {
        showAlert(Alert.AlertType.INFORMATION, "Exportation", 
                "Exportation des données vers un fichier CSV...");
    }
    
    private void updateTotal() {
        double total = paiementData.stream()
                .mapToDouble(PaiementEntry::getMontant)
                .sum();
        
        totalLabel.setText(String.format("Total: %.2f DT", total));
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Inner class for payment data
    public static class PaiementEntry {
        private final String date;
        private final String candidat;
        private final String type;
        private final double montant;
        private final String methode;
        private final String description;
        
        public PaiementEntry(String date, String candidat, String type, 
                           double montant, String methode, String description) {
            this.date = date;
            this.candidat = candidat;
            this.type = type;
            this.montant = montant;
            this.methode = methode;
            this.description = description;
        }
        
        public String getDate() { return date; }
        public String getCandidat() { return candidat; }
        public String getType() { return type; }
        public double getMontant() { return montant; }
        public String getMethode() { return methode; }
        public String getDescription() { return description; }
    }
} 