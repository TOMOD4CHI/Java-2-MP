package org.cpi2.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.cpi2.entitties.Candidat;
import org.cpi2.utils.AlertUtil;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize date pickers with default values
        dateDebutPicker.setValue(LocalDate.now().minusMonths(1));
        dateFinPicker.setValue(LocalDate.now());

        // Load types de facture
        ObservableList<String> typesFacture = FXCollections.observableArrayList(
                "Facture complète",
                "Facture de séances de conduite",
                "Facture de séances de code",
                "Facture d'inscription");
        typeFactureComboBox.setItems(typesFacture);
        
        // Load candidate data
        loadCandidates();
        
        // Add listeners
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
        // This would be replaced with actual database call
        ObservableList<Candidat> candidats = FXCollections.observableArrayList();
        
        // Sample data - in a real app, fetch from database
        Candidat c1 = new Candidat();
        c1.setId(1L);
        c1.setNom("Ben Salem");
        c1.setPrenom("Ahmed");
        c1.setCin("12345678");
        c1.setTelephone("12345678");
        candidats.add(c1);
        
        Candidat c2 = new Candidat();
        c2.setId(2L);
        c2.setNom("Mejri");
        c2.setPrenom("Sarra");
        c2.setCin("87654321");
        c2.setTelephone("87654321");
        candidats.add(c2);
        
        Candidat c3 = new Candidat();
        c3.setId(3L);
        c3.setNom("Trabelsi");
        c3.setPrenom("Mohamed");
        c3.setCin("13579246");
        c3.setTelephone("13579246");
        candidats.add(c3);
        
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
        // This would be replaced with actual calculation based on database queries
        if (candidatComboBox.getValue() != null && typeFactureComboBox.getValue() != null &&
                dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null) {
            
            // Dummy calculation for demonstration
            double montant = Math.random() * 1000 + 100;
            montantField.setText(String.format("%.2f DT", montant));
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
        
        // This would load payments from database
        updateMontantTotal();
        AlertUtil.showInfo("Paiements Chargés", "Les paiements du candidat ont été chargés avec succès.");
    }

    @FXML
    void handlePreview(ActionEvent event) {
        if (!validateInputs()) return;
        
        // This would show a preview of the invoice
        AlertUtil.showInfo("Prévisualisation", "Fonctionnalité de prévisualisation à implémenter.");
    }

    @FXML
    void handleGeneratePDF(ActionEvent event) {
        if (!validateInputs()) return;
        
        // This would generate a PDF of the invoice
        AlertUtil.showInfo("PDF Généré", "La facture a été générée avec succès et enregistrée dans le dossier 'Documents'.");
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