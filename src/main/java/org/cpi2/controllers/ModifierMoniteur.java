package org.cpi2.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.cpi2.entitties.Moniteur;
import org.cpi2.entitties.TypePermis;
import org.cpi2.service.MoniteurService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ModifierMoniteur {
    public TextField cinField;
    public TextField nomField;
    public TextField prenomField;
    public TextField adresseField;
    public TextField telephoneField;
    public TextField emailField;
    public DatePicker dateEmbaucheField;
    public ComboBox<TypePermis> typePermisComboBox;

    private final MoniteurService moniteurService = new MoniteurService();
    private Moniteur currentMoniteur;

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÀ-ÿ\\s]+$");
    private static final Pattern CIN_PATTERN = Pattern.compile("^[A-Z0-9]{6}$");

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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void initialize() {
        // Initialize the typePermisComboBox with available permit types
        typePermisComboBox.getItems().addAll(TypePermis.values());
        typePermisComboBox.setValue(TypePermis.B); // Default value
    }

    public void searchMoniteur(ActionEvent event) {
        String cin = cinField.getText().trim();
        
        if (cin.isEmpty()) {
            showAlert("Erreur de validation", "Veuillez entrer un CIN pour rechercher.");
            return;
        }

        if (!isValidCIN(cin)) {
            showAlert("Erreur de validation", "Le CIN doit contenir 6 caractères (lettres majuscules et chiffres).");
            return;
        }

        currentMoniteur = moniteurService.findByCin(cin);
        if (currentMoniteur != null) {
            // Fill the fields with existing data
            nomField.setText(currentMoniteur.getNom());
            prenomField.setText(currentMoniteur.getPrenom());
            adresseField.setText(currentMoniteur.getAdresse());
            telephoneField.setText(currentMoniteur.getTelephone());
            emailField.setText(currentMoniteur.getEmail());
            dateEmbaucheField.setValue(currentMoniteur.getDateEmbauche());
            
            // Set the type permis from the first speciality
            if (!currentMoniteur.getSpecialites().isEmpty()) {
                typePermisComboBox.setValue(currentMoniteur.getSpecialites().iterator().next());
            } else {
                typePermisComboBox.setValue(TypePermis.B);
            }
        } else {
            showAlert("Erreur", "Aucun moniteur trouvé avec ce CIN.");
        }
    }

    public void modifierAction(ActionEvent event) {
        if (currentMoniteur == null) {
            showAlert("Erreur", "Veuillez d'abord rechercher un moniteur à modifier.");
            return;
        }

        // Get the values from the form
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String adresse = adresseField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String email = emailField.getText().trim();
        LocalDate dateEmbauche = dateEmbaucheField.getValue();
        TypePermis typePermis = typePermisComboBox.getValue();

        // Validate required fields
        if (nom.isEmpty() || prenom.isEmpty() || adresse.isEmpty() || 
            telephone.isEmpty() || email.isEmpty() || dateEmbauche == null || typePermis == null) {
            showAlert("Erreur de validation", "Tous les champs sont obligatoires.");
            return;
        }

        // Validate name format
        if (!isValidName(nom)) {
            showAlert("Erreur de validation", "Le nom ne doit contenir que des lettres et des espaces.");
            return;
        }

        // Validate first name format
        if (!isValidName(prenom)) {
            showAlert("Erreur de validation", "Le prénom ne doit contenir que des lettres et des espaces.");
            return;
        }

        // Validate phone number format
        if (!isValidPhone(telephone)) {
            showAlert("Erreur de validation", "Le numéro de téléphone doit contenir exactement 8 chiffres.");
            return;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            showAlert("Erreur de validation", "L'adresse email n'est pas valide.");
            return;
        }

        // Validate address (basic check for minimum length)
        if (adresse.length() < 10) {
            showAlert("Erreur de validation", "L'adresse doit contenir au moins 10 caractères.");
            return;
        }

        // Validate date d'embauche
        if (dateEmbauche.isAfter(LocalDate.now())) {
            showAlert("Erreur de validation", "La date d'embauche ne peut pas être dans le futur.");
            return;
        }

        // Update the moniteur information
        currentMoniteur.setNom(nom);
        currentMoniteur.setPrenom(prenom);
        currentMoniteur.setAdresse(adresse);
        currentMoniteur.setTelephone(telephone);
        currentMoniteur.setEmail(email);
        currentMoniteur.setDateEmbauche(dateEmbauche);
        
        // Update specialities
        Set<TypePermis> specialites = new HashSet<>();
        specialites.add(typePermis);
        currentMoniteur.setSpecialites(specialites);

        // Save the changes
        boolean success = moniteurService.updateMoniteur(currentMoniteur);

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Les informations du moniteur ont été modifiées avec succès!");
            alert.showAndWait();
            
            // Clear the form
            cinField.clear();
            nomField.clear();
            prenomField.clear();
            adresseField.clear();
            telephoneField.clear();
            emailField.clear();
            dateEmbaucheField.setValue(null);
            typePermisComboBox.setValue(TypePermis.B);
            currentMoniteur = null;
        } else {
            showAlert("Erreur", "Échec de la modification des informations du moniteur.");
        }
    }

    public void cancelAction(ActionEvent event) {
        // Clear all fields
        cinField.clear();
        nomField.clear();
        prenomField.clear();
        adresseField.clear();
        telephoneField.clear();
        emailField.clear();
        dateEmbaucheField.setValue(null);
        typePermisComboBox.setValue(TypePermis.B);
        currentMoniteur = null;
    }
}
