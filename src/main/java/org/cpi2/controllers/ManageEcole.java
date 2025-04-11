package org.cpi2.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.cpi2.entities.AutoEcole;
import org.cpi2.service.AutoEcoleService;
import org.cpi2.utils.AlertUtil;
import org.cpi2.utils.EventBus;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ManageEcole implements Initializable {

    @FXML private Label nomLabel;
    @FXML private Label adresseLabel;
    @FXML private Label telephoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label directeurLabel;
    @FXML private Label passwordLabel;
    
    @FXML private TextField nomField;
    @FXML private TextField adresseField;
    @FXML private TextField telephoneField;
    @FXML private TextField emailField;
    @FXML private TextField directeurField;
    @FXML private PasswordField passwordField;
    
    @FXML private ImageView logoImageView;
    @FXML private Button changeLogoButton;
    @FXML private Button modifierButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private HBox buttonContainer;
    @FXML private HBox editButtonsContainer;
    
    @FXML private VBox infoContainer;
    @FXML private VBox editContainer;
    
    // Error labels for validation
    @FXML private Label nomError;
    @FXML private Label adresseError;
    @FXML private Label telephoneError;
    @FXML private Label emailError;
    @FXML private Label directeurError;
    @FXML private Label passwordError;

    private final AutoEcoleService autoEcoleService = new AutoEcoleService();
    private AutoEcole currentAutoEcole;
    private File selectedLogoFile;

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÀ-ÿ\\s]+$");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Apply drop shadow effect to the logo
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(10.0);
        dropShadow.setOffsetX(3.0);
        dropShadow.setOffsetY(3.0);
        dropShadow.setColor(Color.color(0, 0, 0, 0.3));
        logoImageView.setEffect(dropShadow);

        // Center all buttons
        buttonContainer.setAlignment(Pos.CENTER);
        editButtonsContainer.setAlignment(Pos.CENTER);

        // Setup real-time validation
        setupValidation();

        // Apply styling to form fields
        applyFormStyling();

        // Initially show info container and hide edit container
        showEditMode(false);

        // Load auto-école data
        loadAutoEcoleData();
    }

    private void setupValidation() {
        // Setup real-time validation for each field
        nomField.textProperty().addListener((obs, oldVal, newVal) -> validateField(nomField, nomError, "Le nom", NAME_PATTERN));
        directeurField.textProperty().addListener((obs, oldVal, newVal) -> validateField(directeurField, directeurError, "Le nom du directeur", NAME_PATTERN));
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateField(emailField, emailError, "L'email", EMAIL_PATTERN));
        telephoneField.textProperty().addListener((obs, oldVal, newVal) -> validateField(telephoneField, telephoneError, "Le numéro de téléphone", PHONE_PATTERN));
        adresseField.textProperty().addListener((obs, oldVal, newVal) -> validateNonEmpty(adresseField, adresseError, "L'adresse"));
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validatePassword(passwordField, passwordError));
    }

    private void validateField(TextField field, Label errorLabel, String fieldName, Pattern pattern) {
        String value = field.getText().trim();
        boolean isEmpty = value.isEmpty();
        boolean isValid = !isEmpty && pattern.matcher(value).matches();

        if (isEmpty) {
            updateFieldValidation(field, errorLabel, false,
                String.format("%s ne peut pas être vide", fieldName));
        } else {
            updateFieldValidation(field, errorLabel, isValid,
                isValid ? null : String.format("%s n'est pas valide", fieldName));
        }
    }

    private void validateNonEmpty(TextField field, Label errorLabel, String fieldName) {
        String value = field.getText().trim();
        boolean isValid = !value.isEmpty();

        updateFieldValidation(field, errorLabel, isValid,
            isValid ? null : String.format("%s ne peut pas être vide", fieldName));
    }

    private void validatePassword(PasswordField field, Label errorLabel) {
        String value = field.getText();
        boolean isValid = value.length() >= 4;

        updateFieldValidation(field, errorLabel, isValid,
            isValid ? null : "Le mot de passe doit contenir au moins 4 caractères");
    }

    private void updateFieldValidation(TextInputControl field, Label errorLabel, boolean isValid, String errorMessage) {
            // Remove existing style classes
        field.getStyleClass().removeAll("field-error", "field-valid");

        // Add appropriate style class
        field.getStyleClass().add(isValid ? "field-valid" : "field-error");

        // Update error label
        errorLabel.setText(errorMessage);
        errorLabel.setVisible(errorMessage != null);
        errorLabel.setManaged(errorMessage != null);
    }

    private void applyFormStyling() {
        // Apply styling to text fields
        TextField[] fields = {nomField, adresseField, telephoneField, emailField, directeurField};
        for (TextField field : fields) {
            if (field != null) {
                field.getStyleClass().add("form-field");

                // Add focus effect
                field.focusedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        field.setStyle("-fx-border-color: #3182CE; -fx-border-width: 2px;");
                    } else {
                        field.setStyle("");
                    }
                });
            }
        }

        // Apply styling to password field
        if (passwordField != null) {
            passwordField.getStyleClass().add("form-field");

            // Add focus effect
            passwordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    passwordField.setStyle("-fx-border-color: #3182CE; -fx-border-width: 2px;");
                } else {
                    passwordField.setStyle("");
                }
            });
        }

        // Apply styling to buttons
        saveButton.getStyleClass().add("submit-button");
        cancelButton.getStyleClass().add("cancel-button");
        modifierButton.getStyleClass().add("action-button");
        changeLogoButton.getStyleClass().add("small-button");
    }

    private void loadAutoEcoleData() {
        // Load auto-école data from database
        try {
            currentAutoEcole = autoEcoleService.getAutoEcole();
            if (currentAutoEcole != null) {
                updateInfoLabels();
                populateEditFields();

                // Load logo if it exists
                loadLogo();
            } else {
                // If no auto-école exists, show the edit form
                showEditMode(true);
            }
        } catch (Exception e) {
            AlertUtil.showError( "Erreur", "Impossible de charger les informations de l'auto-école \n"+ e.getMessage());
        }
    }

    private void loadLogo() {
        if (currentAutoEcole != null && currentAutoEcole.getLogo() != null && !currentAutoEcole.getLogo().isEmpty()) {
            try {
                File logoFile = new File(currentAutoEcole.getLogo());
                if (logoFile.exists()) {
                    Image logoImage = new Image(logoFile.toURI().toString());
                    logoImageView.setImage(logoImage);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement du logo: " + e.getMessage());
            }
        }
    }

    private void updateInfoLabels() {
        if (currentAutoEcole != null) {
            nomLabel.setText(currentAutoEcole.getNom());
            adresseLabel.setText(currentAutoEcole.getAdresse());
            telephoneLabel.setText(currentAutoEcole.getTelephone());
            emailLabel.setText(currentAutoEcole.getEmail());
            directeurLabel.setText(currentAutoEcole.getUsername());
            passwordLabel.setText("********"); // Hide actual password for security
        }
    }

    private void populateEditFields() {
        if (currentAutoEcole != null) {
            nomField.setText(currentAutoEcole.getNom());
            adresseField.setText(currentAutoEcole.getAdresse());
            telephoneField.setText(currentAutoEcole.getTelephone());
            emailField.setText(currentAutoEcole.getEmail());
            directeurField.setText(currentAutoEcole.getUsername());
            passwordField.setText(currentAutoEcole.getPassword());
        }
    }

    private void showEditMode(boolean editMode) {
        infoContainer.setVisible(!editMode);
        infoContainer.setManaged(!editMode);

        editContainer.setVisible(editMode);
        editContainer.setManaged(editMode);

        modifierButton.setVisible(!editMode);
        modifierButton.setManaged(!editMode);

        // If entering edit mode, populate fields
        if (editMode && currentAutoEcole != null) {
            populateEditFields();
        }

        // Apply fade transition for smooth transition
        javafx.animation.FadeTransition fadeTransition = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(300),
                editMode ? editContainer : infoContainer);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }

    @FXML
    void handleModifier() {
        showEditMode(true);
    }

    @FXML
    private void handleCancel() {
        showEditMode(false);
        selectedLogoFile = null;
    }

    @FXML
    private void handleSave() {
        // Clear previous error messages
        clearErrorMessages();

        // Validate input
        if (!validateInput()) {
            return;
        }

        try {
            // Update or create auto-école
            if (currentAutoEcole == null) {
                currentAutoEcole = new AutoEcole();
            }

            currentAutoEcole.setNom(nomField.getText().trim());
            currentAutoEcole.setAdresse(adresseField.getText().trim());
            currentAutoEcole.setTelephone(telephoneField.getText().trim());
            currentAutoEcole.setEmail(emailField.getText().trim());
            currentAutoEcole.setUsername(directeurField.getText().trim());

            // Update password if provided
            if (passwordField.getText() != null && !passwordField.getText().isEmpty()) {
                currentAutoEcole.setPassword(passwordField.getText().trim());
            }

            // Handle logo file if selected
            if (selectedLogoFile != null) {
                currentAutoEcole.setLogo(selectedLogoFile.getAbsolutePath());
            }

            // Save to database
            boolean success = currentAutoEcole.getId() == 0 ?
                    autoEcoleService.saveAutoEcole(currentAutoEcole) :
                    autoEcoleService.updateAutoEcole(currentAutoEcole);

            if (success) {
                AlertUtil.showSuccess("Succès", "Auto-école enregistrée \nLes informations ont été enregistrées avec succès.");


                showEditMode(false);
                updateInfoLabels();

                // Publish event to update the footer in MainWindow
                EventBus.publish("AUTO_ECOLE_UPDATED", currentAutoEcole);
            } else {
                AlertUtil.showError("Erreur", "Échec de l'enregistrement \nImpossible d'enregistrer les informations de l'auto-école.");
            }
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Échec de l'enregistrement\n" + e.getMessage());
        }
    }

    @FXML
    private void handleChangeLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un logo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Get the Stage
        Stage stage = (Stage) logoImageView.getScene().getWindow();

        // Show open file dialog
        selectedLogoFile = fileChooser.showOpenDialog(stage);

        if (selectedLogoFile != null) {
            try {
                Image logoImage = new Image(selectedLogoFile.toURI().toString());
                logoImageView.setImage(logoImage);
            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Impossible de charger l'image \n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExport() {
        if (currentAutoEcole == null) {
            AlertUtil.showWarning("Attention", "Aucune information \nIl n'y a pas d'informations à sauvegarder.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder les informations");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers texte", "*.txt")
        );
        fileChooser.setInitialFileName("AutoEcole_" + currentAutoEcole.getNom().replace(" ", "_") + ".txt");

        // Get the Stage
        Stage stage = (Stage) logoImageView.getScene().getWindow();

        // Show save file dialog
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Informations de l'Auto-École");
                writer.println("===========================");
                writer.println("Nom: " + currentAutoEcole.getNom());
                writer.println("Adresse: " + currentAutoEcole.getAdresse());
                writer.println("Téléphone: " + currentAutoEcole.getTelephone());
                writer.println("Email: " + currentAutoEcole.getEmail());

                AlertUtil.showInfo("Succès", "Sauvegarde réussie \nLes informations ont été sauvegardées dans " + file.getName());
            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Échec de la sauvegarde \n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handlePrint() {
        // In a real application, this would use PrinterJob to print the information
        AlertUtil.showInfo("Impression", "Fonctionnalité d'impression \nL'impression sera implémentée dans une future version.");
    }

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

    private void clearErrorMessages() {
        // Hide all error labels
        Label[] errorLabels = {nomError, adresseError, telephoneError, emailError, directeurError, passwordError};
        for (Label label : errorLabels) {
            if (label != null) {
                label.setVisible(false);
                label.setManaged(false);
                label.setText("");
            }
        }
    }

    private boolean validateInput() {
        boolean isValid = true;

        // Validate nom
        if (nomField.getText().trim().isEmpty()) {
            showError(nomError, "Le nom est obligatoire");
            isValid = false;
        } else if (!isValidName(nomField.getText().trim())) {
            showError(nomError, "Le nom doit contenir uniquement des lettres et des espaces");
            isValid = false;
        }

        // Validate adresse
        if (adresseField.getText().trim().isEmpty()) {
            showError(adresseError, "L'adresse est obligatoire");
            isValid = false;
        } else if (adresseField.getText().trim().length() < 10) {
            showError(adresseError, "L'adresse doit contenir au moins 10 caractères");
            isValid = false;
        }

        // Validate telephone
        if (telephoneField.getText().trim().isEmpty()) {
            showError(telephoneError, "Le numéro de téléphone est obligatoire");
            isValid = false;
        } else if (!isValidPhone(telephoneField.getText().trim())) {
            showError(telephoneError, "Le numéro de téléphone doit contenir 8 chiffres");
            isValid = false;
        }

        // Validate email
        if (emailField.getText().trim().isEmpty()) {
            showError(emailError, "L'adresse email est obligatoire");
            isValid = false;
        } else if (!isValidEmail(emailField.getText().trim())) {
            showError(emailError, "L'adresse email n'est pas valide");
            isValid = false;
        }

        // Validate directeur
        if (directeurField.getText().trim().isEmpty()) {
            showError(directeurError, "Le nom du directeur est obligatoire");
            isValid = false;
        }

        // Validate password if it's a new auto-école or if the field is not empty
        if ((currentAutoEcole == null || currentAutoEcole.getId() == 0) &&
            (passwordField.getText() == null || passwordField.getText().isEmpty())) {
            showError(passwordError, "Le mot de passe est obligatoire");
            isValid = false;
        } else if (passwordField.getText() != null && !passwordField.getText().isEmpty() &&
                   passwordField.getText().length() < 5) {
            showError(passwordError, "Le mot de passe doit contenir au moins 5 caractères");
            isValid = false;
        }

        return isValid;
    }

    private void showError(Label errorLabel, String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

}