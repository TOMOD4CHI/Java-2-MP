package org.cpi2.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.collections.FXCollections;
import org.cpi2.entities.*;
import org.cpi2.service.InscriptionService;
import org.cpi2.service.PaiementService;
import org.cpi2.service.CandidatService;
import org.cpi2.service.ExamenService;
import org.cpi2.utils.AlertUtil;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.ProgressBar;

public class Paiement implements Initializable {
    @FXML private TabPane paymentTabPane;
    
    // Inscription payment controls
    @FXML private ComboBox<Candidat> inscriptionCandidatCombo;
    @FXML private TextField inscriptionMontantField;
    @FXML private ComboBox<String> inscriptionTypeCombo;
    @FXML private ComboBox<ModePaiement> inscriptionModeCombo;
    @FXML private Text inscriptionStatusText;
    @FXML private ProgressBar inscriptionProgressBar;
    @FXML private Text inscriptionTotalText;
    @FXML private Text inscriptionPaidText;
    @FXML private Text inscriptionRemainingText;
    
    // Exam payment controls
    @FXML private ComboBox<Examen> examenCombo;
    @FXML private TextField examenMontantField;
    @FXML private ComboBox<ModePaiement> examenModeCombo;
    
    private final PaiementService paiementService;
    private final CandidatService candidatService;
    private final ExamenService examenService;
    private final InscriptionService inscriptionService;
    
