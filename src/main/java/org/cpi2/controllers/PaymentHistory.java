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
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.cpi2.entities.Candidat;
import org.cpi2.entities.Paiement;
import org.cpi2.entities.PaiementInscription;
import org.cpi2.service.CandidatService;
import org.cpi2.service.InscriptionService;
import org.cpi2.service.PaiementService;
import org.cpi2.utils.AlertUtil;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

    private final CandidatService candidatService = new CandidatService();
    private final PaiementService paiementService = new PaiementService();
    private final InscriptionService inscriptionService = new InscriptionService();

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
                "Examen"
        );
        typeComboBox.setItems(types);
        typeComboBox.getSelectionModel().select(0);
    }

    private void loadCandidates() {
        // This would be replaced with actual database call
        ObservableList<Candidat> candidats = FXCollections.observableArrayList();

        candidats.addAll(candidatService.getAllCandidats());
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
                    if (statut.equals("complete")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (statut.equals("en_attente")) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else if (statut.equals("annulee")) {
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
        List<PaymentEntry> payments = new ArrayList<>();
        for(Paiement p : paiementService.getAllPaiements()) {
            String type = p.getTypePaiement();
            if (type.equals("Totale")) {
                type = "Inscription Totale";
            } else if (type.equals("Examen")) {
                type = "Examen";
            } else {
                type = "Tranches d'Inscription";
            }
            payments.add(new PaymentEntry(
                    p.getId(),
                    p.getDatePaiement(),
                    p.getCandidat().getNom() + " " + p.getCandidat().getPrenom(),
                    p.getCandidat().getCin(),
                    p.getMontant(),
                    type,
                    p.getModePaiement().name(),
                    p.getStatut().name().toLowerCase()));
        }
        
        // Sample data
        paymentsList.addAll(payments);
        
        paymentsTable.setItems(paymentsList);
        
        // Update summary
        updateSummary();
    }

    private void updateSummary() {
        int total = paymentsList.size();
        double totalAmount = paymentsList.stream()
                .filter(p -> !p.getStatut().equals("annulee"))
                .mapToDouble(PaymentEntry::getMontant)
                .sum();
        long completed = paymentsList.stream()
                .filter(p -> p.getStatut().equals("complete"))
                .count();
        long pending = inscriptionService.getAllInscriptions().stream()
                .filter(i -> i.getnextPaymentDate()!=null && i.getnextPaymentDate().after(Date.valueOf(LocalDate.now())))
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
                .filter(p -> (p.getType().equals("Inscription Totale") || p.getType().equals("Tranches d'Inscription") )&& !p.getStatut().equals("annulee"))
                .count();
        long examens = paymentsList.stream()
                .filter(p -> p.getType().equals("Examen") && !p.getStatut().equals("annulee"))
                .count();
        
        // Add data to series
        series.getData().add(new XYChart.Data<>("Inscription", inscriptions));
        series.getData().add(new XYChart.Data<>("Examen", examens));
        
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
        //TODO : Complete this shit
        Candidat selectedCandidat = candidatComboBox.getSelectionModel().getSelectedItem();
        String selectedType = typeComboBox.getSelectionModel().getSelectedItem();
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();

        if (dateDebut.isAfter(dateFin)) {
            AlertUtil.showError("Erreur de date", "La date de début ne peut pas être après la date de fin.");
            return;
        }

        paymentsList.clear();
        List<PaymentEntry> filteredPayments = new ArrayList<>();
        for (PaymentEntry payment : paymentsList) {
            boolean matches = selectedCandidat == null || selectedCandidat.getCin().equals(payment.getCin());

            if (!selectedType.equals("Tous les types") && !selectedType.equals(payment.getType())) {
                matches = false;
            }

            if (dateDebut != null && payment.getD().isBefore(dateDebut)) {
                matches = false;
            }

            if (dateFin != null && payment.getD().isAfter(dateFin)) {
                matches = false;
            }

            if (matches) {
                filteredPayments.add(payment);
            }
        }
        AlertUtil.showInfo("Filtres Appliqués", "Les filtres ont été appliqués avec succès.");

        paymentsList= FXCollections.observableArrayList(filteredPayments);
        paymentsTable.setItems(paymentsList);
        updateSummary();
        updateChartData();
    }

    // Payment Entry class to hold table data
    public static class PaymentEntry {
        private final long id;
        private final LocalDate date;
        private final String candidat;
        private final double montant;
        private final String type;
        private final String methode;
        private final String statut;
        private final String cin;
        
        public PaymentEntry(long  id, LocalDate date, String candidat,String cin, double montant,
                          String type, String methode, String statut) {
            this.id = id;
            this.date = date;
            this.candidat = candidat;
            this.montant = montant;
            this.type = type;
            this.methode = methode;
            this.statut = statut;
            this.cin = cin;
        }
        public LocalDate getD(){
            return date;
        }
        public String getCin() {
            return cin;
        }
        public long getId() { return id; }
        public String getDate() { return date.toString(); }
        public String getCandidat() { return candidat; }
        public double getMontant() { return montant; }
        public String getType() { return type; }
        public String getMethode() { return methode; }
        public String getStatut() { return statut; }
    }
} 