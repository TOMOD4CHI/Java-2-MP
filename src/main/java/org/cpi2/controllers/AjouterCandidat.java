package org.cpi2.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.cpi2.entitties.Inscription;
import org.cpi2.service.CandidatService;
import org.cpi2.entitties.Candidat;
import org.cpi2.entitties.TypePermis;
import org.cpi2.service.InscriptionService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class AjouterCandidat {
    private final CandidatService candidatService = new CandidatService();
    private final InscriptionService inscriptionService = new InscriptionService();

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
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;

    @FXML
    private void initialize() {
        typeComboBox.setPrefWidth(200);
        // Clear existing items and add type permis options
        typeComboBox.getItems().clear();
        for (TypePermis type : TypePermis.values()) {
            typeComboBox.getItems().add(type.getLibelle());
        }
        // Set default selection
        if (!typeComboBox.getItems().isEmpty()) {
            typeComboBox.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void cancelAction() {
        nomField.clear();
        prenomField.clear();
        cinField.clear();
        typeComboBox.getSelectionModel().clearSelection();
        addressField.clear();
        phoneField.clear();
        emailField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void confirmAction() {
        // Retrieve all form field values
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String cin = cinField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String typePermis = typeComboBox.getValue();

        // Check required fields
        if (nom.isEmpty() || prenom.isEmpty() || cin.isEmpty() || 
            address.isEmpty() || phone.isEmpty() || typePermis == null) {
            showAlert("Validation Error", "Merci de remplir tous les champs obligatoires!");
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
            Inscription inscription = new Inscription();
            Candidat candidat = new Candidat();
            candidat.setNom(nom);
            candidat.setPrenom(prenom);
            candidat.setCin(cin);
            candidat.setAdresse(address);
            candidat.setTelephone(phone);
            candidat.setEmail(email);
            
            // Set type permis from combo box selection
            if (typePermis != null) {
                // Find the TypePermis enum by its libelle
                for (TypePermis type : TypePermis.values()) {
                    if (type.getLibelle().equals(typePermis)) {
                        candidat.setTypePermis(type);
                        break;
                    }
                }
            }

            inscription.setCin(candidat.getCin());
            inscription.setPaymentStatus(false);
            inscription.setStatus("En Cours");
            inscription.setPaymentCycle("Non défini");
            inscription.setInscriptioDate(Date.valueOf(LocalDate.now()));
            //inscription.setPlan(typeComboBox.getSelectionModel().getSelectedIndex());


            if (candidatService.addCandidat(candidat)) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Candidat ajouté avec succès!");
                alert.showAndWait();
                cancelAction();
            } else {
                showAlert("Erreur", "Erreur lors de l'ajout du candidat!");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }
}
