package org.cpi2.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.cpi2.entities.Moniteur;
import org.cpi2.entities.TypePermis;
import org.cpi2.service.MoniteurService;
import org.cpi2.utils.AlertUtil;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ModifierMoniteur {
    @FXML public TextField cinField;
    @FXML public TextField nomField;
    @FXML public TextField prenomField;
    @FXML public TextField adresseField;
    @FXML public TextField telephoneField;
    @FXML public TextField emailField;
    @FXML public DatePicker dateEmbaucheField;
    @FXML public ComboBox<TypePermis> typePermisComboBox;
    
    @FXML public Label cinError;
    @FXML public Label nomError;
    @FXML public Label prenomError;
    @FXML public Label adresseError;
    @FXML public Label telephoneError;
    @FXML public Label emailError;
    @FXML public Label dateError;
    @FXML public Label typePermisError;

    private final MoniteurService moniteurService = new MoniteurService();
    private Moniteur currentMoniteur;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÀ-ÿ\\s]+$");
    private static final Pattern CIN_PATTERN = Pattern.compile("^[0-9]{8,}$");

    @FXML
    public void initialize() {

        typePermisComboBox.getItems().addAll(TypePermis.values());
        typePermisComboBox.setValue(TypePermis.B); // Default value

        setupValidation();
    }

    private void setupValidation() {

        cinField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateField(cinField, cinError, "Le CIN", CIN_PATTERN);
        });

        nomField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateField(nomField, nomError, "Le nom", NAME_PATTERN);
        });

        prenomField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateField(prenomField, prenomError, "Le prénom", NAME_PATTERN);
        });

        telephoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateField(telephoneField, telephoneError, "Le numéro de téléphone", PHONE_PATTERN);
        });

        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateField(emailField, emailError, "L'email", EMAIL_PATTERN);
        });

        adresseField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = newVal != null && newVal.trim().length() >= 10;
            updateFieldValidation(adresseField, adresseError, isValid, 
                isValid ? null : "L'adresse doit contenir au moins 10 caractères");
        });

        dateEmbaucheField.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = newVal != null && !newVal.isAfter(LocalDate.now());
            updateFieldValidation(dateEmbaucheField, dateError, isValid, 
                isValid ? null : "La date d'embauche ne peut pas être dans le futur");
        });
    }

    private void validateField(TextField field, Label errorLabel, String fieldName, Pattern pattern) {
        String value = field.getText().trim();
        boolean isValid = !value.isEmpty() && pattern.matcher(value).matches();
        updateFieldValidation(field, errorLabel, isValid, 
            isValid ? null : String.format("%s n'est pas valide", fieldName));
    }

    private void updateFieldValidation(Control field, Label errorLabel, boolean isValid, String errorMessage) {
        if (isValid) {
            field.getStyleClass().remove("field-error");
            field.getStyleClass().add("field-valid");
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.getStyleClass().remove("visible");
        } else {
            field.getStyleClass().remove("field-valid");
            field.getStyleClass().add("field-error");
            errorLabel.setText(errorMessage);
            errorLabel.setVisible(true);
            errorLabel.getStyleClass().add("visible");
        }
    }

    @FXML
    public void searchMoniteur(ActionEvent event) {
        String cin = cinField.getText().trim();
        
        if (cin.isEmpty()) {
            AlertUtil.showError("Erreur de validation", "Veuillez entrer un CIN pour rechercher.");
            return;
        }

        if (!isValidCIN(cin)) {
            AlertUtil.showError("Erreur de validation", "Le CIN doit contenir au moins 8 chiffres et ne doit contenir que des chiffres.");
            return;
        }

        currentMoniteur = moniteurService.findByCin(cin);
        if (currentMoniteur != null) {

            nomField.setText(currentMoniteur.getNom());
            prenomField.setText(currentMoniteur.getPrenom());
            adresseField.setText(currentMoniteur.getAdresse());
            telephoneField.setText(currentMoniteur.getTelephone());
            emailField.setText(currentMoniteur.getEmail());
            dateEmbaucheField.setValue(currentMoniteur.getDateEmbauche());

            if (!currentMoniteur.getSpecialites().isEmpty()) {
                typePermisComboBox.setValue(currentMoniteur.getSpecialites().iterator().next());
            } else {
                typePermisComboBox.setValue(TypePermis.B);
            }
        } else {
            AlertUtil.showError("Erreur", "Aucun moniteur trouvé avec ce CIN.");
            clearForm();
        }
    }

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
    
    private boolean isValidAddress(String address) {
        return address != null && address.trim().length() >= 10;
    }


    @FXML
    public void modifierAction(ActionEvent event) {
        if (currentMoniteur == null) {
            AlertUtil.showError("Erreur", "Veuillez d'abord rechercher un moniteur à modifier.");
            return;
        }

        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String adresse = adresseField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String email = emailField.getText().trim();
        LocalDate dateEmbauche = dateEmbaucheField.getValue();
        TypePermis typePermis = typePermisComboBox.getValue();

        boolean isValid = true;
        
        if (!isValidName(nom)) {
            nomError.setText("Le nom ne doit contenir que des lettres et des espaces.");
            nomError.setVisible(true);
            nomError.getStyleClass().add("visible");
            isValid = false;
        }

        if (!isValidName(prenom)) {
            prenomError.setText("Le prénom ne doit contenir que des lettres et des espaces.");
            prenomError.setVisible(true);
            prenomError.getStyleClass().add("visible");
            isValid = false;
        }

        if (!isValidAddress(adresse)) {
            adresseError.setText("L'adresse doit contenir au moins 10 caractères.");
            adresseError.setVisible(true);
            adresseError.getStyleClass().add("visible");
            isValid = false;
        }

        if (!isValidPhone(telephone)) {
            telephoneError.setText("Le numéro de téléphone doit contenir exactement 8 chiffres.");
            telephoneError.setVisible(true);
            telephoneError.getStyleClass().add("visible");
            isValid = false;
        }

        if (!isValidEmail(email)) {
            emailError.setText("L'adresse email n'est pas valide.");
            emailError.setVisible(true);
            emailError.getStyleClass().add("visible");
            isValid = false;
        }

        if (dateEmbauche == null || dateEmbauche.isAfter(LocalDate.now())) {
            dateError.setText("La date d'embauche ne peut pas être dans le futur.");
            dateError.setVisible(true);
            dateError.getStyleClass().add("visible");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        currentMoniteur.setNom(nom);
        currentMoniteur.setPrenom(prenom);
        currentMoniteur.setAdresse(adresse);
        currentMoniteur.setTelephone(telephone);
        currentMoniteur.setEmail(email);
        currentMoniteur.setDateEmbauche(dateEmbauche);

        Set<TypePermis> specialites = new HashSet<>();
        specialites.add(typePermis);
        currentMoniteur.setSpecialites(specialites);

        boolean success = moniteurService.updateMoniteur(currentMoniteur);

        if (success) {
            AlertUtil.showSuccess("Succès", "Les informations du moniteur ont été modifiées avec succès!");
            clearForm();

        } else {
            AlertUtil.showError("Erreur", "Échec de la modification des informations du moniteur.");
        }
    }

    @FXML
    public void cancelAction(ActionEvent event) {
        clearForm();
    }

    private void clearForm() {
        cinField.clear();
        nomField.clear();
        prenomField.clear();
        adresseField.clear();
        telephoneField.clear();
        emailField.clear();
        dateEmbaucheField.setValue(null);
        typePermisComboBox.setValue(TypePermis.B);
        currentMoniteur = null;

        cinError.setVisible(false);
        nomError.setVisible(false);
        prenomError.setVisible(false);
        adresseError.setVisible(false);
        telephoneError.setVisible(false);
        emailError.setVisible(false);
        dateError.setVisible(false);
        typePermisError.setVisible(false);

        cinError.getStyleClass().remove("visible");
        nomError.getStyleClass().remove("visible");
        prenomError.getStyleClass().remove("visible");
        adresseError.getStyleClass().remove("visible");
        telephoneError.getStyleClass().remove("visible");
        emailError.getStyleClass().remove("visible");
        dateError.getStyleClass().remove("visible");
        typePermisError.getStyleClass().remove("visible");

        cinField.getStyleClass().removeAll("field-error", "field-valid");
        nomField.getStyleClass().removeAll("field-error", "field-valid");
        prenomField.getStyleClass().removeAll("field-error", "field-valid");
        adresseField.getStyleClass().removeAll("field-error", "field-valid");
        telephoneField.getStyleClass().removeAll("field-error", "field-valid");
        emailField.getStyleClass().removeAll("field-error", "field-valid");
        dateEmbaucheField.getStyleClass().removeAll("field-error", "field-valid");
    }
}


