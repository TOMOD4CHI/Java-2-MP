package org.cpi2.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.cpi2.controllers.PaymentHistory.PaymentEntry;
import org.cpi2.entities.Inscription;
import org.cpi2.entities.Paiement;
import org.cpi2.entities.PaiementInscription;
import org.cpi2.service.PaiementService;
import org.cpi2.service.CandidatService;
import org.cpi2.service.InscriptionService;
import org.cpi2.utils.AlertUtil;
import org.cpi2.utils.PaymentReceiptGenerator;

import java.io.File;
import java.awt.Desktop;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class PaymentDetailsController implements Initializable {
    @FXML private AnchorPane mypane;
    @FXML private Label idLabel;
    @FXML private Label dateLabel;
    @FXML private Label candidatLabel;
    @FXML private Label montantLabel;
    @FXML private Label typeLabel;
    @FXML private Label methodeLabel;
    @FXML private Label statutLabel;
    @FXML private Label cinLabel;
    
    @FXML private Label totalPaidLabel;
    @FXML private Label remainingLabel;
    @FXML private Label planTypeLabel;
    @FXML private Label nextPaymentLabel;
    @FXML private ProgressBar paymentProgressBar;
    
    @FXML private TableView<PaymentHistoryEntry> paymentsHistoryTable;
    @FXML private TableColumn<PaymentHistoryEntry, String> historyDateColumn;
    @FXML private TableColumn<PaymentHistoryEntry, Double> historyMontantColumn;
    @FXML private TableColumn<PaymentHistoryEntry, String> historyTypeColumn;
    @FXML private TableColumn<PaymentHistoryEntry, String> historyMethodeColumn;
    
    @FXML private Button closeButton;
    @FXML private Button printButton;
    
    private PaymentEntry payment;
    private final PaiementService paiementService = new PaiementService();
    private final CandidatService candidatService = new CandidatService();
    private final InscriptionService inscriptionService = new InscriptionService();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            if (mypane.getScene() != null && mypane.getScene().getWindow() != null) {
                Window window = mypane.getScene().getWindow();

                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    Image icon = new Image(getClass().getResourceAsStream("/images/logo4.png"));                    stage.getIcons().add(icon);
                }
            } else {
                System.err.println("Scene or Window is null.");
            }
        });

        closeButton.setOnAction(this::handleClose);
        printButton.setOnAction(this::handlePrint);

        setupHistoryTable();
    }
    
    private void setupHistoryTable() {
        historyDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        historyMontantColumn.setCellValueFactory(new PropertyValueFactory<>("montant"));
        historyTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        historyMethodeColumn.setCellValueFactory(new PropertyValueFactory<>("methode"));

        historyMontantColumn.setCellFactory(col -> new TableCell<PaymentHistoryEntry, Double>() {
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
    }
    
    public void initData(PaymentEntry payment) {
        this.payment = payment;

        idLabel.setText(String.valueOf(payment.getId()));
        dateLabel.setText(payment.getDate());
        candidatLabel.setText(payment.getCandidat());
        montantLabel.setText(String.format("%.2f DT", payment.getMontant()));
        typeLabel.setText(payment.getType());
        methodeLabel.setText(payment.getMethode());
        statutLabel.setText(payment.getStatut());
        cinLabel.setText(payment.getCin());

        if (payment.getStatut().equalsIgnoreCase("annulee")){
            nextPaymentLabel.setText("N/A");
            remainingLabel.setText("N/A");
            totalPaidLabel.setText("N/A");
            paymentProgressBar.setVisible(false);
        }
        else {
            if (!payment.getType().equals("Examen")) {
                loadInscriptionPaymentDetails();
            } else {
                if (paymentProgressBar != null) {
                    paymentProgressBar.setVisible(false);
                }
                if (totalPaidLabel != null) {
                    totalPaidLabel.setText("N/A");
                }
                if (remainingLabel != null) {
                    remainingLabel.setText("N/A");
                }
                if (planTypeLabel != null) {
                    planTypeLabel.setText("Paiement d'examen");
                }
                if (nextPaymentLabel != null) {
                    nextPaymentLabel.setText("N/A");
                }
            }
        }
        loadPaymentHistory(payment.getCin());
    }
    
    private void loadInscriptionPaymentDetails() {
        try {

            List<Inscription> inscriptions = inscriptionService.getActifInscirptionBycin(payment.getCin());
            
            if (!inscriptions.isEmpty()) {
                Inscription inscription = inscriptions.get(0);
                double totalAmount = inscription.getPlan().getPrice();
                double remainingAmount = paiementService.calculerMontantRestant(inscription.getId());
                double paidAmount = totalAmount - remainingAmount;

                planTypeLabel.setText(inscription.getPlan().getName());
                totalPaidLabel.setText(String.format("%.2f DT / %.2f DT", paidAmount, totalAmount));
                remainingLabel.setText(String.format("%.2f DT", remainingAmount));

                if (paymentProgressBar != null) {
                    paymentProgressBar.setProgress(paidAmount / totalAmount);
                }

                if (inscription.getnextPaymentDate() != null) {
                    nextPaymentLabel.setText(inscription.getnextPaymentDate().toLocalDate().format(dateFormatter));
                } else {
                    nextPaymentLabel.setText("Payé intégralement");
                }
            } else {
                if (paymentProgressBar != null) {
                    paymentProgressBar.setVisible(false);
                }
                planTypeLabel.setText("Aucune inscription active");
                totalPaidLabel.setText("N/A");
                remainingLabel.setText("N/A");
                nextPaymentLabel.setText("N/A");
            }
        } catch (Exception e) {
            e.printStackTrace();

            if (paymentProgressBar != null) {
                paymentProgressBar.setVisible(false);
            }
        }
    }
    
    private void loadPaymentHistory(String cin) {

        ObservableList<PaymentHistoryEntry> historyData = FXCollections.observableArrayList();
        
        try {

            List<Paiement> candidatePayments = paiementService.getAllPaiements().stream().filter(p -> p.getCandidat().getCin().equals(cin)).toList();

            for (Paiement p : candidatePayments) {
                String type = p.getTypePaiement();
                if (type == null) {
                    type = p instanceof PaiementInscription ? "Inscription" : "Examen";
                }
                
                historyData.add(new PaymentHistoryEntry(
                    p.getDatePaiement().format(dateFormatter),
                    p.getMontant(),
                    type,
                    p.getModePaiement().name()
                ));
            }

            paymentsHistoryTable.setItems(historyData);
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible de charger l'historique des paiements: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handlePrint(ActionEvent event) {
        try {

            paiementService.getPaiementById(payment.getId()).ifPresent(paiement -> {
                try {

                    Map<String, Object> receiptData = new HashMap<>();
                    receiptData.put("id", payment.getId());
                    receiptData.put("date", payment.getDate());
                    receiptData.put("candidat", payment.getCandidat());
                    receiptData.put("cin", payment.getCin());
                    receiptData.put("montant", payment.getMontant());
                    receiptData.put("type", payment.getType());
                    receiptData.put("methode", payment.getMethode());

                    if (!payment.getType().equals("Examen")) {
                        List<Inscription> inscriptions = inscriptionService.getActifInscirptionBycin(payment.getCin());
                        if (!inscriptions.isEmpty()) {
                            Inscription inscription = inscriptions.get(0);
                            double totalAmount = inscription.getPlan().getPrice();
                            double remainingAmount = paiementService.calculerMontantRestant(inscription.getId());
                            double paidAmount = totalAmount - remainingAmount;
                            
                            receiptData.put("totalAmount", totalAmount);
                            receiptData.put("remainingAmount", remainingAmount);
                            receiptData.put("paidAmount", paidAmount);
                            receiptData.put("paymentProgress", paidAmount / totalAmount);
                            receiptData.put("planType", inscription.getPlan().getName());
                            receiptData.put("nextPaymentDate", inscription.getnextPaymentDate() != null ? 
                                inscription.getnextPaymentDate().toLocalDate().format(dateFormatter) : "Payé intégralement");
                        }
                    }

                    String pdfPath = PaymentReceiptGenerator.generateSinglePaymentReceipt(receiptData);
                    if (pdfPath != null) {
                        File pdfFile = new File(pdfPath);
                        if (pdfFile.exists()) {
                            Desktop.getDesktop().open(pdfFile);
                            AlertUtil.showInfo("Reçu généré", "Le reçu a été généré avec succès et ouvert.");
                        } else {
                            AlertUtil.showError("Erreur", "Le fichier PDF généré est introuvable.");
                        }
                    } else {
                        AlertUtil.showError("Erreur", "Erreur lors de la génération du reçu.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AlertUtil.showError("Erreur d'impression", "Impossible d'imprimer le reçu: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur d'impression", "Impossible d'imprimer le reçu: " + e.getMessage());
        }
    }

    public static class PaymentHistoryEntry {
        private final String date;
        private final double montant;
        private final String type;
        private final String methode;
        
        public PaymentHistoryEntry(String date, double montant, String type, String methode) {
            this.date = date;
            this.montant = montant;
            this.type = type;
            this.methode = methode;
        }
        
        public String getDate() { return date; }
        public double getMontant() { return montant; }
        public String getType() { return type; }
        public String getMethode() { return methode; }
    }
} 

