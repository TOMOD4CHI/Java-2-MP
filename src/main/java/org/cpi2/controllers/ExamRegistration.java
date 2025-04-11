package org.cpi2.controllers;

import javafx.fxml.FXML;
import org.cpi2.utils.AlertManager;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import org.cpi2.entities.Candidat;
import org.cpi2.service.CandidatService;
import org.cpi2.service.ExamenService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ExamRegistration {

    @FXML private ComboBox<String> candidatComboBox;
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
    @FXML private Label dateFooterLabel;
    private final ExamenService examenService = new ExamenService();
    private final HashMap<String,Double> examTypePrices = examenService.getType_Price();
    private final CandidatService candidatService = new CandidatService();


    @FXML
    public void initialize() {
        // Initialize the exam types
        typeExamenComboBox.getItems().addAll(examTypePrices.keySet());
        
        // Set default date to next week
        dateExamenPicker.setValue(LocalDate.now().plusWeeks(1));
        
        // Default exam fees
        typeExamenComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fraisField.setText(examTypePrices.get(newVal).toString());
            }
        });
        
        // Set current date in footer
        dateFooterLabel.setText("📅 Date: " + LocalDate.now().toString());
        
        // Charger les candidats dans la ComboBox
        loadCandidats();
    }
    
    private void loadCandidats() {
        // Charger les candidats depuis la base de données
        List<org.cpi2.entities.Candidat> candidatsList = candidatService.getAllCandidats();
        
        if (candidatsList.isEmpty()) {
            // Afficher un message d'avertissement si aucun candidat n'est trouvé
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun candidat");
            alert.setHeaderText(null);
            alert.setContentText("Aucun candidat n'est disponible dans la base de données. Veuillez ajouter des candidats avant de planifier des examens.");
            alert.show();
        } else {
            // Ajouter les candidats à la liste déroulante
            for (org.cpi2.entities.Candidat candidat : candidatsList) {
                candidatComboBox.getItems().add(candidat.getId() + " - " + candidat.getNom() + " " + candidat.getPrenom());
            }
        }
    }

    @FXML
    private void candidatSelectionneAction() {
        String selectedCandidat = candidatComboBox.getValue();
        
        if (selectedCandidat == null || selectedCandidat.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner un candidat");
            return;
        }
        
        try {
            // Extraire l'ID du candidat (format: "1 - Nom Prénom")
            Long id = Long.parseLong(selectedCandidat.split(" - ")[0]);
            Optional<Candidat> candidatOpt = candidatService.getCandidatById(id);
            
            if (candidatOpt.isPresent()) {
                Candidat candidat = candidatOpt.get();
                nomField.setText(candidat.getNom());
                prenomField.setText(candidat.getPrenom());
                cinField.setText(candidat.getCin());
                
                // Reset eligibility message
                eligibiliteLabel.setText("Eligibilité non vérifiée");
                eligibiliteLabel.getStyleClass().clear();
                eligibiliteLabel.getStyleClass().add("label");
            } else {
                showAlert(Alert.AlertType.ERROR, "Candidat non trouvé", "Aucun candidat trouvé avec cet ID");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite lors de la récupération du candidat");
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
            //Note every Candidat should have only one examen (Active) at a time
            if(examenService.hasPendingExamens(cinField.getText())){
                showErrorDialog("Le candidat a déjà un examen en cours.");
                return;
            }

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
        if (candidatComboBox.getValue() == null || 
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
        candidatComboBox.getSelectionModel().clearSelection();
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
    private void showErrorDialog(String message) {
        AlertManager.showError("Erreur", message);
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        AlertManager.showAlert(type, title, message);
    }
}
