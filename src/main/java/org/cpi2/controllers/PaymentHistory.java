package org.cpi2.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.cpi2.entitties.Candidat;
import org.cpi2.utils.AlertUtil;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PaymentHistory implements Initializable {

    @FXML
    private ComboBox<Candidat> candidatComboBox;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private DatePicker dateDebutPicker;

    @FXML
    private DatePicker dateFinPicker;

    @FXML
    private TableView<PaymentEntry> paymentsTable;

    @FXML
    private TableColumn<PaymentEntry, Integer> idColumn;

    @FXML
    private TableColumn<PaymentEntry, String> dateColumn;

    @FXML
    private TableColumn<PaymentEntry, String> candidatColumn;

    @FXML
    private TableColumn<PaymentEntry, Double> montantColumn;

    @FXML
    private TableColumn<PaymentEntry, String> typeColumn;

    @FXML
    private TableColumn<PaymentEntry, String> methodeColumn;

    @FXML
    private TableColumn<PaymentEntry, String> statutColumn;

    @FXML
    private TableColumn<PaymentEntry, Void> actionsColumn;

    @FXML
    private Label totalPaymentsLabel;

    @FXML
    private Label totalAmountLabel;

    @FXML
    private Label completedPaymentsLabel;

    @FXML
    private Label pendingPaymentsLabel;

    @FXML
    private BarChart<String, Number> typeDistributionChart;

    private ObservableList<PaymentEntry> paymentsList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup date pickers with default values
        dateDebutPicker.setValue(LocalDate.now().minusMonths(1));
        dateFinPicker.setValue(LocalDate.now());

        // Setup payment types combo box
        setupTypeComboBox();

        // Setup candidates combo box
        loadCandidates();

        // Setup table columns
        setupTableColumns();

        // Add action buttons to the table
        setupActionColumn();

        // Load data
        loadPaymentData();

        // Load chart data
        updateChartData();
    }

    private void setupTypeComboBox() {
        ObservableList<String> types = FXCollections.observableArrayList(
                "Tous les types",
                "Inscription",
                "Séance de conduite",
                "Séance de code",
                "Examen",
                "Autre"
        );
        typeComboBox.setItems(types);
        typeComboBox.getSelectionModel().select(0);
    }

    private void loadCandidates() {
        // This would be replaced with actual database call
        ObservableList<Candidat> candidats = FXCollections.observableArrayList();
        
        // Sample data - in a real app, fetch from database
        Candidat c0 = new Candidat();
        c0.setId(0L);
        c0.setNom("Tous les candidats");
        c0.setPrenom("");
        candidats.add(c0);
        
        Candidat c1 = new Candidat();
        c1.setId(1L);
        c1.setNom("Ben Salem");
        c1.setPrenom("Ahmed");
        c1.setCin("12345678");
        candidats.add(c1);
        
        Candidat c2 = new Candidat();
        c2.setId(2L);
        c2.setNom("Mejri");
        c2.setPrenom("Sarra");
        c2.setCin("87654321");
        candidats.add(c2);
        
        candidatComboBox.setItems(candidats);
        candidatComboBox.setCellFactory(lv -> new ListCell<Candidat>() {
            @Override
            protected void updateItem(Candidat item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else if (item.getId() == 0L) {
                    setText("Tous les candidats");
                } else {
                    setText(item.getNom() + " " + item.getPrenom() + " (" + item.getCin() + ")");
                }
            }
        });
        
        candidatComboBox.setButtonCell(new ListCell<Candidat>() {
            @Override
            protected void updateItem(Candidat item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else if (item.getId() == 0L) {
                    setText("Tous les candidats");
                } else {
                    setText(item.getNom() + " " + item.getPrenom() + " (" + item.getCin() + ")");
                }
            }
        });
        
        candidatComboBox.getSelectionModel().select(0);
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        candidatColumn.setCellValueFactory(new PropertyValueFactory<>("candidat"));
        montantColumn.setCellValueFactory(new PropertyValueFactory<>("montant"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        methodeColumn.setCellValueFactory(new PropertyValueFactory<>("methode"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        
        // Format money values
        montantColumn.setCellFactory(col -> new TableCell<PaymentEntry, Double>() {
            @Override
            protected void updateItem(Double montant, boolean empty) {
                super.updateItem(montant, empty);
                if (empty || montant == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", montant));
                }
            }
        });
        
        // Format status with colors
        statutColumn.setCellFactory(col -> new TableCell<PaymentEntry, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut);
                    if (statut.equals("Payé")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (statut.equals("En attente")) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else if (statut.equals("Annulé")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void setupActionColumn() {
        actionsColumn.setCellFactory(new Callback<TableColumn<PaymentEntry, Void>, TableCell<PaymentEntry, Void>>() {
            @Override
            public TableCell<PaymentEntry, Void> call(TableColumn<PaymentEntry, Void> param) {
                return new TableCell<PaymentEntry, Void>() {
                    private final Button viewBtn = new Button("Voir");
                    private final Button editBtn = new Button("Modifier");
                    {
                        viewBtn.getStyleClass().add("small-button");
                        editBtn.getStyleClass().add("small-button");
                        
                        viewBtn.setOnAction(event -> {
                            PaymentEntry payment = getTableView().getItems().get(getIndex());
                            // View payment details
                            AlertUtil.showInfo("Détails du Paiement", "Détails du paiement #" + payment.getId());
                        });
                        
                        editBtn.setOnAction(event -> {
                            PaymentEntry payment = getTableView().getItems().get(getIndex());
                            // Edit payment
                            AlertUtil.showInfo("Modifier Paiement", "Modifier le paiement #" + payment.getId());
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox buttons = new HBox(5, viewBtn, editBtn);
                            setGraphic(buttons);
                        }
                    }
                };
            }
        });
    }

    private void loadPaymentData() {
        // This would be replaced with actual database call
        paymentsList.clear();
        
        // Sample data
        paymentsList.addAll(
            new PaymentEntry(1, "15/03/2023", "Ben Salem Ahmed", 250.00, "Inscription", "Espèces", "Payé"),
            new PaymentEntry(2, "22/03/2023", "Mejri Sarra", 120.00, "Séance de conduite", "Chèque", "Payé"),
            new PaymentEntry(3, "25/03/2023", "Ben Salem Ahmed", 80.00, "Séance de code", "Virement", "Payé"),
            new PaymentEntry(4, "01/04/2023", "Trabelsi Mohamed", 250.00, "Inscription", "Carte", "Payé"),
            new PaymentEntry(5, "05/04/2023", "Mejri Sarra", 150.00, "Examen", "Espèces", "En attente"),
            new PaymentEntry(6, "12/04/2023", "Ben Salem Ahmed", 120.00, "Séance de conduite", "Virement", "Payé"),
            new PaymentEntry(7, "15/04/2023", "Trabelsi Mohamed", 80.00, "Séance de code", "Espèces", "Payé"),
            new PaymentEntry(8, "20/04/2023", "Mejri Sarra", 120.00, "Séance de conduite", "Chèque", "Annulé"),
            new PaymentEntry(9, "25/04/2023", "Ben Salem Ahmed", 150.00, "Examen", "Carte", "En attente"),
            new PaymentEntry(10, "01/05/2023", "Trabelsi Mohamed", 120.00, "Séance de conduite", "Espèces", "Payé")
        );
        
        paymentsTable.setItems(paymentsList);
        
        // Update summary
        updateSummary();
    }

    private void updateSummary() {
        int total = paymentsList.size();
        double totalAmount = paymentsList.stream()
                .filter(p -> !p.getStatut().equals("Annulé"))
                .mapToDouble(PaymentEntry::getMontant)
                .sum();
        long completed = paymentsList.stream()
                .filter(p -> p.getStatut().equals("Payé"))
                .count();
        long pending = paymentsList.stream()
                .filter(p -> p.getStatut().equals("En attente"))
                .count();
        
        totalPaymentsLabel.setText(String.valueOf(total));
        totalAmountLabel.setText(String.format("%.2f DT", totalAmount));
        completedPaymentsLabel.setText(String.valueOf(completed));
        pendingPaymentsLabel.setText(String.valueOf(pending));
    }

    private void updateChartData() {
        // Clear existing data
        typeDistributionChart.getData().clear();
        
        // Create data series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        // Count payments by type (excluding cancelled)
        long inscriptions = paymentsList.stream()
                .filter(p -> p.getType().equals("Inscription") && !p.getStatut().equals("Annulé"))
                .count();
        long seancesConduite = paymentsList.stream()
                .filter(p -> p.getType().equals("Séance de conduite") && !p.getStatut().equals("Annulé"))
                .count();
        long seancesCode = paymentsList.stream()
                .filter(p -> p.getType().equals("Séance de code") && !p.getStatut().equals("Annulé"))
                .count();
        long examens = paymentsList.stream()
                .filter(p -> p.getType().equals("Examen") && !p.getStatut().equals("Annulé"))
                .count();
        long autres = paymentsList.stream()
                .filter(p -> p.getType().equals("Autre") && !p.getStatut().equals("Annulé"))
                .count();
        
        // Add data to series
        series.getData().add(new XYChart.Data<>("Inscription", inscriptions));
        series.getData().add(new XYChart.Data<>("Conduite", seancesConduite));
        series.getData().add(new XYChart.Data<>("Code", seancesCode));
        series.getData().add(new XYChart.Data<>("Examen", examens));
        series.getData().add(new XYChart.Data<>("Autre", autres));
        
        // Add series to chart
        typeDistributionChart.getData().add(series);
    }

    @FXML
    void handleCreateInvoice(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/Invoice.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Générer une Facture");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir la fenêtre de facturation: " + e.getMessage());
        }
    }

    @FXML
    void handleExportPDF(ActionEvent event) {
        // This would generate a PDF of the payment history
        AlertUtil.showInfo("PDF Généré", "L'historique des paiements a été exporté avec succès en PDF.");
    }

    @FXML
    void handleFilter(ActionEvent event) {
        // Apply filters and reload data
        // In a real app, this would query the database with the selected filters
        AlertUtil.showInfo("Filtres Appliqués", "Les filtres ont été appliqués avec succès.");
        
        // Simulate reloading data
        loadPaymentData();
        updateChartData();
    }

    // Payment Entry class to hold table data
    public static class PaymentEntry {
        private final int id;
        private final String date;
        private final String candidat;
        private final double montant;
        private final String type;
        private final String methode;
        private final String statut;
        
        public PaymentEntry(int id, String date, String candidat, double montant, 
                          String type, String methode, String statut) {
            this.id = id;
            this.date = date;
            this.candidat = candidat;
            this.montant = montant;
            this.type = type;
            this.methode = methode;
            this.statut = statut;
        }
        
        public int getId() { return id; }
        public String getDate() { return date; }
        public String getCandidat() { return candidat; }
        public double getMontant() { return montant; }
        public String getType() { return type; }
        public String getMethode() { return methode; }
        public String getStatut() { return statut; }
    }
} 