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
import org.cpi2.entitties.AutoEcole;
import org.cpi2.service.AutoEcoleService;

import java.io.*;
import java.net.URL;
import java.security.cert.Extension;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ManageEcole implements Initializable {

    @FXML private Label nomLabel;
    @FXML private Label adresseLabel;
    @FXML private Label telephoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label directeurLabel;
    
    @FXML private TextField nomField;
    @FXML private TextField adresseField;
    @FXML private TextField telephoneField;
    @FXML private TextField emailField;
    @FXML private TextField directeurField;
    
    @FXML private ImageView logoImageView;
    @FXML private Button changeLogoButton;
    @FXML private Button modifierButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private HBox buttonContainer;
    @FXML private HBox editButtonsContainer;
    
    @FXML private VBox infoContainer;
    @FXML private VBox editContainer;
    
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
        
        // Initially show info container and hide edit container
        showEditMode(false);
        
        // The auto-école data will be loaded when loadAutoEcole is called from AfficherEcole
    }
    
    void loadAutoEcole(int id) {
        try {
            currentAutoEcole = autoEcoleService.getAutoEcole();
            
            if (currentAutoEcole != null) {
                updateInfoLabels();
                populateEditFields();
                
                // Load logo if it exists
                if (currentAutoEcole.getLogo() != null && !currentAutoEcole.getLogo().isEmpty()) {
                    try {
                        File logoFile = new File(currentAutoEcole.getLogo());
                        if (logoFile.exists()) {
                            Image logoImage = new Image(logoFile.toURI().toString());
                            logoImageView.setImage(logoImage);
                        }
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le logo", e.getMessage());
                    }
                }
            } else {
                // If no auto-école exists, show the edit form
                showEditMode(true);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les informations de l'auto-école", e.getMessage());
        }
    }
    
    private void updateInfoLabels() {
        if (currentAutoEcole != null) {
            nomLabel.setText(currentAutoEcole.getNom());
            adresseLabel.setText(currentAutoEcole.getAdresse());
            telephoneLabel.setText(currentAutoEcole.getTelephone());
            emailLabel.setText(currentAutoEcole.getEmail());
            directeurLabel.setText(currentAutoEcole.getDirecteur() != null ? currentAutoEcole.getDirecteur() : "");
        }
    }
    
    private void populateEditFields() {
        if (currentAutoEcole != null) {
            nomField.setText(currentAutoEcole.getNom());
            adresseField.setText(currentAutoEcole.getAdresse());
            telephoneField.setText(currentAutoEcole.getTelephone());
            emailField.setText(currentAutoEcole.getEmail());
            directeurField.setText(currentAutoEcole.getDirecteur() != null ? currentAutoEcole.getDirecteur() : "");
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
            currentAutoEcole.setDirecteur(directeurField.getText().trim());
            
            // Handle logo file if selected
            if (selectedLogoFile != null) {
                currentAutoEcole.setLogo(selectedLogoFile.getAbsolutePath());
            }
            
            // Save to database
            boolean success = currentAutoEcole.getId() == 0 ? 
                    autoEcoleService.saveAutoEcole(currentAutoEcole) : 
                    autoEcoleService.updateAutoEcole(currentAutoEcole);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Auto-école enregistrée", 
                          "Les informations ont été enregistrées avec succès.");
                
                showEditMode(false);
                updateInfoLabels();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'enregistrement", 
                          "Impossible d'enregistrer les informations de l'auto-école.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'enregistrement", e.getMessage());
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
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'image", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleExport() {
        if (currentAutoEcole == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucune information", 
                      "Il n'y a pas d'informations à sauvegarder.");
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
                writer.println("Directeur: " + currentAutoEcole.getDirecteur());
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Sauvegarde réussie", 
                          "Les informations ont été sauvegardées dans " + file.getName());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la sauvegarde", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handlePrint() {
        // In a real application, this would use PrinterJob to print the information
        showAlert(Alert.AlertType.INFORMATION, "Impression", "Fonctionnalité d'impression", 
                 "L'impression sera implémentée dans une future version.");
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
    
    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();
        
        if (nomField.getText().trim().isEmpty()) {
            errors.append("- Le nom est obligatoire\n");
        } else if (!isValidName(nomField.getText().trim())) {
            errors.append("- Le nom doit contenir uniquement des lettres et des espaces\n");
        }
        
        if (adresseField.getText().trim().isEmpty()) {
            errors.append("- L'adresse est obligatoire\n");
        } else if (adresseField.getText().trim().length() < 10) {
            errors.append("- L'adresse doit contenir au moins 10 caractères\n");
        }
        
        if (telephoneField.getText().trim().isEmpty()) {
            errors.append("- Le numéro de téléphone est obligatoire\n");
        } else if (!isValidPhone(telephoneField.getText().trim())) {
            errors.append("- Le numéro de téléphone doit contenir 8 chiffres\n");
        }
        
        if (emailField.getText().trim().isEmpty()) {
            errors.append("- L'adresse email est obligatoire\n");
        } else if (!isValidEmail(emailField.getText().trim())) {
            errors.append("- L'adresse email n'est pas valide\n");
        }
        
        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", 
                    "Veuillez corriger les erreurs suivantes:", errors.toString());
            return false;
        }
        
        return true;
    }
    
    // Alert utility methods directly integrated
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}