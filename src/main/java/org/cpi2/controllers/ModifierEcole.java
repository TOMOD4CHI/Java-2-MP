package org.cpi2.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import java.util.regex.Pattern;

public class ModifierEcole {

    @FXML private TextField nomField;
    @FXML private TextField adresseField;
    @FXML private TextField telephoneField;
    @FXML private TextField emailField;
    @FXML private TextField directeurField;
    @FXML private ImageView logoImageView;

    private Image logoImage;
    private String logoPath;
    private final AutoEcoleService autoEcoleService = new AutoEcoleService();
    private AutoEcole currentAutoEcole;

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
            
            // Fill the fields with the selected auto-ecole data
            nomField.setText(autoEcole.getNom());
            adresseField.setText(autoEcole.getAdresse());
            telephoneField.setText(autoEcole.getTelephone());
            emailField.setText(autoEcole.getEmail());
            directeurField.setText(autoEcole.getDirecteur());

            // Load the logo if it exists
            logoPath = autoEcole.getLogo();
            if (logoPath != null && !logoPath.isEmpty()) {
                try {
                    File logoFile = new File(logoPath);
                    if (logoFile.exists()) {
                        logoImage = new Image(logoFile.toURI().toString());
                        logoImageView.setImage(logoImage);
                        logoImageView.getStyleClass().add("logo-image");
                        // Apply dropshadow effect
                        logoImageView.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.color(0, 0, 0, 0.3)));
                    } else {
                        // Set default logo if file doesn't exist
                        logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
                    }
                } catch (Exception e) {
                    showAlert("Erreur", "Impossible de charger le logo: " + e.getMessage());
                    // Set default logo on error
                    logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
                }
            } else {
                // Set default logo if path is empty
                logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
            }
        }
    }

    // Initialize the controller with existing data
    @FXML
    public void initialize() {
        // This method will be called by JavaFX when the FXML is loaded
        // Default to app_icon.png if no logo is set
        try {
            logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
        } catch (Exception e) {
            System.err.println("Error loading default logo: " + e.getMessage());
        }
    }

    // Handle file upload for the logo
    @FXML
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
    @FXML
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
        String directeur = directeurField.getText().trim();

        // Validate required fields
        if (nom.isEmpty() || adresse.isEmpty() || telephone.isEmpty() || email.isEmpty() || directeur.isEmpty()) {
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
        
        // Validate directeur name
        if (directeur.length() < 3) {
            showAlert("Erreur de validation", "Le nom du directeur doit contenir au moins 3 caractères.");
            return;
        }

        // Update the auto ecole information
        currentAutoEcole.setNom(nom);
        currentAutoEcole.setAdresse(adresse);
        currentAutoEcole.setTelephone(telephone);
        currentAutoEcole.setEmail(email);
        currentAutoEcole.setDirecteur(directeur);
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
    @FXML
    public void handleCancel(ActionEvent event) {
        // Close the window
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}