    public Paiement() {
        this.paiementService = new PaiementService();
        this.candidatService = new CandidatService();
        this.examenService = new ExamenService();
        this.inscriptionService = new InscriptionService();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupInscriptionForm();
        setupExamenForm();
        
        // Add listeners for real-time updates
        inscriptionCandidatCombo.setOnAction(e -> updateInscriptionPaymentDetails());
        inscriptionMontantField.textProperty().addListener((obs, old, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                inscriptionMontantField.setText(old);
            }
        });
    }
    
    private void setupInscriptionForm() {
        // Initialize inscription payment type options
        inscriptionTypeCombo.setItems(FXCollections.observableArrayList(
            "Journalier", "Hebdomadaire", "Mensuel", "Personnalisé"
        ));
        
        // Initialize payment modes
        inscriptionModeCombo.setItems(FXCollections.observableArrayList(ModePaiement.values()));
        
        // Load candidates
        inscriptionCandidatCombo.setItems(FXCollections.observableArrayList(
            candidatService.getAllCandidats()
        ));
        
        // Set display format for candidate combo box
        inscriptionCandidatCombo.setCellFactory(lv -> new ListCell<Candidat>() {
            @Override
            protected void updateItem(Candidat item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getNom() + " " + item.getPrenom());
            }
        });
        inscriptionCandidatCombo.setButtonCell(inscriptionCandidatCombo.getCellFactory().call(null));
    }
    
    private void setupExamenForm() {
        /*
        // Initialize payment modes
        examenModeCombo.setItems(FXCollections.observableArrayList(ModePaiement.values()));
        
        // Load exams
        examenCombo.setItems(FXCollections.observableArrayList(
            examenService.getPendingExams()
        ));
        
        // Set display format for exam combo box
        examenCombo.setCellFactory(lv -> new ListCell<Examen>() {
            @Override
            protected void updateItem(Examen item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : "Examen " + item.getTypeExamen() + " - " + 
                    item.getCandidat().getNom() + " " + item.getCandidat().getPrenom());
            }


        });
        examenCombo.setButtonCell(examenCombo.getCellFactory().call(null));
        
        // Add listener for amount field validation
        examenMontantField.textProperty().addListener((obs, old, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                examenMontantField.setText(old);
            }
        });
        
         */
    }
    
    private void updateInscriptionPaymentDetails() {
        /*
        Candidat selectedCandidat = inscriptionCandidatCombo.getValue();
        if (selectedCandidat != null) {
            Inscription inscription = selectedCandidat.getInscription();
            if (inscription != null) {
                double totalAmount = inscription.getMontantTotal();
                double paidAmount = paiementService.getPaymentsByCandidat(selectedCandidat.getId())
                    .stream()
                    .filter(p -> p instanceof PaiementInscription)
                    .mapToDouble(Paiement::getMontant)
                    .sum();
                double remainingAmount = totalAmount - paidAmount;
                
                inscriptionTotalText.setText(String.format("%.2f DT", totalAmount));
                inscriptionPaidText.setText(String.format("%.2f DT", paidAmount));
                inscriptionRemainingText.setText(String.format("%.2f DT", remainingAmount));
                
                // Update progress bar and status
                double progress = paidAmount / totalAmount;
                inscriptionProgressBar.setProgress(progress);
                StatutPaiement status = paiementService.getPaymentStatus(totalAmount, paidAmount);
                inscriptionStatusText.setText(status.name());
                inscriptionStatusText.setStyle(status == StatutPaiement.PAID ? 
                    "-fx-fill: green;" : "-fx-fill: orange;");
            }
        }

         */
    }

    private static double getMontant(org.cpi2.entities.Paiement paiement) {
        return paiement.getMontant();
    }



    @FXML
    private void handleInscriptionPayment() {
        /*
        if (!validateInscriptionForm()) {
            return;
        }
        
        Candidat selectedCandidat = inscriptionCandidatCombo.getValue();
        double montant = Double.parseDouble(inscriptionMontantField.getText());
        String typePaiement = inscriptionTypeCombo.getValue();
        ModePaiement modePaiement = inscriptionModeCombo.getValue();
        
        boolean success = paiementService.saveInscriptionPayment(
            selectedCandidat.getInscription(),
            montant,
            modePaiement,
            typePaiement
        );
        
        if (success) {
            AlertUtil.showSuccess("Paiement effectué avec succès");
            clearInscriptionForm();
            updateInscriptionPaymentDetails();
        } else {
            AlertUtil.showError("Erreur lors du paiement");
        }

         */
    }
    
    @FXML
    private void handleExamenPayment() {
        /*
        if (!validateExamenForm()) {
            return;
        }
        
        Examen selectedExam = examenCombo.getValue();
        double montant = Double.parseDouble(examenMontantField.getText());
        ModePaiement modePaiement = examenModeCombo.getValue();
        
        boolean success = paiementService.saveExamPayment(
            selectedExam,
            montant,
            modePaiement
        );
        
        if (success) {
            AlertUtil.showSuccess("Paiement d'examen effectué avec succès");
            clearExamenForm();
        } else {
            AlertUtil.showError("Erreur lors du paiement d'examen");
        }
    }
    
    private boolean validateInscriptionForm() {
        if (inscriptionCandidatCombo.getValue() == null) {
            AlertUtil.showError("Veuillez sélectionner un candidat");
            return false;
        }
        if (inscriptionMontantField.getText().isEmpty()) {
            AlertUtil.showError("Veuillez saisir le montant");
            return false;
        }
        if (inscriptionTypeCombo.getValue() == null) {
            AlertUtil.showError("Veuillez sélectionner le type de paiement");
            return false;
        }
        if (inscriptionModeCombo.getValue() == null) {
            AlertUtil.showError("Veuillez sélectionner le mode de paiement");
            return false;
        }
        return true;

         */
    }
    
    private boolean validateExamenForm() {
        /*
        if (examenCombo.getValue() == null) {
            AlertUtil.showError("Veuillez sélectionner un examen");
            return false;
        }
        if (examenMontantField.getText().isEmpty()) {
            AlertUtil.showError("Veuillez saisir le montant");
            return false;
        }
        if (examenModeCombo.getValue() == null) {
            AlertUtil.showError("Veuillez sélectionner le mode de paiement");
            return false;
        }
        */
        return true;
    }
    
    private void clearInscriptionForm() {
        inscriptionCandidatCombo.setValue(null);
        inscriptionMontantField.clear();
        inscriptionTypeCombo.setValue(null);
        inscriptionModeCombo.setValue(null);
    }
    
    private void clearExamenForm() {
        examenCombo.setValue(null);
        examenMontantField.clear();
        examenModeCombo.setValue(null);
    }
}