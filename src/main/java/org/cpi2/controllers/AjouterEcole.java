package org.cpi2.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.cpi2.entitties.AutoEcole;
import org.cpi2.service.AutoEcoleService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class    AjouterEcole {

    public TextField nomField;
    public TextField adresseField;
    public TextField telephoneField;
    public TextField emailField;
    public ImageView logoImageView;

    private Image logoImage;
    private String logoPath;
    private final AutoEcoleService autoEcoleService = new AutoEcoleService();

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÀ-ÿ\\s]+$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

    private boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

                // Display the image
                logoImage = new Image(file.toURI().toString());
                logoImageView.setImage(logoImage);
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de charger l'image: " + e.getMessage());
            }
        }
    }

    // Handle saving the school data
    public void handleAjouterEcole(ActionEvent event) {
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

        // Create the AutoEcole object
        AutoEcole autoEcole = new AutoEcole();
        autoEcole.setNom(nom);
        autoEcole.setAdresse(adresse);
        autoEcole.setTelephone(telephone);
        autoEcole.setEmail(email);
        autoEcole.setLogoPath(logoPath);

        // Save the auto école
        //autoEcoleService.deleteAutoEcole();
        boolean success = autoEcoleService.saveAutoEcole(autoEcole);

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("L'école a été ajoutée avec succès!");
            alert.showAndWait();
            
            // Clear the form
            nomField.clear();
            adresseField.clear();
            telephoneField.clear();
            emailField.clear();
            logoImageView.setImage(null);
            logoPath = null;
        } else {
            showAlert("Erreur", "Échec de l'ajout de l'école.");
        }
    }

    // Handle cancelling the operation
    public void handleCancel(ActionEvent event) {
        // Clear all fields
        nomField.clear();
        adresseField.clear();
        telephoneField.clear();
        emailField.clear();
        logoImageView.setImage(null);
        logoPath = null;
    }
}
