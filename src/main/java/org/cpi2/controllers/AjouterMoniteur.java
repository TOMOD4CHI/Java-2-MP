package org.cpi2.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.cpi2.entities.Moniteur;
import org.cpi2.entities.TypePermis;
import org.cpi2.service.MoniteurService;
import org.cpi2.utils.ValidationUtils;

import java.net.URL;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

public class AjouterMoniteur implements Initializable {
    @FXML
    public TextField nomField;
    @FXML
    public TextField prenomField;
    @FXML
    public TextField cinField;
    @FXML
    public TextField adresseField;
    @FXML
    public TextField telephoneField;
    @FXML
    public TextField emailField;
    @FXML
    public DatePicker dateEmbaucheField;
    @FXML
    public ComboBox<TypePermis> typePermisComboBox;

    private final MoniteurService moniteurService = new MoniteurService();
    
    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÀ-ÿ\\s]+$");
    private static final Pattern CIN_PATTERN = Pattern.compile("^[0-9]{8,}$");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the typePermisComboBox with available permit types
        typePermisComboBox.getItems().addAll(TypePermis.values());
        typePermisComboBox.setValue(TypePermis.B); // Default value
        
        // Set placeholders for input fields
        nomField.setPromptText("Entrez le nom");
        prenomField.setPromptText("Entrez le prénom");
        cinField.setPromptText("Entrez le CIN (8 chiffres minimum)");
        adresseField.setPromptText("Entrez l'adresse complète");
        telephoneField.setPromptText("Entrez le numéro (8 chiffres)");
        emailField.setPromptText("exemple@domaine.com");
        dateEmbaucheField.setPromptText("Date d'embauche");
        
        setupValidation();
    }
    
    private void setupValidation() {
        // Validate name field
        ValidationUtils.addValidation(nomField, 
            name -> name != null && !name.isEmpty() && NAME_PATTERN.matcher(name).matches(),
            "Le nom ne doit contenir que des lettres et des espaces", 1);
        
        // Validate first name field
        ValidationUtils.addValidation(prenomField,
            prenom -> prenom != null && !prenom.isEmpty() && NAME_PATTERN.matcher(prenom).matches(),
            "Le prénom ne doit contenir que des lettres et des espaces", 1);
        
        // Validate CIN field
        ValidationUtils.addValidation(cinField,
            cin -> cin != null && CIN_PATTERN.matcher(cin).matches(),
            "Le CIN doit contenir au moins 8 chiffres et ne doit contenir que des chiffres", 1);
        
        // Validate address field
        ValidationUtils.addValidation(adresseField,
            address -> address != null && address.length() >= 10,
            "L'adresse doit contenir au moins 10 caractères", 1);
        
        // Validate phone field
        ValidationUtils.addValidation(telephoneField,
            phone -> phone != null && PHONE_PATTERN.matcher(phone).matches(),
            "Le numéro de téléphone doit contenir exactement 8 chiffres", 1);
        
        // Validate email field
        ValidationUtils.addValidation(emailField,
            email -> email != null && EMAIL_PATTERN.matcher(email).matches(),
            "L'adresse email n'est pas valide", 1);
        
        // Validate date field
        ValidationUtils.addValidation(dateEmbaucheField,
            date -> date != null && !date.isAfter(LocalDate.now()),
            "La date d'embauche ne peut pas être dans le futur", 1);
        
        // Validate type permis field
        ValidationUtils.addValidation(typePermisComboBox,
            type -> type != null,
            "Veuillez sélectionner un type de permis", 1);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void ajouterAction(ActionEvent event) {
        // Check if there are any validation errors
        if (ValidationUtils.hasAnyErrors()) {
            return;
        }
        
        // Get the values from the form
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String cin = cinField.getText().trim();
        String adresse = adresseField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String email = emailField.getText().trim();
        LocalDate dateEmbauche = dateEmbaucheField.getValue();
        TypePermis typePermis = typePermisComboBox.getValue();

        // Create a new moniteur
        Moniteur moniteur = new Moniteur();
        moniteur.setNom(nom);
        moniteur.setPrenom(prenom);
        moniteur.setCin(cin);
        moniteur.setAdresse(adresse);
        moniteur.setTelephone(telephone);
        moniteur.setEmail(email);
        moniteur.setDateEmbauche(dateEmbauche);
        
        // Set specialities
        Set<TypePermis> specialites = new HashSet<>();
        specialites.add(typePermis);
        moniteur.setSpecialites(specialites);

        // Save the moniteur
        boolean success = moniteurService.addMoniteur(moniteur);

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Le moniteur a été ajouté avec succès!");
            alert.showAndWait();
            
            // Clear the form
            clearForm();
        } else {
            showAlert("Erreur", "Échec de l'ajout du moniteur.");
        }
    }

    public void cancelAction(ActionEvent event) {
        clearForm();
    }
    
    private void clearForm() {
        // Clear all fields
        nomField.clear();
        prenomField.clear();
        cinField.clear();
        adresseField.clear();
        telephoneField.clear();
        emailField.clear();
        dateEmbaucheField.setValue(null);
        typePermisComboBox.setValue(TypePermis.B);
        
        // Clear validation styles
        ValidationUtils.clearValidation(nomField);
        ValidationUtils.clearValidation(prenomField);
        ValidationUtils.clearValidation(cinField);
        ValidationUtils.clearValidation(adresseField);
        ValidationUtils.clearValidation(telephoneField);
        ValidationUtils.clearValidation(emailField);
        ValidationUtils.clearValidation(dateEmbaucheField);
        ValidationUtils.clearValidation(typePermisComboBox);
    }
}
