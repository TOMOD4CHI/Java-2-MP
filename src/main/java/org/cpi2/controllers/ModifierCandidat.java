package org.cpi2.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.cpi2.service.CandidatService;
import org.cpi2.entitties.Candidat;
import org.cpi2.entitties.TypePermis;

import java.util.regex.Pattern;

public class ModifierCandidat {
    private final CandidatService candidatService = new CandidatService();
    private Candidat candidatToModify;
    
    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÀ-ÿ\\s]+$");
    private static final Pattern CIN_PATTERN = Pattern.compile("^[0-9]{8,}$");

    // Validation methods
    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    private boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    private boolean isValidCIN(String cin) {
        return cin != null && CIN_PATTERN.matcher(cin).matches();
    }
    
    private boolean isValidAddress(String address, int minLength) {
        return address != null && address.length() >= minLength;
    }

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField cinField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> typeComboBox;

    public void initialize() {
        // Initialize type permis options
        for (TypePermis type : TypePermis.values()) {
            typeComboBox.getItems().add(type.name());
        }
    }

    public void setCandidatToModify(Candidat candidat) {
        this.candidatToModify = candidat;
        if (candidat != null) {
            nomField.setText(candidat.getNom());
            prenomField.setText(candidat.getPrenom());
            cinField.setText(candidat.getCin());
            addressField.setText(candidat.getAdresse());
            phoneField.setText(candidat.getTelephone());
            emailField.setText(candidat.getEmail());
            
            // Set the type permis in combo box
            if (candidat.getTypePermis() != null) {
                typeComboBox.setValue(candidat.getTypePermis().name());
            }
        }
    }

    @FXML
    private void confirmAction() {
        if (candidatToModify == null) {
            showAlert("Erreur", "Aucun candidat sélectionné pour la modification.");
            return;
        }

        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String cin = cinField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String typePermis = typeComboBox.getValue();

        if (nom.isEmpty() || prenom.isEmpty() || cin.isEmpty() || address.isEmpty() || phone.isEmpty() || typePermis == null) {
            showAlert("Erreur", "Tous les champs sont obligatoires.");
            return;
        }
        
        // Validate name format
        if (!isValidName(nom)) {
            showAlert("Validation Error", "Le nom ne doit contenir que des lettres et des espaces.");
            return;
        }
        
        // Validate prénom format
        if (!isValidName(prenom)) {
            showAlert("Validation Error", "Le prénom ne doit contenir que des lettres et des espaces.");
            return;
        }

        // Validate CIN (numeric only and at least 8 digits)
        if (!isValidCIN(cin)) {
            showAlert("Validation Error", "Le CIN doit contenir au moins 8 chiffres et ne doit contenir que des chiffres!");
            return;
        }
        
        // Validate phone number
        if (!isValidPhone(phone)) {
            showAlert("Validation Error", "Le numéro de téléphone doit contenir exactement 8 chiffres.");
            return;
        }
        
        // Validate email if provided
        if (!email.isEmpty() && !isValidEmail(email)) {
            showAlert("Validation Error", "L'adresse email n'est pas valide.");
            return;
        }
        
        // Validate address
        if (!isValidAddress(address, 10)) {
            showAlert("Validation Error", "L'adresse doit contenir au moins 10 caractères.");
            return;
        }

        try {
            // Update the candidate object with new values
            candidatToModify.setNom(nom);
            candidatToModify.setPrenom(prenom);
            candidatToModify.setCin(cin);
            candidatToModify.setAdresse(address);
            candidatToModify.setTelephone(phone);
            candidatToModify.setEmail(email);
            
            // Set type permis from combo box selection
            if (typePermis != null) {
                candidatToModify.setTypePermis(TypePermis.valueOf(typePermis));
            }

            if (candidatService.updateCandidat(candidatToModify)) {
                showSuccessMessage();
                closeWindow();
            } else {
                showAlert("Erreur", "La mise à jour du candidat a échoué.");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue lors de la mise à jour: " + e.getMessage());
        }
    }

    @FXML
    private void cancelAction() {
        closeWindow();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessMessage() {
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Succès");
        successAlert.setHeaderText(null);
        successAlert.setContentText("Les informations du candidat ont été mises à jour avec succès.");
        successAlert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}
