package org.cpi2.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import org.cpi2.utils.ValidationUtils;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ModifierEcole implements Initializable {

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set placeholders for input fields
        nomField.setPromptText("Entrez le nom de l'école");
        adresseField.setPromptText("Entrez l'adresse complète");
        telephoneField.setPromptText("Entrez le numéro (8 chiffres)");
        emailField.setPromptText("exemple@domaine.com");
        
        setupValidation();
    }

    private void setupValidation() {
        // Validate name field
        ValidationUtils.addValidation(nomField, 
            name -> name != null && !name.isEmpty() && NAME_PATTERN.matcher(name).matches(),
            "Le nom de l'école ne doit contenir que des lettres et des espaces", 1);
        
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
        // Clear any previous validation
        ValidationUtils.clearValidation(nomField);
        ValidationUtils.clearValidation(adresseField);
        ValidationUtils.clearValidation(telephoneField);
        ValidationUtils.clearValidation(emailField);
        
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
    }

    private void styleFieldsForEditing() {
        // Add visual cues for modification
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(originalAutoEcole.getNom())) {
                nomField.getStyleClass().add("modified-field");
            } else {
                nomField.getStyleClass().removeAll("modified-field");
            }
        });

        adresseField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(originalAutoEcole.getAdresse())) {
                adresseField.getStyleClass().add("modified-field");
            } else {
                adresseField.getStyleClass().removeAll("modified-field");
            }
        });

        telephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(originalAutoEcole.getTelephone())) {
                telephoneField.getStyleClass().add("modified-field");
            } else {
                telephoneField.getStyleClass().removeAll("modified-field");
            }
        });

        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(originalAutoEcole.getEmail())) {
                emailField.getStyleClass().add("modified-field");
            } else {
                emailField.getStyleClass().removeAll("modified-field");
            }
        });
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

    // Handle saving the modified school data
    public void handleModifierEcole(ActionEvent event) {
        // Check if there are any validation errors
        if (ValidationUtils.hasAnyErrors()) {
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

        // Update the AutoEcole object
        currentAutoEcole.setNom(nom);
        currentAutoEcole.setAdresse(adresse);
        currentAutoEcole.setTelephone(telephone);
        currentAutoEcole.setEmail(email);
        currentAutoEcole.setLogo(logoPath);

        // Save the updated auto école
        boolean success = autoEcoleService.updateAutoEcole(currentAutoEcole);

        if (success) {
            showSuccessAlert("Succès", "L'école a été modifiée avec succès!");
            
            // Update the original auto-ecole for reset functionality
            originalAutoEcole = new AutoEcole(
                    currentAutoEcole.getId(),
                    currentAutoEcole.getNom(),
                    currentAutoEcole.getAdresse(),
                    currentAutoEcole.getTelephone(),
                    currentAutoEcole.getEmail(),
                    currentAutoEcole.getLogo()
            );
            
            // Clear styling for modified fields
            nomField.getStyleClass().removeAll("modified-field");
            adresseField.getStyleClass().removeAll("modified-field");
            telephoneField.getStyleClass().removeAll("modified-field");
            emailField.getStyleClass().removeAll("modified-field");
        } else {
            showAlert("Erreur", "Échec de la modification de l'école.");
        }
    }

    // Handle resetting the form to original values
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
                    }
                } catch (Exception e) {
                    logoImageView.setImage(null);
                }
            } else {
                logoImageView.setImage(null);
            }
            
            logoPath = originalAutoEcole.getLogo();
            
            // Clear styling for modified fields
            nomField.getStyleClass().removeAll("modified-field");
            adresseField.getStyleClass().removeAll("modified-field");
            telephoneField.getStyleClass().removeAll("modified-field");
            emailField.getStyleClass().removeAll("modified-field");
            
            // Clear validation errors
            ValidationUtils.clearValidation(nomField);
            ValidationUtils.clearValidation(adresseField);
            ValidationUtils.clearValidation(telephoneField);
            ValidationUtils.clearValidation(emailField);
        }
    }

    // Handle cancelling the operation
    public void handleCancel(ActionEvent event) {
        // Close the current window/dialog
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}