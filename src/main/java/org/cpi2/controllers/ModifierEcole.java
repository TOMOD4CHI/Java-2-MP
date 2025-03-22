package org.cpi2.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.cpi2.entitties.AutoEcole;
import org.cpi2.service.AutoEcoleService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

public class ModifierEcole {

    @FXML public TextField nomField;
    @FXML public TextField adresseField;
    @FXML public TextField telephoneField;
    @FXML public TextField emailField;
    @FXML public ImageView logoImageView;
    @FXML public Label validationMessageLabel;

    private Image logoImage;
    private String logoPath;
    private final AutoEcoleService autoEcoleService = new AutoEcoleService();
    private AutoEcole currentAutoEcole;
    private AutoEcole originalAutoEcole; // For reset functionality

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÀ-ÿ\\s]+$");

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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to set an auto-ecole to modify from the list view
    public void setAutoEcoleToModify(AutoEcole autoEcole) {
        if (autoEcole != null) {
            this.currentAutoEcole = autoEcole;

            // Create a clone of the original auto-ecole for reset functionality
            this.originalAutoEcole = new AutoEcole(
                    autoEcole.getId(),
                    autoEcole.getNom(),
                    autoEcole.getAdresse(),
                    autoEcole.getTelephone(),
                    autoEcole.getEmail(),
                    autoEcole.getLogo()
            );

            // Load data into the form with visual styling
            populateFormFields();
        }
    }

    private void populateFormFields() {
        // Fill the fields with the current auto-ecole data
        nomField.setText(currentAutoEcole.getNom());
        adresseField.setText(currentAutoEcole.getAdresse());
        telephoneField.setText(currentAutoEcole.getTelephone());
        emailField.setText(currentAutoEcole.getEmail());

        // Style fields with current values
        styleFieldsForEditing();

        // Load the logo if it exists
        logoPath = currentAutoEcole.getLogo();
        if (logoPath != null && !logoPath.isEmpty()) {
            try {
                File logoFile = new File(logoPath);
                if (logoFile.exists()) {
                    logoImage = new Image(logoFile.toURI().toString());
                    logoImageView.setImage(logoImage);
                    logoImageView.getStyleClass().add("logo-image");
                    // Apply dropshadow effect
                    logoImageView.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.color(0, 0, 0, 0.3)));
                }
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de charger le logo: " + e.getMessage());
            }
        } else {
            logoImageView.setImage(null);
        }

        // Clear any validation messages
        validationMessageLabel.setText("");
    }

    private void styleFieldsForEditing() {
        // Add visual cues for modification
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(originalAutoEcole.getNom())) {
                nomField.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2px;");
            } else {
                nomField.setStyle("");
            }
        });

        adresseField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(originalAutoEcole.getAdresse())) {
                adresseField.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2px;");
            } else {
                adresseField.setStyle("");
            }
        });

        telephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(originalAutoEcole.getTelephone())) {
                telephoneField.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2px;");
            } else {
                telephoneField.setStyle("");
            }
        });

        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(originalAutoEcole.getEmail())) {
                emailField.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2px;");
            } else {
                emailField.setStyle("");
            }
        });
    }

    // Initialize the controller with existing data
    public void initialize() {
        // Add input validation as users type
        setupFieldValidation();
    }

    private void setupFieldValidation() {
        // Real-time validation feedback
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !isValidName(newValue)) {
                showValidationMessage("Le nom doit contenir uniquement des lettres et des espaces");
            } else {
                clearValidationMessage();
            }
        });

        telephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !isValidPhone(newValue)) {
                showValidationMessage("Le téléphone doit contenir exactement 8 chiffres");
            } else {
                clearValidationMessage();
            }
        });

        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !isValidEmail(newValue)) {
                showValidationMessage("Format email invalide");
            } else {
                clearValidationMessage();
            }
        });
    }

    private void showValidationMessage(String message) {
        validationMessageLabel.setText(message);
        validationMessageLabel.setTextFill(Color.RED);
    }

    private void clearValidationMessage() {
        validationMessageLabel.setText("");
    }

    // Handle file upload for the logo
    public void handleLogoUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        fileChooser.setTitle("Choisir une image");
        Stage stage = (Stage) logoImageView.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                // Create a directory for storing logos if it doesn't exist
                Path logoDir = Paths.get("logos");
                if (!Files.exists(logoDir)) {
                    Files.createDirectories(logoDir);
                }

                // Copy the logo file to the logos directory
                String fileName = System.currentTimeMillis() + "_" + file.getName();
                Path targetPath = logoDir.resolve(fileName);
                Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Save the path for later use
                logoPath = targetPath.toString();

                // Display the image with proper styling
                logoImage = new Image(file.toURI().toString());
                logoImageView.setImage(logoImage);
                logoImageView.getStyleClass().add("logo-image");

                // Apply dropshadow effect
                logoImageView.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.color(0, 0, 0, 0.3)));
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de charger l'image: " + e.getMessage());
            }
        }
    }

    // Handle reset button - restore original values
    public void handleReset(ActionEvent event) {
        if (originalAutoEcole != null) {
            // Reset to original values
            nomField.setText(originalAutoEcole.getNom());
            adresseField.setText(originalAutoEcole.getAdresse());
            telephoneField.setText(originalAutoEcole.getTelephone());
            emailField.setText(originalAutoEcole.getEmail());

            // Reset logo if it was changed
            if (originalAutoEcole.getLogo() != null && !originalAutoEcole.getLogo().isEmpty()) {
                try {
                    File logoFile = new File(originalAutoEcole.getLogo());
                    if (logoFile.exists()) {
                        logoImage = new Image(logoFile.toURI().toString());
                        logoImageView.setImage(logoImage);
                        logoPath = originalAutoEcole.getLogo();
                    }
                } catch (Exception e) {
                    showAlert("Erreur", "Impossible de restaurer le logo original: " + e.getMessage());
                }
            }

            // Clear styles
            nomField.setStyle("");
            adresseField.setStyle("");
            telephoneField.setStyle("");
            emailField.setStyle("");

            // Clear validation messages
            clearValidationMessage();
        }
    }

    // Handle saving the modified school data
    public void handleModifierEcole(ActionEvent event) {
        if (currentAutoEcole == null) {
            showAlert("Erreur", "Aucune école à modifier.");
            return;
        }

        // Get the values from the form
        String nom = nomField.getText().trim();
        String adresse = adresseField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String email = emailField.getText().trim();

        // Validate required fields
        if (nom.isEmpty() || adresse.isEmpty() || telephone.isEmpty() || email.isEmpty()) {
            showAlert("Erreur de validation", "Tous les champs sont obligatoires.");
            return;
        }

        // Validate name format
        if (!isValidName(nom)) {
            showAlert("Erreur de validation", "Le nom de l'école ne doit contenir que des lettres et des espaces.");
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

        // Update the auto ecole information
        currentAutoEcole.setNom(nom);
        currentAutoEcole.setAdresse(adresse);
        currentAutoEcole.setTelephone(telephone);
        currentAutoEcole.setEmail(email);
        if (logoPath != null) {
            currentAutoEcole.setLogo(logoPath);
        }

        boolean success = autoEcoleService.updateAutoEcole(currentAutoEcole);

        if (success) {
            showSuccessAlert("Succès", "Les informations de l'école ont été modifiées avec succès!");

            // Close the window after successful update
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.close();
        } else {
            showAlert("Erreur", "Échec de la modification des informations de l'école.");
        }
    }

    // Handle cancelling the operation
    public void handleCancel(ActionEvent event) {
        // Close the window
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}