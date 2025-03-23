package org.cpi2.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.cpi2.service.CandidatService;
import org.cpi2.entitties.Candidat;
import org.cpi2.entitties.TypePermis;
import org.cpi2.utils.ValidationUtils;

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
        // Clear existing items and initialize type permis options
        typeComboBox.getItems().clear();
        for (TypePermis type : TypePermis.values()) {
            typeComboBox.getItems().add(type.getLibelle());
        }
        // Set default selection
        if (!typeComboBox.getItems().isEmpty()) {
            typeComboBox.getSelectionModel().selectFirst();
        }
        
        // Set placeholders for input fields
        nomField.setPromptText("Entrez le nom");
        prenomField.setPromptText("Entrez le prénom");
        cinField.setPromptText("Entrez le CIN (8 chiffres minimum)");
        addressField.setPromptText("Entrez l'adresse complète");
        phoneField.setPromptText("Entrez le numéro (8 chiffres)");
        emailField.setPromptText("exemple@domaine.com");
        
        setupValidation();
    }
    
    private void setupValidation() {
        // Nom validation
        ValidationUtils.addValidation(nomField, 
            text -> !text.trim().isEmpty(), 
            "Le nom est obligatoire", 1);
        ValidationUtils.addValidation(nomField, 
            this::isValidName, 
            "Le nom ne doit contenir que des lettres et des espaces", 2);
            
        // Prénom validation
        ValidationUtils.addValidation(prenomField, 
            text -> !text.trim().isEmpty(), 
            "Le prénom est obligatoire", 1);
        ValidationUtils.addValidation(prenomField, 
            this::isValidName, 
            "Le prénom ne doit contenir que des lettres et des espaces", 2);
            
        // CIN validation
        ValidationUtils.addValidation(cinField, 
            text -> !text.trim().isEmpty(), 
            "Le CIN est obligatoire", 1);
        ValidationUtils.addValidation(cinField, 
            this::isValidCIN, 
            "Le CIN doit contenir au moins 8 chiffres et ne doit contenir que des chiffres", 2);
            
        // Type permis validation
        ValidationUtils.<String>addValidation(typeComboBox, 
            value -> value != null, 
            "Veuillez sélectionner un type de permis", 1);
            
        // Address validation
        ValidationUtils.addValidation(addressField, 
            text -> !text.trim().isEmpty(), 
            "L'adresse est obligatoire", 1);
        ValidationUtils.addValidation(addressField, 
            text -> isValidAddress(text, 10), 
            "L'adresse doit contenir au moins 10 caractères", 2);
            
        // Phone validation
        ValidationUtils.addValidation(phoneField, 
            text -> !text.trim().isEmpty(), 
            "Le numéro de téléphone est obligatoire", 1);
        ValidationUtils.addValidation(phoneField, 
            this::isValidPhone, 
            "Le numéro de téléphone doit contenir exactement 8 chiffres", 2);
            
        // Email validation (only if not empty)
        ValidationUtils.addValidation(emailField, 
            text -> text.trim().isEmpty() || isValidEmail(text), 
            "L'adresse email n'est pas valide", 1);
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
                typeComboBox.setValue(candidat.getTypePermis().getLibelle());
            }
        }
    }

    @FXML
    private void confirmAction() {
        if (candidatToModify == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Aucun candidat sélectionné pour la modification.");
            alert.showAndWait();
            return;
        }

        // Validate all fields first
        if (ValidationUtils.hasAnyErrors()) {
            // If there are validation errors, don't proceed
            return;
        }

        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String cin = cinField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String typePermis = typeComboBox.getValue();

        // Check required fields (should be caught by validation, but just in case)
        if (nom.isEmpty() || prenom.isEmpty() || cin.isEmpty() || address.isEmpty() || phone.isEmpty() || typePermis == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Tous les champs sont obligatoires.");
            alert.showAndWait();
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
                // Find the TypePermis enum by its libelle
                for (TypePermis type : TypePermis.values()) {
                    if (type.getLibelle().equals(typePermis)) {
                        candidatToModify.setTypePermis(type);
                        break;
                    }
                }
            }

            if (candidatService.updateCandidat(candidatToModify)) {
                showSuccessMessage();
                closeWindow();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("La mise à jour du candidat a échoué.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors de la mise à jour: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void cancelAction() {
        closeWindow();
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
