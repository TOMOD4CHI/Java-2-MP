package org.cpi2.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.cpi2.service.CandidatService;
import org.cpi2.entities.Candidat;
import org.cpi2.entities.TypePermis;
import org.cpi2.utils.AlertUtil;
import org.cpi2.utils.ValidationUtils;

import java.util.Optional;
import java.util.regex.Pattern;

public class ModifierCandidat {
    private final CandidatService candidatService = new CandidatService();
    private Candidat candidatToModify;
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÀ-ÿ\\s]+$");
    private static final Pattern CIN_PATTERN = Pattern.compile("^[0-9]{8,}$");

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
    
    private boolean isValidBirthDate(java.time.LocalDate date) {
        if (date == null) {
            return false;
        }
        
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate minDate = today.minusYears(100); // Maximum reasonable age (100 years)
        java.time.LocalDate maxDate = today.minusYears(16);  // Minimum reasonable age (16 years)
        
        // Check if date is not in the future and within reasonable age range
        return !date.isAfter(today) && !date.isBefore(minDate) && !date.isAfter(maxDate);
    }

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField cinField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private DatePicker birthDatePicker;

    //This might be useless since we can't change the type of permit through the candidat itself
    @FXML private ComboBox<TypePermis> typeComboBox;
    
    // Error labels
    @FXML private Label nomError;
    @FXML private Label prenomError;
    @FXML private Label cinError;
    @FXML private Label addressError;
    @FXML private Label phoneError;
    @FXML private Label emailError;
    @FXML private Label birthDateError;
    @FXML private Label typeError;

    public void initialize() {
        // Clear existing items and initialize type permis options
        typeComboBox.getItems().clear();
        typeComboBox.getItems().addAll(TypePermis.values());
        // Set default selection
        if (!typeComboBox.getItems().isEmpty()) {
            typeComboBox.setValue(TypePermis.B);
        }
        
        // Set placeholders for input fields
        nomField.setPromptText("Entrez le nom");
        prenomField.setPromptText("Entrez le prénom");
        cinField.setPromptText("Entrez le CIN (8 chiffres minimum)");
        addressField.setPromptText("Entrez l'adresse complète");
        phoneField.setPromptText("Entrez le numéro (8 chiffres)");
        emailField.setPromptText("exemple@domaine.com");
        birthDatePicker.setPromptText("Choisir la date de naissance");
        
        // Set default value for birth date to 18 years ago
        birthDatePicker.setValue(java.time.LocalDate.now().minusYears(18));
        
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
        
        // Date de naissance validation
        birthDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!isValidBirthDate(newValue)) {
                if (newValue == null) {
                    birthDateError.setText("La date de naissance est obligatoire");
                } else if (newValue.isAfter(java.time.LocalDate.now())) {
                    birthDateError.setText("La date ne peut pas être dans le futur");
                } else if (newValue.isBefore(java.time.LocalDate.now().minusYears(100))) {
                    birthDateError.setText("L'âge ne peut pas dépasser 100 ans");
                } else if (newValue.isAfter(java.time.LocalDate.now().minusYears(16))) {
                    birthDateError.setText("L'âge minimum est de 16 ans");
                }
                birthDatePicker.setStyle("-fx-border-color: #e53e3e; -fx-border-width: 2px;");
            } else {
                birthDateError.setText("");
                birthDatePicker.setStyle("-fx-border-color: #38a169; -fx-border-width: 2px;");
            }
        });
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
        ValidationUtils.<TypePermis>addValidation(typeComboBox, 
            value -> value != null, 
            "Le type de permis est obligatoire", 1);
            
        // Address validation
        ValidationUtils.addValidation(addressField, 
            text -> !text.trim().isEmpty(), 
            "L'adresse est obligatoire", 1);
        ValidationUtils.addValidation(addressField, 
            text -> isValidAddress(text, 5), 
            "L'adresse doit contenir au moins 5 caractères", 2);
            
        // Phone validation
        ValidationUtils.addValidation(phoneField, 
            text -> !text.trim().isEmpty(), 
            "Le numéro de téléphone est obligatoire", 1);
        ValidationUtils.addValidation(phoneField, 
            this::isValidPhone, 
            "Le numéro doit contenir exactement 8 chiffres", 2);
            
        // Email validation
        ValidationUtils.addValidation(emailField, 
            text -> !text.trim().isEmpty(), 
            "L'email est obligatoire", 1);
        ValidationUtils.addValidation(emailField, 
            this::isValidEmail, 
            "Format d'email invalide", 2);
    }

    @FXML
    private void searchCandidat() {
        String cin = cinField.getText().trim();
        if (!isValidCIN(cin)) {
            AlertUtil.showError("Format CIN invalide", "Le CIN doit contenir au moins 8 chiffres.");
            return;
        }

        try {
            Optional<Candidat> candidatOpt = candidatService.findByCin(cin);
            if (candidatOpt.isPresent()) {
                candidatToModify = candidatOpt.get();
                populateFields(candidatToModify);
            } else {
                AlertUtil.showError("Candidat non trouvé", "Aucun candidat trouvé avec ce CIN.");
                clearFields();
            }
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Une erreur s'est produite lors de la recherche du candidat: " + e.getMessage());
        }
    }

    private void populateFields(Candidat candidat) {
        nomField.setText(candidat.getNom());
        prenomField.setText(candidat.getPrenom());
        cinField.setText(candidat.getCin());
        addressField.setText(candidat.getAdresse());
        phoneField.setText(candidat.getTelephone());
        emailField.setText(candidat.getEmail());
        birthDatePicker.setValue(candidat.getDateNaissance());
        typeComboBox.setValue(candidat.getTypePermis());
    }

    private void clearFields() {
        nomField.clear();
        prenomField.clear();
        cinField.clear();
        addressField.clear();
        phoneField.clear();
        emailField.clear();
        birthDatePicker.setValue(java.time.LocalDate.now().minusYears(18));
        if (!typeComboBox.getItems().isEmpty()) {
            typeComboBox.getSelectionModel().selectFirst();
        }
    }


    @FXML
    private void confirmAction() {
        if (!validateInput()) {
            AlertUtil.showWarning("Validation", "Veuillez corriger les erreurs avant de continuer.");
            return;
        }

        try {
            if (candidatToModify == null) {
                AlertUtil.showError("Aucun candidat", "Veuillez d'abord rechercher un candidat à modifier.");
                return;
            }

            // Update candidat object
            candidatToModify.setNom(nomField.getText().trim());
            candidatToModify.setPrenom(prenomField.getText().trim());
            candidatToModify.setAdresse(addressField.getText().trim());
            candidatToModify.setTelephone(phoneField.getText().trim());
            candidatToModify.setEmail(emailField.getText().trim());
            candidatToModify.setDateNaissance(birthDatePicker.getValue());
            candidatToModify.setTypePermis(typeComboBox.getValue());

            // Save to database
            candidatService.updateCandidat(candidatToModify);


            AlertUtil.showSuccess("Succès", "Le candidat a été modifié avec succès.");

            // Close the window
            ((Stage) nomField.getScene().getWindow()).close();

        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Une erreur s'est produite lors de la modification: " + e.getMessage());
        }
    }

    @FXML
    private void cancelAction() {
        // Réinitialiser les champs
        clearFields();
        candidatToModify = null;
        
        // Effacer les validations
        ValidationUtils.clearValidation(nomField);
        ValidationUtils.clearValidation(prenomField);
        ValidationUtils.clearValidation(cinField);
        ValidationUtils.clearValidation(addressField);
        ValidationUtils.clearValidation(phoneField);
        ValidationUtils.clearValidation(emailField);
        ValidationUtils.clearValidation(birthDatePicker);
        ValidationUtils.clearValidation(typeComboBox);
    }

    private boolean validateInput() {
        boolean isValid = true;

        // Clear previous error messages
        nomError.setText("");
        prenomError.setText("");
        cinError.setText("");
        addressError.setText("");
        phoneError.setText("");
        emailError.setText("");
        birthDateError.setText("");
        typeError.setText("");

        // Validate each field
        if (!isValidName(nomField.getText())) {
            nomError.setText("Le nom est invalide");
            isValid = false;
        }

        if (!isValidName(prenomField.getText())) {
            prenomError.setText("Le prénom est invalide");
            isValid = false;
        }

        if (!isValidCIN(cinField.getText())) {
            cinError.setText("Le CIN est invalide");
            isValid = false;
        }

        if (!isValidAddress(addressField.getText(), 5)) {
            addressError.setText("L'adresse est invalide");
            isValid = false;
        }

        if (!isValidPhone(phoneField.getText())) {
            phoneError.setText("Le numéro de téléphone est invalide");
            isValid = false;
        }

        if (!isValidEmail(emailField.getText())) {
            emailError.setText("L'email est invalide");
            isValid = false;
        }
        
        if (!isValidBirthDate(birthDatePicker.getValue())) {
            if (birthDatePicker.getValue() == null) {
                birthDateError.setText("La date de naissance est obligatoire");
            } else if (birthDatePicker.getValue().isAfter(java.time.LocalDate.now())) {
                birthDateError.setText("La date de naissance ne peut pas être dans le futur");
            } else if (birthDatePicker.getValue().isBefore(java.time.LocalDate.now().minusYears(100))) {
                birthDateError.setText("L'âge ne peut pas dépasser 100 ans");
            } else if (birthDatePicker.getValue().isAfter(java.time.LocalDate.now().minusYears(16))) {
                birthDateError.setText("L'âge minimum est de 16 ans");
            }
            isValid = false;
        }

        if (typeComboBox.getValue() == null) {
            typeError.setText("Le type de permis est obligatoire");
            isValid = false;
        }

        return isValid;
    }
}
