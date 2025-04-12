package org.cpi2.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.cpi2.utils.AlertUtil;
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

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÀ-ÿ\\s]+$");
    private static final Pattern CIN_PATTERN = Pattern.compile("^[0-9]{8,}$");

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        typePermisComboBox.getItems().addAll(TypePermis.values());
        typePermisComboBox.setValue(TypePermis.B); // Default value

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

        ValidationUtils.addValidation(nomField, 
            name -> name != null && !name.isEmpty() && NAME_PATTERN.matcher(name).matches(),
            "Le nom ne doit contenir que des lettres et des espaces", 1);

        ValidationUtils.addValidation(prenomField,
            prenom -> prenom != null && !prenom.isEmpty() && NAME_PATTERN.matcher(prenom).matches(),
            "Le prénom ne doit contenir que des lettres et des espaces", 1);

        ValidationUtils.addValidation(cinField,
            cin -> cin != null && CIN_PATTERN.matcher(cin).matches(),
            "Le CIN doit contenir au moins 8 chiffres et ne doit contenir que des chiffres", 1);

        ValidationUtils.addValidation(adresseField,
            address -> address != null && address.length() >= 10,
            "L'adresse doit contenir au moins 10 caractères", 1);

        ValidationUtils.addValidation(telephoneField,
            phone -> phone != null && PHONE_PATTERN.matcher(phone).matches(),
            "Le numéro de téléphone doit contenir exactement 8 chiffres", 1);

        ValidationUtils.addValidation(emailField,
            email -> email != null && EMAIL_PATTERN.matcher(email).matches(),
            "L'adresse email n'est pas valide", 1);

        ValidationUtils.addValidation(dateEmbaucheField,
            date -> date != null && !date.isAfter(LocalDate.now()),
            "La date d'embauche ne peut pas être dans le futur", 1);

        ValidationUtils.addValidation(typePermisComboBox,
            type -> type != null,
            "Veuillez sélectionner un type de permis", 1);
    }

    public void ajouterAction(ActionEvent event) {
        if (ValidationUtils.hasAnyErrors()) {
            AlertUtil.showWarning("Validation", "Veuillez corriger les erreurs avant de continuer.");
            return;
        }

        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String cin = cinField.getText().trim();
        String adresse = adresseField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String email = emailField.getText().trim();
        LocalDate dateEmbauche = dateEmbaucheField.getValue();
        TypePermis typePermis = typePermisComboBox.getValue();

        Moniteur moniteur = new Moniteur();
        moniteur.setNom(nom);
        moniteur.setPrenom(prenom);
        moniteur.setCin(cin);
        moniteur.setAdresse(adresse);
        moniteur.setTelephone(telephone);
        moniteur.setEmail(email);
        moniteur.setDateEmbauche(dateEmbauche);

        Set<TypePermis> specialites = new HashSet<>();
        specialites.add(typePermis);
        moniteur.setSpecialites(specialites);

        boolean success = moniteurService.addMoniteur(moniteur);

        if (success) {
            AlertUtil.showSuccess("Succès", "Le moniteur a été ajouté avec succès!");

            clearForm();
        } else {
            AlertUtil.showError("Erreur", "Échec de l'ajout du moniteur.");
        }
    }

    public void cancelAction(ActionEvent event) {
        clearForm();
    }
    
    private void clearForm() {

        nomField.clear();
        prenomField.clear();
        cinField.clear();
        adresseField.clear();
        telephoneField.clear();
        emailField.clear();
        dateEmbaucheField.setValue(null);
        typePermisComboBox.setValue(TypePermis.B);

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


