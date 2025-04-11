package org.cpi2.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.cpi2.utils.AlertUtil;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.cpi2.entities.AutoEcole;
import org.cpi2.service.AutoEcoleService;
import org.cpi2.utils.ValidationUtils;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AjouterEcole implements Initializable {

    @FXML
    public TextField nomField;
    @FXML
    public TextField adresseField;
    @FXML
    public TextField telephoneField;
    @FXML
    public TextField emailField;
    @FXML
    public ImageView logoImageView;

    private Image logoImage;
    private String logoPath;
    private final AutoEcoleService autoEcoleService = new AutoEcoleService();

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÀ-ÿ\\s]+$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
                AlertUtil.showError("Erreur", "Impossible de charger l'image: " + e.getMessage());
            }
        }
    }

    // Handle saving the school data
    public void handleAjouterEcole(ActionEvent event) {
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
            AlertUtil.showError("Erreur de validation", "Tous les champs sont obligatoires.");
            return;
        }

        // Create the AutoEcole object
        AutoEcole autoEcole = new AutoEcole();
        autoEcole.setNom(nom);
        autoEcole.setAdresse(adresse);
        autoEcole.setTelephone(telephone);
        autoEcole.setEmail(email);
        autoEcole.setLogo(logoPath);

        // Save the auto école
        boolean success = autoEcoleService.saveAutoEcole(autoEcole);

        if (success) {
            AlertUtil.showSuccess("Succès", "L'école a été ajoutée avec succès!");
            
            // Clear the form
            clearForm();
        } else {
            AlertUtil.showError("Erreur", "Échec de l'ajout de l'école.");
        }
    }


    public void handleCancel(ActionEvent event) {
        clearForm();
    }
    
    private void clearForm() {
        nomField.clear();
        adresseField.clear();
        telephoneField.clear();
        emailField.clear();
        logoImageView.setImage(null);
        logoPath = null;
        
        ValidationUtils.clearValidation(nomField);
        ValidationUtils.clearValidation(adresseField);
        ValidationUtils.clearValidation(telephoneField);
        ValidationUtils.clearValidation(emailField);
    }
}
