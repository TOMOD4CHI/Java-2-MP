package org.cpi2.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.cpi2.entities.Candidat;
import org.cpi2.service.AutoEcoleService;
import org.cpi2.service.CandidatService;
import org.cpi2.service.InscriptionService;
import org.cpi2.service.PaiementService;
import org.cpi2.utils.AlertUtil;
import org.cpi2.utils.InvoiceGenerator;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class Invoice implements Initializable {

    @FXML
    private ComboBox<Candidat> candidatComboBox;

    @FXML
    private ComboBox<String> typeFactureComboBox;

    @FXML
    private DatePicker dateDebutPicker;

    @FXML
    private DatePicker dateFinPicker;

    @FXML
    private TextField montantField;

    @FXML
    private TextArea noteTextArea;

    private final InscriptionService inscriptionService = new InscriptionService();
    private final CandidatService candidatService = new CandidatService(inscriptionService);
    private final PaiementService paiementService = new PaiementService();
    private final AutoEcoleService autoEcoleService = new AutoEcoleService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        dateDebutPicker.setValue(LocalDate.now().minusMonths(1));
        dateFinPicker.setValue(LocalDate.now());

        ObservableList<String> typesFacture = FXCollections.observableArrayList(
                "Facture complète",
                "Facture d'inscription");
        typeFactureComboBox.setItems(typesFacture);

        loadCandidates();

        candidatComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateMontantTotal();
            }
        });
        
        typeFactureComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateMontantTotal();
            }
        });
        
        dateDebutPicker.valueProperty().addListener((obs, oldVal, newVal) -> updateMontantTotal());
        dateFinPicker.valueProperty().addListener((obs, oldVal, newVal) -> updateMontantTotal());
    }
    
    private void loadCandidates() {
        ObservableList<Candidat> candidats = FXCollections.observableArrayList();
        candidats.addAll(candidatService.getAllCandidats());


        candidatComboBox.setItems(candidats);
        candidatComboBox.setCellFactory(lv -> new ListCell<Candidat>() {
            @Override
            protected void updateItem(Candidat item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getNom() + " " + item.getPrenom() + " (" + item.getCin() + ")");
            }
        });
        
        candidatComboBox.setButtonCell(new ListCell<Candidat>() {
            @Override
            protected void updateItem(Candidat item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getNom() + " " + item.getPrenom() + " (" + item.getCin() + ")");
            }
        });
    }
    
    private void updateMontantTotal() {

        if (candidatComboBox.getValue() != null && typeFactureComboBox.getValue() != null &&
                dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null) {
            Candidat candidat = candidatComboBox.getValue();
            String typeFacture = typeFactureComboBox.getValue();
            if(typeFacture.equals("Facture complète")) {
                double montant = paiementService.calculateTotalPayments(candidat.getCin(), dateDebutPicker.getValue(), dateFinPicker.getValue());
                montantField.setText(String.valueOf(montant));
            } else if (typeFacture.equals("Facture d'inscription")) {
                double montant = paiementService.calculateRegistrationFees(candidat.getCin(), dateDebutPicker.getValue(), dateFinPicker.getValue());
                montantField.setText(String.valueOf(montant));
            }
        }
    }

    @FXML
    void handleLoadPayments(ActionEvent event) {
        if (candidatComboBox.getValue() == null) {
            AlertUtil.showError("Sélection Requise", "Veuillez sélectionner un candidat.");
            return;
        }
        
        if (dateDebutPicker.getValue() == null || dateFinPicker.getValue() == null) {
            AlertUtil.showError("Dates Requises", "Veuillez sélectionner les dates de début et de fin.");
            return;
        }
        
        if (dateDebutPicker.getValue().isAfter(dateFinPicker.getValue())) {
            AlertUtil.showError("Dates Invalides", "La date de début doit être antérieure à la date de fin.");
            return;
        }

        updateMontantTotal();
        AlertUtil.showInfo("Paiements Chargés", "Les paiements du candidat ont été chargés avec succès.");
    }

    @FXML
    void handlePreview(ActionEvent event) {
        if (!validateInputs()) return;
        
        Candidat candidat = candidatComboBox.getValue();
        String typeFacture = typeFactureComboBox.getValue();
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();
        double montant = Double.parseDouble(montantField.getText().replace(" DT", "").replace(",", "."));
        String note = noteTextArea.getText();
        
        InvoiceGenerator.previewInvoice(candidat, typeFacture, dateDebut, dateFin, montant, note);
    }

    @FXML
    void handleGeneratePDF(ActionEvent event) {
        if (!validateInputs()) return;
        
        Candidat candidat = candidatComboBox.getValue();
        String typeFacture = typeFactureComboBox.getValue();
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();
        double montant = Double.parseDouble(montantField.getText().replace(" DT", "").replace(",", "."));
        String note = noteTextArea.getText();
        
        InvoiceGenerator.generatePDF(candidat, typeFacture, dateDebut, dateFin, montant, note);
    }
    
    private boolean validateInputs() {
        if (candidatComboBox.getValue() == null) {
            AlertUtil.showError("Sélection Requise", "Veuillez sélectionner un candidat.");
            return false;
        }
        
        if (typeFactureComboBox.getValue() == null) {
            AlertUtil.showError("Type Requis", "Veuillez sélectionner un type de facture.");
            return false;
        }
        
        if (dateDebutPicker.getValue() == null || dateFinPicker.getValue() == null) {
            AlertUtil.showError("Dates Requises", "Veuillez sélectionner les dates de début et de fin.");
            return false;
        }
        
        if (dateDebutPicker.getValue().isAfter(dateFinPicker.getValue())) {
            AlertUtil.showError("Dates Invalides", "La date de début doit être antérieure à la date de fin.");
            return false;
        }
        
        return true;
    }
} 


