package org.cpi2.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;

import java.time.LocalDate;

public class ExamRegistration {

    @FXML private TextField candidatIdField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField cinField;
    @FXML private ComboBox<String> typeExamenComboBox;
    @FXML private DatePicker dateExamenPicker;
    @FXML private TextField fraisField;
    @FXML private CheckBox fraisPaiementCheckBox;
    @FXML private Label eligibiliteLabel;
    @FXML private Button verifierEligibiliteButton;
    @FXML private Button enregistrerButton;
    @FXML private Button annulerButton;

    @FXML
    public void initialize() {
        // Initialize the exam types
        typeExamenComboBox.getItems().addAll("CODE", "CONDUITE");
        
        // Set default date to next week
        dateExamenPicker.setValue(LocalDate.now().plusWeeks(1));
        
        // Default exam fees
        typeExamenComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (newVal.equals("CODE")) {
                    fraisField.setText("80.00");
                } else if (newVal.equals("CONDUITE")) {
                    fraisField.setText("120.00");
                }
            }
        });
    }

    @FXML
    private void rechercheCandidatAction() {
        // les informations vont etre recuperer de base de donnee
        if (!candidatIdField.getText().isEmpty()) {
            // Mock data for example
            nomField.setText("Dupont");
            prenomField.setText("Jean");
            cinField.setText("AB123456");
            
            // Reset eligibility message
            eligibiliteLabel.setText("Eligibilité non vérifiée");
            eligibiliteLabel.getStyleClass().clear();
            eligibiliteLabel.getStyleClass().add("label");
        }
    }
    
    @FXML
    private void verifierEligibiliteAction() {
        // In a real app, this would check candidate eligibility based on completed sessions
        if (nomField.getText().isEmpty() || typeExamenComboBox.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un candidat et un type d'examen pour vérifier l'éligibilité.");
            alert.showAndWait();
            return;
        }
        
        // Mock check - usually this would check database for eligibility
        boolean eligible = true; // For this example, assume eligible
        
        if (eligible) {
            eligibiliteLabel.setText("ELIGIBLE");
            eligibiliteLabel.getStyleClass().clear();
            eligibiliteLabel.getStyleClass().addAll("label", "status-eligible");
        } else {
            eligibiliteLabel.setText("NON ELIGIBLE");
            eligibiliteLabel.getStyleClass().clear();
            eligibiliteLabel.getStyleClass().addAll("label", "status-non-eligible");
        }
    }
    
    @FXML
    private void enregistrerAction() {
        if (validateForm()) {
            //l enregistrement dans la  base de donnee
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Inscription à l'examen enregistrée avec succès!");
            alert.showAndWait();
            
            clearForm();
        }
    }
    
    @FXML
    private void annulerAction() {
        clearForm();
    }
    
    private boolean validateForm() {
        if (candidatIdField.getText().isEmpty() || 
            nomField.getText().isEmpty() || 
            prenomField.getText().isEmpty() || 
            cinField.getText().isEmpty() || 
            typeExamenComboBox.getValue() == null || 
            dateExamenPicker.getValue() == null || 
            fraisField.getText().isEmpty()) {
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de validation");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez remplir tous les champs obligatoires.");
            alert.showAndWait();
            return false;
        }
        
        if (!eligibiliteLabel.getText().equals("ELIGIBLE")) {
            //cherhcer l existence dans la base de donneee
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur d'éligibilité");
            alert.setHeaderText(null);
            alert.setContentText("Le candidat doit être éligible pour s'inscrire à l'examen.");
            alert.showAndWait();
            return false;
        }
        
        return true;
    }
    
    private void clearForm() {
        candidatIdField.clear();
        nomField.clear();
        prenomField.clear();
        cinField.clear();
        typeExamenComboBox.getSelectionModel().clearSelection();
        dateExamenPicker.setValue(LocalDate.now().plusWeeks(1));
        fraisField.clear();
        fraisPaiementCheckBox.setSelected(false);
        eligibiliteLabel.setText("Eligibilité non vérifiée");
        eligibiliteLabel.getStyleClass().clear();
        eligibiliteLabel.getStyleClass().add("label");
    }
}
